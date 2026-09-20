package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.world.DeepworldLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Dense billowy Deepworld cloud banks: ammonia below, methane above.
 */
public class DeepworldCloudDeckFeature implements Feature {
	public static final MapCodec<DeepworldCloudDeckFeature> CODEC = MapCodec.unit(DeepworldCloudDeckFeature::new);

	public DeepworldCloudDeckFeature() {
	}

	@Override
	public MapCodec<DeepworldCloudDeckFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		WorldGenLevel level = world;
		ChunkAccess chunk = level.getChunk(origin);
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();
		long seed = level.getSeed();

		PerlinNoise ammoniaNoise = new PerlinNoise(RandomSource.create(seed ^ 0x51F10E5L));
		PerlinNoise methaneNoise = new PerlinNoise(RandomSource.create(seed ^ 0xA33041A1L));

		BlockState ammonia = ModBlocks.AMMONIA_CLOUD.defaultBlockState();
		BlockState methane = ModBlocks.METHANE_CLOUD.defaultBlockState();

		paintBand(
			level,
			random,
			ammoniaNoise,
			minX,
			minZ,
			DeepworldLayers.AMMONIA_BOTTOM_Y,
			DeepworldLayers.AMMONIA_TOP_Y,
			ammonia,
			0.014,
			0.04
		);
		paintBand(
			level,
			random,
			methaneNoise,
			minX,
			minZ,
			DeepworldLayers.METHANE_BOTTOM_Y,
			DeepworldLayers.METHANE_TOP_Y,
			methane,
			0.011,
			0.02
		);
		return true;
	}

	/**
	 * @param xzScale lower = larger cloud banks / wider clearings
	 * @param threshold higher = sparser coverage
	 */
	private static void paintBand(
		WorldGenLevel level,
		RandomSource random,
		PerlinNoise noise,
		int minX,
		int minZ,
		int y0,
		int y1,
		BlockState cloud,
		double xzScale,
		double threshold
	) {
		int bandHeight = y1 - y0;
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = minX + dx;
				int z = minZ + dz;
				double bank = noise.get(x * xzScale, 0.0, z * xzScale);
				if (bank < threshold) {
					continue;
				}

				// Density falls off near bank edges so clouds look billowy.
				double edge = (bank - threshold) / (1.0 - threshold);
				int puffHeight = Mth.clamp((int) Math.round(bandHeight * (0.55 + edge * 0.45)), 6, bandHeight);
				int base = y0 + random.nextInt(Math.max(1, bandHeight - puffHeight + 1));

				for (int y = base; y < base + puffHeight; y++) {
					double vertical = 1.0 - Math.abs((y - (base + puffHeight * 0.5)) / (puffHeight * 0.55));
					double detail = noise.get(x * xzScale * 2.4, y * 0.08, z * xzScale * 2.4);
					if (vertical * edge + detail * 0.25 < 0.18) {
						continue;
					}
					if (random.nextFloat() < 0.02F) {
						continue;
					}
					BlockPos pos = new BlockPos(x, y, z);
					if (level.getBlockState(pos).isAir()) {
						level.setBlock(pos, cloud, 2);
					}
				}
			}
		}
	}
}
