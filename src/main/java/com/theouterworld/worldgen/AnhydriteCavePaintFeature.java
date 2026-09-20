package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Paints anhydrite patches onto sulfur-cave walls, with smaller accents on
 * ceilings near corners (air below + two solid neighbors). Never touches the
 * surface or sulfuric basalt crust.
 */
public class AnhydriteCavePaintFeature implements Feature {
	public static final MapCodec<AnhydriteCavePaintFeature> CODEC = MapCodec.unit(AnhydriteCavePaintFeature::new);

	private static final int MIN_DEPTH_BELOW_SURFACE = 12;
	private static final Direction[] HORIZONTAL = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

	public AnhydriteCavePaintFeature() {
	}

	@Override
	public MapCodec<AnhydriteCavePaintFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockState anhydrite = ModBlocks.ANHYDRITE.defaultBlockState();
		boolean placed = false;

		int attempts = 10 + random.nextInt(8);
		for (int i = 0; i < attempts; i++) {
			int x = origin.getX() + random.nextInt(16);
			int y = origin.getY() + random.nextInt(12) - 4;
			int z = origin.getZ() + random.nextInt(16);
			cursor.set(x, y, z);
			if (!isDeepEnough(world, cursor)) {
				continue;
			}
			if (!isReplaceableCaveStone(world.getBlockState(cursor))) {
				continue;
			}

			boolean wall = isExposedWall(world, cursor);
			boolean ceilingCorner = isCeilingCorner(world, cursor);
			if (!wall && !ceilingCorner) {
				continue;
			}

			int radius = wall ? 2 + random.nextInt(3) : 1 + random.nextInt(2);
			int painted = paintBlob(world, cursor, anhydrite, radius, random, wall);
			if (painted > 0) {
				placed = true;
			}
		}
		return placed;
	}

	private static int paintBlob(
		WorldGenLevel world,
		BlockPos center,
		BlockState anhydrite,
		int radius,
		RandomSource random,
		boolean preferWall
	) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int painted = 0;
		int r2 = radius * radius;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dy * dy + dz * dz > r2 + random.nextInt(2)) {
						continue;
					}
					cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					if (!isDeepEnough(world, cursor)) {
						continue;
					}
					if (!isReplaceableCaveStone(world.getBlockState(cursor))) {
						continue;
					}
					if (preferWall) {
						if (!isExposedWall(world, cursor) && !(dy >= 0 && isCeilingCorner(world, cursor) && random.nextFloat() < 0.35f)) {
							continue;
						}
					} else if (!isCeilingCorner(world, cursor) && !isExposedWall(world, cursor)) {
						continue;
					}
					world.setBlock(cursor, anhydrite, 2);
					painted++;
				}
			}
		}
		return painted;
	}

	private static boolean isDeepEnough(WorldGenLevel world, BlockPos pos) {
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
		return pos.getY() <= surfaceY - MIN_DEPTH_BELOW_SURFACE;
	}

	private static boolean isExposedWall(WorldGenLevel world, BlockPos pos) {
		for (Direction dir : HORIZONTAL) {
			BlockState neighbor = world.getBlockState(pos.relative(dir));
			if (neighbor.isAir() || !neighbor.getFluidState().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCeilingCorner(WorldGenLevel world, BlockPos pos) {
		if (!world.getBlockState(pos.below()).isAir()) {
			return false;
		}
		int solidSides = 0;
		for (Direction dir : HORIZONTAL) {
			BlockState neighbor = world.getBlockState(pos.relative(dir));
			if (isReplaceableCaveStone(neighbor) || neighbor.is(ModBlocks.ANHYDRITE)) {
				solidSides++;
			}
		}
		return solidSides >= 2;
	}

	private static boolean isReplaceableCaveStone(BlockState state) {
		return state.is(Blocks.SULFUR)
			|| state.is(Blocks.CINNABAR)
			|| state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT);
	}
}
