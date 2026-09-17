package com.theouterworld.world;

/**
 * Spinlands (Haumea) keeps the dimension clock, but celestial discs rotate on an
 * 8 real-minute cycle: 4 minutes of day, 4 minutes of night.
 */
public final class SpinlandsDayCycle {
	/** 8 minutes at 20 TPS (4 min day + 4 min night). */
	public static final long VISUAL_DAY_TICKS = 9_600L;

	private SpinlandsDayCycle() {
	}

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

	/** Moon sits opposite the sun. */
	public static float moonAngleRadians(long clockTime, float partialTick) {
		return sunAngleRadians(clockTime, partialTick) + (float) Math.PI;
	}

	public static float moonAngleRadians(net.minecraft.world.level.Level level, float partialTick) {
		return moonAngleRadians(clockTime(level), partialTick);
	}

	public static boolean isSunUp(long clockTime) {
		return Math.cos(sunAngleRadians(clockTime, 0.0F)) > 0.0;
	}

	public static boolean isSunUp(net.minecraft.world.level.Level level) {
		return isSunUp(clockTime(level));
	}
}
