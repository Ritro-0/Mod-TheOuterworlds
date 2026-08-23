package com.theouterworld.worldgen;

public final class WorldgenNoise {
	private WorldgenNoise() {
	}

	public static double fade(double t) {
		return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
	}

	public static double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	public static double hash(long seed, int x, int z) {
		long n = seed;
		n ^= (long) x * 341873128712L;
		n ^= (long) z * 132897987541L;
		n ^= n >> 33;
		n *= 0xff51afd7ed558ccdL;
		n ^= n >> 33;
		n *= 0xc4ceb9fe1a85ec53L;
		n ^= n >> 33;
		return ((n >>> 11) & 0x1fffffffffffffL) / (double) 0x1fffffffffffffL;
	}

	public static double valueNoise(long seed, double x, double z) {
		int x0 = (int) Math.floor(x);
		int z0 = (int) Math.floor(z);
		int x1 = x0 + 1;
		int z1 = z0 + 1;
		double fx = fade(x - x0);
		double fz = fade(z - z0);
		double v00 = hash(seed, x0, z0);
		double v10 = hash(seed, x1, z0);
		double v01 = hash(seed, x0, z1);
		double v11 = hash(seed, x1, z1);
		return lerp(fz, lerp(fx, v00, v10), lerp(fx, v01, v11));
	}

	public static double octaveNoise(long seed, double x, double z, int octaves, double persistence) {
		double total = 0.0;
		double amplitude = 1.0;
		double frequency = 1.0;
		double max = 0.0;
		for (int i = 0; i < octaves; i++) {
			total += valueNoise(seed + i * 17L, x * frequency, z * frequency) * amplitude;
			max += amplitude;
			amplitude *= persistence;
			frequency *= 2.0;
		}
		return max == 0.0 ? 0.0 : total / max;
	}

	public static double signed(double unitNoise) {
		return unitNoise * 2.0 - 1.0;
	}
}
