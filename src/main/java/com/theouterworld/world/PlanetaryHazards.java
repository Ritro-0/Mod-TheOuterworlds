package com.theouterworld.world;

import com.theouterworld.registry.ModDimensions;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Players and vanilla mobs share vacuum, heat, and pressure.
 * Mod creatures are left alone.
 */
public final class PlanetaryHazards {
	private PlanetaryHazards() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Set<UUID> present = new HashSet<>();
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
				present.add(player.getUUID());
			}
			for (ServerLevel level : server.getAllLevels()) {
				if (!ModDimensions.isVacuum(level.dimension())) {
					continue;
				}
				for (Entity entity : level.getAllEntities()) {
					if (!(entity instanceof Mob mob) || !mob.isAlive()) {
						continue;
					}
					if (!PlanetaryMobRules.isVanillaMob(mob)) {
						continue;
					}
					tick(mob);
					present.add(mob.getUUID());
				}
			}
			SolarIrradiation.prune(present);
			ExtremePressure.prune(present);
			HighworldCrush.prune(present);
		});
	}

	private static void tick(LivingEntity entity) {
		VacuumSuffocation.tick(entity);
		SolarIrradiation.tick(entity);
		ExtremePressure.tick(entity);
		HighworldCrush.tick(entity);
	}
}
