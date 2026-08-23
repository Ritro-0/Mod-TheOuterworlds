package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CraterFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_EXTENT = 48;

	public CraterFeature() {
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
		long seed = world.getSeed();

		boolean placed = false;
		placed |= applyGrid(world, seed + 1103, 64, 0.20, 28, true, minX, minZ, maxX, maxZ);
		placed |= applyGrid(world, seed, 48, 0.30, 20, false, minX, minZ, maxX, maxZ);
		return placed;
	}

	private static boolean applyGrid(
		WorldGenLevel world,
		long seed,
		int cell,
		double spawnChance,
		int jitter,
		boolean degraded,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, cell);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, cell);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, cell);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, cell);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (applyCellCrater(world, seed, cell, cellX, cellZ, spawnChance, jitter, degraded, minX, minZ, maxX, maxZ)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static boolean applyCellCrater(
		WorldGenLevel world,
		long seed,
		int cell,
		int cellX,
		int cellZ,
		double spawnChance,
		int jitter,
		boolean degradedLayer,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		if (WorldgenNoise.hash(seed + 19, cellX, cellZ) > spawnChance) {
			return false;
		}

		double sizeRoll = WorldgenNoise.hash(seed + 41, cellX, cellZ);
		int radius;
		if (sizeRoll < 0.68) {
			radius = 5 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 6.0);
		} else if (sizeRoll < 0.93) {
			radius = 10 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 7.0);
		} else if ((cellX + cellZ * 3) % 5 == 0) {
			radius = 16 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 6.0);
		} else {
			radius = 10 + (int) (WorldgenNoise.hash(seed + 67, cellX, cellZ) * 6.0);
		}
		radius = Math.min(22, radius);

		int centerX = cellX * cell + cell / 2 + (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 83, cellX, cellZ)) * jitter);
		int centerZ = cellZ * cell + cell / 2 + (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 101, cellX, cellZ)) * jitter);

		boolean degraded = degradedLayer || WorldgenNoise.hash(seed + 311, cellX, cellZ) > 0.62;
		double depthScale = degraded ? 0.16 + 0.12 * WorldgenNoise.hash(seed + 149, cellX, cellZ) : 0.28 + 0.12 * WorldgenNoise.hash(seed + 149, cellX, cellZ);
		double depth = radius * depthScale;
		double rimWidth = degraded
			? 2.0 + radius / 7.0 + 3.0 * WorldgenNoise.hash(seed + 331, cellX, cellZ)
			: 2.8 + radius / 5.0 + 2.5 * WorldgenNoise.hash(seed + 331, cellX, cellZ);
		double rimHeight = degraded
			? 0.6 + radius / 10.0 * WorldgenNoise.hash(seed + 347, cellX, cellZ)
			: 1.8 + radius / 5.5 + 1.4 * WorldgenNoise.hash(seed + 347, cellX, cellZ);
		double gapStart = WorldgenNoise.hash(seed + 359, cellX, cellZ) * Math.PI * 2.0;
		double gapWidth = degraded ? 0.7 + 1.4 * WorldgenNoise.hash(seed + 373, cellX, cellZ) : 0.25 + 0.55 * WorldgenNoise.hash(seed + 373, cellX, cellZ);
		boolean hasGap = degraded || WorldgenNoise.hash(seed + 389, cellX, cellZ) > 0.72;

		double rotation = WorldgenNoise.hash(seed + 163, cellX, cellZ) * Math.PI;
		double stretch = 0.68 + 0.55 * WorldgenNoise.hash(seed + 181, cellX, cellZ);
		double phase1 = WorldgenNoise.hash(seed + 197, cellX, cellZ) * Math.PI * 2.0;
		double phase2 = WorldgenNoise.hash(seed + 211, cellX, cellZ) * Math.PI * 2.0;
		double phase3 = WorldgenNoise.hash(seed + 223, cellX, cellZ) * Math.PI * 2.0;
		double phase4 = WorldgenNoise.hash(seed + 401, cellX, cellZ) * Math.PI * 2.0;
		double cosR = Math.cos(rotation);
		double sinR = Math.sin(rotation);
		int minY = world.getMinY() + 1;

		BlockState basalt = ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		BlockState regolith = ModBlocks.REGOLITH.defaultBlockState();
		BlockState suspiciousRegolith = ModBlocks.SUSPICIOUS_REGOLITH.defaultBlockState();
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
					+ 0.16 * Math.sin(2.0 * angle + phase1)
					+ 0.09 * Math.sin(3.0 * angle + phase2)
					+ 0.05 * Math.sin(5.0 * angle + phase4)
					+ 0.06 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 241 + cellX * 17L + cellZ, x / 13.0, z / 13.0)));
				if (dist > localRadius + rimWidth) {
					continue;
				}

				double rimScale = 0.55 + 0.45 * (0.5 + 0.5 * Math.sin(2.0 * angle + phase3));
				if (hasGap && angleDelta(angle, gapStart) < gapWidth * 0.5) {
					rimScale *= 0.12;
				}
				double localRim = rimHeight * rimScale;
				double t = dist / Math.max(1.0, localRadius);
				double elev;
				boolean inner = t <= 1.0;
				double outerT = 0.0;
				if (inner) {
					double bowl = Math.pow(Math.cos(t * Math.PI * 0.5), 1.05);
					double rimApproach = Math.pow(t, 2.2);
					elev = -depth * bowl + localRim * rimApproach;
					elev += 0.7 * WorldgenNoise.signed(WorldgenNoise.valueNoise(seed + 271, x / 14.0, z / 14.0));
				} else {
					outerT = (dist - localRadius) / rimWidth;
					elev = localRim * Math.pow(1.0 - outerT, 1.45);
				}

				int plains = plainsHeight(seed, x, z);
				int surfaceY = Math.max(minY, plains + (int) Math.round(elev));
				int clearTop = plains + (int) Math.ceil(rimHeight) + 16;
				for (int y = clearTop; y > surfaceY; y--) {
					cursor.set(x, y, z);
					if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
					}
				}

				BlockState surface;
				boolean craterFloorRegolith = inner && t < 0.70 && elev < localRim * 0.15;
				if (craterFloorRegolith) {
					surface = WorldgenNoise.hash(seed + 419, x, z) < 0.12 ? suspiciousRegolith : regolith;
				} else if (outerT > 0.50 && WorldgenNoise.hash(seed + 293, x, z) > 0.35) {
					surface = regolith;
				} else {
					surface = basalt;
				}
				cursor.set(x, surfaceY, z);
				if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
					world.setBlock(cursor, surface, 2);
					if (surface.is(ModBlocks.SUSPICIOUS_REGOLITH)
						&& world.getBlockEntity(cursor) instanceof BrushableBlockEntity brushable) {
						brushable.setLootTable(ModLootTables.SUSPICIOUS_REGOLITH, BlockPos.asLong(x, surfaceY, z) ^ seed);
					}
				}
				if (inner && t < 0.70) {
					for (int y = surfaceY - 1; y >= surfaceY - 2 && y > minY; y--) {
						cursor.set(x, y, z);
						if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
							world.setBlock(cursor, basalt, 2);
						}
					}
				}
			}
		}
		return true;
	}

	private static int plainsHeight(long seed, int x, int z) {
		double roll = 8.0 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(seed + 501, x / 95.0, z / 95.0, 2, 0.5));
		double detail = 4.0 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(seed + 521, x / 42.0, z / 42.0, 2, 0.45));
		return 76 + (int) Math.round(roll + detail);
	}

	private static double angleDelta(double angle, double target) {
		double d = angle - target;
		while (d > Math.PI) {
			d -= Math.PI * 2.0;
		}
		while (d < -Math.PI) {
			d += Math.PI * 2.0;
		}
		return Math.abs(d);
	}
}
