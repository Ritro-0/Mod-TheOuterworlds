package com.theouterworld.weather;

import com.theouterworld.network.DustStormSyncPacket;
import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class DustStormManager {
	private static final int MIN_STORM_INTERVAL = 15 * 60 * 20; // 15 minutes in ticks
	private static final int MAX_STORM_INTERVAL = 30 * 60 * 20; // 30 minutes in ticks
	private static final int MIN_STORM_DURATION = 2 * 60 * 20; // 2 minutes in ticks
	private static final int MAX_STORM_DURATION = 5 * 60 * 20; // 5 minutes in ticks

	private long nextStormTime = -1;
	private long stormEndTime = -1;
	private boolean stormActive = false;

	public void tick(ServerLevel world) {
		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}

		long currentTime = world.getGameTime();
		RandomSource random = world.getRandom();

		if (!stormActive && nextStormTime == -1) {
			int interval = MIN_STORM_INTERVAL + random.nextInt(MAX_STORM_INTERVAL - MIN_STORM_INTERVAL);
			nextStormTime = currentTime + interval;
		}

		if (!stormActive && nextStormTime != -1 && currentTime >= nextStormTime) {
			startStorm(world, random);
		}

		if (stormActive && stormEndTime != -1 && currentTime >= stormEndTime) {
			endStorm(world);
		}
	}

	public void startStorm(ServerLevel world, RandomSource random) {
		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}

		if (stormActive) {
			return;
		}

		stormActive = true;
		int duration = MIN_STORM_DURATION + random.nextInt(MAX_STORM_DURATION - MIN_STORM_DURATION);
		stormEndTime = world.getGameTime() + duration;
		nextStormTime = -1;

		syncToAllClients(world);

		com.theouterworld.OuterWorldMod.LOGGER.info(
			"Dust storm started! Duration: {} ticks ({} seconds)",
			duration,
			duration / 20
		);
	}

	public void endStorm(ServerLevel world) {
		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}

		if (!stormActive) {
			return;
		}

		stormActive = false;
		stormEndTime = -1;
		nextStormTime = -1;

		syncToAllClients(world);

		com.theouterworld.OuterWorldMod.LOGGER.info("Dust storm stopped!");
	}

	public void forceStorm(ServerLevel world) {
		RandomSource random = world.getRandom();
		startStorm(world, random);
	}

	public boolean isStormActive() {
		return stormActive;
	}

	/**
	 * Marked interior cells are immune: an active storm does not affect entities there.
	 */
	public boolean affectsPosition(ServerLevel level, BlockPos pos) {
		return stormActive && !InteriorShelterTracker.isInterior(level, pos);
	}

	public boolean affectsEntity(ServerLevel level, Entity entity) {
		return affectsPosition(
			level,
			BlockPos.containing(entity.getX(), entity.getY() + 0.5, entity.getZ())
		);
	}

	private void syncToAllClients(ServerLevel world) {
		DustStormSyncPacket packet = new DustStormSyncPacket(this.stormActive);
		PlayerLookup.level(world).forEach(player -> ServerPlayNetworking.send(player, packet));
	}
}
