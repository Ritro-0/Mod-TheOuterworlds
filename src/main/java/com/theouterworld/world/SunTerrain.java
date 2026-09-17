package com.theouterworld.world;

/**
 * Shared Y layout for The Sun: bedrock (1) → lava (200) → solar plasma (50).
 */
public final class SunTerrain {
	public static final int LAVA_DEPTH = 200;
	public static final int PLASMA_DEPTH = 50;

	private SunTerrain() {
	}

	public static int lavaTopY(int minY) {
		return minY + LAVA_DEPTH;
	}

	public static int plasmaTopY(int minY) {
		return lavaTopY(minY) + PLASMA_DEPTH;
	}

	/** First air / stand Y above the plasma ocean. */
	public static int surfaceY(int minY) {
		return plasmaTopY(minY) + 1;
	}
}
