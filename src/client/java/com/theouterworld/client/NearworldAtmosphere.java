package com.theouterworld.client;

/**
 * Shared sulfuric-yellow atmosphere for the Nearworld.
 * Matches lightning ({@code 1.0, 0.88, 0.12}) and the rain sheet hue.
 */
public final class NearworldAtmosphere {
	/** RGB packed without alpha — same yellow as Nearworld lightning. */
	public static final int HAZE_COLOR = 0xFFE01F;
	public static final float LIGHTNING_RED = 1.0F;
	public static final float LIGHTNING_GREEN = 0.88F;
	public static final float LIGHTNING_BLUE = 0.12F;
	/** Distant scene-setting haze — yellow sky, not a vision wall. */
	public static final float FOG_START = 96.0F;
	public static final float FOG_END = 384.0F;
	public static final float SKY_FOG_END = 384.0F;

	private NearworldAtmosphere() {
	}
}
