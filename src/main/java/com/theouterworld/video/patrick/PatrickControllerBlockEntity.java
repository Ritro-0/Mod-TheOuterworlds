package com.theouterworld.video.patrick;

import com.mojang.serialization.Codec;
import com.theouterworld.registry.ModDimensions;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * TEMP VIDEO FEATURE — cinematic stare drag → slow eye lasers → group Outerworld banishment.
 */
public class PatrickControllerBlockEntity extends BlockEntity {
	private static final double STARE_RANGE = 48.0;
	private static final double LASER_RANGE = 40.0;
	private static final double LASER_HIT_RADIUS = 0.7;

	private static final int PHASE_STARE = 0;
	private static final int PHASE_LOCK = 1;
	private static final int PHASE_LASERS = 2;
	private static final int PHASE_DONE = 3;
	private static final int PHASE_COMPLETE = 4;

	/** Slow camera pull (~4 seconds). */
	private static final int STARE_TICKS = 80;
	/** Hold once everyone is facing him. */
	private static final int LOCK_TICKS = 35;
	/** Lasers grow across this many ticks (~5 seconds). */
	private static final int LASER_GROW_TICKS = 100;
	/** Full-beam linger before banish. */
	private static final int LASER_HOLD_TICKS = 40;
	/** Return finale spin (~10 seconds). */
	private static final int COMPLETE_SPIN_TICKS = 200;
	private static final float COMPLETE_SPIN_TURNS = 2.5F;

	private static final DustParticleOptions LASER_DUST = new DustParticleOptions(0x8B0000, 1.35F);
	private static final DustParticleOptions AURA_DUST = new DustParticleOptions(0x5C0000, 1.0F);
	private static final DustParticleOptions COMPLETE_DUST = new DustParticleOptions(PatrickMessages.PATRICK_CRIMSON, 1.5F);

	private Rotation rotation = Rotation.NONE;
	private Vec3 lookTarget = Vec3.ZERO;
	private final List<Vec3> eyeOrigins = new ArrayList<>();
	private final List<UUID> displayIds = new ArrayList<>();
	private final LinkedHashSet<UUID> markedVictims = new LinkedHashSet<>();
	private final Map<UUID, Vec3> spinOffsets = new HashMap<>();

	private int phase = PHASE_STARE;
	private int phaseTicks = 0;
	private Vec3 spinPivot = Vec3.ZERO;
	private float completeFaceYaw = 0.0F;

	public PatrickControllerBlockEntity(BlockPos pos, BlockState state) {
		super(PatrickVideo.CONTROLLER_BE, pos, state);
	}

	public void configure(Rotation rotation, Vec3 lookTarget, List<Vec3> eyeOrigins, List<UUID> displayIds) {
		this.rotation = rotation;
		this.lookTarget = lookTarget;
		this.eyeOrigins.clear();
		this.eyeOrigins.addAll(eyeOrigins);
		this.displayIds.clear();
		this.displayIds.addAll(displayIds);
		this.phase = PHASE_STARE;
		this.phaseTicks = 0;
		this.markedVictims.clear();
		PatrickVideo.track(worldPosition);
		setChanged();
	}

	public void dismiss(ServerLevel world) {
		for (UUID id : displayIds) {
			Entity entity = world.getEntity(id);
			if (entity != null) {
				entity.discard();
			}
		}
		// Sweep leftovers whose UUIDs were lost, so no half-Patrick is left floating.
		for (Display display : world.getEntitiesOfClass(Display.class, structureBox().inflate(3.0), d -> true)) {
			display.discard();
		}
		displayIds.clear();
		eyeOrigins.clear();
		markedVictims.clear();
		spinOffsets.clear();
		PatrickVideo.untrack(worldPosition);
		world.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
	}

	/**
	 * Any awakened Patrick can be finished. The awaken sequence only advances while his chunk is
	 * ticking, so requiring {@code PHASE_DONE} made the command unusable whenever everyone had
	 * already left for the Outerworld.
	 */
	public boolean canComplete() {
		return phase != PHASE_COMPLETE && !isRemoved();
	}

	public boolean beginComplete(ServerLevel world, ServerPlayer focus) {
		if (!canComplete()) {
			return false;
		}

		ServerPlayer closest = focus != null ? focus : findClosestPlayer(world, 128.0);
		if (closest == null) {
			return false;
		}

		spinPivot = pivot();
		gatherSpinTargets(world);

		Vec3 toPlayer = closest.getEyePosition().subtract(spinPivot);
		completeFaceYaw = (float) Mth.atan2(toPlayer.z, toPlayer.x);
		lookTarget = closest.getEyePosition();

		PatrickMessages.broadcastReturn(world);
		strikeLightning(world, spinPivot);
		world.playSound(null, worldPosition, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 0.55F);

		phase = PHASE_COMPLETE;
		phaseTicks = 0;
		applyAssemblyRotation(world, completeFaceYaw);
		setChanged();
		return true;
	}

	public static void tick(Level world, BlockPos pos, BlockState state, PatrickControllerBlockEntity entity) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}
		entity.tickServer(serverWorld);
	}

	private void tickServer(ServerLevel world) {
		if (phase == PHASE_COMPLETE) {
			tickComplete(world);
			return;
		}
		if (eyeOrigins.isEmpty() || phase == PHASE_DONE) {
			return;
		}

		List<ServerPlayer> players = playersInRange(world);
		for (ServerPlayer player : players) {
			markedVictims.add(player.getUUID());
		}

		switch (phase) {
			case PHASE_STARE -> tickStare(world, players);
			case PHASE_LOCK -> tickLock(world, players);
			case PHASE_LASERS -> tickLasers(world, players);
			default -> {
			}
		}
	}

	private void tickStare(ServerLevel world, List<ServerPlayer> players) {
		float progress = Math.min(1.0F, phaseTicks / (float) STARE_TICKS);
		// Ease-in camera pull so the turn is readable on video.
		float ease = progress * progress;
		spawnAura(world, 0.25F + progress * 0.75F);

		if (phaseTicks == 0) {
			world.playSound(null, worldPosition, SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.55F);
		}
		if (phaseTicks % 20 == 0) {
			world.playSound(null, worldPosition, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.7F, 0.7F);
		}

		boolean allLocked = !players.isEmpty();
		for (ServerPlayer player : players) {
			if (!pullCamera(world, player, ease)) {
				allLocked = false;
			}
		}

		phaseTicks++;
		if (phaseTicks >= STARE_TICKS || (progress > 0.85F && allLocked)) {
			phase = PHASE_LOCK;
			phaseTicks = 0;
			world.playSound(null, worldPosition, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 0.85F, 0.6F);
			setChanged();
		}
	}

	private void tickLock(ServerLevel world, List<ServerPlayer> players) {
		spawnAura(world, 1.0F);
		for (ServerPlayer player : players) {
			pullCamera(world, player, 1.0F);
		}
		if (phaseTicks == 0) {
			world.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.9F, 0.5F);
		}
		if (phaseTicks % 8 == 0) {
			for (Vec3 eye : eyeOrigins) {
				world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, eye.x, eye.y, eye.z, 4, 0.05, 0.05, 0.05, 0.01);
				world.sendParticles(LASER_DUST, eye.x, eye.y, eye.z, 2, 0.04, 0.04, 0.04, 0.0);
			}
		}

		phaseTicks++;
		if (phaseTicks >= LOCK_TICKS) {
			phase = PHASE_LASERS;
			phaseTicks = 0;
			world.playSound(null, worldPosition, SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.1F, 0.75F);
			setChanged();
		}
	}

	private void tickLasers(ServerLevel world, List<ServerPlayer> players) {
		for (ServerPlayer player : players) {
			pullCamera(world, player, 1.0F);
		}

		float grow = Math.min(1.0F, phaseTicks / (float) LASER_GROW_TICKS);
		spawnAura(world, 1.0F);

		if (phaseTicks % 12 == 0) {
			world.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.55F, 0.55F + grow * 0.5F);
		}

		for (ServerPlayer player : players) {
			if (player.distanceToSqr(lookTarget) > LASER_RANGE * LASER_RANGE) {
				continue;
			}
			Vec3 eye = nearestEye(player.getEyePosition());
			if (eye == null) {
				continue;
			}
			drawGrowingLaser(world, eye, player.getEyePosition(), grow);
		}

		// Also fire a beam from every eye toward the look-focus centroid of marked players.
		if (!players.isEmpty() && grow > 0.05F) {
			Vec3 focus = players.get(0).getEyePosition();
			for (int i = 1; i < players.size(); i++) {
				focus = focus.add(players.get(i).getEyePosition());
			}
			focus = focus.scale(1.0 / players.size());
			for (Vec3 eye : eyeOrigins) {
				drawGrowingLaser(world, eye, focus, grow * 0.85F);
			}
		}

		phaseTicks++;
		if (phaseTicks >= LASER_GROW_TICKS + LASER_HOLD_TICKS) {
			banishVictims(world);
			phase = PHASE_DONE;
			phaseTicks = 0;
			setChanged();
		}
	}

	private void tickComplete(ServerLevel world) {
		float spinProgress = Math.min(1.0F, phaseTicks / (float) COMPLETE_SPIN_TICKS);
		float angle = completeFaceYaw + spinProgress * COMPLETE_SPIN_TURNS * Mth.TWO_PI;
		applyAssemblyRotation(world, angle);
		spawnCompleteParticles(world, spinProgress);

		if (phaseTicks % 20 == 0) {
			world.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.8F, 0.35F + spinProgress);
		}
		if (phaseTicks % 35 == 0) {
			world.playSound(null, worldPosition, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.9F, 0.5F);
		}

		phaseTicks++;
		if (phaseTicks >= COMPLETE_SPIN_TICKS) {
			spawnCompleteFireworks(world);
			world.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 0.65F);
			world.playSound(null, worldPosition, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 1.5F, 0.8F);
			dismiss(world);
		}
	}

	private AABB structureBox() {
		return PatrickMatcher.structureBox(worldPosition, rotation);
	}

	/** Head-height focus point, recomputed if the stored look target drifted onto a player. */
	private Vec3 pivot() {
		Vec3 head = PatrickMatcher.lookTarget(worldPosition, rotation);
		return lookTarget.distanceToSqr(head) > 64.0 ? head : lookTarget;
	}

	/**
	 * Snapshot the display entities to spin. UUIDs stored at awaken time can go stale (entities
	 * unloaded, world reloaded, or an older awaken), so fall back to whatever displays are standing
	 * inside Patrick's footprint.
	 */
	private void gatherSpinTargets(ServerLevel world) {
		spinOffsets.clear();
		for (UUID id : displayIds) {
			Entity entity = world.getEntity(id);
			if (entity != null) {
				spinOffsets.put(id, entity.position().subtract(spinPivot));
			}
		}
		if (!spinOffsets.isEmpty()) {
			return;
		}

		List<Display> found = world.getEntitiesOfClass(Display.class, structureBox().inflate(3.0), d -> true);
		displayIds.clear();
		for (Display display : found) {
			displayIds.add(display.getUUID());
			spinOffsets.put(display.getUUID(), display.position().subtract(spinPivot));
		}
	}

	private void applyAssemblyRotation(ServerLevel world, float yawRadians) {
		for (Map.Entry<UUID, Vec3> entry : spinOffsets.entrySet()) {
			Entity entity = world.getEntity(entry.getKey());
			if (entity == null) {
				continue;
			}
			Vec3 rotated = rotateY(entry.getValue(), yawRadians);
			Vec3 pos = spinPivot.add(rotated);
			entity.setPos(pos.x, pos.y, pos.z);
			entity.setYRot((float) Math.toDegrees(yawRadians) - 90.0F);
			entity.setYHeadRot(entity.getYRot());
		}
	}

	private static Vec3 rotateY(Vec3 offset, float yawRadians) {
		float cos = Mth.cos(yawRadians);
		float sin = Mth.sin(yawRadians);
		return new Vec3(
			offset.x * cos - offset.z * sin,
			offset.y,
			offset.x * sin + offset.z * cos
		);
	}

	private void spawnCompleteParticles(ServerLevel world, float intensity) {
		double cx = spinPivot.x;
		double cy = spinPivot.y;
		double cz = spinPivot.z;
		int ringCount = 6 + Mth.floor(intensity * 10);
		for (int i = 0; i < ringCount; i++) {
			double a = (world.getGameTime() * 0.22) + i * (Math.PI * 2.0 / ringCount);
			double r = 1.2 + intensity * 2.8;
			world.sendParticles(
				COMPLETE_DUST,
				cx + Math.cos(a) * r,
				cy + Math.sin(a * 0.5) * 0.6 + intensity * 1.5,
				cz + Math.sin(a) * r,
				1,
				0.0,
				0.02,
				0.0,
				0.0
			);
		}
		world.sendParticles(
			ColorParticleOption.create(ParticleTypes.FLASH, PatrickMessages.PATRICK_CRIMSON | 0xFF000000),
			cx,
			cy + 2.0,
			cz,
			1,
			0.0,
			0.0,
			0.0,
			0.0
		);
		if (phaseTicks % 4 == 0) {
			world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy, cz, 8, 1.5, 2.0, 1.5, 0.02);
			world.sendParticles(ParticleTypes.WITCH, cx, cy + 1.5, cz, 6, 1.2, 1.8, 1.2, 0.01);
			world.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy + 2.5, cz, 10, 1.8, 2.2, 1.8, 0.15);
		}
	}

	private void spawnCompleteFireworks(ServerLevel world) {
		Vec3 center = spinPivot;
		var random = world.getRandom();
		for (int i = 0; i < 18; i++) {
			double ox = (random.nextDouble() - 0.5) * 5.0;
			double oy = random.nextDouble() * 4.0 + 1.0;
			double oz = (random.nextDouble() - 0.5) * 5.0;
			ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
			stack.set(
				DataComponents.FIREWORKS,
				new Fireworks(
					1 + random.nextInt(2),
					List.of(
						new FireworkExplosion(
							random.nextBoolean()
								? FireworkExplosion.Shape.BURST
								: FireworkExplosion.Shape.STAR,
							IntList.of(
								PatrickMessages.PATRICK_CRIMSON,
								0xFF0000,
								0x4A0000,
								0xFF4500
							),
							IntList.of(0x1A0000, 0x8B0000),
							true,
							true
						)
					)
				)
			);
			FireworkRocketEntity rocket = new FireworkRocketEntity(
				world,
				center.x + ox,
				center.y + oy,
				center.z + oz,
				stack
			);
			world.addFreshEntity(rocket);
		}
	}

	private static void strikeLightning(ServerLevel world, Vec3 pos) {
		LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
		if (lightning != null) {
			lightning.snapTo(pos.x, pos.y + 2.0, pos.z);
			lightning.setVisualOnly(true);
			world.addFreshEntity(lightning);
		}
	}

	private ServerPlayer findClosestPlayer(ServerLevel world, double range) {
		ServerPlayer closest = null;
		double best = range * range;
		for (ServerPlayer player : world.players()) {
			if (!player.isAlive() || player.isSpectator()) {
				continue;
			}
			double dist = player.distanceToSqr(lookTarget);
			if (dist < best) {
				best = dist;
				closest = player;
			}
		}
		return closest;
	}

	private boolean pullCamera(ServerLevel world, ServerPlayer player, float amount) {
		Vec3 eyes = player.getEyePosition();
		Vec3 delta = lookTarget.subtract(eyes);
		double horiz = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float targetYaw = (float) (Mth.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
		float targetPitch = (float) (-(Mth.atan2(delta.y, horiz) * (180.0 / Math.PI)));

		float yaw = Mth.rotLerp(amount, player.getYRot(), targetYaw);
		float pitch = Mth.lerp(amount, player.getXRot(), targetPitch);
		player.teleportTo(world, player.getX(), player.getY(), player.getZ(), Set.of(), yaw, pitch, false);

		float yawErr = Math.abs(Mth.wrapDegrees(player.getYRot() - targetYaw));
		float pitchErr = Math.abs(player.getXRot() - targetPitch);
		return yawErr < 8.0F && pitchErr < 8.0F;
	}

	private void drawGrowingLaser(ServerLevel world, Vec3 from, Vec3 to, float progress) {
		Vec3 delta = to.subtract(from);
		double length = delta.length();
		if (length < 0.05 || progress <= 0.0F) {
			return;
		}
		Vec3 dir = delta.scale(1.0 / length);
		double reach = length * Mth.clamp(progress, 0.0F, 1.0F);
		int steps = Math.max(1, Mth.ceil(reach * 5.0));
		for (int i = 0; i <= steps; i++) {
			double t = (reach * i) / steps;
			Vec3 point = from.add(dir.scale(t));
			world.sendParticles(LASER_DUST, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
			if (i % 4 == 0) {
				world.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
			}
			if (progress > 0.92F && i % 5 == 0) {
				world.sendParticles(ParticleTypes.SOUL, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
			}
		}
		world.sendParticles(
			ColorParticleOption.create(ParticleTypes.FLASH, PatrickMessages.PATRICK_CRIMSON | 0xFF000000),
			from.x,
			from.y,
			from.z,
			1,
			0.0,
			0.0,
			0.0,
			0.0
		);
	}

	private void spawnAura(ServerLevel world, float intensity) {
		double cx = lookTarget.x;
		double cy = lookTarget.y;
		double cz = lookTarget.z;
		int count = 2 + Mth.floor(intensity * 6);
		for (int i = 0; i < count; i++) {
			double a = (world.getGameTime() * 0.15) + i * (Math.PI * 2.0 / count);
			double r = 0.8 + intensity * 1.4;
			world.sendParticles(
				AURA_DUST,
				cx + Math.cos(a) * r,
				cy + Math.sin(world.getGameTime() * 0.08 + i) * 0.35,
				cz + Math.sin(a) * r,
				1,
				0.0,
				0.0,
				0.0,
				0.0
			);
		}
	}

	private void banishVictims(ServerLevel source) {
		List<ServerPlayer> victims = new ArrayList<>();
		for (UUID id : markedVictims) {
			ServerPlayer player = source.getServer().getPlayerList().getPlayer(id);
			if (player != null && player.isAlive() && !player.isSpectator() && player.level() == source) {
				if (player.distanceToSqr(lookTarget) <= (STARE_RANGE + 8) * (STARE_RANGE + 8)) {
					victims.add(player);
				}
			}
		}
		if (victims.isEmpty()) {
			victims.addAll(playersInRange(source));
		}
		if (victims.isEmpty()) {
			return;
		}

		PatrickMessages.broadcastBanishment(source, victims);

		source.playSound(null, worldPosition, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 0.7F);
		source.playSound(null, worldPosition, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.55F, 0.5F);

		for (ServerPlayer player : victims) {
			stripAndGrantHelmet(player);
			teleportToOuterworld(source, player);
		}
	}

	private static void stripAndGrantHelmet(ServerPlayer player) {
		player.getInventory().clearContent();
		player.getEnderChestInventory().clearContent();
		ItemStack helmet = new ItemStack(Items.TINTED_GLASS);
		player.setItemSlot(EquipmentSlot.HEAD, helmet);
		player.containerMenu.broadcastChanges();
		player.inventoryMenu.broadcastChanges();
	}

	private List<ServerPlayer> playersInRange(ServerLevel world) {
		AABB stareBox = new AABB(worldPosition).inflate(STARE_RANGE);
		return world.getEntitiesOfClass(
			ServerPlayer.class,
			stareBox,
			player -> player.isAlive() && !player.isSpectator()
		);
	}

	private Vec3 nearestEye(Vec3 target) {
		Vec3 best = null;
		double bestDist = Double.MAX_VALUE;
		for (Vec3 eye : eyeOrigins) {
			double d = eye.distanceToSqr(target);
			if (d < bestDist) {
				bestDist = d;
				best = eye;
			}
		}
		return best;
	}

	private void teleportToOuterworld(ServerLevel source, ServerPlayer player) {
		ServerLevel target = source.getServer().getLevel(ModDimensions.OUTERWORLD_WORLD_KEY);
		if (target == null) {
			return;
		}

		BlockPos landing = findSurface(target, player.blockPosition().getX(), player.blockPosition().getZ());
		Vec3 destination = Vec3.atBottomCenterOf(landing);

		Entity teleported = player.teleport(new TeleportTransition(
			target,
			destination,
			Vec3.ZERO,
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		if (teleported != null) {
			teleported.resetFallDistance();
		}
		target.playSound(
			null,
			BlockPos.containing(destination),
			SoundEvents.PORTAL_TRAVEL,
			SoundSource.PLAYERS,
			0.7F,
			0.65F
		);
	}

	private static BlockPos findSurface(ServerLevel world, int x, int z) {
		int top = world.getMaxY();
		int bottom = world.getMinY();
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (groundState.isAir() || !groundState.isCollisionShapeFullBlock(world, ground)) {
				continue;
			}
			BlockPos stand = ground.above();
			if (world.getBlockState(stand).canBeReplaced() && world.getBlockState(stand.above()).canBeReplaced()) {
				return stand;
			}
		}
		return new BlockPos(x, Math.max(bottom + 64, 64), z);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("PatrickRotation", rotation.name());
		output.putDouble("LookX", lookTarget.x);
		output.putDouble("LookY", lookTarget.y);
		output.putDouble("LookZ", lookTarget.z);
		output.putInt("Phase", phase);
		output.putInt("PhaseTicks", phaseTicks);
		output.putFloat("CompleteFaceYaw", completeFaceYaw);
		output.putDouble("SpinPivotX", spinPivot.x);
		output.putDouble("SpinPivotY", spinPivot.y);
		output.putDouble("SpinPivotZ", spinPivot.z);
		output.store("DisplayIds", UUIDUtil.CODEC.listOf(), List.copyOf(displayIds));
		output.store("Victims", UUIDUtil.CODEC.listOf(), List.copyOf(markedVictims));
		List<Double> eyes = new ArrayList<>(eyeOrigins.size() * 3);
		for (Vec3 eye : eyeOrigins) {
			eyes.add(eye.x);
			eyes.add(eye.y);
			eyes.add(eye.z);
		}
		output.store("Eyes", Codec.DOUBLE.listOf(), eyes);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		try {
			rotation = Rotation.valueOf(input.getStringOr("PatrickRotation", Rotation.NONE.name()));
		} catch (IllegalArgumentException ignored) {
			rotation = Rotation.NONE;
		}
		lookTarget = new Vec3(
			input.getDoubleOr("LookX", worldPosition.getX() + 0.5),
			input.getDoubleOr("LookY", worldPosition.getY() + 4.0),
			input.getDoubleOr("LookZ", worldPosition.getZ() + 0.5)
		);
		phase = Mth.clamp(input.getIntOr("Phase", PHASE_STARE), PHASE_STARE, PHASE_COMPLETE);
		phaseTicks = Math.max(0, input.getIntOr("PhaseTicks", 0));
		completeFaceYaw = input.getFloatOr("CompleteFaceYaw", 0.0F);
		spinPivot = new Vec3(
			input.getDoubleOr("SpinPivotX", lookTarget.x),
			input.getDoubleOr("SpinPivotY", lookTarget.y),
			input.getDoubleOr("SpinPivotZ", lookTarget.z)
		);
		displayIds.clear();
		displayIds.addAll(input.read("DisplayIds", UUIDUtil.CODEC.listOf()).orElse(List.of()));
		markedVictims.clear();
		markedVictims.addAll(input.read("Victims", UUIDUtil.CODEC.listOf()).orElse(List.of()));
		eyeOrigins.clear();
		List<Double> eyes = input.read("Eyes", Codec.DOUBLE.listOf()).orElse(List.of());
		for (int i = 0; i + 2 < eyes.size(); i += 3) {
			eyeOrigins.add(new Vec3(eyes.get(i), eyes.get(i + 1), eyes.get(i + 2)));
		}
		if (!displayIds.isEmpty() || !eyeOrigins.isEmpty()) {
			PatrickVideo.track(worldPosition);
		}
	}
}
