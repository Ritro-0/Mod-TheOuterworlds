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
 * High-altitude volcanic bowls on Mons. Each chunk independently paints the portion of
 * nearby calderas that falls inside it so the bowls are not clipped at chunk borders.
 */
public class CalderaFeature implements Feature {
	public static final MapCodec<CalderaFeature> CODEC = MapCodec.unit(CalderaFeature::new);

	private static final int MAX_EXTENT = 24;
	private static final int CELL = 48;
	private static final double SPAWN_CHANCE = 0.12;
	private static final int MIN_SURFACE_Y = 118;

	public CalderaFeature() {
	}

	@Override
	public MapCodec<CalderaFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		return applyGrid(world, world.getSeed() + 9041, minX, minZ, maxX, maxZ);
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
		if (WorldgenNoise.hash(seed + 19, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		int radius = 10 + (int) (WorldgenNoise.hash(seed + 41, cellX, cellZ) * 7.0);
		int depth = 3 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 3.0);
		int jitter = 8;
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 83, cellX, cellZ)) * jitter);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 101, cellX, cellZ)) * jitter);

		int x0 = Math.max(minX, centerX - radius);
		int x1 = Math.min(maxX, centerX + radius);
		int z0 = Math.max(minZ, centerZ - radius);
		int z1 = Math.min(maxZ, centerZ + radius);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		BlockState basalt = ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minY = world.getMinY() + 1;
		boolean placed = false;

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double dist = Math.hypot(x - centerX, z - centerZ);
				if (dist > radius) {
					continue;
				}
				int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				if (surfaceY < MIN_SURFACE_Y) {
					continue;
				}
				double t = dist / radius;
				int floorY = Math.max(minY, surfaceY - (int) Math.round(depth * (1.0 - t * t)));
				for (int y = surfaceY; y > floorY; y--) {
					cursor.set(x, y, z);
					if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						world.setBlock(cursor, air, 2);
					}
				}
				cursor.set(x, floorY, z);
				if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
					world.setBlock(cursor, basalt, 2);
				}
				placed = true;
			}
		}
		return placed;
	}
}
