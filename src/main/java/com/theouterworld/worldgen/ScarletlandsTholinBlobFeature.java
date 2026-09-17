package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Large, several-block-deep tholin patches on SCARLETLANDS methane ice.
 */
public class ScarletlandsTholinBlobFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_EXTENT = 24;
	private static final int CELL = 72;
	private static final double SPAWN_CHANCE = 0.48;

	public ScarletlandsTholinBlobFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		ChunkAccess chunk = world.getChunk(origin);
		return applyGrid(world, chunk, world.getSeed() + 55103, minX, minZ, maxX, maxZ);
	}

	private static boolean applyGrid(WorldGenLevel world, ChunkAccess chunk, long seed, int minX, int minZ, int maxX, int maxZ) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (applyCell(world, chunk, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean applyCell(
		WorldGenLevel world,
		ChunkAccess chunk,
		long seed,
		int cellX,
		int cellZ,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed + 11, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		int radius = 8 + (int) (WorldgenNoise.hash(seed + 29, cellX, cellZ) * 13.0);
		int depth = 3 + (int) (WorldgenNoise.hash(seed + 47, cellX, cellZ) * 4.0);
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 61, cellX, cellZ)) * 14.0);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 79, cellX, cellZ)) * 14.0);
		int minY = world.getMinY() + 1;
		BlockState tholin = ModBlocks.THOLIN.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean placed = false;

		int x0 = Math.max(minX, centerX - radius);
		int x1 = Math.min(maxX, centerX + radius);
		int z0 = Math.max(minZ, centerZ - radius);
		int z1 = Math.min(maxZ, centerZ + radius);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double nx = (x - centerX) / (double) radius;
				double nz = (z - centerZ) / (double) radius;
				double wobble = 0.18 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 97, x / 8.0, z / 8.0));
				if (nx * nx + nz * nz > 1.0 + wobble) {
					continue;
				}

				int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				cursor.set(x, surfaceY, z);
				BlockState top = chunk.getBlockState(cursor);
				if (!isScarletSurface(top)) {
					continue;
				}

				int localDepth = depth + (int) Math.round(WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 113, x / 6.0, z / 6.0)));
				localDepth = Math.max(2, Math.min(6, localDepth));
				for (int dy = 0; dy < localDepth; dy++) {
					int y = surfaceY - dy;
					if (y <= minY) {
						break;
					}
					cursor.set(x, y, z);
					BlockState existing = world.getBlockState(cursor);
					if (existing.is(Blocks.BEDROCK) || !isScarletSurface(existing)) {
						break;
					}
					world.setBlock(cursor, tholin, 2);
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean isScarletSurface(BlockState state) {
		return state.is(ModBlocks.METHANE_ICE)
			|| state.is(ModBlocks.THOLIN)
			|| state.is(Blocks.BLUE_ICE)
			|| state.is(ModBlocks.BLUE_ICE);
	}
}
