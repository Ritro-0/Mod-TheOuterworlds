package com.theouterworld.entity;

import com.theouterworld.world.FrostworldFaunaSpawner;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Marks a seafloor vent. While {@code charge} is above zero the vent blows a black fog.
 * Each feeder inside the fog burns one charge per tick, and the fog stays until they have
 * spent enough time in it. Anything else living takes a light tick of damage.
 */
public class OceanVentEntity extends Entity {
	public static final double CLOUD_RADIUS = 5.0;
	public static final double CLOUD_HEIGHT = 10.0;
	private static final int FULL_CHARGE = 2400;
	private static final float CLOUD_DAMAGE = 1.0F;

	private int charge;
	private int cooldown;
	private boolean seeded;
	private boolean fromSave;
	private boolean started;

	public OceanVentEntity(EntityType<? extends OceanVentEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setNoGravity(true);
		this.setSilent(true);
		this.setInvisible(true);
	}

	public boolean isErupting() {
		return this.charge > 0;
	}

	public boolean contains(Vec3 pos) {
		double dx = pos.x - this.getX();
		double dz = pos.z - this.getZ();
		double dy = pos.y - this.getY();
		return dy >= -0.5 && dy <= CLOUD_HEIGHT && dx * dx + dz * dz <= CLOUD_RADIUS * CLOUD_RADIUS;
	}

	public Vec3 randomPointInside(RandomSource random) {
		double angle = random.nextDouble() * Math.PI * 2.0;
		double radius = random.nextDouble() * (CLOUD_RADIUS - 1.0);
		return new Vec3(
			this.getX() + Math.cos(angle) * radius,
			this.getY() + 0.5 + random.nextDouble() * 4.5,
			this.getZ() + Math.sin(angle) * radius
		);
	}

	public static boolean cloudContains(Level level, Vec3 pos) {
		AABB search = new AABB(pos, pos).inflate(CLOUD_RADIUS, CLOUD_HEIGHT, CLOUD_RADIUS);
		for (OceanVentEntity vent : level.getEntitiesOfClass(OceanVentEntity.class, search)) {
			if (vent.isErupting() && vent.contains(pos)) {
				return true;
			}
		}
		return false;
	}

	public static OceanVentEntity nearestActive(Entity origin, double range) {
		OceanVentEntity best = null;
		double bestDistance = range * range;
		for (OceanVentEntity vent : origin.level().getEntitiesOfClass(OceanVentEntity.class, origin.getBoundingBox().inflate(range))) {
			if (!vent.isErupting()) {
				continue;
			}
			double distance = origin.distanceToSqr(vent);
			if (distance < bestDistance) {
				best = vent;
				bestDistance = distance;
			}
		}
		return best;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		this.setDeltaMovement(Vec3.ZERO);
		super.tick();
		if (!(this.level() instanceof ServerLevel server)) {
			return;
		}
		if (!this.started) {
			this.started = true;
			if (!this.seeded) {
				this.seeded = true;
				FrostworldFaunaSpawner.spawnFeederSchool(server, this.position(), 8 + this.random.nextInt(4));
			}
			if (this.charge <= 0 && this.cooldown <= 0) {
				if (!this.fromSave && this.random.nextFloat() < 0.55F) {
					this.charge = FULL_CHARGE;
				} else {
					this.cooldown = 400 + this.random.nextInt(1600);
				}
			}
		}
		if (this.charge > 0) {
			this.erupt(server);
		} else if (--this.cooldown <= 0) {
			this.charge = FULL_CHARGE;
		}
	}

	private void erupt(ServerLevel server) {
		AABB box = new AABB(
			this.getX() - CLOUD_RADIUS,
			this.getY() - 0.5,
			this.getZ() - CLOUD_RADIUS,
			this.getX() + CLOUD_RADIUS,
			this.getY() + CLOUD_HEIGHT,
			this.getZ() + CLOUD_RADIUS
		);
		List<LivingEntity> inside = server.getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive() && this.contains(entity.position()));
		int feeders = 0;
		boolean damageTick = this.tickCount % 25 == 0;
		for (LivingEntity entity : inside) {
			if (entity instanceof FeederEntity) {
				feeders++;
				continue;
			}
			if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
				continue;
			}
			if (damageTick) {
				entity.hurtServer(server, server.damageSources().magic(), CLOUD_DAMAGE);
			}
		}
		this.charge -= feeders;
		if (this.charge <= 0) {
			this.charge = 0;
			this.cooldown = 600 + this.random.nextInt(1400);
			return;
		}
		server.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 1.6, this.getZ(), 18, 2.4, 3.4, 2.4, 0.02);
		server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.2, this.getZ(), 10, 2.0, 2.8, 2.0, 0.01);
		server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.2, this.getZ(), 4, 1.3, 2.2, 1.3, 0.01);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("Charge", this.charge);
		output.putInt("Cooldown", this.cooldown);
		output.putBoolean("Seeded", this.seeded);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.charge = input.getIntOr("Charge", 0);
		this.cooldown = input.getIntOr("Cooldown", 0);
		this.seeded = input.getBooleanOr("Seeded", false);
		this.fromSave = true;
	}
}
