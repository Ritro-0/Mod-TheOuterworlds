package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.world.DeepworldLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.FluidState;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

/**
 * Replaces the bottom liquid-hydrogen column with liquid helium sources in Deepworld.
 */
public class DeepworldHeliumLayerFeature implements Feature {
	public static final MapCodec<DeepworldHeliumLayerFeature> CODEC = MapCodec.unit(DeepworldHeliumLayerFeature::new);

	public DeepworldHeliumLayerFeature() {
	}

	@Override
	public MapCodec<DeepworldHeliumLayerFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		WorldGenLevel level = world;
		ChunkAccess chunk = level.getChunk(origin);
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();
		BlockState helium = ModBlocks.LIQUID_HELIUM.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				for (int y = DeepworldLayers.HELIUM_BOTTOM_Y; y <= DeepworldLayers.HELIUM_TOP_Y; y++) {
					cursor.set(minX + dx, y, minZ + dz);
					FluidState fluid = level.getFluidState(cursor);
					if (fluid.is(ModFluids.LIQUID_HYDROGEN) || fluid.is(ModFluids.FLOWING_LIQUID_HYDROGEN)) {
						level.setBlock(cursor, helium, 2);
					}
				}
			}
		}
		return true;
	}
}
