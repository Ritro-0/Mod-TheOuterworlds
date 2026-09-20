package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;

/**
 * Shallow lava pools on the floors of open Nearworld sulfur caves.
 * Never carves wall or ceiling pockets — those were placing osmium in vertical lava blobs.
 */
public class NearworldCaveLavaPoolFeature implements Feature {
	public static final MapCodec<NearworldCaveLavaPoolFeature> CODEC = MapCodec.unit(NearworldCaveLavaPoolFeature::new);

	private static final Direction[] HORIZONTAL = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
	private static final int MIN_AIR_ABOVE = 4;
	private static final int MIN_DEPTH_BELOW_SURFACE = 10;
	private static final int MIN_OPEN_SIDES = 2;

	public NearworldCaveLavaPoolFeature() {
	}

	@Override
	public MapCodec<NearworldCaveLavaPoolFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int attempts = 10 + random.nextInt(8);
		for (int i = 0; i < attempts; i++) {
			int x = origin.getX() + random.nextInt(16);
			int z = origin.getZ() + random.nextInt(16);
			int startY = Math.min(world.getMaxY() - 2, origin.getY() + 10);
			int endY = Math.max(world.getMinY() + 4, origin.getY() - 16);
			for (int y = startY; y >= endY; y--) {
				cursor.set(x, y, z);
				if (!isCaveFloor(world, cursor)) {
					continue;
				}
				if (placePool(world, cursor.immutable(), random)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean placePool(WorldGenLevel world, BlockPos floor, RandomSource random) {
		int radiusX = 2 + random.nextInt(3);
		int radiusZ = 2 + random.nextInt(3);
		int depth = 1 + random.nextInt(2);
		BlockState rim = Blocks.CINNABAR.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean placed = false;

		for (int dx = -radiusX - 1; dx <= radiusX + 1; dx++) {
			for (int dz = -radiusZ - 1; dz <= radiusZ + 1; dz++) {
				double nx = dx / (double) radiusX;
				double nz = dz / (double) radiusZ;
				double dist = Math.hypot(nx, nz);
				if (dist > 1.2) {
					continue;
				}

				int x = floor.getX() + dx;
				int z = floor.getZ() + dz;
				int localFloor = findLocalFloor(world, x, floor.getY(), z);
				if (localFloor == Integer.MIN_VALUE) {
					continue;
				}
				if (!isDeepEnough(world, x, localFloor, z)) {
					continue;
				}

				if (dist > 1.0) {
					cursor.set(x, localFloor, z);
					if (isReplaceableFloor(world.getBlockState(cursor))) {
						world.setBlock(cursor, rim, 2);
					}
					continue;
				}

				int lavaTop = localFloor;
				int lavaBottom = localFloor - depth + 1;
				for (int y = lavaTop; y >= lavaBottom; y--) {
					cursor.set(x, y, z);
					BlockState current = world.getBlockState(cursor);
					if (current.is(Blocks.BEDROCK) || (!isReplaceableFloor(current) && !current.isAir())) {
						continue;
					}
					WorldgenLava.place(world, cursor, y == lavaTop);
					placed = true;
				}
			}
		}
		return placed;
	}

	private static int findLocalFloor(WorldGenLevel world, int x, int aroundY, int z) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int maxY = Math.min(world.getMaxY() - 2, aroundY + 3);
		int minY = Math.max(world.getMinY() + 3, aroundY - 3);
		for (int y = maxY; y >= minY; y--) {
			cursor.set(x, y, z);
			if (isCaveFloor(world, cursor)) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}

	private static boolean isCaveFloor(WorldGenLevel world, BlockPos pos) {
		BlockState floor = world.getBlockState(pos);
		if (!isReplaceableFloor(floor)) {
			return false;
		}
		if (!isDeepEnough(world, pos.getX(), pos.getY(), pos.getZ())) {
			return false;
		}
		int openSides = 0;
		for (int dy = 1; dy <= MIN_AIR_ABOVE; dy++) {
			if (!world.getBlockState(pos.above(dy)).isAir()) {
				return false;
			}
		}
		for (Direction dir : HORIZONTAL) {
			if (world.getBlockState(pos.above().relative(dir)).isAir()) {
				openSides++;
			}
		}
		return openSides >= MIN_OPEN_SIDES;
	}

	private static boolean isDeepEnough(WorldGenLevel world, int x, int y, int z) {
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		return y <= surfaceY - MIN_DEPTH_BELOW_SURFACE;
	}

	private static boolean isReplaceableFloor(BlockState state) {
		if (state.is(Blocks.BEDROCK) || state.is(Blocks.LAVA)) {
			return false;
		}
		return state.is(Blocks.SULFUR)
			|| state.is(Blocks.CINNABAR)
			|| state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT)
			|| state.is(Blocks.MAGMA_BLOCK)
			|| state.is(ModBlocks.SULFURIC_BASALT)
			|| state.is(ModBlocks.ANHYDRITE)
			|| state.is(BlockTags.BASE_STONE_OVERWORLD)
			|| state.is(BlockTags.BASE_STONE_NETHER);
	}
}
