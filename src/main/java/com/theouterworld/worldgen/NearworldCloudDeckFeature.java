package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Nearworld sulfuric cloud banks in the sky, above the tallest volcanoes.
 * Banks are solid, with a soft outline. A few air-facing blocks in some banks are acid deposits.
 */
public class NearworldCloudDeckFeature implements Feature {
	public static final MapCodec<NearworldCloudDeckFeature> CODEC = MapCodec.unit(NearworldCloudDeckFeature::new);

	/** Sky band. Terrain and volcano summits stay below this. */
	private static final int BOTTOM_Y = 260;
	private static final int TOP_Y = 288;

	public NearworldCloudDeckFeature() {
	}

	@Override
	public MapCodec<NearworldCloudDeckFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		ChunkAccess chunk = world.getChunk(origin);
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();
		long seed = world.getSeed();

		PerlinNoise noise = new PerlinNoise(RandomSource.create(seed ^ 0x50F1C10DL));
		BlockState cloud = ModBlocks.SULFURIC_CLOUD.defaultBlockState();
		paintBand(world, noise, minX, minZ, BOTTOM_Y, TOP_Y, cloud, 0.014, 0.04);
		scatterDeposits(world, random, minX, minZ);
		return true;
	}

	/**
	 * @param xzScale lower = larger cloud banks / wider clearings
	 * @param threshold higher = sparser coverage
	 */
	private static void paintBand(
		WorldGenLevel level,
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
				double outline = noise.get(x * xzScale * 1.35, 12.0, z * xzScale * 1.35) * 0.05;
				double bank = noise.get(x * xzScale, 0.0, z * xzScale) + outline;
				if (bank < threshold) {
					continue;
				}

				double edge = Mth.clamp((bank - threshold) / (1.0 - threshold), 0.0, 1.0);
				int puffHeight = Mth.clamp((int) Math.round(bandHeight * (0.55 + edge * 0.45)), 6, bandHeight);
				int slack = Math.max(1, bandHeight - puffHeight + 1);
				double lift = noise.get(x * xzScale * 0.65, 28.0, z * xzScale * 0.65);
				int base = y0 + Mth.clamp((int) Math.round((lift * 0.5 + 0.5) * (slack - 1)), 0, slack - 1);

				for (int y = base; y < base + puffHeight; y++) {
					double vertical = 1.0 - Math.abs((y - (base + puffHeight * 0.5)) / (puffHeight * 0.5));
					if (vertical < 0.18) {
						double rim = noise.get(x * xzScale * 0.9, y * 0.03, z * xzScale * 0.9);
						if (vertical + rim * 0.12 < 0.12) {
							continue;
						}
					}
					BlockPos pos = new BlockPos(x, y, z);
					if (level.getBlockState(pos).isAir()) {
						level.setBlock(pos, cloud, 2);
					}
				}
			}
		}
	}

	/**
	 * Sometimes turn a couple of air-facing cloud blocks into small deposit patches.
	 * Only faces that open onto air inside this chunk count, so chunk borders
	 * do not get deposits just because the next chunk has not painted yet.
	 */
	private static void scatterDeposits(WorldGenLevel level, RandomSource random, int minX, int minZ) {
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		List<BlockPos> exposed = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = BOTTOM_Y; y < TOP_Y; y++) {
					cursor.set(x, y, z);
					if (!level.getBlockState(cursor).is(ModBlocks.SULFURIC_CLOUD)) {
						continue;
					}
					if (isAirExposedInChunk(level, cursor, minX, maxX, minZ, maxZ)) {
						exposed.add(cursor.immutable());
					}
				}
			}
		}
		if (exposed.size() < 8 || random.nextFloat() > 0.45F) {
			return;
		}

		BlockState deposit = ModBlocks.SULFURIC_CLOUD_DEPOSIT.defaultBlockState();
		for (int section = 0; section < 2 && !exposed.isEmpty(); section++) {
			BlockPos pos = exposed.remove(random.nextInt(exposed.size()));
			if (!level.getBlockState(pos).is(ModBlocks.SULFURIC_CLOUD)) {
				continue;
			}
			level.setBlock(pos, deposit, 2);
			BlockPos extra = adjacentExposed(pos, exposed);
			if (extra != null && level.getBlockState(extra).is(ModBlocks.SULFURIC_CLOUD)) {
				exposed.remove(extra);
				level.setBlock(extra, deposit, 2);
			}
		}
	}

	private static BlockPos adjacentExposed(BlockPos pos, List<BlockPos> exposed) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbor = pos.relative(direction);
			if (exposed.contains(neighbor)) {
				return neighbor;
			}
		}
		return null;
	}

	private static boolean isAirExposedInChunk(
		WorldGenLevel level,
		BlockPos pos,
		int minX,
		int maxX,
		int minZ,
		int maxZ
	) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbor = pos.relative(direction);
			if (neighbor.getX() < minX || neighbor.getX() > maxX || neighbor.getZ() < minZ || neighbor.getZ() > maxZ) {
				continue;
			}
			if (level.getBlockState(neighbor).isAir()) {
				return true;
			}
		}
		return false;
	}
}
