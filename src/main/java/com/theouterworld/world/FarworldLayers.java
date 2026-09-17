package com.theouterworld.world;

/**
 * Vertical layer anchors for Farworld (ice giant).
 * Methane-only cloud decks start 50 blocks below Highworld and thicken upward.
 */
public final class FarworldLayers {
	public static final int MIN_Y = -64;
	public static final int WORLD_HEIGHT = 512;

	/** Top of the ~10-block raw-metal crust above bedrock. */
	public static final int METAL_TOP_Y = -54;

	/** Top of the ~200-block liquid ammonia ocean (same sea level as Highworld). */
	public static final int AMMONIA_SEA_LEVEL = 146;

	/** Air column above ammonia where teal lightning strikes. */
	public static final int LIGHTNING_TOP_Y = 196;

	/** Lower methane deck — sparse. */
	public static final int METHANE_LOWER_BOTTOM_Y = 196;
	public static final int METHANE_LOWER_TOP_Y = 216;

	/** Mid methane deck — more coverage. */
	public static final int METHANE_MID_BOTTOM_Y = 216;
	public static final int METHANE_MID_TOP_Y = 246;

	/** Upper methane deck — dense arrival layer. */
	public static final int METHANE_UPPER_BOTTOM_Y = 246;
	public static final int METHANE_UPPER_TOP_Y = 286;

	/** Open air above clouds (~50 blocks of clear sky). */
	public static final int OPEN_AIR_TOP_Y = 336;

	public static final int FARWORLD_IRIDIUM_PIECES = 3;

	private FarworldLayers() {
	}
}
