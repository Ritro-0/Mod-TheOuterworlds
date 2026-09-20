package com.theouterworld.worldgen;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Places Outerworld olivine geodes in the Nearworld, but only in columns under
 * the shared Nearworld volcano footprint.
 */
public class NearworldUnderVolcanoOlivineGeodeFeature implements Feature {
	public static final MapCodec<NearworldUnderVolcanoOlivineGeodeFeature> CODEC = MapCodec.unit(NearworldUnderVolcanoOlivineGeodeFeature::new);

	private static final ResourceKey<Feature> OLIVINE_GEODE =
		ResourceKey.create(Registries.FEATURE, OuterWorldMod.id("olivine_geode"));

	public NearworldUnderVolcanoOlivineGeodeFeature() {
	}

	@Override
	public MapCodec<NearworldUnderVolcanoOlivineGeodeFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {

		if (random.nextInt(18) != 0) {
			return false;
		}

		Holder.Reference<Feature> geode = world.registryAccess()
			.lookupOrThrow(Registries.FEATURE)
			.get(OLIVINE_GEODE)
			.orElse(null);
		if (geode == null) {
			return false;
		}

		int minY = world.getMinY() + 6;
		for (int attempt = 0; attempt < 12; attempt++) {
			int x = origin.getX() + random.nextInt(16);
			int z = origin.getZ() + random.nextInt(16);
			if (!NearworldVolcanoFeature.covers(world, x, z)) {
				continue;
			}

			int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
			int maxY = Math.min(surfaceY - 12, 24);
			if (maxY <= minY) {
				continue;
			}
			int y = minY + random.nextInt(maxY - minY + 1);
			BlockPos pos = new BlockPos(x, y, z);
			if (!isHost(world.getBlockState(pos))) {
				continue;
			}
			return geode.value().place(world, chunkGenerator, random, pos);
		}
		return false;
	}

	private static boolean isHost(BlockState state) {
		return state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT)
			|| state.is(Blocks.MAGMA_BLOCK)
			|| state.is(Blocks.SULFUR)
			|| state.is(Blocks.CINNABAR)
			|| state.is(ModBlocks.SULFURIC_BASALT)
			|| state.is(ModBlocks.ANHYDRITE);
	}
}
