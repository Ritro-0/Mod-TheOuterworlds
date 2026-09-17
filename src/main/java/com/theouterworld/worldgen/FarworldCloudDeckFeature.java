package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.world.FarworldLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * Farworld: methane-only layered decks — sparse below, denser mid, dense on top.
 */
public class FarworldCloudDeckFeature extends Feature<NoneFeatureConfiguration> {
	public FarworldCloudDeckFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		ChunkAccess chunk = level.getChunk(origin);
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();
		long seed = level.getSeed();

		ImprovedNoise lowerNoise = new ImprovedNoise(RandomSource.create(seed ^ 0xF4A1C0DEL));
		ImprovedNoise midNoise = new ImprovedNoise(RandomSource.create(seed ^ 0xA33041F4L));
		ImprovedNoise upperNoise = new ImprovedNoise(RandomSource.create(seed ^ 0xC10D07F4L));

		BlockState methane = ModBlocks.METHANE_CLOUD.defaultBlockState();

		// Sparse lower wisps
		paintBand(level, random, lowerNoise, minX, minZ,
			FarworldLayers.METHANE_LOWER_BOTTOM_Y, FarworldLayers.METHANE_LOWER_TOP_Y,
			methane, 0.016, 0.28);
		// More mid coverage
		paintBand(level, random, midNoise, minX, minZ,
			FarworldLayers.METHANE_MID_BOTTOM_Y, FarworldLayers.METHANE_MID_TOP_Y,
			methane, 0.012, 0.08);
		// Dense upper deck
		paintBand(level, random, upperNoise, minX, minZ,
			FarworldLayers.METHANE_UPPER_BOTTOM_Y, FarworldLayers.METHANE_UPPER_TOP_Y,
			methane, 0.009, 0.01);
		return true;
	}

	private static void paintBand(
		WorldGenLevel level,
		RandomSource random,
		ImprovedNoise noise,
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
				double bank = noise.noise(x * xzScale, 0.0, z * xzScale);
				if (bank < threshold) {
					continue;
				}

				double edge = (bank - threshold) / (1.0 - threshold);
				int puffHeight = Mth.clamp((int) Math.round(bandHeight * (0.60 + edge * 0.40)), 6, bandHeight);
				int base = y0 + random.nextInt(Math.max(1, bandHeight - puffHeight + 1));

				for (int y = base; y < base + puffHeight; y++) {
					double vertical = 1.0 - Math.abs((y - (base + puffHeight * 0.5)) / (puffHeight * 0.55));
					double detail = noise.noise(x * xzScale * 2.4, y * 0.08, z * xzScale * 2.4);
					if (vertical * edge + detail * 0.25 < 0.15) {
						continue;
					}
					if (random.nextFloat() < 0.015F) {
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
