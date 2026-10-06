package com.theouterworld.entity.ai;

import net.minecraft.world.level.Level;

/** Villager hours: up at tick 10, in bed from tick 12000, read from the dimension's clock. */
public final class WeaverSchedule {
	public static final long DAY_LENGTH = 24000L;
	public static final long WAKE = 10L;
	public static final long BEDTIME = 12000L;

	private WeaverSchedule() {
	}

	public static long totalTicks(Level level) {
		return level.dimensionType().defaultClock().isPresent()
			? level.getDefaultClockTime()
			: level.getOverworldClockTime();
	}

	public static long timeOfDay(Level level) {
		return Math.floorMod(totalTicks(level), DAY_LENGTH);
	}

	public static boolean isBedtime(Level level) {
		long time = timeOfDay(level);
		return time >= BEDTIME || time < WAKE;
	}

	/** Changes once per morning at {@link #WAKE}. Setting the time back starts a new day too. */
	public static long dayIndex(Level level) {
		return Math.floorDiv(totalTicks(level) - WAKE, DAY_LENGTH);
	}

	/** Ticks since this morning's wake-up. */
	public static long sinceWake(Level level) {
		return Math.floorMod(totalTicks(level) - WAKE, DAY_LENGTH);
	}
}
