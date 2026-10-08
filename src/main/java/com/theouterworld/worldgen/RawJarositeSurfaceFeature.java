package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * A small exposed patch of raw jarosite on the open surface. It uses the same surface test as
 * the pebbles, so it never lands on a cave floor. Each column in the patch finds its own
 * surface block, so the patch follows the ground. The patch stays inside the chunk being
 * generated, so no neighboring chunk is ever read or written.
 */
public class RawJarositeSurfaceFeature implements Feature {
	public static final MapCodec<RawJarositeSurfaceFeature> CODEC = MapCodec.unit(RawJarositeSurfaceFeature::new);

	private static final int ATTEMPTS = 4;
	private static final int RADIUS = 2;

	public RawJarositeSurfaceFeature() {
	}

	@Override
	public MapCodec<RawJarositeSurfaceFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int chunkX = origin.getX() & ~15;
		int chunkZ = origin.getZ() & ~15;
		for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
			int x = attempt == 0 ? origin.getX() : chunkX + random.nextInt(16);
			int z = attempt == 0 ? origin.getZ() : chunkZ + random.nextInt(16);
			if (placePatch(world, random, x, z, chunkX, chunkZ)) {
				return true;
			}
		}
		return false;
	}

	private static boolean placePatch(WorldGenLevel world, RandomSource random, int cx, int cz, int chunkX, int chunkZ) {
		// Only start on a real surface column, so a failed spot retries elsewhere.
		if (!isExposable(world, cx, cz)) {
			return false;
		}
		int placed = 0;
		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				int x = cx + dx;
				int z = cz + dz;
				if (x < chunkX || x > chunkX + 15 || z < chunkZ || z > chunkZ + 15) {
					continue;
				}
				if (dx * dx + dz * dz > RADIUS * RADIUS) {
					continue;
				}
				boolean center = dx == 0 && dz == 0;
				if (!center && random.nextFloat() > 0.7F) {
					continue;
				}
				BlockPos open = OxidizedBasaltPebbleFeature.openSurface(world, x, z);
				if (open == null) {
					continue;
				}
				BlockPos floor = open.below();
				if (!replaceable(world.getBlockState(floor))) {
					continue;
				}
				world.setBlock(floor, ModBlocks.RAW_JAROSITE.defaultBlockState(), Block.UPDATE_CLIENTS);
				placed++;
			}
		}
		return placed > 0;
	}

	private static boolean isExposable(WorldGenLevel world, int x, int z) {
		BlockPos open = OxidizedBasaltPebbleFeature.openSurface(world, x, z);
		return open != null && replaceable(world.getBlockState(open.below()));
	}

	private static boolean replaceable(BlockState state) {
		return state.is(ModBlocks.REGOLITH) || state.is(ModBlocks.OXIDIZED_BASALT);
	}
}
