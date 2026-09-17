package com.theouterworld.weather;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.ReplayCompat;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

public class DustStormTicker {
	private static final Map<ServerLevel, DustStormManager> managers = new HashMap<>();

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(world -> {
			DustStormManager manager = getManager(world);
			if (manager != null) {
				manager.tick(world);
			}
		});
	}

	public static DustStormManager getManager(ServerLevel world) {
		// Inside a replay the recorded packets are the source of truth; a manager here would invent
		// its own weather and broadcast over them.
		if (ReplayCompat.isReplay(world)) {
			return null;
		}
		if (world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return managers.computeIfAbsent(world, w -> new DustStormManager());
		}
		return null;
	}
}
