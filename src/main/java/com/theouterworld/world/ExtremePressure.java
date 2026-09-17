package com.theouterworld.world;

import com.theouterworld.registry.ModDamageTypes;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GraphiteProtection;
import com.theouterworld.util.IridiumProtection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Surface pressure: Nearworld needs Iridium (any 1) or 3 graphite-trimmed pieces;
 * Emberworld needs Iridium (any 1) or 3 graphite-trimmed Redsteel body pieces.
 * Stronger and faster than Innerworld solar burn.
 */
public final class ExtremePressure {
	private static final float RAMP_SECONDS = 4.0F;
	private static final float MAX_DAMAGE = 3.0F;
	private static final int DAMAGE_INTERVAL_TICKS = 10;
	private static final int MAX_EXPOSURE_TICKS = (int) (RAMP_SECONDS * 20.0F);

	private static final Map<UUID, Integer> EXPOSURE_TICKS = new HashMap<>();

	private ExtremePressure() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
			Iterator<Map.Entry<UUID, Integer>> it = EXPOSURE_TICKS.entrySet().iterator();
			while (it.hasNext()) {
				UUID id = it.next().getKey();
				if (server.getPlayerList().getPlayer(id) == null) {
					it.remove();
				}
			}
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		boolean near = ModDimensions.isNearworld(level.dimension());
		boolean ember = ModDimensions.isEmberworld(level.dimension());
		if (!near && !ember) {
			EXPOSURE_TICKS.remove(player.getUUID());
			return;
		}

		GameType mode = player.gameMode();
		if (mode == null || !mode.isSurvival() || player.getAbilities().invulnerable || !player.isAlive()) {
			EXPOSURE_TICKS.remove(player.getUUID());
			return;
		}

		boolean protectedNow = IridiumProtection.hasIridiumArmor(player)
			|| (near && GraphiteProtection.hasPressureResistance(player))
			|| (ember && GraphiteProtection.hasEmberworldPressureResistance(player));
		if (protectedNow) {
			EXPOSURE_TICKS.remove(player.getUUID());
			return;
		}

		int ticks = Math.min(MAX_EXPOSURE_TICKS, EXPOSURE_TICKS.getOrDefault(player.getUUID(), 0) + 1);
		EXPOSURE_TICKS.put(player.getUUID(), ticks);

		if (ticks < 4) {
			return;
		}
		if (player.tickCount % DAMAGE_INTERVAL_TICKS != 0) {
			return;
		}

		float intensity = ticks / (float) MAX_EXPOSURE_TICKS;
		float damage = MAX_DAMAGE * intensity * intensity;
		if (damage <= 0.05F) {
			return;
		}
		player.hurtServer(level, player.damageSources().source(ModDamageTypes.EXTREME_PRESSURE), damage);
	}

	public static float getIntensity(ServerPlayer player, float partialTick) {
		Integer ticks = EXPOSURE_TICKS.get(player.getUUID());
		if (ticks == null) {
			return 0.0F;
		}
		return Mth.clamp((ticks + partialTick) / (float) MAX_EXPOSURE_TICKS, 0.0F, 1.0F);
	}
}
