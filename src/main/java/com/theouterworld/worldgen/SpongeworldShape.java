package com.theouterworld.worldgen;

import java.util.ArrayList;
import java.util.List;

/**
 * Hyperion / Spongeworld geometry.
 * <p>
 * One coherent ovular icy body, densely pocked with overlapping rounded cavities
 * of many scales (intentionally trypophobia-inducing) plus a few giant impact bowls.
 * Pits are precomputed and spatially hashed so chunk gen stays cheap.
 */
public final class SpongeworldShape {
	public static final int CENTER_Y = 128;
	public static final double RADIUS_X = 200.0;
	public static final double RADIUS_Y = 128.0;
	public static final double RADIUS_Z = 168.0;
	public static final double BOUND_RADIUS = 220.0;
	public static final int BOUND_PAD = 24;

	private static final int HASH_SHIFT = 5; // 32-block cells
	private static final int HASH_OFFSET = 256;
	private static final int HASH_AXIS = 16; // 512 / 32
	private static final int HASH_MASK = HASH_AXIS - 1;

	private static final Crater[] CRATERS = {
		// Open-floor giants: bowls that dump into the interior.
		new Crater(80, 180, -40, 0.50, 0.70, -0.40, 72, 95, 1.35, true),
		new Crater(-90, 90, 70, -0.60, -0.25, 0.70, 80, 110, 1.40, true),
		new Crater(30, 55, 110, 0.20, -0.85, 0.45, 64, 88, 1.25, true),
		// Shallower surface bowls that still read as craters.
		new Crater(-40, 200, 20, -0.20, 0.95, 0.15, 55, 34, 1.05, false),
		new Crater(140, 130, -70, 0.80, 0.10, -0.55, 48, 30, 1.00, false)
	};

	private static final Pit[] PITS;
	private static final int[][] CELL_PITS;

	static {
		List<Pit> built = new ArrayList<>(700);
		// Dense rounded scoops — trypophobia crust, mostly shallow.
		addSurfacePits(built, 70, 16.0, 34.0, 0.55, 0.95, 0.82, 1.00, 1101L, Kind.SCOOP);
		addSurfacePits(built, 140, 8.0, 17.0, 0.50, 0.90, 0.82, 1.00, 2202L, Kind.SCOOP);
		addSurfacePits(built, 240, 3.8, 8.5, 0.45, 0.85, 0.84, 1.00, 3303L, Kind.SCOOP);
		// Deep penetrators: same oval language, centered inward so floors vanish.
		addDeepPits(built, 55, 22.0, 48.0, 0.50, 0.95, 5505L);
		addInteriorPits(built, 80, 18.0, 42.0, 0.55, 1.05, 4404L);
		addThroughHoles(built, 10, 6606L);
		addCollapses(built, 14, 7707L);
		PITS = built.toArray(Pit[]::new);
		CELL_PITS = indexPits(PITS);
	}

	private SpongeworldShape() {
	}

	public static boolean chunkMayIntersect(int chunkMinX, int chunkMinZ) {
		double maxR = BOUND_RADIUS + BOUND_PAD;
		int chunkMaxX = chunkMinX + 15;
		int chunkMaxZ = chunkMinZ + 15;
		if (chunkMaxX < -maxR || chunkMinX > maxR) {
			return false;
		}
		return !(chunkMaxZ < -maxR || chunkMinZ > maxR);
	}

	public static boolean columnMayIntersect(int x, int z) {
		double nx = x / (RADIUS_X + 28.0);
		double nz = z / (RADIUS_Z + 28.0);
		return nx * nx + nz * nz < 1.15;
	}

	public static int yStart() {
		return CENTER_Y - (int) RADIUS_Y - 48;
	}

	public static int yEnd() {
		return CENTER_Y + (int) RADIUS_Y + 48;
	}

	/** @return density &gt; 0 means solid ice. */
	public static double density(int x, int y, int z) {
		return evaluate(x, y, z).density;
	}

	public static Eval evaluate(int x, int y, int z) {
		double shell = bodyField(x, y, z);
		if (shell < -0.28) {
			return Eval.EMPTY;
		}
		double scoop = 0.0;
		double voids = 0.0;
		double crater = craterCarve(x, y, z);
		if (crater >= 0.0) {
			scoop = crater;
		} else {
			voids = -crater;
		}
		int[] nearby = CELL_PITS[cellIndex(x, y, z)];
		if (nearby != null) {
			for (int id : nearby) {
				Pit pit = PITS[id];
				double ox = (x - pit.x) / pit.rx;
				double oy = (y - pit.y) / pit.ry;
				double oz = (z - pit.z) / pit.rz;
				double r2 = ox * ox + oy * oy + oz * oz;
				if (r2 >= 1.0) {
					continue;
				}
				double bowl = 1.0 - r2;
				double hit = bowl * bowl * pit.strength;
				if (pit.kind == Kind.SCOOP) {
					if (hit > scoop) {
						scoop = hit;
					}
				} else {
					// Soft-OR: overlapping ovals merge and eat shared walls.
					voids += hit * (1.0 - Math.min(1.0, voids));
				}
			}
		}
		double carve = Math.max(scoop, voids);
		return new Eval(shell - carve, 0.0);
	}

	public static boolean isSolid(int x, int y, int z) {
		return evaluate(x, y, z).density > 0.0;
	}

	public static double craterFloorFactor(int x, int y, int z) {
		return evaluate(x, y, z).floor;
	}

	public static boolean isCarbonicDeposit(int x, int y, int z) {
		if (!isOuterCrust(x, y, z)) {
			return false;
		}
		double n = valueNoise(x * 0.0065, y * 0.007, z * 0.0065, 4401);
		double m = valueNoise(x * 0.018, y * 0.016, z * 0.017, 4402);
		return n > 0.42 && n < 0.58 && m > 0.15;
	}

	/** Tholin as irregular patches on the remaining outer crust only. */
	public static boolean isOuterTholinPatch(int x, int y, int z) {
		if (!isOuterCrust(x, y, z)) {
			return false;
		}
		double n = valueNoise(x * 0.012, y * 0.011, z * 0.012, 8801);
		double m = valueNoise(x * 0.028, y * 0.026, z * 0.027, 8802);
		return n > 0.08 && n < 0.62 && m > 0.12;
	}

	private static boolean isOuterCrust(int x, int y, int z) {
		double body = bodyField(x, y, z);
		return body > 0.02 && body < 0.20;
	}

	private static double bodyField(int x, int y, int z) {
		double nx = x / RADIUS_X;
		double ny = (y - CENTER_Y) / RADIUS_Y;
		double nz = z / RADIUS_Z;
		double r = Math.sqrt(nx * nx + ny * ny + nz * nz);
		double warp = valueNoise(x * 0.0048, y * 0.0052, z * 0.0049, 101) * 0.10
			+ valueNoise(x * 0.011, y * 0.01, z * 0.012, 202) * 0.045;
		return 1.0 + warp - r;
	}

	/**
	 * Positive = shallow scoop. Negative magnitude = open void that should soft-OR
	 * with interior chambers (crater floor missing).
	 */
	private static double craterCarve(int x, int y, int z) {
		double scoop = 0.0;
		double open = 0.0;
		for (Crater c : CRATERS) {
			double[] n = c.normalizedNormal();
			double rx = x - c.x;
			double ry = y - c.y;
			double rz = z - c.z;
			double along = rx * n[0] + ry * n[1] + rz * n[2];
			if (along > c.radius * 0.12) {
				continue;
			}
			double px = rx - along * n[0];
			double py = ry - along * n[1];
			double pz = rz - along * n[2];
			double radial = Math.sqrt(px * px + py * py + pz * pz) / c.radius;
			double rimLimit = 1.0 + valueNoise(x * 0.035, y * 0.035, z * 0.035, c.seed()) * 0.16;
			if (radial > rimLimit) {
				continue;
			}
			double bowl = 1.0 - (radial / rimLimit);
			bowl = bowl * bowl;
			double depthAlong = Math.max(0.0, -along / c.depth);
			if (!c.openFloor && depthAlong > 1.15) {
				continue;
			}
			double falloff = c.openFloor ? 1.0 : (1.0 - depthAlong * 0.35);
			double excavation = bowl * falloff * c.strength;
			if (!c.openFloor && radial > 0.78 && radial < 1.05 && along > -c.depth * 0.25 && along < c.depth * 0.15) {
				excavation -= 0.40 * (1.0 - Math.abs(radial - 0.9) / 0.15) * c.strength;
			}
			if (c.openFloor) {
				if (excavation > open) {
					open = excavation;
				}
			} else if (excavation > scoop) {
				scoop = excavation;
			}
		}
		return open > scoop ? -open : scoop;
	}

	private static void addSurfacePits(
		List<Pit> pits,
		int count,
		double minR,
		double maxR,
		double minElong,
		double maxElong,
		double minRadial,
		double maxRadial,
		long seed,
		Kind kind
	) {
		for (int i = 0; i < count; i++) {
			double[] p = onEllipsoid(seed + i, minRadial, maxRadial);
			pits.add(makePit(p[0], p[1], p[2], minR, maxR, minElong, maxElong, seed + i, kind, 0.70, 1.05));
		}
	}

	private static void addDeepPits(List<Pit> pits, int count, double minR, double maxR, double minElong, double maxElong, long seed) {
		for (int i = 0; i < count; i++) {
			// Sit inward of the crust so the oval eats the crater floor and the chamber behind it.
			double[] p = onEllipsoid(seed + i, 0.38, 0.72);
			pits.add(makePit(p[0], p[1], p[2], minR, maxR, minElong, maxElong, seed + i, Kind.DEEP, 1.05, 1.45));
		}
	}

	private static void addInteriorPits(
		List<Pit> pits,
		int count,
		double minR,
		double maxR,
		double minElong,
		double maxElong,
		long seed
	) {
		int added = 0;
		int guard = 0;
		while (added < count && guard < count * 8) {
			guard++;
			double nx = hash01(seed + guard * 11L) * 2.0 - 1.0;
			double ny = hash01(seed + guard * 13L + 1) * 2.0 - 1.0;
			double nz = hash01(seed + guard * 17L + 2) * 2.0 - 1.0;
			if (nx * nx + ny * ny + nz * nz > 0.50) {
				continue;
			}
			double px = nx * RADIUS_X;
			double py = CENTER_Y + ny * RADIUS_Y;
			double pz = nz * RADIUS_Z;
			pits.add(makePit(px, py, pz, minR, maxR, minElong, maxElong, seed + guard, Kind.DEEP, 1.00, 1.35));
			added++;
		}
	}

	private static void addThroughHoles(List<Pit> pits, int count, long seed) {
		for (int i = 0; i < count; i++) {
			double ox = (hash01(seed + i * 19L) - 0.5) * RADIUS_X * 0.35;
			double oy = CENTER_Y + (hash01(seed + i * 23L + 1) - 0.5) * RADIUS_Y * 0.35;
			double oz = (hash01(seed + i * 29L + 2) - 0.5) * RADIUS_Z * 0.35;
			double tube = 12.0 + hash01(seed + i * 31L + 3) * 16.0;
			double elong = 0.75 + hash01(seed + i * 37L + 4) * 0.35;
			int axis = (int) (hash01(seed + i * 41L + 5) * 3.0);
			double rx;
			double ry;
			double rz;
			if (axis == 0) {
				rx = RADIUS_X + 36.0;
				ry = tube;
				rz = tube * elong;
			} else if (axis == 1) {
				rx = tube * elong;
				ry = RADIUS_Y + 36.0;
				rz = tube;
			} else {
				rx = tube;
				ry = tube * elong;
				rz = RADIUS_Z + 36.0;
			}
			pits.add(new Pit(ox, oy, oz, rx, ry, rz, 1.25, Kind.THROUGH));
		}
	}

	private static void addCollapses(List<Pit> pits, int count, long seed) {
		for (int i = 0; i < count; i++) {
			double[] p = onEllipsoid(seed + i, 0.92, 1.06);
			double r = 28.0 + hash01(seed + i * 53L) * 32.0;
			pits.add(makePit(p[0], p[1], p[2], r, r + 12.0, 0.65, 0.95, seed + i, Kind.COLLAPSE, 1.15, 1.45));
		}
	}

	private static double[] onEllipsoid(long seed, double minRadial, double maxRadial) {
		double u = hash01(seed * 17L);
		double v = hash01(seed * 31L + 3);
		double theta = u * Math.PI * 2.0;
		double phi = Math.acos(2.0 * v - 1.0);
		double radial = minRadial + hash01(seed * 43L + 9) * (maxRadial - minRadial);
		double sinPhi = Math.sin(phi);
		return new double[] {
			RADIUS_X * radial * sinPhi * Math.cos(theta),
			CENTER_Y + RADIUS_Y * radial * Math.cos(phi),
			RADIUS_Z * radial * sinPhi * Math.sin(theta)
		};
	}

	private static Pit makePit(
		double px,
		double py,
		double pz,
		double minR,
		double maxR,
		double minElong,
		double maxElong,
		long seed,
		Kind kind,
		double minStrength,
		double maxStrength
	) {
		double sizeT = hash01(seed + 53);
		double rx = minR + sizeT * (maxR - minR);
		double elong = minElong + hash01(seed + 71) * (maxElong - minElong);
		double axis = hash01(seed + 89);
		double ry;
		double rz;
		if (axis < 0.33) {
			ry = rx * elong;
			rz = rx / elong;
		} else if (axis < 0.66) {
			ry = rx / elong;
			rz = rx * elong;
		} else {
			ry = rx * (0.7 + elong * 0.3);
			rz = rx * (1.1 / elong);
			rx = rx * elong;
		}
		double strength = minStrength + hash01(seed + 101) * (maxStrength - minStrength);
		return new Pit(px, py, pz, rx, ry, rz, strength, kind);
	}

	@SuppressWarnings("unchecked")
	private static int[][] indexPits(Pit[] pits) {
		List<Integer>[] buckets = new List[HASH_AXIS * HASH_AXIS * HASH_AXIS];
		for (int i = 0; i < pits.length; i++) {
			Pit pit = pits[i];
			int minX = cellCoord(pit.x - pit.rx);
			int maxX = cellCoord(pit.x + pit.rx);
			int minY = cellCoord(pit.y - pit.ry);
			int maxY = cellCoord(pit.y + pit.ry);
			int minZ = cellCoord(pit.z - pit.rz);
			int maxZ = cellCoord(pit.z + pit.rz);
			for (int gx = minX; gx <= maxX; gx++) {
				for (int gy = minY; gy <= maxY; gy++) {
					for (int gz = minZ; gz <= maxZ; gz++) {
						int idx = packedCell(gx, gy, gz);
						if (buckets[idx] == null) {
							buckets[idx] = new ArrayList<>(8);
						}
						buckets[idx].add(i);
					}
				}
			}
		}
		int[][] out = new int[buckets.length][];
		for (int i = 0; i < buckets.length; i++) {
			if (buckets[i] == null) {
				continue;
			}
			out[i] = buckets[i].stream().mapToInt(Integer::intValue).toArray();
		}
		return out;
	}

	private static int cellIndex(int x, int y, int z) {
		return packedCell(cellCoord(x), cellCoord(y), cellCoord(z));
	}

	private static int cellCoord(double v) {
		int c = (int) Math.floor((v + HASH_OFFSET) / (1 << HASH_SHIFT));
		if (c < 0) {
			return 0;
		}
		if (c > HASH_MASK) {
			return HASH_MASK;
		}
		return c;
	}

	private static int packedCell(int gx, int gy, int gz) {
		return ((gy & HASH_MASK) * HASH_AXIS + (gz & HASH_MASK)) * HASH_AXIS + (gx & HASH_MASK);
	}

	private static double valueNoise(double x, double y, double z, int seed) {
		int x0 = (int) Math.floor(x);
		int y0 = (int) Math.floor(y);
		int z0 = (int) Math.floor(z);
		double fx = smooth(x - x0);
		double fy = smooth(y - y0);
		double fz = smooth(z - z0);
		double n000 = hashCorner(x0, y0, z0, seed);
		double n100 = hashCorner(x0 + 1, y0, z0, seed);
		double n010 = hashCorner(x0, y0 + 1, z0, seed);
		double n110 = hashCorner(x0 + 1, y0 + 1, z0, seed);
		double n001 = hashCorner(x0, y0, z0 + 1, seed);
		double n101 = hashCorner(x0 + 1, y0, z0 + 1, seed);
		double n011 = hashCorner(x0, y0 + 1, z0 + 1, seed);
		double n111 = hashCorner(x0 + 1, y0 + 1, z0 + 1, seed);
		double nx00 = lerp(n000, n100, fx);
		double nx10 = lerp(n010, n110, fx);
		double nx01 = lerp(n001, n101, fx);
		double nx11 = lerp(n011, n111, fx);
		return lerp(lerp(nx00, nx10, fy), lerp(nx01, nx11, fy), fz) * 2.0 - 1.0;
	}

	private static double smooth(double t) {
		return t * t * (3.0 - 2.0 * t);
	}

	private static double hashCorner(int x, int y, int z, int seed) {
		long n = x * 374761393L + y * 668265263L + z * 2147483647L + seed * 1442695041L;
		n = (n ^ (n >> 13)) * 1274126177L;
		n ^= n >> 16;
		return ((n & 0xFFFFFFL) / (double) 0xFFFFFFL);
	}

	private static double hash01(long n) {
		n ^= n >>> 33;
		n *= 0xff51afd7ed558ccdL;
		n ^= n >>> 33;
		n *= 0xc4ceb9fe1a85ec53L;
		n ^= n >>> 33;
		return ((n >>> 11) & 0x1fffffffffffffL) / (double) 0x1fffffffffffffL;
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	public record Eval(double density, double floor) {
		static final Eval EMPTY = new Eval(-1.0, 0.0);
	}

	private enum Kind {
		SCOOP,
		DEEP,
		THROUGH,
		COLLAPSE
	}

	private record Pit(
		double x,
		double y,
		double z,
		double rx,
		double ry,
		double rz,
		double strength,
		Kind kind
	) {
	}

	private record Crater(
		int x,
		int y,
		int z,
		double nx,
		double ny,
		double nz,
		double radius,
		double depth,
		double strength,
		boolean openFloor
	) {
		double[] normalizedNormal() {
			double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
			if (len < 1.0e-6) {
				return new double[] {0.0, 1.0, 0.0};
			}
			return new double[] {nx / len, ny / len, nz / len};
		}

		int seed() {
			return x * 31 + y * 17 + z * 13;
		}
	}
}
