package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.entity.OceanVentEntity;
import com.theouterworld.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Round bowls in the Frostworld seabed, ringed with seagrass until a proper vent block exists.
 * A marker entity in the bowl blows the black fog and hosts the feeder schools.
 */
public class FrostworldOceanVentFeature implements Feature {
	public static final MapCodec<FrostworldOceanVentFeature> CODEC = MapCodec.unit(FrostworldOceanVentFeature::new);

	private static final int MAX_EXTENT = 16;
	private static final int CELL = 96;
	private static final double SPAWN_CHANCE = 0.45;

	public FrostworldOceanVentFeature() {
	}

	@Override
	public MapCodec<FrostworldOceanVentFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		return applyGrid(world, world.getSeed() + 81217L, minX, minZ, minX + 15, minZ + 15);
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
		if (WorldgenNoise.hash(seed + 3, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}
		int jitter = 18;
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 41, cellX, cellZ)) * jitter);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 67, cellX, cellZ)) * jitter);
		int centerFloor = OceanFloor.surfaceY(world, centerX, centerZ);
		if (centerFloor == Integer.MIN_VALUE || centerFloor <= -48) {
			return false;
		}
		if (world.getBlockState(new BlockPos(centerX, centerFloor, centerZ)).is(Blocks.BEDROCK)) {
			return false;
		}

		double radius = 7.0 + WorldgenNoise.hash(seed + 89, cellX, cellZ) * 4.0;
		int depth = 4 + (int) (WorldgenNoise.hash(seed + 97, cellX, cellZ) * 4.0);
		int bottom = Math.max(-63, centerFloor - depth);
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int reach = (int) Math.ceil(radius) + 3;
		int x0 = Math.max(minX, centerX - reach);
		int x1 = Math.min(maxX, centerX + reach);
		int z0 = Math.max(minZ, centerZ - reach);
		int z1 = Math.min(maxZ, centerZ + reach);

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double dx = x + 0.5 - centerX;
				double dz = z + 0.5 - centerZ;
				double radial = Math.sqrt(dx * dx + dz * dz) / radius;
				int floor = OceanFloor.surfaceY(world, x, z);
				if (floor == Integer.MIN_VALUE) {
					continue;
				}
				if (radial <= 1.0) {
					double bowl = 1.0 - radial * radial;
					int digTo = Math.max(-63, floor - (int) Math.round(depth * bowl));
					for (int y = floor; y > digTo; y--) {
						cursor.set(x, y, z);
						BlockState state = world.getBlockState(cursor);
						if (state.isAir() || !state.getFluidState().isEmpty()) {
							continue;
						}
						world.setBlock(cursor, Blocks.WATER.defaultBlockState(), 2);
						placed = true;
					}
				} else if (radial <= 1.35 && floor > -48) {
					cursor.set(x, floor, z);
					if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						BlockPos plant = cursor.above();
						if (world.getFluidState(plant).is(FluidTags.WATER)) {
							world.setBlock(cursor, Blocks.SAND.defaultBlockState(), 2);
							world.setBlock(plant, Blocks.SEAGRASS.defaultBlockState(), 2);
							placed = true;
						}
					}
				}
			}
		}

		if (centerX >= minX && centerX <= maxX && centerZ >= minZ && centerZ <= maxZ) {
			OceanVentEntity vent = ModEntities.OCEAN_VENT.create(world.getLevel(), EntitySpawnReason.STRUCTURE);
			if (vent != null) {
				vent.snapTo(centerX + 0.5, bottom + 1.0, centerZ + 0.5, 0.0F, 0.0F);
				world.addFreshEntity(vent);
				placed = true;
			}
		}
		return placed;
	}
}
