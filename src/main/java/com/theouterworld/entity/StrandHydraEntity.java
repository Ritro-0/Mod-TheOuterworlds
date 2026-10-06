package com.theouterworld.entity;

import com.theouterworld.registry.ModDamageTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Sessile tentacle colony. One Frostworld fish or player at a time is reeled along a yellow
 * particle path, and only while both it and the hydra are underwater. The beam lasts at most
 * ten seconds, then the hydra waits thirty seconds. Other creatures are ignored unless they
 * stay within five blocks for more than ten seconds. The beam does not hurt; touching the
 * hydra does. Creative and spectator players are left alone.
 */
public class StrandHydraEntity extends PlantedOceanEntity {
	public static final double PULL_RANGE = 20.0;
	private static final double PULL_PER_TICK = 0.22;
	/**
	 * Added to player velocity after they swim. Water drag leaves Depth Strider sprint-swim
	 * just ahead of this, and a normal swim still loses ground.
	 */
	private static final double PLAYER_PULL = 0.052;
	private static final int BEAM_HOLD = 200;
	private static final int BEAM_COOLDOWN = 600;
	private static final double CLOSE_RANGE = 5.0;
	private static final int CLOSE_LINGER = 200;
	private static final float TOUCH_DAMAGE = 2.0F;
	private static final DustParticleOptions YELLOW_BEAM = new DustParticleOptions(0xFFE14A, 0.9F);
	private static final List<PlayerPull> PLAYER_PULLS = new ArrayList<>();

	private int beamCooldown;
	private int beamTicks;
	private @Nullable UUID beamTarget;
	private final Map<UUID, Integer> closeTicks = new HashMap<>();

	private record PlayerPull(Player player, Vec3 pull) {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(StrandHydraEntity::applyPlayerPulls);
	}

	public record Tentacle(float originX, float originZ, int u, int v) {
		public float pivotX() {
			return this.originX + 2.0F;
		}

		public float pivotY() {
			return 15.0F;
		}

		public float pivotZ() {
			return this.originZ + 2.0F;
		}
	}

	public static final Tentacle[] TENTACLES = {
		new Tentacle(-8.0F, -2.0F, 0, 25),
		new Tentacle(-2.0F, -8.0F, 16, 25),
		new Tentacle(-2.0F, -2.0F, 32, 25),
		new Tentacle(-2.0F, 4.0F, 48, 25),
		new Tentacle(4.0F, -2.0F, 0, 61),
		new Tentacle(-8.0F, -8.0F, 16, 61),
		new Tentacle(-8.0F, 4.0F, 32, 61),
		new Tentacle(4.0F, 4.0F, 48, 61),
		new Tentacle(4.0F, -8.0F, 64, 0)
	};

	public StrandHydraEntity(EntityType<? extends StrandHydraEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.MOVEMENT_SPEED, 0.0)
			.add(Attributes.FOLLOW_RANGE, 24.0);
	}

	public static float swayX(int index, float age) {
		float wind = Mth.sin(age * 0.02F) * 0.18F;
		return wind + Mth.sin(age * 0.055F + index * 0.85F) * 0.28F;
	}

	public static float swayZ(int index, float age) {
		float wind = Mth.cos(age * 0.017F) * 0.14F;
		return wind + Mth.cos(age * 0.048F + index * 1.1F) * 0.24F;
	}

	public Vec3 tentacleTip(int index, float ageInTicks) {
		Tentacle tentacle = TENTACLES[index];
		Vector3f local = new Vector3f(0.0F, -32.0F, 0.0F);
		new Quaternionf().rotationZYX(swayZ(index, ageInTicks), 0.0F, swayX(index, ageInTicks)).transform(local);
		return modelPointToWorld(
			this,
			tentacle.pivotX() + local.x,
			tentacle.pivotY() + local.y,
			tentacle.pivotZ() + local.z,
			this.yBodyRot
		);
	}

	public static Vec3 modelPointToWorld(Entity entity, float pixelX, float pixelY, float pixelZ, float bodyYaw) {
		float x = -pixelX / 16.0F;
		float y = -pixelY / 16.0F + 1.501F;
		float z = pixelZ / 16.0F;
		float rot = (180.0F - bodyYaw) * Mth.DEG_TO_RAD;
		float cos = Mth.cos(rot);
		float sin = Mth.sin(rot);
		float rotatedX = x * cos + z * sin;
		float rotatedZ = -x * sin + z * cos;
		return new Vec3(entity.getX() + rotatedX, entity.getY() + y, entity.getZ() + rotatedZ);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!(this.level() instanceof ServerLevel server) || !this.isAlive()) {
			return;
		}
		this.touchDamage(server);
		this.trackCloseApproach(server);
		if (this.beamCooldown > 0) {
			this.beamCooldown--;
			this.beamTarget = null;
			this.beamTicks = 0;
			return;
		}
		if (this.beamTarget != null) {
			this.beamTicks++;
			if (this.beamTicks >= BEAM_HOLD) {
				this.releaseTarget();
				return;
			}
			LivingEntity target = this.resolveTarget(server);
			if (target == null || !this.stillHolding(target)) {
				this.releaseTarget();
				return;
			}
			if (this.isUnderWater() && target.isUnderWater()) {
				this.reel(server, target, this.tickCount);
				if (!target.isAlive() || target.isRemoved()) {
					this.releaseTarget();
				}
			}
			return;
		}
		if (!this.isUnderWater()) {
			return;
		}
		LivingEntity found = this.findTarget(server);
		if (found == null) {
			return;
		}
		this.beamTarget = found.getUUID();
		this.beamTicks = 0;
		this.reel(server, found, this.tickCount);
	}

	private @Nullable LivingEntity resolveTarget(ServerLevel server) {
		if (this.beamTarget == null) {
			return null;
		}
		Entity entity = server.getEntity(this.beamTarget);
		if (entity instanceof LivingEntity living && living.isAlive()) {
			return living;
		}
		return null;
	}

	private boolean stillHolding(LivingEntity entity) {
		return this.canConsider(entity) && this.distanceToSqr(entity) <= PULL_RANGE * PULL_RANGE;
	}

	private void releaseTarget() {
		this.beamTarget = null;
		this.beamTicks = 0;
		this.beamCooldown = BEAM_COOLDOWN;
	}

	private @Nullable LivingEntity findTarget(ServerLevel server) {
		LivingEntity nearest = null;
		double nearestDistance = PULL_RANGE * PULL_RANGE;
		for (LivingEntity candidate : server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(PULL_RANGE), this::canReel)) {
			double distance = this.distanceToSqr(candidate);
			if (distance < nearestDistance) {
				nearest = candidate;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private boolean canReel(LivingEntity entity) {
		return this.canConsider(entity) && entity.isUnderWater() && this.distanceToSqr(entity) <= PULL_RANGE * PULL_RANGE;
	}

	private boolean canConsider(LivingEntity entity) {
		if (!this.isEligible(entity)) {
			return false;
		}
		if (this.isPreferredPrey(entity)) {
			return true;
		}
		if (this.beamTarget != null && this.beamTarget.equals(entity.getUUID())) {
			return true;
		}
		Integer ticks = this.closeTicks.get(entity.getUUID());
		return ticks != null && ticks > CLOSE_LINGER;
	}

	private boolean isEligible(LivingEntity entity) {
		if (entity == this || !entity.isAlive() || entity.isRemoved() || entity instanceof StrandHydraEntity) {
			return false;
		}
		return !(entity instanceof Player player && (player.isSpectator() || player.isCreative()));
	}

	private boolean isPreferredPrey(LivingEntity entity) {
		return entity instanceof Player
			|| entity instanceof FeederEntity
			|| entity instanceof DriftmiteEntity
			|| entity instanceof FrostworldJellyEntity;
	}

	private void trackCloseApproach(ServerLevel server) {
		AABB box = this.getBoundingBox().inflate(CLOSE_RANGE);
		Map<UUID, Integer> next = new HashMap<>();
		for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, box, this::isEligible)) {
			if (this.isPreferredPrey(entity) || this.distanceToSqr(entity) > CLOSE_RANGE * CLOSE_RANGE) {
				continue;
			}
			int ticks = this.closeTicks.getOrDefault(entity.getUUID(), 0) + 1;
			next.put(entity.getUUID(), ticks);
		}
		this.closeTicks.clear();
		this.closeTicks.putAll(next);
	}

	private void touchDamage(ServerLevel server) {
		if (this.tickCount % 10 != 0) {
			return;
		}
		AABB box = this.getBoundingBox().inflate(0.05);
		for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, box, this::isEligible)) {
			entity.hurtServer(server, server.damageSources().source(ModDamageTypes.HYDRA_CONSUME), TOUCH_DAMAGE);
		}
	}

	private void reel(ServerLevel server, LivingEntity entity, float age) {
		if (entity.isPassenger()) {
			entity.stopRiding();
		}
		Vec3 center = entity.getBoundingBox().getCenter();
		int tentacle = this.nearestTentacle(center, age);
		Vec3 tip = this.tentacleTip(tentacle, age);
		Vec3 toTip = tip.subtract(center);
		double distance = toTip.length();
		this.drawBeam(server, tip, center, distance);
		if (entity instanceof Player player) {
			this.queuePlayerPull(player, toTip, distance);
			return;
		}
		if (entity instanceof Mob mob) {
			mob.getNavigation().stop();
		}
		if (distance < 1.0E-4) {
			return;
		}
		Vec3 step = toTip.scale(Math.min(PULL_PER_TICK, distance) / distance);
		entity.move(MoverType.SELF, step);
		entity.resetFallDistance();
		entity.needsSync = true;
		entity.setDeltaMovement(Vec3.ZERO);
	}

	private void queuePlayerPull(Player player, Vec3 toTip, double distance) {
		Vec3 pull = distance < 1.0E-4
			? Vec3.ZERO
			: toTip.scale(PLAYER_PULL / distance);
		pull = new Vec3(pull.x, pull.y * 0.55, pull.z);
		PLAYER_PULLS.add(new PlayerPull(player, pull));
	}

	private static void applyPlayerPulls(ServerLevel level) {
		if (PLAYER_PULLS.isEmpty()) {
			return;
		}
		for (int i = PLAYER_PULLS.size() - 1; i >= 0; i--) {
			PlayerPull queued = PLAYER_PULLS.get(i);
			if (queued.player.level() != level) {
				continue;
			}
			PLAYER_PULLS.remove(i);
			Player player = queued.player;
			if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
				continue;
			}
			player.setDeltaMovement(player.getDeltaMovement().add(queued.pull));
			player.syncVelocity = true;
			player.needsSync = true;
			player.resetFallDistance();
		}
	}

	private int nearestTentacle(Vec3 point, float age) {
		int nearest = 0;
		double nearestDistance = Double.MAX_VALUE;
		for (int i = 0; i < TENTACLES.length; i++) {
			double distance = this.tentacleTip(i, age).distanceToSqr(point);
			if (distance < nearestDistance) {
				nearest = i;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private void drawBeam(ServerLevel server, Vec3 from, Vec3 to, double distance) {
		int points = Mth.clamp((int) (distance / 0.55), 2, 28);
		for (int i = 0; i <= points; i++) {
			double t = i / (double) points;
			server.sendParticles(
				YELLOW_BEAM,
				Mth.lerp(t, from.x, to.x),
				Mth.lerp(t, from.y, to.y),
				Mth.lerp(t, from.z, to.z),
				1,
				0.0,
				0.0,
				0.0,
				0.0
			);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("BeamCooldown", this.beamCooldown);
		output.putInt("BeamTicks", this.beamTicks);
		if (this.beamTarget != null) {
			output.store("BeamTarget", UUIDUtil.CODEC, this.beamTarget);
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.beamCooldown = input.getIntOr("BeamCooldown", 0);
		this.beamTicks = input.getIntOr("BeamTicks", 0);
		this.beamTarget = input.read("BeamTarget", UUIDUtil.CODEC).orElse(null);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SQUID_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SQUID_DEATH;
	}
}
