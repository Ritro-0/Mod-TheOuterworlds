package com.theouterworld.world;

/**
 * Vertical layer anchors for Deepworld (Saturn).
 * Same total height as Highworld; helium fills the bottom 50 fluid blocks.
 */
public final class DeepworldLayers {
	public static final int MIN_Y = -64;
	public static final int WORLD_HEIGHT = 512;

	/** Top of the ~10-block raw-metal crust above bedrock. */
	public static final int METAL_TOP_Y = -54;

	/** Liquid helium column above the metal crust (~50 blocks). */
	public static final int HELIUM_BOTTOM_Y = METAL_TOP_Y + 1;
	public static final int HELIUM_TOP_Y = METAL_TOP_Y + 50;

	/** Top of the liquid hydrogen ocean (same sea level as Highworld). */
	public static final int HYDROGEN_SEA_LEVEL = 146;

	public static final int LIGHTNING_TOP_Y = 246;

	/** Lower cloud deck (was sulfide on Jupiter). */
	public static final int AMMONIA_BOTTOM_Y = 246;
	public static final int AMMONIA_TOP_Y = 266;

	/** Upper cloud deck (methane). */
	public static final int METHANE_BOTTOM_Y = 266;
	public static final int METHANE_TOP_Y = 296;

	public static final int OPEN_AIR_TOP_Y = 346;

	public static final int DEEPWORLD_IRIDIUM_PIECES = 3;

	private DeepworldLayers() {
	}
}
