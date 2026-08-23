package com.theouterworld.weather;

import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

public class DustStormTicker {
	private static final Map<ServerLevel, DustStormManager> managers = new HashMap<>();

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(world -> {
			if (world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
				DustStormManager manager = managers.computeIfAbsent(world, w -> new DustStormManager());
				manager.tick(world);
			}
		});
	}

	public static DustStormManager getManager(ServerLevel world) {
		if (world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return managers.computeIfAbsent(world, w -> new DustStormManager());
		}
		return null;
	}
}
