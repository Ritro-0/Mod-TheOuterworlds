package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

/**
 * Spaced impact craters of mixed sizes on SCARLETLANDS (Makemake).
 * Floors expose blue ice; rims stay methane ice with occasional tholin.
 */
public class ScarletlandsCraterFeature implements Feature {
	public static final MapCodec<ScarletlandsCraterFeature> CODEC = MapCodec.unit(ScarletlandsCraterFeature::new);

	private static final int MAX_EXTENT = 112;
	private static final int CELL = 280;
	private static final double SPAWN_CHANCE = 0.16;
	private static final int JITTER = 36;

	public ScarletlandsCraterFeature() {
	}

	@Override
	public MapCodec<ScarletlandsCraterFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		ChunkAccess chunk = world.getChunk(origin);
		return applyGrid(world, chunk, world.getSeed() + 44027, minX, minZ, maxX, maxZ);
	}

	private static boolean applyGrid(WorldGenLevel world, ChunkAccess chunk, long seed, int minX, int minZ, int maxX, int maxZ) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (applyCellCrater(world, chunk, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean applyCellCrater(
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
		if (WorldgenNoise.hash(seed + 19, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		double sizeRoll = WorldgenNoise.hash(seed + 41, cellX, cellZ);
		int radius;
		if (sizeRoll < 0.50) {
			radius = 14 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 11.0);
		} else if (sizeRoll < 0.85) {
			radius = 26 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 13.0);
		} else {
			radius = 42 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 15.0);
		}

		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 83, cellX, cellZ)) * JITTER);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 101, cellX, cellZ)) * JITTER);

		double depth = radius * (0.32 + 0.12 * WorldgenNoise.hash(seed + 149, cellX, cellZ));
		double rimWidth = 3.0 + radius / 6.0 + 2.0 * WorldgenNoise.hash(seed + 331, cellX, cellZ);
		double rimHeight = 2.2 + radius / 7.0 + 1.6 * WorldgenNoise.hash(seed + 347, cellX, cellZ);
		boolean complex = radius >= 40;
		double peakHeight = complex ? radius / 9.0 : 0.0;

		double rotation = WorldgenNoise.hash(seed + 163, cellX, cellZ) * Math.PI;
		double stretch = 0.82 + 0.32 * WorldgenNoise.hash(seed + 181, cellX, cellZ);
		double phase1 = WorldgenNoise.hash(seed + 197, cellX, cellZ) * Math.PI * 2.0;
		double phase2 = WorldgenNoise.hash(seed + 211, cellX, cellZ) * Math.PI * 2.0;
		double phase3 = WorldgenNoise.hash(seed + 223, cellX, cellZ) * Math.PI * 2.0;
		double cosR = Math.cos(rotation);
		double sinR = Math.sin(rotation);
		int minY = world.getMinY() + 1;

		BlockState methaneIce = ModBlocks.METHANE_ICE.defaultBlockState();
		BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
		BlockState tholin = ModBlocks.THOLIN.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int extent = (int) Math.ceil(radius * 1.55 + rimWidth + 4.0);

		int x0 = Math.max(minX, centerX - extent);
		int x1 = Math.min(maxX, centerX + extent);
		int z0 = Math.max(minZ, centerZ - extent);
		int z1 = Math.min(maxZ, centerZ + extent);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double dx = x - centerX;
				double dz = z - centerZ;
				double rx = dx * cosR + dz * sinR;
				double rz = (-dx * sinR + dz * cosR) / stretch;
				double dist = Math.hypot(rx, rz);
				double angle = Math.atan2(rz, rx);
				double localRadius = radius * (1.0
					+ 0.10 * Math.sin(2.0 * angle + phase1)
					+ 0.05 * Math.sin(3.0 * angle + phase2)
					+ 0.04 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 241 + cellX * 17L + cellZ, x / 16.0, z / 16.0)));
				if (dist > localRadius + rimWidth) {
					continue;
				}

				double rimScale = 0.70 + 0.30 * (0.5 + 0.5 * Math.sin(2.0 * angle + phase3));
				double localRim = rimHeight * rimScale;
				double t = dist / Math.max(1.0, localRadius);
				double elev;
				boolean inner = t <= 1.0;
				if (inner) {
					double bowl = Math.pow(Math.cos(t * Math.PI * 0.5), 1.12);
					double rimApproach = Math.pow(t, 2.4);
					elev = -depth * bowl + localRim * rimApproach;
					if (complex && t < 0.18) {
						elev += peakHeight * (1.0 - t / 0.18);
					}
					elev += 0.45 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 271, x / 16.0, z / 16.0));
				} else {
					double outerT = (dist - localRadius) / rimWidth;
					elev = localRim * Math.pow(1.0 - outerT, 1.55);
				}

				int originalSurface = findSurfaceY(chunk, x, z, minY, cursor);
				int surfaceY = Math.max(minY, originalSurface + (int) Math.round(elev));
				int clearTop = Math.max(originalSurface, surfaceY) + 12;
				for (int y = clearTop; y > surfaceY; y--) {
					cursor.set(x, y, z);
					if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						world.setBlock(cursor, air, 2);
					}
				}

				if (surfaceY > originalSurface) {
					for (int y = originalSurface + 1; y < surfaceY; y++) {
						cursor.set(x, y, z);
						if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
							world.setBlock(cursor, blueIce, 2);
						}
					}
				}

				cursor.set(x, surfaceY, z);
				if (world.getBlockState(cursor).is(Blocks.BEDROCK)) {
					continue;
				}

				boolean iceFloor = inner && t < 0.50 && !(complex && t < 0.18);
				boolean tholinPatch = WorldgenNoise.valueNoise(seed + 401, x / 10.0, z / 10.0) > 0.72;
				BlockState surface;
				if (iceFloor) {
					surface = blueIce;
				} else if (tholinPatch && (inner || t < 1.12)) {
					surface = tholin;
				} else {
					surface = methaneIce;
				}
				world.setBlock(cursor, surface, 2);

				if (iceFloor) {
					for (int y = surfaceY - 1; y >= surfaceY - 3 && y > minY; y--) {
						cursor.set(x, y, z);
						if (world.getBlockState(cursor).is(Blocks.BEDROCK)) {
							break;
						}
						world.setBlock(cursor, blueIce, 2);
					}
				}

				for (int y = surfaceY - 1; y >= surfaceY - 10 && y > minY; y--) {
					cursor.set(x, y, z);
					BlockState below = world.getBlockState(cursor);
					if (below.is(Blocks.BEDROCK) || below.is(Blocks.BLUE_ICE) || below.is(ModBlocks.BLUE_ICE)) {
						break;
					}
					if (below.isAir() || below.canBeReplaced()) {
						world.setBlock(cursor, blueIce, 2);
						continue;
					}
					if (inner && t < 0.70 && below.is(ModBlocks.METHANE_ICE)) {
						world.setBlock(cursor, blueIce, 2);
						continue;
					}
					break;
				}
			}
		}
		return true;
	}

	private static int findSurfaceY(ChunkAccess chunk, int x, int z, int minY, BlockPos.MutableBlockPos cursor) {
		int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		while (y > minY) {
			cursor.set(x, y, z);
			BlockState state = chunk.getBlockState(cursor);
			if (!state.isAir()) {
				return y;
			}
			y--;
		}
		return minY;
	}
}
