package com.theouterworld.weather;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 3D flood fill through air only. Any non-air block (glass, panes, fences, doors,
 * trapdoors, stairs, etc.) counts as a sealing wall.
 * Sealed if the reachable air volume stays under {@link #MAX_VOLUME}.
 */
public final class InteriorFloodFiller {
	public static final int MAX_VOLUME = 4096;

	private InteriorFloodFiller() {
	}

	/**
	 * @return sealed interior cells, or null if open / too large
	 */
	public static @Nullable Result tryFindInterior(Level level, BlockPos seed) {
		ScanResult scan = scan(level, seed);
		return scan.sealed() ? new Result(scan.visitedAir()) : null;
	}

	/**
	 * Full scan for commands/debug: always returns visited air (capped), plus whether it sealed.
	 */
	public static ScanResult scan(Level level, BlockPos seed) {
		BlockPos start = findAirSeed(level, seed);
		if (start == null) {
			return new ScanResult(false, Set.of());
		}

		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();
		Set<BlockPos> air = new HashSet<>();

		queue.add(start.immutable());
		boolean sealed = true;

		while (!queue.isEmpty()) {
			BlockPos pos = queue.removeFirst();
			if (!visited.add(pos)) {
				continue;
			}

			if (!isInLoadedWorld(level, pos)) {
				sealed = false;
				break;
			}

			if (!isPassable(level.getBlockState(pos))) {
				continue;
			}

			air.add(pos);
			if (air.size() > MAX_VOLUME) {
				sealed = false;
				break;
			}

			for (Direction direction : Direction.values()) {
				BlockPos next = pos.relative(direction);
				if (!visited.contains(next)) {
					queue.add(next);
				}
			}
		}

		if (air.isEmpty()) {
			sealed = false;
		}

		return new ScanResult(sealed, air);
	}

	private static @Nullable BlockPos findAirSeed(Level level, BlockPos seed) {
		BlockPos feet = seed.immutable();
		if (isPassable(level.getBlockState(feet))) {
			return feet;
		}
		BlockPos above = feet.above();
		if (isPassable(level.getBlockState(above))) {
			return above;
		}
		return null;
	}

	/** Only air is passable — every other block seals. */
	static boolean isPassable(BlockState state) {
		return state.isAir();
	}

	private static boolean isInLoadedWorld(Level level, BlockPos pos) {
		int y = pos.getY();
		return y >= level.getMinY() && y <= level.getMaxY() && level.hasChunkAt(pos);
	}

	public record ScanResult(boolean sealed, Set<BlockPos> visitedAir) {
	}

	public record Result(Set<BlockPos> cells) {
		public int minX() {
			int min = Integer.MAX_VALUE;
			for (BlockPos pos : cells) {
				min = Math.min(min, pos.getX());
			}
			return min;
		}

		public int minY() {
			int min = Integer.MAX_VALUE;
			for (BlockPos pos : cells) {
				min = Math.min(min, pos.getY());
			}
			return min;
		}

		public int minZ() {
			int min = Integer.MAX_VALUE;
			for (BlockPos pos : cells) {
				min = Math.min(min, pos.getZ());
			}
			return min;
		}

		public int maxX() {
			int max = Integer.MIN_VALUE;
			for (BlockPos pos : cells) {
				max = Math.max(max, pos.getX());
			}
			return max;
		}

		public int maxY() {
			int max = Integer.MIN_VALUE;
			for (BlockPos pos : cells) {
				max = Math.max(max, pos.getY());
			}
			return max;
		}

		public int maxZ() {
			int max = Integer.MIN_VALUE;
			for (BlockPos pos : cells) {
				max = Math.max(max, pos.getZ());
			}
			return max;
		}
	}
}
