package com.theouterworld.weather;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks dust storm exposure for each player, similar to how powdered snow tracks freezing.
 * Exposure builds up over time when in a dust storm and decays when out of it.
 */
public class DustStormExposure {
	private static final Map<UUID, ExposureData> playerExposure = new HashMap<>();

	private static class ExposureData {
		int exposureTicks = 0;

		void enterDuststorm() {
			if (exposureTicks < 0) {
				exposureTicks = 0;
			}
		}

		void exitDuststorm() {
			exposureTicks = -30;
		}

		void serverTick() {
			if (exposureTicks > 120) {
				exposureTicks = 120;
			}

			if (exposureTicks > -30) {
				exposureTicks += exposureTicks >= 0 ? 1 : -1;
			}
		}

		float getIntensity(float partialTick) {
			return Mth.clamp((exposureTicks + partialTick) / 120f, 0f, 1f);
		}

		int getExposureTicks() {
			return exposureTicks;
		}
	}

	public static void enterDuststorm(Player player) {
		playerExposure.computeIfAbsent(player.getUUID(), k -> new ExposureData()).enterDuststorm();
	}

	public static void exitDuststorm(Player player) {
		ExposureData data = playerExposure.get(player.getUUID());
		if (data != null) {
			data.exitDuststorm();
		}
	}

	public static void serverTick(Player player) {
		ExposureData data = playerExposure.get(player.getUUID());
		if (data != null) {
			data.serverTick();
		}
	}

	public static float getIntensity(Player player, float partialTick) {
		ExposureData data = playerExposure.get(player.getUUID());
		if (data == null) {
			return 0f;
		}
		return data.getIntensity(partialTick);
	}

	public static int getExposureTicks(Player player) {
		ExposureData data = playerExposure.get(player.getUUID());
		if (data == null) {
			return 0;
		}
		return data.getExposureTicks();
	}

	public static void removePlayer(Player player) {
		playerExposure.remove(player.getUUID());
	}
}
