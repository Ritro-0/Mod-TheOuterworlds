package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Rare, large subsurface ice caverns for Wanderlands — carved ellipsoids painted with packed ice.
 */
public class WanderlandsIceCavernFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_EXTENT = 48;
	private static final int CELL = 288;
	private static final double SPAWN_CHANCE = 0.085;
	private static final int JITTER = 40;

	public WanderlandsIceCavernFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		ChunkAccess chunk = world.getChunk(origin);
		return applyGrid(world, chunk, random, world.getSeed() + 44027, minX, minZ, maxX, maxZ);
	}

	private static boolean applyGrid(
		WorldGenLevel world,
		ChunkAccess chunk,
		RandomSource random,
		long seed,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (carveCellCavern(world, chunk, random, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean carveCellCavern(
		WorldGenLevel world,
		ChunkAccess chunk,
		RandomSource random,
		long seed,
		int cellX,
		int cellZ,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 3, cellX, cellZ)) * JITTER);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 7, cellX, cellZ)) * JITTER);

		int sampleX = Mth.clamp(centerX, minX, maxX);
		int sampleZ = Mth.clamp(centerZ, minZ, maxZ);
		int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sampleX, sampleZ);
		int minY = world.getMinY() + 6;
		int maxCenterY = surfaceY - 14;
		if (maxCenterY <= minY + 10) {
			return false;
		}

		int centerY = minY + 8 + (int) (WorldgenNoise.hash(seed + 11, cellX, cellZ) * (maxCenterY - minY - 8));
		int rx = 22 + (int) (WorldgenNoise.hash(seed + 13, cellX, cellZ) * 14.0);
		int ry = 9 + (int) (WorldgenNoise.hash(seed + 17, cellX, cellZ) * 6.0);
		int rz = 22 + (int) (WorldgenNoise.hash(seed + 19, cellX, cellZ) * 14.0);

		int extent = Math.max(rx, rz) + 4;
		int x0 = Math.max(minX, centerX - extent);
		int x1 = Math.min(maxX, centerX + extent);
		int z0 = Math.max(minZ, centerZ - extent);
		int z1 = Math.min(maxZ, centerZ + extent);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean carved = false;

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				int columnSurface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				for (int y = centerY - ry - 3; y <= centerY + ry + 3; y++) {
					if (y <= world.getMinY() || y >= columnSurface - 4) {
						continue;
					}
					double nx = (x - centerX) / (double) rx;
					double ny = (y - centerY) / (double) ry;
					double nz = (z - centerZ) / (double) rz;
					double d2 = nx * nx + ny * ny + nz * nz;
					cursor.set(x, y, z);
					BlockState existing = world.getBlockState(cursor);
					if (existing.is(Blocks.BEDROCK) || existing.isAir()) {
						continue;
					}
					if (!existing.is(ModBlocks.ANORTHOSITE)
						&& !existing.is(ModBlocks.ANORTHOSITIC_REGOLITH)
						&& !existing.is(ModBlocks.SALT_BLOCK)
						&& !existing.is(Blocks.PACKED_ICE)) {
						continue;
					}
					if (d2 <= 1.0) {
						world.setBlock(cursor, air, 2);
						carved = true;
					} else if (d2 <= 1.35) {
						world.setBlock(cursor, ice, 2);
						carved = true;
					}
				}
			}
		}

		if (carved && random.nextFloat() < 0.55F) {
			paintExtraIcePatches(world, chunk, random, centerX, centerY, centerZ, rx, ry, rz, minX, minZ, maxX, maxZ);
		}
		return carved;
	}

	private static void paintExtraIcePatches(
		WorldGenLevel world,
		ChunkAccess chunk,
		RandomSource random,
		int cx,
		int cy,
		int cz,
		int rx,
		int ry,
		int rz,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int attempts = 18 + random.nextInt(16);
		for (int i = 0; i < attempts; i++) {
			int x = cx + random.nextInt(rx * 2 + 1) - rx;
			int y = cy + random.nextInt(ry * 2 + 1) - ry;
			int z = cz + random.nextInt(rz * 2 + 1) - rz;
			if (x < minX || x > maxX || z < minZ || z > maxZ || y <= world.getMinY()) {
				continue;
			}
			int columnSurface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
			if (y >= columnSurface - 4) {
				continue;
			}
			cursor.set(x, y, z);
			BlockState state = world.getBlockState(cursor);
			if (!state.is(ModBlocks.ANORTHOSITE)) {
				continue;
			}
			boolean touchesAir = false;
			for (Direction dir : Direction.values()) {
				BlockPos neighbor = cursor.relative(dir);
				if (neighbor.getX() < minX || neighbor.getX() > maxX
					|| neighbor.getZ() < minZ || neighbor.getZ() > maxZ) {
					continue;
				}
				if (world.getBlockState(neighbor).isAir()) {
					touchesAir = true;
					break;
				}
			}
			if (touchesAir) {
				world.setBlock(cursor, ice, 2);
			}
		}
	}
}
