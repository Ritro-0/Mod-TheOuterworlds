package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.util.RandomSource;

/**
 * Occasional methane ponds and small lakes on Amberworld high ground,
 * plus short river-like channels that drain toward lower terrain.
 */
public class AmberworldMethanePondFeature implements Feature {
	public static final MapCodec<AmberworldMethanePondFeature> CODEC = MapCodec.unit(AmberworldMethanePondFeature::new);

	private static final int MAX_EXTENT = 28;
	private static final int CELL = 64;
	private static final double POND_CHANCE = 0.28;
	private static final double RIVER_CHANCE = 0.14;
	private static final int SEA_LEVEL = 63;

	public AmberworldMethanePondFeature() {
	}

	@Override
	public MapCodec<AmberworldMethanePondFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		long seed = world.getSeed() + 77123L;
		RandomState noise = world.getLevel().getChunkSource().randomState();
		boolean placed = applyPondGrid(world, chunkGenerator, noise, seed, minX, minZ, minX + 15, minZ + 15);
		placed |= applyRiverGrid(world, chunkGenerator, noise, seed + 99L, minX, minZ, minX + 15, minZ + 15);
		return placed;
	}

	/** Noise surface. Reading the chunk heightmap here reaches into chunks that are still generating. */
	private static int surfaceAt(ChunkGenerator chunkGenerator, RandomState noise, WorldGenLevel world, int x, int z) {
		return chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, world, noise);
	}

	/**
	 * Pond centres are pure hash math, so anything that wants to sit "near a methane lake"
	 * can ask without reading terrain it is not allowed to touch during generation. The
	 * high-ground rejection in {@link #placePond} is not replayed here, so a hit only means
	 * a pond was rolled for that cell.
	 */
	/**
	 * Little ponds and the short channels that drain them. Ocean shoreline is not an inlet
	 * unless one of those channels actually runs through it.
	 */
	public static boolean isLittleInlet(
		ChunkGenerator chunkGenerator,
		RandomState noise,
		WorldGenLevel world,
		int x,
		int z
	) {
		long worldSeed = world.getSeed();
		return nearPondShore(chunkGenerator, noise, world, worldSeed, x, z)
			|| nearRiver(chunkGenerator, noise, world, worldSeed, x, z);
	}

	public static boolean nearPondShore(
		ChunkGenerator chunkGenerator,
		RandomState noise,
		WorldGenLevel world,
		long worldSeed,
		int x,
		int z
	) {
		long seed = worldSeed + 77123L;
		int reach = 24 + CELL / 2;
		int cellMinX = Math.floorDiv(x - reach, CELL);
		int cellMaxX = Math.floorDiv(x + reach, CELL);
		int cellMinZ = Math.floorDiv(z - reach, CELL);
		int cellMaxZ = Math.floorDiv(z + reach, CELL);
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (WorldgenNoise.hash(seed + 13, cellX, cellZ) > POND_CHANCE) {
					continue;
				}
				int centerX = cellX * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 79, cellX, cellZ)) * 12.0);
				int centerZ = cellZ * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 97, cellX, cellZ)) * 12.0);
				int surfaceY = surfaceAt(chunkGenerator, noise, world, centerX, centerZ);
				if (surfaceY <= SEA_LEVEL + 2) {
					continue;
				}
				int radiusX = 3 + (int) (WorldgenNoise.hash(seed + 41, cellX, cellZ) * 6.0) + 6;
				int radiusZ = 3 + (int) (WorldgenNoise.hash(seed + 53, cellX, cellZ) * 5.0) + 6;
				double nx = (x - centerX) / (double) Math.max(1, radiusX);
				double nz = (z - centerZ) / (double) Math.max(1, radiusZ);
				if (nx * nx + nz * nz <= 1.0) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean nearRiver(
		ChunkGenerator chunkGenerator,
		RandomState noise,
		WorldGenLevel world,
		long worldSeed,
		int x,
		int z
	) {
		long seed = worldSeed + 77123L + 99L;
		int cell = CELL * 2;
		int reach = 96;
		int cellMinX = Math.floorDiv(x - reach, cell);
		int cellMaxX = Math.floorDiv(x + reach, cell);
		int cellMinZ = Math.floorDiv(z - reach, cell);
		int cellMaxZ = Math.floorDiv(z + reach, cell);
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (WorldgenNoise.hash(seed + 7, cellX, cellZ) > RIVER_CHANCE) {
					continue;
				}
				int startX = cellX * cell + CELL
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 17, cellX, cellZ)) * 18.0);
				int startZ = cellZ * cell + CELL
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 29, cellX, cellZ)) * 18.0);
				int startY = surfaceAt(chunkGenerator, noise, world, startX, startZ);
				if (startY <= SEA_LEVEL + 4) {
					continue;
				}
				double angle = WorldgenNoise.hash(seed + 37, cellX, cellZ) * Math.PI * 2.0;
				double rx = startX;
				double rz = startZ;
				for (int step = 0; step < 48; step++) {
					angle += WorldgenNoise.signed(WorldgenNoise.hash(seed + 47 + step, cellX, cellZ)) * 0.35;
					rx += Math.cos(angle) * 1.6;
					rz += Math.sin(angle) * 1.6;
					double dx = rx - x;
					double dz = rz - z;
					if (dx * dx + dz * dz <= 36.0) {
						return true;
					}
					int ix = (int) Math.round(rx);
					int iz = (int) Math.round(rz);
					int surface = surfaceAt(chunkGenerator, noise, world, ix, iz);
					if (Math.max(SEA_LEVEL, surface - 2) <= SEA_LEVEL) {
						break;
					}
				}
			}
		}
		return false;
	}

	public static boolean hasPondCenterNear(long worldSeed, int x, int z, int radius) {
		long seed = worldSeed + 77123L;
		int reach = radius + CELL / 2;
		int cellMinX = Math.floorDiv(x - reach, CELL);
		int cellMaxX = Math.floorDiv(x + reach, CELL);
		int cellMinZ = Math.floorDiv(z - reach, CELL);
		int cellMaxZ = Math.floorDiv(z + reach, CELL);
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (WorldgenNoise.hash(seed + 13, cellX, cellZ) > POND_CHANCE) {
					continue;
				}
				int centerX = cellX * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 79, cellX, cellZ)) * 12.0);
				int centerZ = cellZ * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 97, cellX, cellZ)) * 12.0);
				int dx = centerX - x;
				int dz = centerZ - z;
				if (dx * dx + dz * dz <= radius * radius) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean applyPondGrid(
		WorldGenLevel world,
		ChunkGenerator chunkGenerator,
		RandomState noise,
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
				if (placePond(world, chunkGenerator, noise, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean placePond(
		WorldGenLevel world,
		ChunkGenerator chunkGenerator,
		RandomState noise,
		long seed,
		int cellX,
		int cellZ,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed + 13, cellX, cellZ) > POND_CHANCE) {
			return false;
		}
		int radiusX = 3 + (int) (WorldgenNoise.hash(seed + 41, cellX, cellZ) * 6.0);
		int radiusZ = 3 + (int) (WorldgenNoise.hash(seed + 53, cellX, cellZ) * 5.0);
		int depth = 2 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 2.0);
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 79, cellX, cellZ)) * 12.0);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 97, cellX, cellZ)) * 12.0);
		int surfaceY = surfaceAt(chunkGenerator, noise, world, centerX, centerZ);
		if (surfaceY <= SEA_LEVEL + 2) {
			return false;
		}
		return carveBasin(world, chunkGenerator, noise, centerX, centerZ, radiusX, radiusZ, depth, minX, minZ, maxX, maxZ, true);
	}

	private static boolean applyRiverGrid(
		WorldGenLevel world,
		ChunkGenerator chunkGenerator,
		RandomState noise,
		long seed,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL * 2);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL * 2);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL * 2);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL * 2);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (placeRiver(world, chunkGenerator, noise, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean placeRiver(
		WorldGenLevel world,
		ChunkGenerator chunkGenerator,
		RandomState noise,
		long seed,
		int cellX,
		int cellZ,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed + 7, cellX, cellZ) > RIVER_CHANCE) {
			return false;
		}
		int startX = cellX * CELL * 2 + CELL
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 17, cellX, cellZ)) * 18.0);
		int startZ = cellZ * CELL * 2 + CELL
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 29, cellX, cellZ)) * 18.0);
		int startY = surfaceAt(chunkGenerator, noise, world, startX, startZ);
		if (startY <= SEA_LEVEL + 4) {
			return false;
		}
		double angle = WorldgenNoise.hash(seed + 37, cellX, cellZ) * Math.PI * 2.0;
		boolean placed = false;
		double x = startX;
		double z = startZ;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockState methane = ModBlocks.LIQUID_METHANE.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int step = 0; step < 48; step++) {
			angle += WorldgenNoise.signed(WorldgenNoise.hash(seed + 47 + step, cellX, cellZ)) * 0.35;
			x += Math.cos(angle) * 1.6;
			z += Math.sin(angle) * 1.6;
			int ix = (int) Math.round(x);
			int iz = (int) Math.round(z);
			int surface = surfaceAt(chunkGenerator, noise, world, ix, iz);
			int waterY = Math.max(SEA_LEVEL, surface - 2);
			int halfWidth = 1 + (step % 5 == 0 ? 1 : 0);
			for (int dx = -halfWidth; dx <= halfWidth; dx++) {
				for (int dz = -halfWidth; dz <= halfWidth; dz++) {
					if (dx * dx + dz * dz > halfWidth * halfWidth + 1) {
						continue;
					}
					int cx = ix + dx;
					int cz = iz + dz;
					if (cx < minX || cx > maxX || cz < minZ || cz > maxZ) {
						continue;
					}
					int top = surfaceAt(chunkGenerator, noise, world, cx, cz);
					for (int y = top; y >= waterY; y--) {
						cursor.set(cx, y, cz);
						if (y > waterY) {
							world.setBlock(cursor, air, 2);
						} else {
							world.setBlock(cursor, methane, 2);
							placed = true;
						}
					}
				}
			}
			if (waterY <= SEA_LEVEL) {
				break;
			}
		}
		return placed;
	}

	private static boolean carveBasin(
		WorldGenLevel world,
		ChunkGenerator chunkGenerator,
		RandomState noise,
		int centerX,
		int centerZ,
		int radiusX,
		int radiusZ,
		int depth,
		int minX,
		int minZ,
		int maxX,
		int maxZ,
		boolean rim
	) {
		BlockState methane = ModBlocks.LIQUID_METHANE.defaultBlockState();
		BlockState rimBlock = ModBlocks.THOLIN.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean placed = false;
		int x0 = Math.max(minX, centerX - radiusX - 2);
		int x1 = Math.min(maxX, centerX + radiusX + 2);
		int z0 = Math.max(minZ, centerZ - radiusZ - 2);
		int z1 = Math.min(maxZ, centerZ + radiusZ + 2);
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double nx = (x - centerX) / (double) Math.max(1, radiusX);
				double nz = (z - centerZ) / (double) Math.max(1, radiusZ);
				double dist = nx * nx + nz * nz;
				if (dist > 1.05) {
					continue;
				}
				int top = surfaceAt(chunkGenerator, noise, world, x, z);
				int floor = top - depth;
				for (int y = top; y >= floor; y--) {
					cursor.set(x, y, z);
					if (rim && dist > 0.72) {
						if (y == floor || y == floor + 1) {
							world.setBlock(cursor, rimBlock, 2);
							placed = true;
						}
					} else if (y > floor) {
						world.setBlock(cursor, air, 2);
					} else {
						world.setBlock(cursor, methane, 2);
						placed = true;
					}
				}
			}
		}
		return placed;
	}
}
