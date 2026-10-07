package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

/**
 * Wide, gradual Nearworld shield volcanoes with a contained lava caldera.
 * The crater rim follows the surrounding shield slope (no floating crown).
 * Slope rivers are deferred — lava stays inside a continuous rock rim for now.
 */
public class NearworldVolcanoFeature implements Feature {
	public static final MapCodec<NearworldVolcanoFeature> CODEC = MapCodec.unit(NearworldVolcanoFeature::new);

	private static final int MAX_EXTENT = 144;
	private static final int CELL = 224;
	private static final double SPAWN_CHANCE = 0.32;
	/** Slightly steeper mid-slope than a pure shield; toe fades separately. */
	private static final double PROFILE_EXPONENT = 1.18;
	/** Beyond this radial t, cone height fades to zero (kills the flat plateau skirt). */
	private static final double TOE_START = 0.72;

	public NearworldVolcanoFeature() {
	}

	@Override
	public MapCodec<NearworldVolcanoFeature> codec() {
		return CODEC;
	}

	/** True when the column sits inside a Nearworld volcano footprint (including lava-flow skirts). */
	public static boolean covers(WorldGenLevel world, int x, int z) {
		long seed = world.getSeed() + 55121L;
		int cellMinX = Math.floorDiv(x - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(x + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(z - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(z + MAX_EXTENT, CELL);
		List<Shape> shapes = new ArrayList<>();
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				Shape shape = createShape(seed, cellX, cellZ);
				if (shape != null) {
					shapes.add(shape);
				}
			}
		}
		return ownerAt(shapes, x, z) != null;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		return applyGrid(world, world.getSeed() + 55121L, minX, minZ, minX + 15, minZ + 15);
	}

	private static boolean applyGrid(WorldGenLevel world, long seed, int minX, int minZ, int maxX, int maxZ) {
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(maxX + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + MAX_EXTENT, CELL);
		List<Shape> shapes = new ArrayList<>();
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				Shape shape = createShape(seed, cellX, cellZ);
				if (shape != null && overlaps(shape, minX, minZ, maxX, maxZ)) {
					shapes.add(shape);
				}
			}
		}
		if (shapes.isEmpty()) {
			return false;
		}

		BlockState crust = ModBlocks.SULFURIC_BASALT.defaultBlockState();
		BlockState fill = Blocks.BASALT.defaultBlockState();
		BlockState cooled = Blocks.SMOOTH_BASALT.defaultBlockState();
		BlockState magma = Blocks.MAGMA_BLOCK.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minWorldY = world.getMinY() + 1;
		boolean placed = false;
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				Shape owner = ownerAt(shapes, x, z);
				if (owner == null) {
					continue;
				}
				if (paintColumn(world, owner, x, z, crust, fill, cooled, magma, cursor, minWorldY)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private static Shape createShape(long seed, int cellX, int cellZ) {
		if (WorldgenNoise.hash(seed + 11, cellX, cellZ) > SPAWN_CHANCE) {
			return null;
		}

		int outerRadius = 52 + (int) (WorldgenNoise.hash(seed + 29, cellX, cellZ) * 26.0);
		int height = 26 + (int) (WorldgenNoise.hash(seed + 47, cellX, cellZ) * 16.0);
		int jitter = 14;
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 71, cellX, cellZ)) * jitter);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 97, cellX, cellZ)) * jitter);

		double rotation = WorldgenNoise.hash(seed + 113, cellX, cellZ) * Math.PI * 2.0;
		double stretch = 0.86 + 0.24 * WorldgenNoise.hash(seed + 131, cellX, cellZ);
		int peakX = centerX + (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 149, cellX, cellZ)) * outerRadius * 0.08);
		int peakZ = centerZ + (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 163, cellX, cellZ)) * outerRadius * 0.08);

		int craterRadius = Math.max(7, outerRadius / 5);
		int conduitRadius = 3 + (int) (WorldgenNoise.hash(seed + 181, cellX, cellZ) * 3.0);
		int chamberRadius = craterRadius + 2 + (int) (WorldgenNoise.hash(seed + 193, cellX, cellZ) * 5.0);
		double rimT = craterRadius / (double) outerRadius;
		int lavaLevel = Math.max(craterRadius + 6, (int) Math.round(height * Math.pow(1.0 - rimT, PROFILE_EXPONENT)) - 1);
		int craterDepth = Math.max(6, craterRadius / 2 + 2);
		int chamberBottom = 5 + (int) (WorldgenNoise.hash(seed + 199, cellX, cellZ) * 5.0);

		int flowCount = 3 + (int) (WorldgenNoise.hash(seed + 211, cellX, cellZ) * 3.0);
		double[] flowAngles = new double[flowCount];
		double[] flowWidths = new double[flowCount];
		double acc = WorldgenNoise.hash(seed + 223, cellX, cellZ) * Math.PI * 2.0;
		for (int i = 0; i < flowCount; i++) {
			double step = (Math.PI * 2.0 / flowCount)
				+ WorldgenNoise.signed(WorldgenNoise.hash(seed + 227 + i, cellX, cellZ)) * 0.42;
			acc += step;
			flowAngles[i] = acc;
			flowWidths[i] = (i == 0 ? 5.4 : 3.2)
				+ WorldgenNoise.hash(seed + 251 + i, cellX, cellZ) * (i == 0 ? 3.2 : 2.4);
		}
		int flowRunout = 10 + (int) (WorldgenNoise.hash(seed + 271, cellX, cellZ) * 10.0);

		int ridgeCount = 6 + (int) (WorldgenNoise.hash(seed + 283, cellX, cellZ) * 4.0);
		double[] ridgeAngles = new double[ridgeCount];
		double ridgeBase = WorldgenNoise.hash(seed + 293, cellX, cellZ) * Math.PI * 2.0;
		for (int i = 0; i < ridgeCount; i++) {
			ridgeAngles[i] = ridgeBase + (Math.PI * 2.0 * i / ridgeCount)
				+ WorldgenNoise.signed(WorldgenNoise.hash(seed + 307 + i, cellX, cellZ)) * 0.28;
		}

		double phase1 = WorldgenNoise.hash(seed + 401, cellX, cellZ) * Math.PI * 2.0;
		double phase2 = WorldgenNoise.hash(seed + 409, cellX, cellZ) * Math.PI * 2.0;
		double phase3 = WorldgenNoise.hash(seed + 419, cellX, cellZ) * Math.PI * 2.0;
		return new Shape(
			seed, centerX, centerZ, peakX, peakZ,
			outerRadius, height, craterRadius, conduitRadius, chamberRadius,
			lavaLevel, craterDepth, chamberBottom, stretch, Math.cos(rotation), Math.sin(rotation),
			phase1, phase2, phase3, flowAngles, flowWidths, ridgeAngles, flowRunout
		);
	}

	private static boolean overlaps(Shape shape, int minX, int minZ, int maxX, int maxZ) {
		int extent = shape.outerRadius + shape.flowRunout + 4;
		return shape.centerX + extent >= minX
			&& shape.centerX - extent <= maxX
			&& shape.centerZ + extent >= minZ
			&& shape.centerZ - extent <= maxZ;
	}

	private static Shape ownerAt(List<Shape> shapes, int x, int z) {
		Shape best = null;
		double bestT = Double.POSITIVE_INFINITY;
		for (Shape shape : shapes) {
			double t = coverageT(shape, x, z);
			if (t < bestT) {
				bestT = t;
				best = shape;
			}
		}
		return bestT < Double.POSITIVE_INFINITY ? best : null;
	}

	private static double coverageT(Shape shape, int x, int z) {
		ColumnGeom geom = geom(shape, x, z);
		if (geom.dist > geom.flowLength && geom.peakDist > geom.localRadius + 2.0) {
			return Double.POSITIVE_INFINITY;
		}
		return geom.peakDist / Math.max(1.0, geom.localRadius);
	}

	private static ColumnGeom geom(Shape shape, int x, int z) {
		double dx = x - shape.centerX;
		double dz = z - shape.centerZ;
		double rx = dx * shape.cosR + dz * shape.sinR;
		double rz = (-dx * shape.sinR + dz * shape.cosR) / shape.stretch;
		double dist = Math.hypot(rx, rz);
		double angle = Math.atan2(rz, rx);
		double radiusNoise = 0.11 * Math.sin(2.0 * angle + shape.phase1)
			+ 0.07 * Math.sin(3.0 * angle + shape.phase2)
			+ 0.04 * Math.sin(5.0 * angle + shape.phase3)
			+ 0.06 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(shape.seed + 431L, x / 22.0, z / 22.0, 3, 0.55));
		double localRadius = shape.outerRadius * (1.0 + radiusNoise);
		double peakDist = Math.hypot(x - shape.peakX, z - shape.peakZ);
		double flowClearance = nearestFlowWidth(dx, dz, dist, angle, shape);
		boolean onFlow = flowClearance > 0.0;
		double flowLength = localRadius + (onFlow ? shape.flowRunout : 0);
		return new ColumnGeom(dx, dz, dist, angle, localRadius, peakDist, flowClearance, onFlow, flowLength);
	}

	private static boolean paintColumn(
		WorldGenLevel world,
		Shape shape,
		int x,
		int z,
		BlockState crust,
		BlockState fill,
		BlockState cooled,
		BlockState magma,
		BlockPos.MutableBlockPos cursor,
		int minWorldY
	) {
		ColumnGeom geom = geom(shape, x, z);
		// Slope rivers deferred — stay inside the solid cone footprint for now.
		if (geom.peakDist > geom.localRadius + 2.0) {
			return false;
		}

		double t = Math.min(1.0, geom.peakDist / Math.max(1.0, geom.localRadius));
		double profile = Math.pow(Math.max(0.0, 1.0 - t), PROFILE_EXPONENT);
		double ridge = ridgeBoost(geom.angle, t, shape.ridgeAngles) * shape.height * 0.12;
		double roughness = 3.2 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(shape.seed + 443L, x / 18.0, z / 18.0, 3, 0.5))
			+ 1.4 * WorldgenNoise.signed(WorldgenNoise.valueNoise(shape.seed + 457L, x / 6.0, z / 6.0));
		int coneHeight = (int) Math.round(shape.height * profile + ridge + roughness);

		boolean inCrater = geom.peakDist < shape.craterRadius;
		// Inner disk is the open lava pool; outer crater band is a continuous rock rim.
		boolean inLavaPool = geom.peakDist < shape.craterRadius - 1.75;
		if (inCrater) {
			double ct = geom.peakDist / Math.max(1.0, shape.craterRadius);
			double rimJagged = 0.9 * WorldgenNoise.signed(WorldgenNoise.valueNoise(shape.seed + 461L, x / 3.0, z / 3.0));
			if (inLavaPool) {
				// Pool surface sits at lava level (no rock lid, no side walls of fluid).
				coneHeight = shape.lavaLevel;
			} else {
				// Sink the rim into the surrounding shield slope instead of a tall crown.
				// Edge slope height is already ~lavaLevel+1 (see createShape); blend a low lip
				// on the inner face out to that edge height with light jaggedness only.
				double wall = Math.pow(Math.max(0.0, (ct - 0.55) / 0.45), 1.35);
				double edgeT = shape.craterRadius / Math.max(1.0, geom.localRadius);
				double edgeProfile = Math.pow(Math.max(0.0, 1.0 - edgeT), PROFILE_EXPONENT);
				int edgeSlope = Math.max(
					shape.lavaLevel + 1,
					(int) Math.round(shape.height * edgeProfile + ridge * 0.35)
				);
				int lip = shape.lavaLevel + 1;
				coneHeight = lip + (int) Math.round((edgeSlope - lip) * wall + rimJagged * wall);
				coneHeight = Math.max(lip, coneHeight);
			}
		}

		// Fade the outer skirt instead of clamping to a flat +2 plateau.
		if (!inCrater && t > TOE_START) {
			double toe = 1.0 - (t - TOE_START) / Math.max(0.001, 1.0 - TOE_START);
			toe = Math.max(0.0, toe);
			double edgeNoise = 0.55 + 0.45 * WorldgenNoise.hash(shape.seed + 487L, x, z);
			coneHeight = (int) Math.round(coneHeight * toe * edgeNoise);
			if (coneHeight <= 0) {
				return false;
			}
		} else if (!inCrater) {
			coneHeight = Math.max(1, coneHeight);
		} else {
			coneHeight = Math.max(1, coneHeight);
		}

		int localGround = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		int topY = localGround + coneHeight;
		int lavaY = localGround + shape.lavaLevel;
		int digDepth = (!inCrater && t > TOE_START) ? 1 : 3;
		int y0 = Math.max(minWorldY, localGround - digDepth);
		if (y0 > topY) {
			return false;
		}

		boolean inConduit = geom.peakDist < shape.conduitRadius;
		double chamberN = 0.18 * WorldgenNoise.signed(WorldgenNoise.octaveNoise(shape.seed + 499L, x / 7.0, z / 7.0, 2, 0.5));
		boolean inChamber = geom.peakDist < shape.chamberRadius * (1.0 + chamberN);
		int chamberTop = localGround + Math.max(shape.chamberBottom + 8, shape.lavaLevel / 3);
		int craterBottom = lavaY - shape.craterDepth;

		for (int y = y0; y <= topY; y++) {
			cursor.set(x, y, z);
			if (world.getBlockState(cursor).is(Blocks.BEDROCK)) {
				continue;
			}

			// Only contained crater-pool lava (static sources). No side flows / conduits / dikes.
			boolean craterLava = inLavaPool && y >= craterBottom && y <= lavaY;

			if (craterLava) {
				WorldgenLava.place(world, cursor, false);
			} else if (nearMagma(geom.peakDist, inChamber, inCrater, inLavaPool, shape, y, lavaY, topY, chamberTop)) {
				world.setBlock(cursor, magma, 2);
			} else if (y >= topY - 1) {
				BlockState surface = crust;
				if (WorldgenNoise.hash(shape.seed + 509L + y, x, z) > 0.82 && t < 0.55 && !inLavaPool) {
					surface = magma;
				}
				world.setBlock(cursor, surface, 2);
			} else if (inConduit && y < lavaY - 2) {
				// Buried magma plug under the pool — never exposed lava walls.
				world.setBlock(cursor, magma, 2);
			} else {
				world.setBlock(cursor, fill, 2);
			}
		}
		return true;
	}

	private static boolean nearMagma(
		double peakDist,
		boolean inChamber,
		boolean inCrater,
		boolean inLavaPool,
		Shape shape,
		int y,
		int lavaY,
		int topY,
		int chamberTop
	) {
		if (peakDist < shape.conduitRadius + 1.8 && y < lavaY - 1) {
			return true;
		}
		if (inChamber && y <= chamberTop + 1 && peakDist < shape.chamberRadius && y < lavaY - 4) {
			return true;
		}
		// Magma flecks on the inner rim face just above the pool.
		return inCrater && !inLavaPool && y >= lavaY - 1 && y <= lavaY + 1;
	}

	private static double nearestFlowWidth(double dx, double dz, double dist, double ellipseAngle, Shape shape) {
		double angle = Math.atan2(dz, dx);
		double meander = WorldgenNoise.signed(WorldgenNoise.valueNoise(shape.seed + 521L, (shape.centerX + dx) * 0.045, (shape.centerZ + dz) * 0.045)) * 0.28;
		angle += meander;
		double best = Double.POSITIVE_INFINITY;
		double width = 3.0;
		for (int i = 0; i < shape.flowAngles.length; i++) {
			double d = Math.abs(wrapAngle(angle - shape.flowAngles[i]));
			if (d < best) {
				best = d;
				width = shape.flowWidths[i];
			}
		}
		double scored = best * Math.max(1.0, dist);
		if (scored < width) {
			return width - scored;
		}
		double collapse = Math.abs(wrapAngle(ellipseAngle - shape.flowAngles[0]));
		if (collapse < 0.38 && dist > shape.craterRadius * 0.6) {
			return 2.0;
		}
		return -1.0;
	}

	private static double ridgeBoost(double angle, double t, double[] ridgeAngles) {
		double best = 0.0;
		double fade = (1.0 - t) * t * 4.0;
		for (double ridge : ridgeAngles) {
			double d = wrapAngle(angle - ridge);
			double lobe = Math.exp(-d * d * 14.0);
			if (lobe > best) {
				best = lobe;
			}
		}
		return best * fade;
	}

	private static double wrapAngle(double a) {
		while (a > Math.PI) {
			a -= Math.PI * 2.0;
		}
		while (a < -Math.PI) {
			a += Math.PI * 2.0;
		}
		return a;
	}

	private record ColumnGeom(
		double dx,
		double dz,
		double dist,
		double angle,
		double localRadius,
		double peakDist,
		double flowClearance,
		boolean onFlow,
		double flowLength
	) {
	}

	private record Shape(
		long seed,
		int centerX,
		int centerZ,
		int peakX,
		int peakZ,
		int outerRadius,
		int height,
		int craterRadius,
		int conduitRadius,
		int chamberRadius,
		int lavaLevel,
		int craterDepth,
		int chamberBottom,
		double stretch,
		double cosR,
		double sinR,
		double phase1,
		double phase2,
		double phase3,
		double[] flowAngles,
		double[] flowWidths,
		double[] ridgeAngles,
		int flowRunout
	) {
	}
}
