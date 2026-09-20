package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
 * Large, irregular subsurface cave systems for Beyondlands / Beyondlands II.
 * Halls are noisy and elongated rather than round chambers; worms stitch them together.
 * Height samples stay inside the generating chunk so WorldGenRegion never loads neighbors.
 */
public class BeyondlandsCaveFeature implements Feature {
	public static final MapCodec<BeyondlandsCaveFeature> CODEC = MapCodec.unit(BeyondlandsCaveFeature::new);

	private static final int MAX_EXTENT = 112;
	private static final int CELL = 256;
	private static final double SPAWN_CHANCE = 0.20;

	public BeyondlandsCaveFeature() {
	}

	@Override
	public MapCodec<BeyondlandsCaveFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		ChunkAccess chunk = world.getChunk(origin);
		return applyGrid(world, chunk, world.getSeed() + 91013, minX, minZ, maxX, maxZ);
	}

	private static boolean applyGrid(
		WorldGenLevel world,
		ChunkAccess chunk,
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
				if (carveCell(world, chunk, seed, cellX, cellZ, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean carveCell(
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
		if (WorldgenNoise.hash(seed, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 3, cellX, cellZ)) * 48);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 7, cellX, cellZ)) * 48);

		int sampleX = Mth.clamp(centerX, minX, maxX);
		int sampleZ = Mth.clamp(centerZ, minZ, maxZ);
		int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sampleX, sampleZ);
		int minY = world.getMinY() + 8;
		int maxCenterY = surfaceY - 18;
		if (maxCenterY <= minY + 16) {
			return false;
		}

		int centerY = minY + 12 + (int) (WorldgenNoise.hash(seed + 11, cellX, cellZ) * (maxCenterY - minY - 12));
		boolean carved = false;

		int mainRx = 48 + (int) (WorldgenNoise.hash(seed + 13, cellX, cellZ) * 32.0);
		int mainRy = 14 + (int) (WorldgenNoise.hash(seed + 17, cellX, cellZ) * 14.0);
		int mainRz = 36 + (int) (WorldgenNoise.hash(seed + 19, cellX, cellZ) * 40.0);
		if (WorldgenNoise.hash(seed + 23, cellX, cellZ) > 0.5) {
			int swap = mainRx;
			mainRx = mainRz;
			mainRz = swap;
		}
		carved |= carveBlob(world, chunk, seed + 31, centerX, centerY, centerZ, mainRx, mainRy, mainRz, minX, minZ, maxX, maxZ);

		int halls = 2 + (int) (WorldgenNoise.hash(seed + 29, cellX, cellZ) * 3.0);
		for (int i = 0; i < halls; i++) {
			double yaw = WorldgenNoise.hash(seed + 37 + i * 4, cellX, cellZ) * Math.PI * 2.0;
			double dist = 28.0 + WorldgenNoise.hash(seed + 41 + i * 4, cellX, cellZ) * 36.0;
			int hx = centerX + (int) (Math.cos(yaw) * dist);
			int hz = centerZ + (int) (Math.sin(yaw) * dist);
			int hy = centerY + (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 43 + i * 4, cellX, cellZ)) * 10.0);
			int hrx = 18 + (int) (WorldgenNoise.hash(seed + 47 + i * 4, cellX, cellZ) * 22.0);
			int hry = 10 + (int) (WorldgenNoise.hash(seed + 53 + i * 4, cellX, cellZ) * 10.0);
			int hrz = 16 + (int) (WorldgenNoise.hash(seed + 59 + i * 4, cellX, cellZ) * 28.0);
			carved |= carveBlob(world, chunk, seed + 61 + i, hx, hy, hz, hrx, hry, hrz, minX, minZ, maxX, maxZ);
		}

		int worms = 2 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 3.0);
		for (int w = 0; w < worms; w++) {
			double yaw = WorldgenNoise.hash(seed + 71 + w * 8, cellX, cellZ) * Math.PI * 2.0;
			double pitch = WorldgenNoise.signed(WorldgenNoise.hash(seed + 73 + w * 8, cellX, cellZ)) * 0.35;
			double x = centerX;
			double y = centerY;
			double z = centerZ;
			int steps = 10 + (int) (WorldgenNoise.hash(seed + 79 + w * 8, cellX, cellZ) * 10.0);
			for (int step = 0; step < steps; step++) {
				yaw += WorldgenNoise.signed(WorldgenNoise.hash(seed + 83 + w * 17 + step, cellX, cellZ)) * 0.55;
				pitch = Mth.clamp(
					pitch + WorldgenNoise.signed(WorldgenNoise.hash(seed + 89 + w * 17 + step, cellX, cellZ)) * 0.18,
					-0.55,
					0.55
				);
				double stepLen = 9.0 + WorldgenNoise.hash(seed + 97 + w * 17 + step, cellX, cellZ) * 6.0;
				x += Math.cos(yaw) * Math.cos(pitch) * stepLen;
				z += Math.sin(yaw) * Math.cos(pitch) * stepLen;
				y += Math.sin(pitch) * stepLen;
				int wr = 8 + (int) (WorldgenNoise.hash(seed + 101 + w * 17 + step, cellX, cellZ) * 7.0);
				int wry = 6 + (int) (WorldgenNoise.hash(seed + 103 + w * 17 + step, cellX, cellZ) * 5.0);
				carved |= carveBlob(
					world,
					chunk,
					seed + 107 + w * 13 + step,
					(int) x,
					(int) y,
					(int) z,
					wr + 4,
					wry,
					wr,
					minX,
					minZ,
					maxX,
					maxZ
				);
			}
		}
		return carved;
	}

	private static boolean carveBlob(
		WorldGenLevel world,
		ChunkAccess chunk,
		long seed,
		int centerX,
		int centerY,
		int centerZ,
		int rx,
		int ry,
		int rz,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int extentX = rx + 6;
		int extentZ = rz + 6;
		int x0 = Math.max(minX, centerX - extentX);
		int x1 = Math.min(maxX, centerX + extentX);
		int z0 = Math.max(minZ, centerZ - extentZ);
		int z1 = Math.min(maxZ, centerZ + extentZ);
		if (x0 > x1 || z0 > z1) {
			return false;
		}

		int minWorldY = world.getMinY() + 2;
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean carved = false;

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				int columnSurface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				int y0 = Math.max(minWorldY, centerY - ry - 4);
				int y1 = Math.min(columnSurface - 6, centerY + ry + 4);
				if (y0 > y1) {
					continue;
				}
				for (int y = y0; y <= y1; y++) {
					double nx = (x - centerX) / (double) rx;
					double ny = (y - centerY) / (double) ry;
					double nz = (z - centerZ) / (double) rz;
					double warp = WorldgenNoise.signed(WorldgenNoise.octaveNoise(
						seed,
						x * 0.038 + y * 0.012,
						z * 0.038 + y * 0.009,
						3,
						0.55
					)) * 0.55;
					warp += WorldgenNoise.signed(WorldgenNoise.valueNoise(
						seed + 11,
						x * 0.09 + y * 0.07,
						z * 0.09
					)) * 0.22;
					if (nx * nx + ny * ny * 0.82 + nz * nz > 1.0 + warp) {
						continue;
					}
					cursor.set(x, y, z);
					BlockState existing = world.getBlockState(cursor);
					if (existing.is(Blocks.BEDROCK) || existing.isAir()) {
						continue;
					}
					if (!isCaveReplaceable(existing)) {
						continue;
					}
					world.setBlock(cursor, air, 2);
					carved = true;
				}
			}
		}
		return carved;
	}

	private static boolean isCaveReplaceable(BlockState state) {
		return state.is(ModBlocks.NITROGEN_ICE)
			|| state.is(ModBlocks.METHANE_ICE)
			|| state.is(ModBlocks.THOLIN)
			|| state.is(ModBlocks.ANORTHOSITE)
			|| state.is(Blocks.PACKED_ICE)
			|| state.is(ModBlocks.PACKED_ICE)
			|| state.is(Blocks.BLUE_ICE)
			|| state.is(ModBlocks.BLUE_ICE);
	}
}
