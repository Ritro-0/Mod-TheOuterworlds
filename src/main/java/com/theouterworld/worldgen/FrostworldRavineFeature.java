package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.entity.StrandHydraEntity;
import com.theouterworld.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Long, wandering trenches in the Frostworld seabed. The cut deepens to bedrock along the middle,
 * where a Strand Hydra is planted, and the walls slope instead of punching round holes.
 */
public class FrostworldRavineFeature implements Feature {
	public static final MapCodec<FrostworldRavineFeature> CODEC = MapCodec.unit(FrostworldRavineFeature::new);

	private static final int MAX_EXTENT = 200;
	private static final int CELL = 176;
	private static final double SPAWN_CHANCE = 0.34;
	private static final int BEDROCK_SURFACE = -64;

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
		return applyGrid(world, world.getSeed() + 29011L, minX, minZ, minX + 15, minZ + 15);
	}

	private static boolean applyGrid(WorldGenLevel world, long seed, int minX, int minZ, int maxX, int maxZ) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (applyCell(world, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean applyCell(
		WorldGenLevel world,
		long seed,
		int cellX,
		int cellZ,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed + 5, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}
		int length = 96 + (int) (WorldgenNoise.hash(seed + 17, cellX, cellZ) * 64.0);
		double x = cellX * CELL + CELL / 2.0
			+ WorldgenNoise.signed(WorldgenNoise.hash(seed + 29, cellX, cellZ)) * 28.0;
		double z = cellZ * CELL + CELL / 2.0
			+ WorldgenNoise.signed(WorldgenNoise.hash(seed + 31, cellX, cellZ)) * 28.0;
		double angle = WorldgenNoise.hash(seed + 43, cellX, cellZ) * Math.PI * 2.0;
		double bend = WorldgenNoise.signed(WorldgenNoise.hash(seed + 47, cellX, cellZ)) * 0.04;
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int step = 0; step < length; step++) {
			angle += bend;
			angle += WorldgenNoise.signed(WorldgenNoise.hash(seed + 1000 + step, cellX, cellZ)) * 0.11;
			x += Math.cos(angle) * 3.1;
			z += Math.sin(angle) * 3.1;
			double along = step / (double) Math.max(1, length - 1);
			double depthFactor = depthAlong(along);
			double width = 3.2
				+ WorldgenNoise.hash(seed + 2000 + step, cellX, cellZ) * 2.8
				+ Math.sin(along * 11.0 + cellX) * 0.8;
			int pad = Mth.ceil(width) + 1;
			int x0 = Math.max(minX, Mth.floor(x) - pad);
			int x1 = Math.min(maxX, Mth.floor(x) + pad);
			int z0 = Math.max(minZ, Mth.floor(z) - pad);
			int z1 = Math.min(maxZ, Mth.floor(z) + pad);
			boolean reachedBedrock = false;
			for (int bx = x0; bx <= x1; bx++) {
				for (int bz = z0; bz <= z1; bz++) {
					double dx = bx + 0.5 - x;
					double dz = bz + 0.5 - z;
					double edge = Math.sqrt(dx * dx + dz * dz) / width;
					if (edge >= 1.0) {
						continue;
					}
					int floor = OceanFloor.surfaceY(world, bx, bz);
					if (floor <= BEDROCK_SURFACE) {
						continue;
					}
					double digFactor = depthFactor * (1.0 - edge * edge);
					int dig = Math.max(2, (int) Math.round((floor - BEDROCK_SURFACE) * digFactor));
					int bottom = Math.max(BEDROCK_SURFACE, floor - dig);
					if (bottom <= BEDROCK_SURFACE + 1 && edge < 0.35) {
						reachedBedrock = true;
					}
					for (int y = floor; y > bottom; y--) {
						cursor.set(bx, y, bz);
						BlockState state = world.getBlockState(cursor);
						if (state.is(Blocks.BEDROCK) || state.isAir() || !state.getFluidState().isEmpty()) {
							continue;
						}
						world.setBlock(cursor, Blocks.WATER.defaultBlockState(), 2);
						placed = true;
					}
				}
			}
			if (reachedBedrock && along > 0.22 && along < 0.78 && step % 40 == 0) {
				int hx = Mth.floor(x);
				int hz = Mth.floor(z);
				if (hx >= minX && hx <= maxX && hz >= minZ && hz <= maxZ && plantHydra(world, hx, hz)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	/** Shallow mouths, a long bedrock floor through the middle, then back out. */
	private static double depthAlong(double along) {
		if (along < 0.14) {
			return (along / 0.14) * 0.28;
		}
		if (along > 0.86) {
			return ((1.0 - along) / 0.14) * 0.28;
		}
		double mid = (along - 0.14) / 0.72;
		return 0.45 + 0.55 * Math.sin(mid * Math.PI);
	}

	private static boolean plantHydra(WorldGenLevel world, int x, int z) {
		int y = BEDROCK_SURFACE + 1;
		if (!world.getBlockState(new BlockPos(x, BEDROCK_SURFACE, z)).is(Blocks.BEDROCK)) {
			return false;
		}
		BlockState atFeet = world.getBlockState(new BlockPos(x, y, z));
		if (!atFeet.isAir() && !atFeet.getFluidState().is(FluidTags.WATER)) {
			return false;
		}
		StrandHydraEntity hydra = ModEntities.STRAND_HYDRA.create(world.getLevel(), EntitySpawnReason.STRUCTURE);
		if (hydra == null) {
			return false;
		}
		hydra.snapTo(x + 0.5, y, z + 0.5, 0.0F, 0.0F);
		world.addFreshEntity(hydra);
		return true;
	}
}
