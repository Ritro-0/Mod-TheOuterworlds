package com.theouterworld.world;

/**
 * Vertical layer anchors for Edgeworld (outer ice giant).
 * Similar to Farworld but clouds start higher (near High/Deepworld decks) with less coverage.
 */
public final class EdgeworldLayers {
	public static final int MIN_Y = -64;
	public static final int WORLD_HEIGHT = 512;

	public static final int METAL_TOP_Y = -54;

	/** Liquid methane ocean — same sea level as other gas giants. */
	public static final int METHANE_SEA_LEVEL = 146;

	public static final int LIGHTNING_TOP_Y = 246;

	/** Sparse lower methane wisps — near Highworld sulfide height. */
	public static final int METHANE_LOWER_BOTTOM_Y = 246;
	public static final int METHANE_LOWER_TOP_Y = 256;

	/** Mid methane — moderate. */
	public static final int METHANE_MID_BOTTOM_Y = 256;
	public static final int METHANE_MID_TOP_Y = 276;

	/** Upper methane — densest of the three, still less than Farworld. */
	public static final int METHANE_UPPER_BOTTOM_Y = 276;
	public static final int METHANE_UPPER_TOP_Y = 296;

	public static final int OPEN_AIR_TOP_Y = 346;

	public static final int EDGEWORLD_IRIDIUM_PIECES = 3;

	private EdgeworldLayers() {
	}
}
