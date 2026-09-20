package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

/**
 * Occasional lava ponds carved into Emberworld's sulfuric surface.
 * Only places on high ground (surface above Y=76); lower elevations are lava ocean.
 */
public class EmberworldLavaPondFeature implements Feature {
	public static final MapCodec<EmberworldLavaPondFeature> CODEC = MapCodec.unit(EmberworldLavaPondFeature::new);

	private static final int MAX_EXTENT = 14;
	private static final int CELL = 72;
	private static final double SPAWN_CHANCE = 0.32;
	/** Ponds only on terrain that sits above the lava ocean / shoreline. */
	private static final int MIN_SURFACE_Y = 77;

	public EmberworldLavaPondFeature() {
	}

	@Override
	public MapCodec<EmberworldLavaPondFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		return applyGrid(world, world.getSeed() + 91337L, minX, minZ, minX + 15, minZ + 15);
	}

	private static boolean applyGrid(WorldGenLevel world, long seed, int minX, int minZ, int maxX, int maxZ) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (applyCell(world, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean applyCell(
		WorldGenLevel world,
		long seed,
		int cellX,
		int cellZ,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed + 13, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		int radiusX = 4 + (int) (WorldgenNoise.hash(seed + 41, cellX, cellZ) * 5.0);
		int radiusZ = 4 + (int) (WorldgenNoise.hash(seed + 53, cellX, cellZ) * 4.0);
		int depth = 2 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 2.0);
		int jitter = 10;
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 79, cellX, cellZ)) * jitter);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 97, cellX, cellZ)) * jitter);

		int x0 = Math.max(minX, centerX - radiusX - 2);
		int x1 = Math.min(maxX, centerX + radiusX + 2);
		int z0 = Math.max(minZ, centerZ - radiusZ - 2);
		int z1 = Math.min(maxZ, centerZ + radiusZ + 2);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
		if (surfaceY < MIN_SURFACE_Y) {
			return false;
		}
		// Skip if already in a lava ocean depression.
		if (world.getBlockState(new BlockPos(centerX, surfaceY - 1, centerZ)).liquid()) {
			return false;
		}

		BlockState lava = Blocks.LAVA.defaultBlockState();
		BlockState rim = ModBlocks.SULFURIC_BASALT.defaultBlockState();
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double nx = (x - centerX) / (double) Math.max(1, radiusX);
				double nz = (z - centerZ) / (double) Math.max(1, radiusZ);
				double dist = nx * nx + nz * nz;
				if (dist > 1.05) {
					continue;
				}
				int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				int floor = top - depth;
				for (int y = top; y >= floor; y--) {
					cursor.set(x, y, z);
					if (dist > 0.72) {
						if (y == floor || y == floor + 1) {
							world.setBlock(cursor, rim, 2);
							placed = true;
						}
					} else if (y > floor) {
						world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
					} else {
						world.setBlock(cursor, lava, 2);
						placed = true;
					}
				}
			}
		}
		return placed;
	}
}
