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
 * Impact craters for the Innerworld (Mercury) highlands.
 * Radii 5–50, non-overlapping, with subdued rims rather than lunar walls.
 */
public class InnerworldCraterFeature implements Feature {
	public static final MapCodec<InnerworldCraterFeature> CODEC = MapCodec.unit(InnerworldCraterFeature::new);

	private static final int MAX_EXTENT = 64;
	/** Denser crater field; radius is clamped so bowls never overlap. */
	private static final int CELL = 100;
	private static final double SPAWN_CHANCE = 0.97;
	private static final int JITTER = 5;

	public InnerworldCraterFeature() {
	}

	@Override
	public MapCodec<InnerworldCraterFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		ChunkAccess chunk = world.getChunk(origin);
		return applyGrid(world, chunk, world.getSeed() + 42071, minX, minZ, maxX, maxZ);
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
		if (sizeRoll < 0.40) {
			radius = 5 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 8.0);
		} else if (sizeRoll < 0.75) {
			radius = 13 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 15.0);
		} else if (sizeRoll < 0.92) {
			radius = 28 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 14.0);
		} else {
			radius = 42 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 9.0);
		}
		int maxRadius = CELL / 2 - JITTER - 8;
		radius = Math.min(radius, Math.max(5, maxRadius));

		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 83, cellX, cellZ)) * JITTER);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 101, cellX, cellZ)) * JITTER);

		double depthRoll = WorldgenNoise.hash(seed + 149, cellX, cellZ);
		double depthFactor;
		if (depthRoll < 0.28) {
			// Rare shallow saucers — barely sink into the plain.
			depthFactor = 0.10 + 0.06 * WorldgenNoise.hash(seed + 151, cellX, cellZ);
		} else if (depthRoll < 0.62) {
			depthFactor = 0.22 + 0.12 * WorldgenNoise.hash(seed + 151, cellX, cellZ);
		} else if (depthRoll < 0.86) {
			depthFactor = 0.40 + 0.16 * WorldgenNoise.hash(seed + 151, cellX, cellZ);
		} else {
			// Rare deep impact bowls; large sites dig especially hard.
			depthFactor = 0.58 + 0.28 * WorldgenNoise.hash(seed + 151, cellX, cellZ);
		}
		if (radius >= 28) {
			depthFactor *= 1.15 + 0.35 * WorldgenNoise.hash(seed + 157, cellX, cellZ);
		}
		if (radius >= 42) {
			depthFactor *= 1.10 + 0.25 * WorldgenNoise.hash(seed + 159, cellX, cellZ);
		}
		double depth = radius * depthFactor;
		double rimWidth = 2.0 + radius / 10.0 + WorldgenNoise.hash(seed + 331, cellX, cellZ);
		// Mercury rims stay subdued — usually under ~2 blocks.
		double rimHeight = 0.45 + radius / 28.0 + 0.35 * WorldgenNoise.hash(seed + 347, cellX, cellZ);
		boolean complex = radius >= 36;
		double peakHeight = complex ? 0.8 + radius / 40.0 : 0.0;

		double rotation = WorldgenNoise.hash(seed + 163, cellX, cellZ) * Math.PI;
		double stretch = 0.86 + 0.24 * WorldgenNoise.hash(seed + 181, cellX, cellZ);
		double phase1 = WorldgenNoise.hash(seed + 197, cellX, cellZ) * Math.PI * 2.0;
		double phase2 = WorldgenNoise.hash(seed + 211, cellX, cellZ) * Math.PI * 2.0;
		double phase3 = WorldgenNoise.hash(seed + 223, cellX, cellZ) * Math.PI * 2.0;
		double cosR = Math.cos(rotation);
		double sinR = Math.sin(rotation);
		int minY = world.getMinY() + 1;

		BlockState stone = ModBlocks.KOMATIITE.defaultBlockState();
		BlockState regolith = ModBlocks.MAGNESIAN_REGOLITH.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int extent = (int) Math.ceil(radius * 1.45 + rimWidth + 3.0);

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
					+ 0.08 * Math.sin(2.0 * angle + phase1)
					+ 0.04 * Math.sin(3.0 * angle + phase2)
					+ 0.03 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 241 + cellX * 17L + cellZ, x / 16.0, z / 16.0)));
				if (dist > localRadius + rimWidth) {
					continue;
				}

				double rimScale = 0.70 + 0.30 * (0.5 + 0.5 * Math.sin(2.0 * angle + phase3));
				double localRim = rimHeight * rimScale;
				double t = dist / Math.max(1.0, localRadius);
				double elev;
				boolean inner = t <= 1.0;
				if (inner) {
					double bowl = Math.pow(Math.cos(t * Math.PI * 0.5), 1.05);
					double rimApproach = Math.pow(t, 3.2);
					elev = -depth * bowl + localRim * rimApproach * 0.55;
					if (complex && t < 0.14) {
						elev += peakHeight * (1.0 - t / 0.14);
					}
					elev += 0.15 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 271, x / 16.0, z / 16.0));
				} else {
					double outerT = (dist - localRadius) / rimWidth;
					elev = localRim * Math.pow(1.0 - outerT, 1.8) * 0.55;
				}

				int originalSurface = findSurfaceY(chunk, x, z, minY, cursor);
				int surfaceY = Math.max(minY, originalSurface + (int) Math.round(elev));
				int clearTop = Math.max(originalSurface, surfaceY) + 10;
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
							world.setBlock(cursor, stone, 2);
						}
					}
				}

				cursor.set(x, surfaceY, z);
				if (world.getBlockState(cursor).is(Blocks.BEDROCK)) {
					continue;
				}

				BlockState existing = world.getBlockState(cursor);
				BlockState surface;
				boolean exposeBedrock = inner && t < 0.35 && !(complex && t < 0.14);
				if (exposeBedrock) {
					surface = exposedStone(existing, stone);
				} else {
					surface = regolith;
				}
				world.setBlock(cursor, surface, 2);

				// Seal noodle caves under the crater so bowls aren't blown open from below.
				int sealDepth = inner
					? Math.max(24, (int) Math.ceil(depth) + 16)
					: 10;
				for (int y = surfaceY - 1; y >= surfaceY - sealDepth && y > minY; y--) {
					cursor.set(x, y, z);
					BlockState below = world.getBlockState(cursor);
					if (below.is(Blocks.BEDROCK)) {
						break;
					}
					if (below.isAir() || below.canBeReplaced()) {
						world.setBlock(cursor, stone, 2);
					}
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

	private static BlockState exposedStone(BlockState existing, BlockState stone) {
		if (existing.is(ModBlocks.ENSTATITE) || existing.is(ModBlocks.KOMATIITE)) {
			return existing;
		}
		return stone;
	}
}
