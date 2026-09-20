package com.theouterworld.worldgen;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

public class ErgDuneFeature implements Feature {
	public static final MapCodec<ErgDuneFeature> CODEC = MapCodec.unit(ErgDuneFeature::new);

	private static final double WIND = Math.toRadians(18.0);
	/**
	 * Probed at roughly the erg surface, where depth reads near zero. Surface biomes are all
	 * depth-0 points, so a probe far above or below the terrain drifts toward the cave band and
	 * starts reporting neighbours as non-ergs.
	 */
	private static final int BIOME_PROBE_Y = 70;
	private static final ResourceKey<Biome> ERGS = ResourceKey.create(
		Registries.BIOME,
		OuterWorldMod.id("outerworld_ergs")
	);

	public ErgDuneFeature() {
	}

	@Override
	public MapCodec<ErgDuneFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		long seed = world.getSeed();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		BlockState regolith = ModBlocks.REGOLITH.defaultBlockState();
		BlockState basalt = ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minY = world.getMinY() + 1;
		boolean placed = false;

		// Sampled at the chunk corners and interpolated across it. Neighbouring chunks share those
		// corners, so the dune field stays continuous without scanning per column.
		double cornerNegNeg = interiorFactor(world, minX, minZ);
		double cornerPosNeg = interiorFactor(world, minX + 16, minZ);
		double cornerNegPos = interiorFactor(world, minX, minZ + 16);
		double cornerPosPos = interiorFactor(world, minX + 16, minZ + 16);
		if (cornerNegNeg <= 0.0 && cornerPosNeg <= 0.0 && cornerNegPos <= 0.0 && cornerPosPos <= 0.0) {
			return false;
		}

		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				int x = minX + lx;
				int z = minZ + lz;
				if (!isErgs(world, x, z)) {
					continue;
				}

				double tx = lx / 16.0;
				double tz = lz / 16.0;
				double factor = WorldgenNoise.lerp(
					tz,
					WorldgenNoise.lerp(tx, cornerNegNeg, cornerPosNeg),
					WorldgenNoise.lerp(tx, cornerNegPos, cornerPosPos)
				);
				if (factor <= 0.0) {
					continue;
				}
				int existingTop = surfaceTop(world, x, z, minY);
				int rollingFloor = rollingFloor(seed, x, z);
				int dune = duneHeight(seed, x, z);
				int floorY = (int) Math.round(WorldgenNoise.lerp(factor, existingTop, rollingFloor));
				int scaledDune = (int) Math.round(dune * factor);
				int topY = floorY + scaledDune;
				if (topY < minY) {
					continue;
				}

				boolean trough = factor > 0.65 && scaledDune <= 2;
				int reshapeFrom = Math.max(minY, Math.min(existingTop, floorY) - 1);
				for (int y = reshapeFrom; y <= topY; y++) {
					cursor.set(x, y, z);
					if (world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						continue;
					}
					world.setBlock(cursor, trough || y <= floorY ? basalt : regolith, 2);
				}
				if (existingTop > topY) {
					for (int y = topY + 1; y <= existingTop; y++) {
						cursor.set(x, y, z);
						if (!world.getBlockState(cursor).is(Blocks.BEDROCK) && !world.getBlockState(cursor).isAir()) {
							world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
						}
					}
				}
				placed = true;
			}
		}
		return placed;
	}

	private static int surfaceTop(WorldGenLevel world, int x, int z, int minY) {
		int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		if (top >= minY) {
			return top;
		}
		ChunkAccess chunk = world.getChunk(x >> 4, z >> 4);
		int fallback = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		return Math.max(minY, fallback);
	}

	/**
	 * Asks the biome source directly rather than reading placed chunk data. The neighbourhood scan
	 * reaches two chunks out, which is outside the writable region, so a chunk-backed lookup would
	 * report those samples as "not ergs" and flatten the dune field to nothing.
	 */
	private static boolean isErgs(WorldGenLevel world, int x, int z) {
		return world.getUncachedNoiseBiome(
			QuartPos.fromBlock(x),
			QuartPos.fromBlock(BIOME_PROBE_Y),
			QuartPos.fromBlock(z)
		).is(ERGS);
	}

	private static double interiorFactor(WorldGenLevel world, int x, int z) {
		int radius = 24;
		int step = 8;
		int ergs = 0;
		int total = 0;
		for (int dx = -radius; dx <= radius; dx += step) {
			for (int dz = -radius; dz <= radius; dz += step) {
				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				total++;
				if (isErgs(world, x + dx, z + dz)) {
					ergs++;
				}
			}
		}
		if (total == 0) {
			return 0.0;
		}
		return smoothstep((ergs / (double) total - 0.55) / 0.40);
	}

	static int rollingFloor(long seed, int x, int z) {
		double broad = 3.6 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(seed + 13, x / 64.0, z / 64.0, 3, 0.5));
		double detail = 2.2 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(seed + 31, x / 22.0, z / 22.0, 2, 0.55));
		return 67 + (int) Math.round(broad + detail);
	}

	static int duneHeight(long seed, int x, int z) {
		double wind = WIND + Math.toRadians(10.0 * WorldgenNoise.signed(
			WorldgenNoise.octaveNoise(seed + 7, x / 420.0, z / 420.0, 2, 0.45)
		));
		double cs = Math.cos(wind);
		double sn = Math.sin(wind);
		double u = x * cs + z * sn;
		double v = -x * sn + z * cs;

		u += 11.0 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(seed + 43, x / 88.0, z / 88.0, 3, 0.5));
		u += 5.0 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(seed + 59, x / 36.0, z / 36.0, 2, 0.5));
		u += 2.2 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 71, x / 14.0, z / 14.0));

		double spacing = 42.0 + 12.0 * WorldgenNoise.octaveNoise(seed + 11, x / 280.0, z / 280.0, 2, 0.45);
		double amplitude = 9.0 + 6.0 * WorldgenNoise.octaveNoise(seed + 29, x / 200.0, z / 200.0, 2, 0.5);
		double t = fract(u / spacing);
		double profile;
		if (t < 0.26) {
			profile = 0.08 * smoothstep(t / 0.26);
		} else if (t < 0.78) {
			profile = 0.08 + 0.92 * smoothstep((t - 0.26) / 0.52);
		} else {
			profile = 1.0 - smoothstep((t - 0.78) / 0.22);
		}

		double crest = 0.78 + 0.22 * WorldgenNoise.octaveNoise(seed + 89, v / 58.0, u / 110.0, 3, 0.5);
		int height = (int) Math.round(amplitude * profile * crest);
		if (height <= 2) {
			height += Math.max(0, (int) Math.round(1.6 * WorldgenNoise.signed(
				WorldgenNoise.octaveNoise(seed + 97, x / 18.0, z / 18.0, 2, 0.55)
			)));
		}
		return Math.max(0, height);
	}

	private static double smoothstep(double s) {
		s = Math.max(0.0, Math.min(1.0, s));
		return s * s * (3.0 - 2.0 * s);
	}

	private static double fract(double value) {
		return value - Math.floor(value);
	}
}
