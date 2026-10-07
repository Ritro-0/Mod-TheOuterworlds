package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.entity.StrandHydraEntity;
import com.theouterworld.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Plants Strand Hydras on the bedrock floors of Frostworld ravines.
 * The canyons themselves are cut by the seafloor density: long, sloped valleys, not round shafts.
 */
public class FrostworldRavineFeature implements Feature {
	public static final MapCodec<FrostworldRavineFeature> CODEC = MapCodec.unit(FrostworldRavineFeature::new);

	private static final double SPAWN_CHANCE = 0.12;
	private static final int BEDROCK_FLOOR = -60;

	public FrostworldRavineFeature() {
	}

	@Override
	public MapCodec<FrostworldRavineFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int chunkX = minX >> 4;
		int chunkZ = minZ >> 4;
		if (WorldgenNoise.hash(world.getSeed() + 29011L, chunkX, chunkZ) > SPAWN_CHANCE) {
			return false;
		}

		int bestY = Integer.MAX_VALUE;
		int bestX = minX;
		int bestZ = minZ;
		int deep = 0;
		for (int x = minX; x <= minX + 15; x++) {
			for (int z = minZ; z <= minZ + 15; z++) {
				int y = OceanFloor.surfaceY(world, x, z);
				if (y > BEDROCK_FLOOR) {
					continue;
				}
				deep++;
				if (y < bestY) {
					bestY = y;
					bestX = x;
					bestZ = z;
				}
			}
		}
		if (deep < 3) {
			return false;
		}
		return plantHydra(world, bestX, bestZ);
	}

	private static boolean plantHydra(WorldGenLevel world, int x, int z) {
		int floor = OceanFloor.surfaceY(world, x, z);
		if (floor < world.getMinY() || floor > BEDROCK_FLOOR) {
			return false;
		}
		if (!world.getBlockState(new BlockPos(x, floor, z)).is(Blocks.BEDROCK)) {
			return false;
		}
		for (int dy = 1; dy <= 3; dy++) {
			if (!world.getFluidState(new BlockPos(x, floor + dy, z)).is(FluidTags.WATER)) {
				return false;
			}
		}
		StrandHydraEntity hydra = ModEntities.STRAND_HYDRA.create(world.getLevel(), EntitySpawnReason.STRUCTURE);
		if (hydra == null) {
			return false;
		}
		hydra.snapTo(x + 0.5, floor + 1.0, z + 0.5, 0.0F, 0.0F);
		world.addFreshEntity(hydra);
		return true;
	}
}
