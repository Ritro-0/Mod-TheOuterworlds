package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.WeaverPadBlock;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Weaver Anchors: colony-scale woven cities of tholin fibre. A double-helix spring
 * rises as the focal point; teardrop pods, sagging walkways, spiral stairs and
 * hanging nets spread off it into the haze.
 *
 * <p>Each Anchor spans many chunks, so the plan is derived from a grid-cell hash
 * rather than the placement origin. Every overlapping chunk replays the same
 * recipe and paints only its own 16×16 slice. Centre height comes from the noise
 * generator, never from a neighbour's heightmap.
 */
public class AmberworldAnchorFeature implements Feature {
	public static final MapCodec<AmberworldAnchorFeature> CODEC = MapCodec.unit(AmberworldAnchorFeature::new);

	private static final int CELL = 416;
	private static final int JITTER = 96;
	private static final int RADIUS = 52;
	private static final int SEA_LEVEL = 63;
	private static final int HILL_HEIGHT = SEA_LEVEL + 9;
	/** How far inland an Anchor may sit and still count as coastal. */
	private static final int OCEAN_REACH = 72;

	private static final double BASE_CHANCE = 0.28;
	private static final double HILL_BONUS = 0.18;

	public AmberworldAnchorFeature() {
	}

	@Override
	public MapCodec<AmberworldAnchorFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		if (!ModDimensions.isAmberworld(world.getLevel().dimension())) {
			return false;
		}
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		long seed = world.getSeed() + 5514011L;
		RandomState noise = world.getLevel().getChunkSource().randomState();

		int reach = RADIUS + JITTER + CELL / 2;
		int cellMinX = Math.floorDiv(minX - reach, CELL);
		int cellMaxX = Math.floorDiv(maxX + reach, CELL);
		int cellMinZ = Math.floorDiv(minZ - reach, CELL);
		int cellMaxZ = Math.floorDiv(maxZ + reach, CELL);

		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				placed |= placeAnchor(world, chunkGenerator, noise, seed, cellX, cellZ, minX, minZ, maxX, maxZ);
			}
		}
		return placed;
	}

	private static boolean placeAnchor(
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
		int centerX = cellX * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 3, cellX, cellZ)) * JITTER);
		int centerZ = cellZ * CELL + CELL / 2
			+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(seed + 5, cellX, cellZ)) * JITTER);

		if (centerX + RADIUS < minX || centerX - RADIUS > maxX || centerZ + RADIUS < minZ || centerZ - RADIUS > maxZ) {
			return false;
		}

		int baseY = chunkGenerator.getBaseHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, world, noise);
		if (baseY <= SEA_LEVEL + 1) {
			return false;
		}

		if (!nearMethane(chunkGenerator, noise, world, centerX, centerZ)) {
			return false;
		}
		boolean onHill = baseY >= HILL_HEIGHT;
		double chance = BASE_CHANCE + (onHill ? HILL_BONUS : 0.0);
		if (WorldgenNoise.hash(seed + 11, cellX, cellZ) > chance) {
			return false;
		}

		RandomSource rng = RandomSource.create(
			seed ^ ((long) cellX * 341873128712L) ^ ((long) cellZ * 132897987541L)
		);
		Painter painter = new Painter(world, minX, minZ, maxX, maxZ);
		buildAnchor(painter, rng, centerX, baseY, centerZ);
		return painter.placed;
	}

	/** True when the methane ocean, not a surface pond, is within {@link #OCEAN_REACH}. */
	private static boolean nearMethane(
		ChunkGenerator chunkGenerator,
		RandomState noise,
		WorldGenLevel world,
		int centerX,
		int centerZ
	) {
		for (int i = 0; i < 8; i++) {
			double angle = i * (Math.PI / 4.0);
			for (int dist = 12; dist <= OCEAN_REACH; dist += 12) {
				int x = centerX + Mth.floor(Math.cos(angle) * dist + 0.5);
				int z = centerZ + Mth.floor(Math.sin(angle) * dist + 0.5);
				int surface = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, world, noise);
				if (surface <= SEA_LEVEL) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Every random draw here happens unconditionally so the plan comes out byte-identical in
	 * each chunk that paints part of it. All bounds clamping lives inside {@link Painter}.
	 */
	private static void buildAnchor(Painter painter, RandomSource rng, int cx, int baseY, int cz) {
		int height = 56 + rng.nextInt(22);
		double spireRadius = 5.6 + rng.nextDouble() * 1.8;
		double twist = (rng.nextBoolean() ? 1.0 : -1.0) * (0.15 + rng.nextDouble() * 0.08);
		double phase = rng.nextDouble() * Mth.TWO_PI;
		boolean highCrown = rng.nextBoolean();
		boolean longArm = rng.nextFloat() < 0.7F;

		int podCount = 6 + rng.nextInt(4);
		Pod[] pods = new Pod[podCount];
		int topFloor = baseY + 10;
		for (int i = 0; i < podCount; i++) {
			double bunch = rng.nextDouble() * 0.9;
			double angle = phase + i * (Mth.TWO_PI / podCount) + (rng.nextDouble() - 0.5) * 0.55 + bunch;
			double dist = 18.0 + rng.nextDouble() * 16.0;
			if (longArm && i == 0) {
				dist = 32.0 + rng.nextDouble() * 8.0;
			}
			int floorY = baseY + 8
				+ Mth.floor((height - 22) * ((i + 0.35 + (rng.nextDouble() - 0.5) * 0.45) / podCount));
			int radius = 6 + rng.nextInt(3);
			int podHeight = 12 + rng.nextInt(6);
			boolean child = rng.nextFloat() < 0.45F;
			int px = cx + Mth.floor(Math.cos(angle) * dist + 0.5);
			int pz = cz + Mth.floor(Math.sin(angle) * dist + 0.5);
			pods[i] = new Pod(px, pz, floorY, radius, podHeight, child, rng.nextDouble() * 0.8 + 0.4);
			topFloor = Math.max(topFloor, floorY);
		}

		placeRootFlare(painter, rng, cx, baseY, cz);
		placeSpire(painter, rng, cx, baseY, cz, height, spireRadius, twist, phase);
		placeSpiralStair(painter, cx, baseY, cz, topFloor + 4, spireRadius + 4.2, phase, twist);

		for (Pod pod : pods) {
			double ang = Math.atan2(pod.z() - cz, pod.x() - cx);
			int ax = cx + Mth.floor(Math.cos(ang) * 9.0 + 0.5);
			int az = cz + Mth.floor(Math.sin(ang) * 9.0 + 0.5);
			placeWalkway(painter, ax, az, pod.floorY() - 1, pod.x(), pod.z(), pod.floorY() - 1, pod.sway(), true);
		}
		for (int i = 0; i < podCount; i++) {
			Pod a = pods[i];
			Pod b = pods[(i + 1) % podCount];
			placeWalkway(painter, a.x(), a.z(), a.floorY() - 1, b.x(), b.z(), b.floorY() - 1, 0.7 + rng.nextDouble(), true);
		}

		for (Pod pod : pods) {
			placeTeardropPod(painter, rng, pod, cx, cz, baseY, spireRadius + 4.2, phase, twist, baseY);
			if (pod.child()) {
				placeHangingChild(painter, rng, pod, cx, cz, baseY);
			}
		}

		if (highCrown) {
			placeCrown(painter, rng, cx, baseY + height, cz, phase);
		}

		int netCount = 5 + rng.nextInt(5);
		for (int i = 0; i < netCount; i++) {
			Pod pod = pods[rng.nextInt(podCount)];
			double t = 0.25 + rng.nextDouble() * 0.5;
			int[] point = sampleWalkway(cx, cz, pod, t);
			Direction side = towards(cx, cz, pod.x(), pod.z()).getClockWise();
			if ((i & 1) == 1) {
				side = side.getOpposite();
			}
			placeReachableNet(painter, point[0], point[1], point[2], side);
		}

		int lampCount = 8 + rng.nextInt(6);
		for (int i = 0; i < lampCount; i++) {
			Pod pod = pods[rng.nextInt(podCount)];
			double t = 0.15 + rng.nextDouble() * 0.7;
			int[] point = sampleWalkway(cx, cz, pod, t);
			placeHangingLantern(painter, point[0], point[1], point[2], 2 + rng.nextInt(4));
		}
	}

	/** Splayed fibre stilts that plant the colony on hills, slopes, and methane shores. */
	private static void placeRootFlare(Painter painter, RandomSource rng, int cx, int baseY, int cz) {
		int legs = 7 + rng.nextInt(3);
		double start = rng.nextDouble() * Mth.TWO_PI;
		for (int i = 0; i < legs; i++) {
			double angle = start + i * (Mth.TWO_PI / legs) + (rng.nextDouble() - 0.5) * 0.35;
			int reach = 14 + rng.nextInt(9);
			int rise = 5 + rng.nextInt(5);
			for (int step = 0; step <= reach; step++) {
				double t = step / (double) reach;
				int x = cx + Mth.floor(Math.cos(angle) * reach * t + 0.5);
				int z = cz + Mth.floor(Math.sin(angle) * reach * t + 0.5);
				int topY = baseY + Mth.floor((1.0 - t) * rise);
				int groundY = painter.surfaceAt(x, z);
				int bottom = groundY == Integer.MIN_VALUE ? topY : Math.min(groundY - 1, topY);
				for (int y = bottom; y <= topY; y++) {
					painter.blob(x, y, z, t < 0.35 ? 1.6 : 1.15);
				}
			}
		}
	}

	/**
	 * Two thick fibre strands coil up as a spring: opposite rings that orbit each other
	 * while the whole form tapers, flares, and climbs. The centre stays open.
	 */
	private static void placeSpire(
		Painter painter,
		RandomSource rng,
		int cx,
		int baseY,
		int cz,
		int height,
		double radius,
		double twist,
		double phase
	) {
		for (int h = 0; h <= height; h++) {
			int y = baseY + h;
			double t = h / (double) height;
			double envelope = 1.22 - 0.32 * t + 0.20 * Math.sin(t * Math.PI);
			double coil = 0.82 + 0.24 * Math.sin(h * 0.36);
			double r = radius * envelope * coil;
			double angle = phase + h * twist;

			strand(painter, cx, y, cz, angle, r, 1.85);
			strand(painter, cx, y, cz, angle + Math.PI, r, 1.85);
			strand(painter, cx, y, cz, angle * 1.35 + 0.4, r * 0.38, 1.05);

			if (h > 4 && h % 7 == 0) {
				orbitRings(painter, cx, y, cz, angle, r + 1.4);
			}
		}

		int tipY = baseY + height;
		orbitRings(painter, cx, tipY, cz, phase + height * twist, radius * 0.7);
		placeHangingLantern(painter, cx + 1, tipY + 1, cz, 2);
		placeHangingLantern(painter, cx - 1, tipY + 1, cz, 3 + rng.nextInt(2));
	}

	private static void strand(Painter painter, int cx, int y, int cz, double angle, double radius, double thickness) {
		painter.blob(
			cx + Mth.floor(Math.cos(angle) * radius + 0.5),
			y,
			cz + Mth.floor(Math.sin(angle) * radius + 0.5),
			thickness
		);
	}

	/** Two opposing arcs — the "orbiting rings" of the spring — drawn in the XZ plane. */
	private static void orbitRings(Painter painter, int cx, int y, int cz, double heading, double radius) {
		for (int side = 0; side < 2; side++) {
			double base = heading + side * Math.PI;
			for (double a = -1.15; a <= 1.15; a += 0.16) {
				strand(painter, cx, y, cz, base + a, radius, 1.2);
				if (Math.abs(a) < 0.7) {
					strand(painter, cx, y + 1, cz, base + a, radius * 0.92, 1.05);
				}
			}
		}
	}

	/** Walkable spiral around the spring, two blocks wide, from the stilts up to the highest pod. */
	private static void placeSpiralStair(
		Painter painter,
		int cx,
		int baseY,
		int cz,
		int topY,
		double radius,
		double phase,
		double twist
	) {
		double angle = phase + Math.PI * 0.5;
		double step = 1.2 / radius;
		for (int y = baseY + 1; y <= topY; y++) {
			for (int w = 0; w <= 1; w++) {
				double r = radius + w;
				int x = cx + Mth.floor(Math.cos(angle) * r + 0.5);
				int z = cz + Mth.floor(Math.sin(angle) * r + 0.5);
				painter.fiber(x, y, z);
				painter.carve(x, y + 1, z);
				painter.carve(x, y + 2, z);
				painter.carve(x, y + 3, z);
				painter.carve(x, y + 4, z);
			}
			double outer = radius + 1;
			int railX = cx + Mth.floor(Math.cos(angle) * (outer + 1) + 0.5);
			int railZ = cz + Mth.floor(Math.sin(angle) * (outer + 1) + 0.5);
			if ((y & 1) == 0) {
				painter.fiber(railX, y + 1, railZ);
			}
			angle += step * Math.signum(twist == 0.0 ? 1.0 : twist);
		}
	}

	/**
	 * Hollow teardrop chamber: pointed below, bulbous above, with a 2×3 doorway facing
	 * the spire, a straw bed, window-punctures, and a hanging lantern.
	 */
	private static void placeTeardropPod(
		Painter painter,
		RandomSource rng,
		Pod pod,
		int cx,
		int cz,
		int groundY,
		double spiralRadius,
		double spiralPhase,
		double spiralTwist,
		int spiralBaseY
	) {
		int px = pod.x();
		int pz = pod.z();
		int floorY = pod.floorY();
		int radius = pod.radius();
		int height = pod.height();
		int tipY = floorY - Mth.floor(height * 0.42);
		int topY = tipY + height;

		for (int dy = 0; dy <= height; dy++) {
			double r = shellRadius(dy / (double) height, radius);
			int y = tipY + dy;
			int ir = Mth.ceil(r);
			for (int dx = -ir; dx <= ir; dx++) {
				for (int dz = -ir; dz <= ir; dz++) {
					if (Math.sqrt(dx * dx + dz * dz) <= r + 0.2) {
						painter.fiber(px + dx, y, pz + dz);
					}
				}
			}
		}

		for (int dy = 0; dy <= height; dy++) {
			int y = tipY + dy;
			boolean living = y >= floorY && y <= floorY + 4;
			boolean upper = y > floorY + 4 && y < topY;
			if (!living && !upper) {
				continue;
			}
			double r = shellRadius(dy / (double) height, radius);
			double inner = Math.max(0.0, r - (living ? 1.55 : 1.35));
			if (inner < 0.8) {
				continue;
			}
			int ir = Mth.floor(inner);
			for (int dx = -ir; dx <= ir; dx++) {
				for (int dz = -ir; dz <= ir; dz++) {
					if (Math.sqrt(dx * dx + dz * dz) <= inner) {
						painter.carve(px + dx, y, pz + dz);
					}
				}
			}
		}

		double floorShell = shellRadius((floorY - tipY) / (double) height, radius);
		int floorR = Math.max(2, Mth.floor(floorShell - 1.55));
		for (int dx = -floorR; dx <= floorR; dx++) {
			for (int dz = -floorR; dz <= floorR; dz++) {
				if (dx * dx + dz * dz <= floorR * floorR) {
					painter.fiber(px + dx, floorY - 1, pz + dz);
				}
			}
		}

		Direction inward = towards(px, pz, cx, cz);
		Direction side = inward.getClockWise();
		for (int depth = Mth.floor(floorShell) - 2; depth <= Mth.ceil(floorShell) + 1; depth++) {
			if (depth < 1) {
				continue;
			}
			for (int lateral = -1; lateral <= 2; lateral++) {
				int x = px + inward.getStepX() * depth + side.getStepX() * lateral;
				int z = pz + inward.getStepZ() * depth + side.getStepZ() * lateral;
				for (int up = 0; up <= 4; up++) {
					painter.carve(x, floorY + up, z);
					painter.keep(x, floorY + up, z);
				}
			}
		}

		int windowCount = 3 + rng.nextInt(3);
		for (int i = 0; i < windowCount; i++) {
			double wa = rng.nextDouble() * Mth.TWO_PI;
			int wy = floorY + 2 + rng.nextInt(2);
			double wr = shellRadius((wy - tipY) / (double) height, radius);
			int wx = px + Mth.floor(Math.cos(wa) * wr + 0.5);
			int wz = pz + Mth.floor(Math.sin(wa) * wr + 0.5);
			painter.carve(wx, wy, wz);
		}

		placeWeaverPad(painter, px, floorY, pz, inward.getOpposite());
		painter.sealFiber(px, pz, tipY, tipY + height, radius + 2);
		BlockPos plate = placeHomeLanding(
			painter, px, pz, floorY, inward, floorShell, cx, cz, groundY, spiralRadius, spiralPhase, spiralTwist, spiralBaseY
		);
		int standX = px + side.getStepX();
		int standZ = pz + side.getStepZ();
		painter.fiber(standX, floorY - 1, standZ);
		for (int up = 0; up <= 3; up++) {
			painter.carve(standX, floorY + up, standZ);
		}
		painter.spawnResident(standX, floorY, standZ, new BlockPos(px, floorY, pz), plate);
		int ceiling = painter.findFiberSupport(px, floorY + 3, pz, 8);
		if (ceiling != Integer.MIN_VALUE) {
			placeHangingLantern(painter, px, ceiling, pz, 1);
		}
	}

	private static double shellRadius(double t, int radius) {
		t = Mth.clamp(t, 0.0, 1.0);
		double profile = Math.pow(Math.sin(t * Math.PI), 0.55) * (0.72 + 0.28 * t);
		if (t < 0.12) {
			profile = Math.max(profile, 0.16 + t / 0.12 * 0.25);
		}
		if (t > 0.88) {
			profile *= 1.0 - (t - 0.88) / 0.12 * 0.35;
		}
		return radius * Math.max(0.18, profile);
	}

	private static void placeHangingChild(Painter painter, RandomSource rng, Pod parent, int cx, int cz, int groundY) {
		double away = Math.atan2(parent.z() - cz, parent.x() - cx) + (rng.nextDouble() - 0.5) * 0.8;
		int drop = 6 + rng.nextInt(5);
		int dist = 4 + rng.nextInt(4);
		int x = parent.x() + Mth.floor(Math.cos(away) * dist + 0.5);
		int z = parent.z() + Mth.floor(Math.sin(away) * dist + 0.5);
		int attachY = parent.floorY() - 2;
		for (int y = attachY; y > attachY - drop; y--) {
			painter.blob(x, y, z, 1.1);
		}
		Pod child = new Pod(x, z, attachY - drop, 4, 9, false, 0.0);
		placeTeardropPod(painter, rng, child, parent.x(), parent.z(), groundY, 0.0, 0.0, 0.0, 0);
	}

	private static void placeCrown(Painter painter, RandomSource rng, int cx, int tipY, int cz, double phase) {
		orbitRings(painter, cx, tipY + 2, cz, phase, 4.5);
		for (int i = 0; i < 3; i++) {
			double a = phase + i * (Mth.TWO_PI / 3.0);
			int x = cx + Mth.floor(Math.cos(a) * 7.0 + 0.5);
			int z = cz + Mth.floor(Math.sin(a) * 7.0 + 0.5);
			placeWalkway(painter, cx, cz, tipY, x, z, tipY - 1, 0.4, false);
			Pod nest = new Pod(x, z, tipY - 1, 5, 10, false, 0.3);
			placeTeardropPod(painter, rng, nest, cx, cz, tipY - 1, 0.0, 0.0, 0.0, 0);
		}
	}

	private static void placeWeaverPad(Painter painter, int px, int floorY, int pz, Direction facing) {
		BlockState bed = ModBlocks.WEAVER_PAD.defaultBlockState()
			.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
		painter.set(px, floorY, pz, bed.setValue(BlockStateProperties.BED_PART, BedPart.FOOT));
		painter.set(
			px + facing.getStepX(),
			floorY,
			pz + facing.getStepZ(),
			bed.setValue(BlockStateProperties.BED_PART, BedPart.HEAD)
		);
	}

	/**
	 * A gapless two-wide floor from inside the door, through the home plate, on to the
	 * spiral (or the pod this one hangs from), plus a stair down the flank to the ground.
	 * The leap shaft is only the air above the plate, so it does not cut the floor.
	 */
	private static BlockPos placeHomeLanding(
		Painter painter,
		int px,
		int pz,
		int floorY,
		Direction inward,
		double floorShell,
		int towardX,
		int towardZ,
		int groundY,
		double spiralRadius,
		double spiralPhase,
		double spiralTwist,
		int spiralBaseY
	) {
		Direction side = inward.getClockWise();
		int plateY = floorY - 1;
		int plateDepth = Mth.ceil(floorShell) + 4;
		int plateX = px + inward.getStepX() * plateDepth;
		int plateZ = pz + inward.getStepZ() * plateDepth;

		for (int depth = 1; depth <= plateDepth; depth++) {
			int x = px + inward.getStepX() * depth;
			int z = pz + inward.getStepZ() * depth;
			layDeck(painter, x, plateY, z, side);
		}
		painter.reserveColumn(plateX, plateZ, plateY);

		int endX;
		int endZ;
		int endY = plateY;
		if (spiralRadius > 1.0) {
			int climbed = Math.max(0, floorY - (spiralBaseY + 1));
			double angle = spiralPhase + Math.PI * 0.5
				+ climbed * (1.2 / spiralRadius) * Math.signum(spiralTwist == 0.0 ? 1.0 : spiralTwist);
			double ring = spiralRadius + 1.0;
			endX = towardX + Mth.floor(Math.cos(angle) * ring + 0.5);
			endZ = towardZ + Mth.floor(Math.sin(angle) * ring + 0.5);
			endY = floorY;
		} else {
			endX = towardX;
			endZ = towardZ;
			endY = groundY;
		}
		layCourse(painter, plateX, plateZ, plateY, endX, endZ, endY, side, spiralRadius > 1.0 ? 1 : 2);

		int stairX = plateX;
		int stairZ = plateZ;
		int stairY = plateY;
		for (int step = 0; stairY > groundY && step < 80; step++) {
			stairX += side.getStepX();
			stairZ += side.getStepZ();
			stairY--;
			layDeck(painter, stairX, stairY, stairZ, inward);
		}
		painter.force(plateX, plateY, plateZ, ModBlocks.THOLIN_FIBER_HOME_PLATE.defaultBlockState());
		return new BlockPos(plateX, plateY, plateZ);
	}

	/** Two-wide tread with five blocks of headroom, so a step always touches the last one. */
	private static void layDeck(Painter painter, int x, int y, int z, Direction width) {
		for (int lateral = 0; lateral <= 1; lateral++) {
			int wx = x + width.getStepX() * lateral;
			int wz = z + width.getStepZ() * lateral;
			painter.fiber(wx, y, wz);
			painter.keep(wx, y, wz);
			for (int up = 1; up <= 5; up++) {
				painter.carve(wx, y + up, wz);
			}
		}
	}

	/**
	 * Face-adjacent steps only. Stops short of a spiral core so the path ends on the
	 * outer ring instead of inside the coil.
	 */
	private static void layCourse(
		Painter painter,
		int x0,
		int z0,
		int y0,
		int x1,
		int z1,
		int y1,
		Direction width,
		int stopDistance
	) {
		int x = x0;
		int z = z0;
		int y = y0;
		layDeck(painter, x, y, z, width);
		for (int guard = 0; guard < 160; guard++) {
			int dx = x1 - x;
			int dz = z1 - z;
			if (dx * dx + dz * dz <= stopDistance * stopDistance && y == y1) {
				return;
			}
			if (dx != 0) {
				x += Integer.signum(dx);
			} else if (dz != 0) {
				z += Integer.signum(dz);
			}
			if (y < y1) {
				y++;
			} else if (y > y1) {
				y--;
			}
			layDeck(painter, x, y, z, width);
			if (x == x1 && z == z1 && y == y1) {
				return;
			}
		}
	}

	/** Sagging, slightly bowed 2-wide fibre walkway with rail posts. */
	private static void placeWalkway(
		Painter painter,
		int x0,
		int z0,
		int y0,
		int x1,
		int z1,
		int y1,
		double sway,
		boolean railed
	) {
		int dx = x1 - x0;
		int dz = z1 - z0;
		int dy = y1 - y0;
		double horiz = Math.sqrt(dx * dx + dz * dz);
		if (horiz < 1.5) {
			return;
		}
		int steps = Math.max(Mth.ceil(horiz), Math.abs(dy) * 2);
		double nx = dx / horiz;
		double nz = dz / horiz;
		double px = -nz;
		double pz = nx;
		double sag = Math.min(7.0, horiz * 0.16);
		double bow = sway * Math.min(5.5, horiz * 0.12);

		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			double dip = Math.sin(t * Math.PI) * sag;
			double curve = Math.sin(t * Math.PI) * bow;
			int x = x0 + Mth.floor(dx * t + px * curve + 0.5);
			int z = z0 + Mth.floor(dz * t + pz * curve + 0.5);
			int y = y0 + Mth.floor(dy * t + 0.5) - Mth.floor(dip);
			for (int w = 0; w <= 1; w++) {
				int wx = x + Mth.floor(px * w + 0.25);
				int wz = z + Mth.floor(pz * w + 0.25);
				painter.fiber(wx, y, wz);
				painter.carve(wx, y + 1, wz);
				painter.carve(wx, y + 2, wz);
				painter.carve(wx, y + 3, wz);
			}
			if (railed && (i & 1) == 0) {
				int rx = x + Mth.floor(px * 2.0 + 0.5);
				int rz = z + Mth.floor(pz * 2.0 + 0.5);
				painter.fiber(rx, y + 1, rz);
				int lx = x - Mth.floor(px + 0.5);
				int lz = z - Mth.floor(pz + 0.5);
				painter.fiber(lx, y + 1, lz);
			}
		}
	}

	/**
	 * A net hung just off a deck, with three blocks of clear air on that deck.
	 * A Weaver stands on the deck and reaches the weave; the weave is not the floor.
	 */
	private static void placeReachableNet(Painter painter, int x, int floorY, int z, Direction side) {
		painter.fiber(x, floorY, z);
		for (int up = 1; up <= 3; up++) {
			painter.carve(x, floorY + up, z);
		}
		int hx = x + side.getStepX();
		int hz = z + side.getStepZ();
		painter.fiber(hx, floorY + 3, hz);
		painter.net(hx, floorY + 2, hz);
		painter.net(hx, floorY + 1, hz);
	}

	private static void placeHangingLantern(Painter painter, int x, int supportY, int z, int chainLength) {
		int attachY = painter.findFiberSupport(x, supportY, z, 3);
		if (attachY == Integer.MIN_VALUE) {
			return;
		}
		int y = attachY - 1;
		for (int i = 0; i < chainLength; i++, y--) {
			painter.set(x, y, z, Blocks.IRON_CHAIN.defaultBlockState());
		}
		painter.set(x, y, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
	}

	/** Point along the same sagging walkway the pod uses, so hung things actually attach. */
	private static int[] sampleWalkway(int cx, int cz, Pod pod, double t) {
		double ang = Math.atan2(pod.z() - cz, pod.x() - cx);
		int x0 = cx + Mth.floor(Math.cos(ang) * 9.0 + 0.5);
		int z0 = cz + Mth.floor(Math.sin(ang) * 9.0 + 0.5);
		int y0 = pod.floorY() - 1;
		int x1 = pod.x();
		int z1 = pod.z();
		int y1 = pod.floorY() - 1;
		int dx = x1 - x0;
		int dz = z1 - z0;
		int dy = y1 - y0;
		double horiz = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
		double px = -dz / horiz;
		double pz = dx / horiz;
		double sag = Math.min(7.0, horiz * 0.16);
		double bow = pod.sway() * Math.min(5.5, horiz * 0.12);
		double dip = Math.sin(t * Math.PI) * sag;
		double curve = Math.sin(t * Math.PI) * bow;
		return new int[] {
			x0 + Mth.floor(dx * t + px * curve + 0.5),
			y0 + Mth.floor(dy * t + 0.5) - Mth.floor(dip),
			z0 + Mth.floor(dz * t + pz * curve + 0.5)
		};
	}

	private static Direction towards(int fromX, int fromZ, int toX, int toZ) {
		int dx = toX - fromX;
		int dz = toZ - fromZ;
		if (Math.abs(dx) >= Math.abs(dz)) {
			return dx >= 0 ? Direction.EAST : Direction.WEST;
		}
		return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
	}

	private record Pod(int x, int z, int floorY, int radius, int height, boolean child, double sway) {}

	/** Clamps every write to the chunk currently being generated. */
	private static final class Painter {
		private final WorldGenLevel world;
		private final int minX;
		private final int minZ;
		private final int maxX;
		private final int maxZ;
		private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		private final List<Shaft> shafts = new ArrayList<>();
		/** Door decks and plates. Later stairs and shells must not erase them. */
		private final Set<Long> kept = new HashSet<>();
		private boolean placed;

		private Painter(WorldGenLevel world, int minX, int minZ, int maxX, int maxZ) {
			this.world = world;
			this.minX = minX;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxZ = maxZ;
		}

		private boolean inChunk(int x, int y, int z) {
			return x >= minX && x <= maxX && z >= minZ && z <= maxZ
				&& y > world.getMinY() && y < world.getMinY() + world.getHeight();
		}

		int surfaceAt(int x, int z) {
			if (x < minX || x > maxX || z < minZ || z > maxZ) {
				return Integer.MIN_VALUE;
			}
			return world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		}

		void keep(int x, int y, int z) {
			kept.add(BlockPos.asLong(x, y, z));
		}

		/** Pod shell, floor, and bed. Later walkway carves cannot open these cells. */
		void sealFiber(int cx, int cz, int minY, int maxY, int radius) {
			for (int y = minY; y <= maxY; y++) {
				for (int dx = -radius; dx <= radius; dx++) {
					for (int dz = -radius; dz <= radius; dz++) {
						int x = cx + dx;
						int z = cz + dz;
						if (!inChunk(x, y, z)) {
							continue;
						}
						cursor.set(x, y, z);
						BlockState state = world.getBlockState(cursor);
						if (state.is(ModBlocks.THOLIN_FIBER) || state.is(ModBlocks.WEAVER_PAD) || state.is(Blocks.STRAW_BED)) {
							keep(x, y, z);
						}
					}
				}
			}
		}

		void set(int x, int y, int z, BlockState state) {
			if (kept.contains(BlockPos.asLong(x, y, z))) {
				return;
			}
			if (!state.isAir() && columnBlocks(x, y, z)) {
				return;
			}
			write(x, y, z, state);
		}

		void force(int x, int y, int z, BlockState state) {
			write(x, y, z, state);
			keep(x, y, z);
		}

		/**
		 * A 5×5 shaft from just above the plate to past the leap apex.
		 * Later fibre cannot refill it, and the plate itself is not part of the shaft.
		 */
		void reserveColumn(int cx, int cz, int plateY) {
			int yMin = plateY + 1;
			int yMax = Math.min(world.getMinY() + world.getHeight() - 1, plateY + 42);
			shafts.add(new Shaft(cx - 2, cz - 2, cx + 2, cz + 2, yMin, yMax));
			for (int y = yMin; y <= yMax; y++) {
				for (int dx = -2; dx <= 2; dx++) {
					for (int dz = -2; dz <= 2; dz++) {
						carve(cx + dx, y, cz + dz);
					}
				}
			}
		}

		private boolean columnBlocks(int x, int y, int z) {
			for (Shaft shaft : shafts) {
				if (shaft.blocks(x, y, z)) {
					return true;
				}
			}
			return false;
		}

		private void write(int x, int y, int z, BlockState state) {
			if (!inChunk(x, y, z)) {
				return;
			}
			cursor.set(x, y, z);
			world.setBlock(cursor, state, 2);
			placed = true;
		}

		void fiber(int x, int y, int z) {
			set(x, y, z, ModBlocks.THOLIN_FIBER.defaultBlockState());
		}

		int findFiberSupport(int x, int startY, int z, int searchUp) {
			for (int dy = 0; dy <= searchUp; dy++) {
				int y = startY + dy;
				if (!inChunk(x, y, z)) {
					continue;
				}
				cursor.set(x, y, z);
				if (world.getBlockState(cursor).is(ModBlocks.THOLIN_FIBER)) {
					return y;
				}
			}
			return Integer.MIN_VALUE;
		}

		void spawnResident(int x, int y, int z, BlockPos bed, BlockPos plate) {
			if (!inChunk(x, y, z)) {
				return;
			}
			WeaverEntity weaver = ModEntities.WEAVER.create(world.getLevel(), EntitySpawnReason.STRUCTURE);
			if (weaver == null) {
				return;
			}
			weaver.snapTo(x + 0.5, y, z + 0.5, 0.0F, 0.0F);
			weaver.setPersistenceRequired();
			weaver.assignGeneratedHome(bed, plate);
			claimPad(bed, weaver);
			world.addFreshEntity(weaver);
		}

		void claimPad(BlockPos bed, WeaverEntity weaver) {
			if (!inChunk(bed.getX(), bed.getY(), bed.getZ())) {
				return;
			}
			WeaverPadBlock.claimInPlace(world, bed, weaver.getUUID());
			BlockState state = world.getBlockState(bed);
			if (!(state.getBlock() instanceof AbstractBedBlock)) {
				return;
			}
			BlockPos other = bed.relative(AbstractBedBlock.getConnectedDirection(state));
			if (inChunk(other.getX(), other.getY(), other.getZ())) {
				WeaverPadBlock.claimInPlace(world, other, weaver.getUUID());
			}
		}

		void blob(int x, int y, int z, double radius) {
			int ir = Mth.ceil(radius);
			for (int dx = -ir; dx <= ir; dx++) {
				for (int dz = -ir; dz <= ir; dz++) {
					if (dx * dx + dz * dz <= radius * radius + 0.25) {
						fiber(x + dx, y, z + dz);
					}
				}
			}
		}

		void net(int x, int y, int z) {
			set(x, y, z, ModBlocks.WEAVER_NET.defaultBlockState());
		}

		void carve(int x, int y, int z) {
			set(x, y, z, Blocks.AIR.defaultBlockState());
		}

		private record Shaft(int minX, int minZ, int maxX, int maxZ, int yMin, int yMax) {
			boolean blocks(int x, int y, int z) {
				return y >= yMin && y <= yMax && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
			}
		}
	}
}
