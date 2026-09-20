package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.world.SunTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

/**
 * Flat solar crust: bedrock → 200 lava → 50 solar plasma (source), forever.
 */
public class SunLayersFeature implements Feature {
	public static final MapCodec<SunLayersFeature> CODEC = MapCodec.unit(SunLayersFeature::new);

	private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
	private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
	private static final BlockState PLASMA = ModBlocks.SOLAR_PLASMA.defaultBlockState();

	public SunLayersFeature() {
	}

	@Override
	public MapCodec<SunLayersFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int minY = world.getMinY();
		int lavaTop = SunTerrain.lavaTopY(minY);
		int plasmaTop = SunTerrain.plasmaTopY(minY);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int x = minX; x < minX + 16; x++) {
			for (int z = minZ; z < minZ + 16; z++) {
				cursor.set(x, minY, z);
				world.setBlock(cursor, BEDROCK, 2);
				for (int y = minY + 1; y <= lavaTop; y++) {
					cursor.set(x, y, z);
					world.setBlock(cursor, LAVA, 2);
				}
				for (int y = lavaTop + 1; y <= plasmaTop; y++) {
					cursor.set(x, y, z);
					world.setBlock(cursor, PLASMA, 2);
				}
			}
		}
		return true;
	}
}
