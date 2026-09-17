package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Spaced lava ponds and lakes carved into Nearworld plains. Grid-based so
 * they stay a comfortable distance apart and never litter the surface.
 */
public class NearworldLavaLakeFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_EXTENT = 22;
	private static final int CELL = 56;
	private static final double SPAWN_CHANCE = 0.58;

	public NearworldLavaLakeFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		return applyGrid(world, world.getSeed() + 77291L, minX, minZ, minX + 15, minZ + 15);
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
		if (WorldgenNoise.hash(seed + 13, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		boolean large = WorldgenNoise.hash(seed + 29, cellX, cellZ) < 0.28;
		int radiusX = large
			? 11 + (int) (WorldgenNoise.hash(seed + 41, cellX, cellZ) * 7.0)
			: 6 + (int) (WorldgenNoise.hash(seed + 41, cellX, cellZ) * 5.0);
		int radiusZ = large
			? 10 + (int) (WorldgenNoise.hash(seed + 53, cellX, cellZ) * 7.0)
			: 6 + (int) (WorldgenNoise.hash(seed + 53, cellX, cellZ) * 4.0);
		int depth = large ? 4 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 2.0)
			: 3 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 2.0);
		int jitter = 8;
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 79, cellX, cellZ)) * jitter);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 97, cellX, cellZ)) * jitter);

		int x0 = Math.max(minX, centerX - radiusX - 2);
		int x1 = Math.min(maxX, centerX + radiusX + 2);
		int z0 = Math.max(minZ, centerZ - radiusZ - 2);
		int z1 = Math.min(maxZ, centerZ + radiusZ + 2);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		BlockState rim = ModBlocks.SULFURIC_BASALT.defaultBlockState();
		BlockState fill = Blocks.BASALT.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minY = world.getMinY() + 2;
		boolean placed = false;

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double nx = (x - centerX) / (double) radiusX;
				double nz = (z - centerZ) / (double) radiusZ;
				double dist = Math.hypot(nx, nz);
				if (dist > 1.25) {
					continue;
				}

				int ground = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				if (ground < minY + 8) {
					continue;
				}

				int bowl = (int) Math.round(depth * (1.0 - dist * dist));
				int lavaY = ground - 1;
				int floorY = Math.max(minY, ground - Math.max(1, bowl));

				if (dist > 1.0) {
					cursor.set(x, ground, z);
					if (!world.getBlockState(cursor).is(Blocks.BEDROCK) && !world.getBlockState(cursor).is(Blocks.LAVA)) {
						world.setBlock(cursor, rim, 2);
					}
					continue;
				}

				for (int y = ground + 1; y >= floorY; y--) {
					cursor.set(x, y, z);
					if (world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						continue;
					}
					if (y > lavaY) {
						world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
					} else if (y == floorY) {
						world.setBlock(cursor, fill, 2);
					} else {
						WorldgenLava.place(world, cursor, y == lavaY);
						placed = true;
					}
				}
			}
		}
		return placed;
	}
}
