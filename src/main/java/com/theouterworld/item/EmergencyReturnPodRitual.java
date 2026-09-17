package com.theouterworld.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side charge sequence for the Emergency Return Pod:
 * flame sphere over 10 seconds, lightning at center, then teleport to overworld spawn.
 */
public final class EmergencyReturnPodRitual {
	public static final double RADIUS = 12.0;
	public static final int DURATION_TICKS = 200;
	private static final int PARTICLES_PER_TICK = 14;
	private static final double RADIUS_SQ = RADIUS * RADIUS;

	private static final List<ActiveRitual> ACTIVE = new ArrayList<>();

	private EmergencyReturnPodRitual() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tickAll());
	}

	public static boolean isActive(UUID playerId) {
		for (ActiveRitual ritual : ACTIVE) {
			if (ritual.playerId.equals(playerId)) {
				return true;
			}
		}
		return false;
	}

	public static void start(ServerLevel level, UUID playerId, InteractionHand hand, Vec3 center) {
		ACTIVE.removeIf(ritual -> ritual.playerId.equals(playerId));
		ACTIVE.add(new ActiveRitual(level, playerId, hand, center, 0));
	}

	private static void tickAll() {
		if (ACTIVE.isEmpty()) {
			return;
		}

		Iterator<ActiveRitual> iterator = ACTIVE.iterator();
		while (iterator.hasNext()) {
			ActiveRitual ritual = iterator.next();
			if (!tick(ritual)) {
				iterator.remove();
			}
		}
	}

	/**
	 * @return {@code true} if the ritual should remain active
	 */
	private static boolean tick(ActiveRitual ritual) {
		ServerLevel level = ritual.level;
		if (level.getServer() == null || level != level.getServer().getLevel(level.dimension())) {
			return false;
		}

		ServerPlayer player = level.getServer().getPlayerList().getPlayer(ritual.playerId);
		if (player == null || player.level() != level || !player.isAlive()) {
			return false;
		}

		ItemStack stack = player.getItemInHand(ritual.hand);
		if (!stack.is(ModItems.EMERGENCY_RETURN_POD)) {
			return false;
		}

		ritual.ticks++;
		spawnChargeParticles(level, ritual.center, ritual.ticks, level.getRandom());

		if (ritual.ticks < DURATION_TICKS) {
			if (ritual.ticks % 40 == 0) {
				level.playSound(
					null,
					ritual.center.x,
					ritual.center.y,
					ritual.center.z,
					SoundEvents.FIRE_AMBIENT,
					SoundSource.PLAYERS,
					0.6F,
					0.8F + level.getRandom().nextFloat() * 0.3F
				);
			}
			return true;
		}

		complete(level, player, ritual);
		return false;
	}

	private static void spawnChargeParticles(ServerLevel level, Vec3 center, int ticks, RandomSource random) {
		double progress = Mth.clamp(ticks / (double) DURATION_TICKS, 0.0, 1.0);
		double currentRadius = RADIUS * (0.15 + 0.85 * progress);
		int count = 4 + (int) (PARTICLES_PER_TICK * progress);

		for (int i = 0; i < count; i++) {
			double theta = random.nextDouble() * Math.PI * 2.0;
			double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
			double sinPhi = Math.sin(phi);
			double x = center.x + currentRadius * sinPhi * Math.cos(theta);
			double y = center.y + currentRadius * Math.cos(phi);
			double z = center.z + currentRadius * sinPhi * Math.sin(theta);
			level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	private static void complete(ServerLevel level, ServerPlayer activator, ActiveRitual ritual) {
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return;
		}

		strikeLightning(level, ritual.center);

		ItemStack stack = activator.getItemInHand(ritual.hand);
		if (stack.is(ModItems.EMERGENCY_RETURN_POD)) {
			activator.setItemInHand(ritual.hand, new ItemStack(ModItems.BROKEN_EMERGENCY_RETURN_POD));
		}

		AABB box = new AABB(
			ritual.center.x - RADIUS,
			ritual.center.y - RADIUS,
			ritual.center.z - RADIUS,
			ritual.center.x + RADIUS,
			ritual.center.y + RADIUS,
			ritual.center.z + RADIUS
		);

		List<Entity> targets = new ArrayList<>(level.getEntities((Entity) null, box, entity ->
			entity.isAlive()
				&& !entity.isRemoved()
				&& entity.distanceToSqr(ritual.center) <= RADIUS_SQ
				&& canTeleport(entity)
		));

		LevelData.RespawnData respawnData = overworld.getRespawnData();
		BlockPos spawnPos = respawnData.pos();
		Vec3 destination = Vec3.atBottomCenterOf(activator.adjustSpawnLocation(overworld, spawnPos));

		level.playSound(
			null,
			ritual.center.x,
			ritual.center.y,
			ritual.center.z,
			SoundEvents.PORTAL_TRIGGER,
			SoundSource.PLAYERS,
			1.0F,
			0.7F
		);

		for (Entity entity : targets) {
			Vec3 entityDestination = entity == activator
				? destination
				: Vec3.atBottomCenterOf(entity.adjustSpawnLocation(overworld, spawnPos));
			teleportToOverworldSpawn(entity, overworld, entityDestination, respawnData.yaw(), respawnData.pitch());
		}

		overworld.playSound(
			null,
			spawnPos,
			SoundEvents.PORTAL_TRAVEL,
			SoundSource.PLAYERS,
			0.5F,
			1.0F
		);
	}

	private static boolean canTeleport(Entity entity) {
		if (entity instanceof Player player && player.isSpectator()) {
			return false;
		}
		return !entity.isPassenger();
	}

	private static void strikeLightning(ServerLevel level, Vec3 center) {
		LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (lightning != null) {
			lightning.snapTo(center.x, center.y, center.z);
			lightning.setVisualOnly(true);
			level.addFreshEntity(lightning);
		}
	}

	private static void teleportToOverworldSpawn(
		Entity entity,
		ServerLevel overworld,
		Vec3 destination,
		float yaw,
		float pitch
	) {
		Entity teleported = entity.teleport(new TeleportTransition(
			overworld,
			destination,
			Vec3.ZERO,
			yaw,
			pitch,
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		if (teleported != null) {
			teleported.resetFallDistance();
		}
	}

	private static final class ActiveRitual {
		private final ServerLevel level;
		private final UUID playerId;
		private final InteractionHand hand;
		private final Vec3 center;
		private int ticks;

		private ActiveRitual(ServerLevel level, UUID playerId, InteractionHand hand, Vec3 center, int ticks) {
			this.level = level;
			this.playerId = playerId;
			this.hand = hand;
			this.center = center;
			this.ticks = ticks;
		}
	}
}
