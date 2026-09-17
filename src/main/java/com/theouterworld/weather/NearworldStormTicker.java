package com.theouterworld.weather;

import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Forces perpetual sulfuric storms in the Nearworld via per-dimension rain/thunder
 * levels (weather data itself is server-global in 26.2, so we do not touch it).
 * Spawns lightning frequently; bolts still prefer rods / Channeling.
 */
public final class NearworldStormTicker {
	private static final int SYNC_INTERVAL = 40;
	private static final int LIGHTNING_CHECK_INTERVAL = 20;
	private static final float LIGHTNING_CHANCE_PER_PLAYER = 0.35F;

	private NearworldStormTicker() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(NearworldStormTicker::tickWorld);
	}

	private static void tickWorld(ServerLevel world) {
		if (!ModDimensions.isNearworld(world.dimension())) {
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

		RandomSource random = world.getRandom();
		for (ServerPlayer player : world.players()) {
			if (random.nextFloat() > LIGHTNING_CHANCE_PER_PLAYER) {
				continue;
			}
			strikeNear(world, player, random);
		}
	}

	private static void strikeNear(ServerLevel world, ServerPlayer player, RandomSource random) {
		int offsetX = random.nextInt(48) - 24;
		int offsetZ = random.nextInt(48) - 24;
		BlockPos around = player.blockPosition().offset(offsetX, 0, offsetZ);
		BlockPos strikePos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, around);

		LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
		if (bolt == null) {
			return;
		}
		bolt.snapTo(strikePos.getX() + 0.5, strikePos.getY(), strikePos.getZ() + 0.5);
		bolt.setVisualOnly(false);
		world.addFreshEntity(bolt);
	}
}
