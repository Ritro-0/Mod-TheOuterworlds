package com.theouterworld.weather;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.EdgeworldLayers;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.AABB;

/**
 * Edgeworld: deep-blue lightning over the liquid methane sea (no perpetual rain).
 */
public final class EdgeworldStormTicker {
	private static final int LIGHTNING_CHECK_INTERVAL = 40;
	private static final float LIGHTNING_CHANCE = 0.35F;
	private static final double PLAYER_AVOID_RADIUS = 36.0;

	private EdgeworldStormTicker() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(EdgeworldStormTicker::tickWorld);
	}

	private static void tickWorld(ServerLevel world) {
		if (!ModDimensions.isEdgeworld(world.dimension())) {
			return;
		}
		if (world.getDifficulty() == Difficulty.PEACEFUL) {
			return;
		}
		if (world.getGameTime() % LIGHTNING_CHECK_INTERVAL != 0) {
			return;
		}
		if (world.players().isEmpty()) {
			return;
		}

		RandomSource random = world.getRandom();
		if (random.nextFloat() > LIGHTNING_CHANCE) {
			return;
		}
		ServerPlayer anchor = world.players().get(random.nextInt(world.players().size()));
		strikeInAirGap(world, anchor, random);
	}

	private static void strikeInAirGap(ServerLevel world, ServerPlayer anchor, RandomSource random) {
		for (int attempt = 0; attempt < 8; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = 40.0 + random.nextDouble() * 72.0;
			int x = anchor.blockPosition().getX() + Mth.floor(Math.cos(angle) * dist);
			int z = anchor.blockPosition().getZ() + Mth.floor(Math.sin(angle) * dist);

			int y = EdgeworldLayers.METHANE_SEA_LEVEL;
			BlockPos boltPos = new BlockPos(x, y, z);

			AABB avoid = new AABB(boltPos).inflate(PLAYER_AVOID_RADIUS, 80.0, PLAYER_AVOID_RADIUS);
			boolean nearPlayer = false;
			for (ServerPlayer player : world.players()) {
				if (avoid.contains(player.position())) {
					nearPlayer = true;
					break;
				}
			}
			if (nearPlayer) {
				continue;
			}

			LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
			if (bolt == null) {
				return;
			}
			bolt.snapTo(boltPos.getX() + 0.5, boltPos.getY(), boltPos.getZ() + 0.5);
			bolt.setVisualOnly(false);
			world.addFreshEntity(bolt);
			return;
		}
	}
}
