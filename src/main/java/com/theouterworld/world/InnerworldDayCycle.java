package com.theouterworld.world;

/**
 * Innerworld (Mercury) keeps normal dayTime / day counters, but celestial motion
 * is stretched to a 90 real-minute visual day/night cycle (108000 ticks).
 * Deterministic from the dimension's default clock time, so it stays consistent across reloads.
 */
public final class InnerworldDayCycle {
	/** 90 minutes at 20 TPS. */
	public static final long VISUAL_DAY_TICKS = 108_000L;

	private InnerworldDayCycle() {
	}

	/** Absolute clock ticks used for the stretched visual cycle. */
	public static long clockTime(net.minecraft.world.level.Level level) {
		return level.getDefaultClockTime();
	}

	/** 0–1 fraction through the visual day; 0 ≈ dawn, 0.25 ≈ noon, 0.5 ≈ dusk, 0.75 ≈ midnight. */
	public static float visualTimeOfDay(long clockTime, float partialTick) {
		double ticks = Math.floorMod(clockTime, VISUAL_DAY_TICKS) + partialTick;
		return (float) (ticks / (double) VISUAL_DAY_TICKS);
	}

	/** Celestial sun angle in radians; 0 is noon / zenith (matches SkyRenderer convention). */
	public static float sunAngleRadians(long clockTime, float partialTick) {
		float t = visualTimeOfDay(clockTime, partialTick);
		return (t - 0.25F) * ((float) Math.PI * 2.0F);
	}

	public static float sunAngleRadians(net.minecraft.world.level.Level level, float partialTick) {
		return sunAngleRadians(clockTime(level), partialTick);
	}

	/** True while the stretched sun is above the horizon. */
	public static boolean isSunUp(long clockTime) {
		return Math.cos(sunAngleRadians(clockTime, 0.0F)) > 0.0;
	}

	public static boolean isSunUp(net.minecraft.world.level.Level level) {
		return isSunUp(clockTime(level));
	}
}
