package com.theouterworld.world;

/**
 * Emberworld (Io) celestial motion helpers.
 * Sun uses the normal Overworld day cycle. The Io moon disc spends its time above
 * the horizon at Innerworld-sun angular speed, then races under the horizon at
 * normal day speed until it rises again.
 */
public final class EmberworldDayCycle {
	/** Half-cycle while Io is above the horizon — matches Innerworld daytime length. */
	public static final long SLOW_HALF_TICKS = InnerworldDayCycle.VISUAL_DAY_TICKS / 2L;
	/** Half-cycle while Io is below the horizon — one vanilla half-day. */
	public static final long FAST_HALF_TICKS = 12_000L;
	public static final long MOON_CYCLE_TICKS = SLOW_HALF_TICKS + FAST_HALF_TICKS;

	private EmberworldDayCycle() {
	}

	public static long clockTime(net.minecraft.world.level.Level level) {
		return level.getDefaultClockTime();
	}

	/**
	 * Io moon angle in radians (0 = zenith / noon convention, same as SkyRenderer).
	 * Above horizon: slow Innerworld-paced arc. Below horizon: fast vanilla-paced arc.
	 */
	public static float moonAngleRadians(long clockTime, float partialTick) {
		double ticks = Math.floorMod(clockTime, MOON_CYCLE_TICKS) + partialTick;
		if (ticks < SLOW_HALF_TICKS) {
			float u = (float) (ticks / (double) SLOW_HALF_TICKS);
			// Rise (-π/2) → zenith (0) → set (+π/2)
			return (u - 0.5F) * (float) Math.PI;
		}
		float u = (float) ((ticks - SLOW_HALF_TICKS) / (double) FAST_HALF_TICKS);
		// Set (+π/2) → nadir (π) → rise (+3π/2)
		return (float) Math.PI * 0.5F + u * (float) Math.PI;
	}

	public static float moonAngleRadians(net.minecraft.world.level.Level level, float partialTick) {
		return moonAngleRadians(clockTime(level), partialTick);
	}

	public static boolean isMoonUp(long clockTime) {
		return Math.cos(moonAngleRadians(clockTime, 0.0F)) > 0.0;
	}

	/** Vanilla-speed sun: true while the Overworld sun is above the horizon. */
	public static boolean isSunUp(long clockTime) {
		long day = Math.floorMod(clockTime, 24_000L);
		return day >= 0L && day < 12_000L;
	}

	public static boolean isSunUp(net.minecraft.world.level.Level level) {
		return isSunUp(clockTime(level));
	}
}
