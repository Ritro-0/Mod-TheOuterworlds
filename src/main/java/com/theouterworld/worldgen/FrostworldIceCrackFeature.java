package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Crevasses split through Frostworld's ice crust into the subsurface ocean. A wandering main
 * fissure tapers shut at both tips and breaks through to the water along its middle; shallower
 * side cracks branch off it. Walls are jagged, narrow with depth, and lean as they descend.
 * Every crack is rebuilt from the cell seed, so neighbouring chunks carve the same shape.
 */
public class FrostworldIceCrackFeature implements Feature {
	public static final MapCodec<FrostworldIceCrackFeature> CODEC = MapCodec.unit(FrostworldIceCrackFeature::new);

	private static final int CELL = 110;
	private static final int JITTER = 22;
	/** How far a crack can reach outside its own cell: half the longest fissure plus wall drift. */
	private static final int MAX_EXTENT = 36;
	private static final double SPAWN_CHANCE = 0.22;
	private static final int OCEAN_TOP_Y = 91;
	/** Through-cuts stop this far under the waterline so the opening is always flooded. */
	private static final int BREAKTHROUGH_Y = OCEAN_TOP_Y - 4;
	private static final int MAIN_SEGMENTS = 10;
	private static final int BRANCH_SEGMENTS = 4;

	public FrostworldIceCrackFeature() {
	}

	@Override
	public MapCodec<FrostworldIceCrackFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		long seed = world.getSeed() + 44027L;
		int cellMinX = Math.floorDiv(minX - MAX_EXTENT, CELL);
		int cellMaxX = Math.floorDiv(minX + 15 + MAX_EXTENT, CELL);
		int cellMinZ = Math.floorDiv(minZ - MAX_EXTENT, CELL);
		int cellMaxZ = Math.floorDiv(minZ + 15 + MAX_EXTENT, CELL);
		boolean placed = false;
		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (WorldgenNoise.hash(seed + 7, cellX, cellZ) > SPAWN_CHANCE) {
					continue;
				}
				long crackSeed = seed ^ ((long) cellX * 73428767L) ^ ((long) cellZ * 912931L);
				for (Crack crack : buildCracks(crackSeed, cellX, cellZ)) {
					if (crack.carve(world, minX, minZ, minX + 15, minZ + 15)) {
						placed = true;
					}
				}
			}
		}
		return placed;
	}

	private static List<Crack> buildCracks(long seed, int cellX, int cellZ) {
		double centerX = cellX * CELL + CELL / 2.0 + WorldgenNoise.signed(WorldgenNoise.hash(seed + 59, cellX, cellZ)) * JITTER;
		double centerZ = cellZ * CELL + CELL / 2.0 + WorldgenNoise.signed(WorldgenNoise.hash(seed + 71, cellX, cellZ)) * JITTER;
		double heading = WorldgenNoise.hash(seed + 83, cellX, cellZ) * Math.PI * 2.0;
		double length = 36.0 + WorldgenNoise.hash(seed + 97, cellX, cellZ) * 30.0;

		// Walk out from the centre both ways so the widest, deepest stretch sits mid-crack.
		int half = MAIN_SEGMENTS / 2;
		double step = length / MAIN_SEGMENTS;
		double[] xs = new double[MAIN_SEGMENTS + 1];
		double[] zs = new double[MAIN_SEGMENTS + 1];
		xs[half] = centerX;
		zs[half] = centerZ;
		for (int dir = -1; dir <= 1; dir += 2) {
			double angle = dir > 0 ? heading : heading + Math.PI;
			for (int i = 1; i <= half; i++) {
				int from = half + dir * (i - 1);
				int to = half + dir * i;
				angle += WorldgenNoise.signed(WorldgenNoise.hash(seed + 131 + dir * 7, i, cellX ^ cellZ)) * 0.5;
				xs[to] = xs[from] + Math.cos(angle) * step;
				zs[to] = zs[from] + Math.sin(angle) * step;
			}
		}

		List<Crack> cracks = new ArrayList<>();
		double mainWidth = 2.6 + WorldgenNoise.hash(seed + 149, cellX, cellZ) * 2.2;
		double mainLean = WorldgenNoise.signed(WorldgenNoise.hash(seed + 151, cellX, cellZ)) * 5.0;
		cracks.add(new Crack(seed + 1000, xs, zs, mainWidth, 1.0, mainLean, false));

		int branches = 1 + (int) (WorldgenNoise.hash(seed + 163, cellX, cellZ) * 3.0);
		for (int b = 0; b < branches; b++) {
			int at = 2 + (int) (WorldgenNoise.hash(seed + 171 + b, cellX, cellZ) * (MAIN_SEGMENTS - 3));
			double tangent = Math.atan2(zs[at + 1] - zs[at - 1], xs[at + 1] - xs[at - 1]);
			double side = WorldgenNoise.hash(seed + 181 + b, cellX, cellZ) < 0.5 ? -1.0 : 1.0;
			double angle = tangent + side * (0.55 + WorldgenNoise.hash(seed + 191 + b, cellX, cellZ) * 0.6);
			double branchLength = 10.0 + WorldgenNoise.hash(seed + 199 + b, cellX, cellZ) * 16.0;
			double branchStep = branchLength / BRANCH_SEGMENTS;
			double[] bx = new double[BRANCH_SEGMENTS + 1];
			double[] bz = new double[BRANCH_SEGMENTS + 1];
			bx[0] = xs[at];
			bz[0] = zs[at];
			for (int i = 1; i <= BRANCH_SEGMENTS; i++) {
				angle += WorldgenNoise.signed(WorldgenNoise.hash(seed + 211 + b * 13, i, cellX ^ cellZ)) * 0.45;
				bx[i] = bx[i - 1] + Math.cos(angle) * branchStep;
				bz[i] = bz[i - 1] + Math.sin(angle) * branchStep;
			}
			double width = mainWidth * (0.4 + WorldgenNoise.hash(seed + 223 + b, cellX, cellZ) * 0.25);
			double reach = 0.25 + WorldgenNoise.hash(seed + 227 + b, cellX, cellZ) * 0.5;
			double lean = WorldgenNoise.signed(WorldgenNoise.hash(seed + 229 + b, cellX, cellZ)) * 3.0;
			cracks.add(new Crack(seed + 2000 + b * 97L, bx, bz, width, reach, lean, true));
		}
		return cracks;
	}

	/** One fissure along a polyline. Branches start full width at the junction and pinch out at their tip. */
	private static final class Crack {
		private final long seed;
		private final double[] xs;
		private final double[] zs;
		private final double[] arcStart;
		private final double totalLength;
		private final double halfWidth;
		/** Fraction of the crust this crack cuts through at its widest; 1 breaks into the ocean. */
		private final double reach;
		/** Sideways drift of the crack's centre from the surface to its floor. */
		private final double lean;
		private final boolean branch;

		Crack(long seed, double[] xs, double[] zs, double halfWidth, double reach, double lean, boolean branch) {
			this.seed = seed;
			this.xs = xs;
			this.zs = zs;
			this.halfWidth = halfWidth;
			this.reach = reach;
			this.lean = lean;
			this.branch = branch;
			this.arcStart = new double[xs.length];
			double arc = 0.0;
			for (int i = 0; i < xs.length - 1; i++) {
				this.arcStart[i] = arc;
				arc += Math.hypot(xs[i + 1] - xs[i], zs[i + 1] - zs[i]);
			}
			this.arcStart[xs.length - 1] = arc;
			this.totalLength = arc;
		}

		boolean carve(WorldGenLevel world, int minX, int minZ, int maxX, int maxZ) {
			double pad = this.halfWidth + Math.abs(this.lean) + 3.0;
			double boxMinX = Double.MAX_VALUE;
			double boxMaxX = -Double.MAX_VALUE;
			double boxMinZ = Double.MAX_VALUE;
			double boxMaxZ = -Double.MAX_VALUE;
			for (int i = 0; i < this.xs.length; i++) {
				boxMinX = Math.min(boxMinX, this.xs[i]);
				boxMaxX = Math.max(boxMaxX, this.xs[i]);
				boxMinZ = Math.min(boxMinZ, this.zs[i]);
				boxMaxZ = Math.max(boxMaxZ, this.zs[i]);
			}
			int x0 = Math.max(minX, Mth.floor(boxMinX - pad));
			int x1 = Math.min(maxX, Mth.ceil(boxMaxX + pad));
			int z0 = Math.max(minZ, Mth.floor(boxMinZ - pad));
			int z1 = Math.min(maxZ, Mth.ceil(boxMaxZ + pad));
			if (x0 > x1 || z0 > z1) {
				return false;
			}

			boolean placed = false;
			BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
			double[] nearest = new double[2];
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					nearest(x + 0.5, z + 0.5, nearest);
					double offset = nearest[0];
					double arc = nearest[1];
					double s = Mth.clamp(arc / this.totalLength, 0.0, 1.0);
					double taper = this.branch ? Math.pow(1.0 - s, 0.7) : Math.pow(Math.sin(Math.PI * s), 0.55);
					if (taper <= 0.02 || Math.abs(offset) > pad) {
						continue;
					}
					double widthNoise = 0.6 + WorldgenNoise.valueNoise(this.seed + 3, arc * 0.11, 0.0) * 0.8;
					double surfaceHalf = this.halfWidth * taper * widthNoise;

					int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
					if (top <= OCEAN_TOP_Y + 4) {
						continue;
					}
					double depthShare = Mth.clamp(taper * this.reach * 1.3, 0.0, 1.0);
					double bottom = depthShare >= 1.0
						? BREAKTHROUGH_Y
						: top - (top - BREAKTHROUGH_Y) * depthShare * (0.75 + WorldgenNoise.valueNoise(this.seed + 5, arc * 0.2, 0.0) * 0.25);
					double span = Math.max(1.0, top - bottom);

					for (int y = top; y >= bottom; y--) {
						double frac = (top - y) / span;
						double halfAtDepth = surfaceHalf * (1.0 - 0.6 * frac);
						if (depthShare >= 1.0) {
							// Wide enough that wall grain can never pinch the through-cut shut.
							halfAtDepth = Math.max(halfAtDepth, 1.4);
						}
						double centre = this.lean * frac
							+ WorldgenNoise.signed(WorldgenNoise.valueNoise(this.seed + 7, arc * 0.18, y * 0.16)) * 1.3;
						double rel = offset - centre;
						// Each wall gets its own grain so the two faces never mirror.
						double grain = rel >= 0.0
							? WorldgenNoise.valueNoise(this.seed + 11, arc * 0.45, y * 0.4)
							: WorldgenNoise.valueNoise(this.seed + 13, arc * 0.45, y * 0.4);
						if (Math.abs(rel) > halfAtDepth + (grain - 0.5) * 1.1) {
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

		/** Signed perpendicular distance to the polyline and arc length at the closest point. */
		private void nearest(double px, double pz, double[] out) {
			double bestDist = Double.MAX_VALUE;
			for (int i = 0; i < this.xs.length - 1; i++) {
				double ax = this.xs[i];
				double az = this.zs[i];
				double dx = this.xs[i + 1] - ax;
				double dz = this.zs[i + 1] - az;
				double lenSq = dx * dx + dz * dz;
				double t = lenSq < 1.0E-6 ? 0.0 : Mth.clamp(((px - ax) * dx + (pz - az) * dz) / lenSq, 0.0, 1.0);
				double qx = px - (ax + dx * t);
				double qz = pz - (az + dz * t);
				double dist = qx * qx + qz * qz;
				if (dist < bestDist) {
					bestDist = dist;
					double cross = dx * qz - dz * qx;
					out[0] = Math.copySign(Math.sqrt(dist), cross);
					out[1] = this.arcStart[i] + Math.sqrt(lenSq) * t;
				}
			}
		}
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
