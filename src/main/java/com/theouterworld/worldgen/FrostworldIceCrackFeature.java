package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Noisy cave-like breaches through Frostworld's ice crust into the subsurface ocean.
 * Soft bowl entrances at the surface taper into irregular shafts rather than clean cuts.
 */
public class FrostworldIceCrackFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_EXTENT = 18;
	private static final int CELL = 110;
	private static final double SPAWN_CHANCE = 0.18;
	private static final int OCEAN_TOP_Y = 91;

	public FrostworldIceCrackFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		return applyGrid(world, world.getSeed() + 44027L, minX, minZ, minX + 15, minZ + 15);
	}

	private static boolean applyGrid(
		WorldGenLevel world,
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
		if (WorldgenNoise.hash(seed + 7, cellX, cellZ) > SPAWN_CHANCE) {
			return false;
		}

		int jitter = 22;
		int mouthX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 59, cellX, cellZ)) * jitter);
		int mouthZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 71, cellX, cellZ)) * jitter);

		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mouthX, mouthZ);
		if (surfaceY <= OCEAN_TOP_Y + 10) {
			return false;
		}

		double mouthRadius = 4.5 + WorldgenNoise.hash(seed + 101, cellX, cellZ) * 4.5;
		int blobCount = 5 + (int) (WorldgenNoise.hash(seed + 113, cellX, cellZ) * 5.0);
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		// Soft surface bowl — irregular crater mouth that reads as a cave entrance.
		int bowlPad = (int) Math.ceil(mouthRadius) + 3;
		int bowlMinX = Math.max(minX, mouthX - bowlPad);
		int bowlMaxX = Math.min(maxX, mouthX + bowlPad);
		int bowlMinZ = Math.max(minZ, mouthZ - bowlPad);
		int bowlMaxZ = Math.min(maxZ, mouthZ + bowlPad);
		for (int x = bowlMinX; x <= bowlMaxX; x++) {
			for (int z = bowlMinZ; z <= bowlMaxZ; z++) {
				double nx = (x - mouthX) / mouthRadius;
				double nz = (z - mouthZ) / mouthRadius;
				double radial = Math.sqrt(nx * nx + nz * nz);
				double edgeNoise = WorldgenNoise.hash(seed + 131, x, z) * 0.55;
				if (radial > 1.15 + edgeNoise * 0.35) {
					continue;
				}
				int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
				double bowl = (1.0 - Mth.clamp(radial, 0.0, 1.0));
				bowl = bowl * bowl;
				int dig = (int) Math.round(3.0 + bowl * 7.0 + edgeNoise * 2.0);
				int floor = Math.max(OCEAN_TOP_Y + 2, top - dig);
				for (int y = top; y >= floor; y--) {
					if (carveIce(world, cursor.set(x, y, z), y)) {
						placed = true;
					}
				}
			}
		}

		// Descending blob chain: wandering cave shaft into the ocean.
		double x = mouthX;
		double z = mouthZ;
		double y = surfaceY - 2.0;
		double angle = WorldgenNoise.hash(seed + 151, cellX, cellZ) * Math.PI * 2.0;
		for (int i = 0; i < blobCount; i++) {
			double t = i / (double) Math.max(1, blobCount - 1);
			double radius = mouthRadius * (0.85 - t * 0.45)
				+ WorldgenNoise.hash(seed + 163 + i, cellX, cellZ) * 2.2;
			radius = Math.max(2.2, radius);

			angle += WorldgenNoise.signed(WorldgenNoise.hash(seed + 173 + i, cellX, cellZ)) * 0.7;
			x += Math.cos(angle) * (1.4 + WorldgenNoise.hash(seed + 181 + i, cellX, cellZ) * 1.8);
			z += Math.sin(angle) * (1.4 + WorldgenNoise.hash(seed + 191 + i, cellX, cellZ) * 1.8);
			y -= 5.0 + WorldgenNoise.hash(seed + 199 + i, cellX, cellZ) * 5.0
				+ (i == blobCount - 1 ? 6.0 : 0.0);

			if (carveEllipsoid(
				world,
				seed,
				cursor,
				x,
				y,
				z,
				radius * (0.9 + WorldgenNoise.hash(seed + 211 + i, cellX, cellZ) * 0.35),
				radius * (0.7 + WorldgenNoise.hash(seed + 223 + i, cellX, cellZ) * 0.45),
				radius * (0.9 + WorldgenNoise.hash(seed + 227 + i, cellX, cellZ) * 0.35),
				minX,
				minZ,
				maxX,
				maxZ
			)) {
				placed = true;
			}
		}

		// Guarantee an opening into open water beneath the crust.
		int shaftX = Mth.floor(x);
		int shaftZ = Mth.floor(z);
		int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, shaftX, shaftZ);
		for (int sy = top; sy >= OCEAN_TOP_Y - 6; sy--) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx * dx + dz * dz > 2) {
						continue;
					}
					int cx = shaftX + dx;
					int cz = shaftZ + dz;
					if (cx < minX || cx > maxX || cz < minZ || cz > maxZ) {
						continue;
					}
					if (carveIce(world, cursor.set(cx, sy, cz), sy)) {
						placed = true;
					}
				}
			}
		}

		return placed;
	}

	private static boolean carveEllipsoid(
		WorldGenLevel world,
		long seed,
		BlockPos.MutableBlockPos cursor,
		double cx,
		double cy,
		double cz,
		double rx,
		double ry,
		double rz,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int x0 = Math.max(minX, Mth.floor(cx - rx - 1));
		int x1 = Math.min(maxX, Mth.ceil(cx + rx + 1));
		int z0 = Math.max(minZ, Mth.floor(cz - rz - 1));
		int z1 = Math.min(maxZ, Mth.ceil(cz + rz + 1));
		int y0 = Mth.floor(cy - ry - 1);
		int y1 = Mth.ceil(cy + ry + 1);
		boolean placed = false;
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				for (int y = y0; y <= y1; y++) {
					double nx = (x + 0.5 - cx) / rx;
					double ny = (y + 0.5 - cy) / ry;
					double nz = (z + 0.5 - cz) / rz;
					double dist = nx * nx + ny * ny + nz * nz;
					double roughness = WorldgenNoise.hash(seed + 251, x, y * 3 + z) * 0.35;
					if (dist > 1.0 + roughness) {
						continue;
					}
					if (carveIce(world, cursor.set(x, y, z), y)) {
						placed = true;
					}
				}
			}
		}
		return placed;
	}

	private static boolean carveIce(WorldGenLevel world, BlockPos.MutableBlockPos pos, int y) {
		BlockState state = world.getBlockState(pos);
		if (!isIceCrust(state) && !state.isAir() && !state.liquid()) {
			return false;
		}
		// WorldGenLevel bypasses Level.setBlock climate conversion; place real sources.
		BlockState fill = y >= OCEAN_TOP_Y ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState();
		world.setBlock(pos, fill, 2);
		return true;
	}

	private static boolean isIceCrust(BlockState state) {
		return state.is(ModBlocks.CARBONIC_ICE) || state.is(ModBlocks.DRY_ICE);
	}
}
