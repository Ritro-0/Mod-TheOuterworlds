package com.theouterworld.client;

/**
 * Amberworld (Titan) atmosphere — Nearworld-style yellow haze, toned down for Titan.
 */
public final class AmberworldAtmosphere {
	/**
	 * Muted amber haze (Nearworld yellow scaled down) so the sky reads as thick
	 * atmosphere instead of a neon wash.
	 */
	public static final int HAZE_COLOR = 0xA8881A;
	public static final float FOG_START = NearworldAtmosphere.FOG_START;
	public static final float FOG_END = NearworldAtmosphere.FOG_END;
	public static final float SKY_FOG_END = NearworldAtmosphere.SKY_FOG_END;
	/** Saturn disc scaled so the planet body matches Jupiter's apparent size on Io. */
	public static final float SATURN_MOON_SCALE = 3.25F;
	/** Keep Saturn a quarter-turn from the sun so the discs never share a sky slot. */
	public static final float SATURN_SUN_OFFSET = (float) (Math.PI * 0.5);

	private AmberworldAtmosphere() {
	}
}
