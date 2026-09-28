package com.theouterworld.client;

import net.minecraft.util.Mth;

/**
 * Mars-like rusty sky for the Outerworld. Daytime haze dies off after sunset so the
 * night sky and a small Earth-moon can actually read.
 */
public final class OuterworldAtmosphere {
	/** Packed RGB without alpha — orange-red, not Nearworld yellow. */
	public static final int SKY_COLOR = 0xD15736;
	public static final int NIGHT_SKY_COLOR = 0x00000A;
	public static final float STAR_BRIGHTNESS = 0.55F;

	private OuterworldAtmosphere() {
	}

	/**
	 * 1 at midday, 0 once the sun is well below the horizon. Celestial angle 0 is noon.
	 */
	public static float dayHaze(float sunAngle) {
		return Mth.clamp((float) Math.cos(sunAngle) * 1.35F + 0.08F, 0.0F, 1.0F);
	}

	public static int skyColor(float sunAngle) {
		return blend(NIGHT_SKY_COLOR, SKY_COLOR, dayHaze(sunAngle));
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
