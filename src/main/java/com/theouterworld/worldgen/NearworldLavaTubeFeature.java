package com.theouterworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Nearworld tube caves that stay buried (no floor lava). Same carving style as
 * {@link NearworldTubeCaveFeature}, kept as a separate feature for placement density.
 */
public class NearworldLavaTubeFeature implements Feature {
	public static final MapCodec<NearworldLavaTubeFeature> CODEC = MapCodec.unit(NearworldLavaTubeFeature::new);

	public NearworldLavaTubeFeature() {
	}

	@Override
	public MapCodec<NearworldLavaTubeFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
		if (surfaceY < world.getMinY() + 24) {
			return false;
		}
		return NearworldTubeCaveFeature.carveTube(world, random, origin, surfaceY, minX, minZ, maxX, maxZ, false);
	}
}
