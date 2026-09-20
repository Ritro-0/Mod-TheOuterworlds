package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.world.EdgeworldLayers;
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
 * Edgeworld: methane-only decks starting near Highworld height, thinner/sparser than Farworld.
 */
public class EdgeworldCloudDeckFeature implements Feature {
	public static final MapCodec<EdgeworldCloudDeckFeature> CODEC = MapCodec.unit(EdgeworldCloudDeckFeature::new);

	public EdgeworldCloudDeckFeature() {
	}

	@Override
	public MapCodec<EdgeworldCloudDeckFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		WorldGenLevel level = world;
		ChunkAccess chunk = level.getChunk(origin);
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();
		long seed = level.getSeed();

		PerlinNoise lowerNoise = new PerlinNoise(RandomSource.create(seed ^ 0xED6E401L));
		PerlinNoise midNoise = new PerlinNoise(RandomSource.create(seed ^ 0xED6E402L));
		PerlinNoise upperNoise = new PerlinNoise(RandomSource.create(seed ^ 0xED6E403L));

		BlockState methane = ModBlocks.METHANE_CLOUD.defaultBlockState();

		paintBand(level, random, lowerNoise, minX, minZ,
			EdgeworldLayers.METHANE_LOWER_BOTTOM_Y, EdgeworldLayers.METHANE_LOWER_TOP_Y,
			methane, 0.018, 0.38);
		paintBand(level, random, midNoise, minX, minZ,
			EdgeworldLayers.METHANE_MID_BOTTOM_Y, EdgeworldLayers.METHANE_MID_TOP_Y,
			methane, 0.014, 0.18);
		paintBand(level, random, upperNoise, minX, minZ,
			EdgeworldLayers.METHANE_UPPER_BOTTOM_Y, EdgeworldLayers.METHANE_UPPER_TOP_Y,
			methane, 0.011, 0.06);
		return true;
	}

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

				double edge = (bank - threshold) / (1.0 - threshold);
				int puffHeight = Mth.clamp((int) Math.round(bandHeight * (0.50 + edge * 0.45)), 4, bandHeight);
				int base = y0 + random.nextInt(Math.max(1, bandHeight - puffHeight + 1));

				for (int y = base; y < base + puffHeight; y++) {
					double vertical = 1.0 - Math.abs((y - (base + puffHeight * 0.5)) / (puffHeight * 0.55));
					double detail = noise.get(x * xzScale * 2.4, y * 0.08, z * xzScale * 2.4);
					if (vertical * edge + detail * 0.25 < 0.18) {
						continue;
					}
					if (random.nextFloat() < 0.03F) {
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
