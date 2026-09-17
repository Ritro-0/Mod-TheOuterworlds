package com.theouterworld.world;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Air-exposed solar plasma sources briefly flow outward a couple of blocks
 * (like a short water splash), then retract to the source, forever.
 * <p>
 * Only true source blocks drive the cycle. Surge cells are always flowing
 * (amount 1–7) so they can never become new sources and keep expanding.
 */
public final class SolarPlasmaPulse {
	/** "A couple of blocks" — short water-like reach. */
	public static final int MAX_RADIUS = 2;
	/** Full expand + retract cycle length in ticks. */
	private static final int CYCLE_TICKS = 48;
	private static final int TICK_INTERVAL = 2;

	private SolarPlasmaPulse() {
	}

	public static int radius(long gameTime) {
		int phase = (int) (gameTime % CYCLE_TICKS);
		int half = CYCLE_TICKS / 2;
		if (phase < half) {
			return (phase * MAX_RADIUS + half / 2) / half;
		}
		int retract = phase - half;
		return Math.max(0, MAX_RADIUS - (retract * MAX_RADIUS + half / 2) / half);
	}

	/**
	 * A source that should pulse: real crust source with only air / surge above
	 * (so our own upward splash does not silence it).
	 */
	public static boolean isAirExposedSource(Level level, BlockPos pos) {
		FluidState fluid = level.getFluidState(pos);
		if (fluid.getType() != ModFluids.SOLAR_PLASMA || !fluid.isSource()) {
			return false;
		}
		// Never let floating sources above the crust (old expand bug) keep pulsing.
		if (pos.getY() > SunTerrain.plasmaTopY(level.getMinY())) {
			return false;
		}
		FluidState aboveFluid = level.getFluidState(pos.above());
		if (isSurgePlasma(aboveFluid)) {
			return true;
		}
		return level.getBlockState(pos.above()).isAir();
	}

	public static void pulseFromSource(ServerLevel level, BlockPos source) {
		int radius = radius(level.getGameTime());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -MAX_RADIUS; dx <= MAX_RADIUS; dx++) {
			for (int dy = -MAX_RADIUS; dy <= MAX_RADIUS; dy++) {
				for (int dz = -MAX_RADIUS; dz <= MAX_RADIUS; dz++) {
					int dist = manhattan(dx, dy, dz);
					if (dist == 0 || dist > MAX_RADIUS) {
						continue;
					}
					cursor.set(source.getX() + dx, source.getY() + dy, source.getZ() + dz);
					if (dist <= radius) {
						tryPlaceSurge(level, cursor, dist);
					} else {
						tryClearSurge(level, cursor);
					}
				}
			}
		}
	}

	public static void tickSurge(ServerLevel level, BlockPos pos) {
		FluidState fluid = level.getFluidState(pos);
		if (!isSurgePlasma(fluid)) {
			return;
		}
		int radius = radius(level.getGameTime());
		if (!coveredByActiveSource(level, pos, radius)) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
	}

	public static void wakeNeighbors(Level level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbour = pos.relative(direction);
			if (isPlasma(level.getFluidState(neighbour))) {
				level.scheduleTick(neighbour, ModBlocks.SOLAR_PLASMA, TICK_INTERVAL);
			}
		}
	}

	/** Ensure air-exposed sources near players are ticking (worldgen may not schedule). */
	public static void wakeSurfaceAround(Level level, BlockPos center, int area) {
		int plasmaY = SunTerrain.plasmaTopY(level.getMinY());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -area; dx <= area; dx++) {
			for (int dy = 0; dy <= MAX_RADIUS + 1; dy++) {
				for (int dz = -area; dz <= area; dz++) {
					cursor.set(center.getX() + dx, plasmaY + dy, center.getZ() + dz);
					FluidState fluid = level.getFluidState(cursor);
					// Dissolve errant sources left above the crust by the old expand bug.
					if (dy > 0 && fluid.getType() == ModFluids.SOLAR_PLASMA && fluid.isSource()
						&& level instanceof ServerLevel server) {
						server.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
						continue;
					}
					if (dy == 0 && isAirExposedSource(level, cursor)) {
						level.scheduleTick(cursor.immutable(), ModBlocks.SOLAR_PLASMA, 1);
					}
				}
			}
		}
	}

	private static void tryPlaceSurge(ServerLevel level, BlockPos pos, int dist) {
		BlockState state = level.getBlockState(pos);
		FluidState fluid = state.getFluidState();
		if (fluid.getType() == ModFluids.SOLAR_PLASMA && fluid.isSource()) {
			return;
		}
		if (!state.isAir() && !isSurgePlasma(fluid)) {
			return;
		}
		// Amount must stay 1–7. Amount 8 becomes a source via LiquidBlock LEVEL 0.
		int amount = Math.max(1, Math.min(7, 8 - dist));
		BlockState surge = ModFluids.FLOWING_SOLAR_PLASMA.getFlowing(amount, false).createLegacyBlock();
		if (!state.equals(surge)) {
			level.setBlock(pos, surge, 2);
		}
	}

	private static void tryClearSurge(ServerLevel level, BlockPos pos) {
		if (isSurgePlasma(level.getFluidState(pos))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
	}

	private static boolean coveredByActiveSource(Level level, BlockPos cell, int radius) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -MAX_RADIUS; dx <= MAX_RADIUS; dx++) {
			for (int dy = -MAX_RADIUS; dy <= MAX_RADIUS; dy++) {
				for (int dz = -MAX_RADIUS; dz <= MAX_RADIUS; dz++) {
					int dist = manhattan(dx, dy, dz);
					if (dist == 0 || dist > radius) {
						continue;
					}
					cursor.set(cell.getX() + dx, cell.getY() + dy, cell.getZ() + dz);
					if (isAirExposedSource(level, cursor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static int manhattan(int dx, int dy, int dz) {
		return Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
	}

	private static boolean isPlasma(FluidState fluid) {
		return fluid.getType() == ModFluids.SOLAR_PLASMA || fluid.getType() == ModFluids.FLOWING_SOLAR_PLASMA;
	}

	private static boolean isSurgePlasma(FluidState fluid) {
		return fluid.getType() == ModFluids.FLOWING_SOLAR_PLASMA;
	}
}
