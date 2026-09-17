package com.theouterworld.client;

/**
 * Emberworld (Io) sky helpers — permanent night with a thin daytime yellow wash.
 */
public final class EmberworldAtmosphere {
	public static final int NIGHT_SKY_COLOR = 0x00000A;
	/** Very thin yellow haze while the sun is up. */
	public static final int DAY_HAZE_COLOR = 0xFFE01F;
	/** Blend weight of yellow into the night sky during daytime. */
	public static final float DAY_HAZE_BLEND = 0.12F;
	public static final float STAR_BRIGHTNESS = 0.55F;

	private EmberworldAtmosphere() {
	}

	public static int skyColor(boolean sunUp) {
		if (!sunUp) {
			return NIGHT_SKY_COLOR;
		}
		return blend(NIGHT_SKY_COLOR, DAY_HAZE_COLOR, DAY_HAZE_BLEND);
	}

	private static int blend(int a, int b, float t) {
		int ar = (a >> 16) & 0xFF;
		int ag = (a >> 8) & 0xFF;
		int ab = a & 0xFF;
		int br = (b >> 16) & 0xFF;
		int bg = (b >> 8) & 0xFF;
		int bb = b & 0xFF;
		int r = Math.round(ar + (br - ar) * t);
		int g = Math.round(ag + (bg - ag) * t);
		int bl = Math.round(ab + (bb - ab) * t);
		return (r << 16) | (g << 8) | bl;
	}
}
