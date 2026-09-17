package com.theouterworld.worldgen;

/**
 * Potatoworlds (Phobos + Deimos) — two solid irregular potato bodies in empty space.
 * Each is about half Spongeworld's scale. Phobos is cratered; Deimos is not.
 */
public final class PotatoworldsShape {
	public static final BodySpec PHOBOS = new BodySpec(
		-130.0, 128.0, 0.0,
		100.0, 64.0, 84.0,
		true,
		101L
	);
	public static final BodySpec DEIMOS = new BodySpec(
		150.0, 118.0, 30.0,
		92.0, 58.0, 78.0,
		false,
		707L
	);

	private static final BodySpec[] BODIES = {PHOBOS, DEIMOS};
	private static final double BOUND_PAD = 28.0;

	/** Phobos impact bowls — strip surface cover and carve the crust. */
	private static final Crater[] PHOBOS_CRATERS = {
		new Crater(-70, 160, -20, 0.55, 0.70, -0.35, 28, 18, 1.15),
		new Crater(-180, 100, 40, -0.75, -0.20, 0.55, 34, 22, 1.20),
		new Crater(-100, 70, -50, 0.25, -0.85, -0.40, 24, 16, 1.10),
		new Crater(-145, 175, 55, -0.15, 0.90, 0.35, 22, 14, 1.05),
		new Crater(-90, 135, 70, 0.65, 0.15, 0.70, 18, 12, 1.00),
		new Crater(-160, 90, -60, -0.45, -0.35, -0.75, 20, 13, 1.05)
	};

	private PotatoworldsShape() {
	}

	public static boolean chunkMayIntersect(int chunkMinX, int chunkMinZ) {
		int chunkMaxX = chunkMinX + 15;
		int chunkMaxZ = chunkMinZ + 15;
		for (BodySpec body : BODIES) {
			double maxX = body.cx + body.rx + BOUND_PAD;
			double minX = body.cx - body.rx - BOUND_PAD;
			double maxZ = body.cz + body.rz + BOUND_PAD;
			double minZ = body.cz - body.rz - BOUND_PAD;
			if (chunkMaxX >= minX && chunkMinX <= maxX && chunkMaxZ >= minZ && chunkMinZ <= maxZ) {
				return true;
			}
		}
		return false;
	}

	public static boolean columnMayIntersect(int x, int z) {
		for (BodySpec body : BODIES) {
			double nx = (x - body.cx) / (body.rx + 20.0);
			double nz = (z - body.cz) / (body.rz + 20.0);
			if (nx * nx + nz * nz < 1.2) {
				return true;
			}
		}
		return false;
	}

	public static int yStart() {
		int min = Integer.MAX_VALUE;
		for (BodySpec body : BODIES) {
			min = Math.min(min, (int) Math.floor(body.cy - body.ry - 40.0));
		}
		return min;
	}

	public static int yEnd() {
		int max = Integer.MIN_VALUE;
		for (BodySpec body : BODIES) {
			max = Math.max(max, (int) Math.ceil(body.cy + body.ry + 40.0));
		}
		return max;
	}

	/** Preferred rift-pad deck on Phobos' outer shell. */
	public static int phobosDeckY() {
		return (int) Math.round(PHOBOS.cy + PHOBOS.ry * 0.55);
	}

	public static int phobosDeckX() {
		return (int) Math.round(PHOBOS.cx);
	}

	public static int phobosDeckZ() {
		return (int) Math.round(PHOBOS.cz);
	}

	public static Eval evaluate(int x, int y, int z) {
		Eval best = Eval.EMPTY;
		for (BodySpec body : BODIES) {
			Eval sample = evaluateBody(body, x, y, z);
			if (sample.density > best.density) {
				best = sample;
			}
		}
		return best;
	}

	public static boolean isSolid(int x, int y, int z) {
		return evaluate(x, y, z).density > 0.0;
	}

	private static Eval evaluateBody(BodySpec body, int x, int y, int z) {
		double shell = bodyField(body, x, y, z);
		if (shell < -0.22) {
			return Eval.EMPTY;
		}
		double carve = 0.0;
		boolean craterFloor = false;
		if (body.cratered) {
			CraterHit hit = craterCarve(x, y, z);
			carve = hit.excavation;
			craterFloor = hit.floor;
		}
		double density = shell - carve;
		if (density <= 0.0) {
			return Eval.EMPTY;
		}
		return new Eval(density, body, craterFloor, shell);
	}

	private static double bodyField(BodySpec body, int x, int y, int z) {
		double nx = (x - body.cx) / body.rx;
		double ny = (y - body.cy) / body.ry;
		double nz = (z - body.cz) / body.rz;
		double r = Math.sqrt(nx * nx + ny * ny + nz * nz);
		// Irregular potato warp — lumpy, not a smooth egg.
		double warp = valueNoise(x * 0.0055, y * 0.0060, z * 0.0054, (int) body.seed) * 0.14
			+ valueNoise(x * 0.013, y * 0.012, z * 0.014, (int) body.seed + 91) * 0.07
			+ valueNoise(x * 0.028, y * 0.025, z * 0.027, (int) body.seed + 173) * 0.035;
		return 1.0 + warp - r;
	}

	private static CraterHit craterCarve(int x, int y, int z) {
		double scoop = 0.0;
		boolean floor = false;
		for (Crater c : PHOBOS_CRATERS) {
			double[] n = c.normalizedNormal();
			double rx = x - c.x;
			double ry = y - c.y;
			double rz = z - c.z;
			double along = rx * n[0] + ry * n[1] + rz * n[2];
			if (along > c.radius * 0.15) {
				continue;
			}
			double px = rx - along * n[0];
			double py = ry - along * n[1];
			double pz = rz - along * n[2];
			double radial = Math.sqrt(px * px + py * py + pz * pz) / c.radius;
			double rimLimit = 1.0 + valueNoise(x * 0.04, y * 0.04, z * 0.04, c.seed()) * 0.14;
			if (radial > rimLimit) {
				continue;
			}
			double bowl = 1.0 - (radial / rimLimit);
			bowl = bowl * bowl;
			double depthAlong = Math.max(0.0, -along / c.depth);
			if (depthAlong > 1.2) {
				continue;
			}
			double excavation = bowl * (1.0 - depthAlong * 0.28) * c.strength;
			if (radial > 0.72 && radial < 1.08 && along > -c.depth * 0.3 && along < c.depth * 0.12) {
				excavation -= 0.35 * (1.0 - Math.abs(radial - 0.9) / 0.18) * c.strength;
			}
			if (excavation > scoop) {
				scoop = excavation;
			}
			// Flat-ish crater floors and rims stay bare of regolith.
			if (depthAlong < 0.95 && radial < 0.92 && along < c.depth * 0.05) {
				floor = true;
			}
		}
		return new CraterHit(scoop, floor);
	}

	/**
	 * One-block surface veneer: solid cell with at least one empty orthogonal neighbor.
	 */
	public static boolean isExposedSurface(int x, int y, int z) {
		Eval self = evaluate(x, y, z);
		if (self.density <= 0.0) {
			return false;
		}
		return !isSolid(x + 1, y, z)
			|| !isSolid(x - 1, y, z)
			|| !isSolid(x, y + 1, z)
			|| !isSolid(x, y - 1, z)
			|| !isSolid(x, y, z + 1)
			|| !isSolid(x, y, z - 1);
	}

	/** Deep interior → bedrock. */
	public static boolean isBedrockCore(Eval sample) {
		return sample.shell > 0.32;
	}

	/**
	 * Surface dust / tholin veneer — skipped on crater floors and noisy bald patches.
	 */
	public static boolean hasSurfaceCover(Eval sample, int x, int y, int z) {
		if (sample.body == null || sample.craterFloor || !isExposedSurface(x, y, z)) {
			return false;
		}
		double n = valueNoise(x * 0.07, y * 0.065, z * 0.07, (int) sample.body.seed + 501);
		double m = valueNoise(x * 0.019, y * 0.017, z * 0.018, (int) sample.body.seed + 502);
		// Mostly covered, with irregular bald patches.
		return !(n > 0.55 && m > 0.20);
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

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	public record BodySpec(
		double cx,
		double cy,
		double cz,
		double rx,
		double ry,
		double rz,
		boolean cratered,
		long seed
	) {
		public boolean isPhobos() {
			return this == PHOBOS;
		}
	}

	public record Eval(double density, BodySpec body, boolean craterFloor, double shell) {
		static final Eval EMPTY = new Eval(-1.0, null, false, -1.0);
	}

	private record CraterHit(double excavation, boolean floor) {
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
		double strength
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
