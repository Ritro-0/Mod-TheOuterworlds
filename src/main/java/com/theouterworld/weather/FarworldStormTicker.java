package com.theouterworld.weather;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.FarworldLayers;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
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
 * Farworld: eternal diamond rain plus teal lightning striking the liquid ammonia sea.
 */
public final class FarworldStormTicker {
	private static final int SYNC_INTERVAL = 40;
	private static final int LIGHTNING_CHECK_INTERVAL = 40;
	private static final float LIGHTNING_CHANCE = 0.35F;
	private static final double PLAYER_AVOID_RADIUS = 36.0;

	private FarworldStormTicker() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(FarworldStormTicker::tickWorld);
	}

	private static void tickWorld(ServerLevel world) {
		if (!ModDimensions.isFarworld(world.dimension())) {
			return;
		}

		world.setRainLevel(1.0F);
		world.setThunderLevel(1.0F);

		if (world.getGameTime() % SYNC_INTERVAL == 0) {
			var players = world.getServer().getPlayerList();
			players.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F), world.dimension());
			players.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F), world.dimension());
			players.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1.0F), world.dimension());
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

			int y = FarworldLayers.AMMONIA_SEA_LEVEL;
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
