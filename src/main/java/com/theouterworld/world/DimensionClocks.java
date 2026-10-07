package com.theouterworld.world;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Clock faces follow the sun the sky is actually drawing. Innerworld and Spinlands
 * orbit on their own periods. Every other mod dimension follows the ordinary
 * 24000-tick day, because the vanilla clock item only knows the overworld and
 * otherwise spins at random. This does not change the dimension's day counter.
 */
public final class DimensionClocks {
	private DimensionClocks() {
	}

	public static boolean isSunUp(Level level) {
		if (ModDimensions.isInnerworld(level.dimension())) {
			return InnerworldDayCycle.isSunUp(level);
		}
		if (ModDimensions.isSpinlands(level.dimension())) {
			return SpinlandsDayCycle.isSunUp(level);
		}
		return com.theouterworld.entity.ai.WeaverSchedule.timeOfDay(level) < com.theouterworld.entity.ai.WeaverSchedule.BEDTIME;
	}

	/**
	 * Vanilla clock needle. {@link Float#NaN} leaves the overworld, Nether, and End
	 * on the clock item's own behavior.
	 */
	public static float clockAngle(Level level) {
		ResourceKey<Level> dimension = level.dimension();
		if (Level.OVERWORLD.equals(dimension) || Level.NETHER.equals(dimension) || Level.END.equals(dimension)) {
			return Float.NaN;
		}
		long cycle;
		long ticks;
		if (ModDimensions.isInnerworld(dimension)) {
			cycle = InnerworldDayCycle.VISUAL_DAY_TICKS;
			ticks = Math.floorMod(InnerworldDayCycle.clockTime(level), cycle);
		} else if (ModDimensions.isSpinlands(dimension)) {
			cycle = SpinlandsDayCycle.VISUAL_DAY_TICKS;
			ticks = Math.floorMod(SpinlandsDayCycle.clockTime(level), cycle);
		} else {
			cycle = 24_000L;
			ticks = com.theouterworld.entity.ai.WeaverSchedule.timeOfDay(level);
		}
		double mapped = ticks * (24000.0 / (double) cycle);
		double fractional = Mth.frac(mapped / 24000.0 - 0.25);
		double smoothed = 0.5 - Math.cos(fractional * Math.PI) / 2.0;
		return (float) ((fractional * 2.0 + smoothed) / 3.0);
	}
}
