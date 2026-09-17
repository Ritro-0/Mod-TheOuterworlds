package com.theouterworld.world;

/**
 * Vertical layer anchors for Highworld (Jupiter).
 * World height: min_y -64, height 512 → max Y 447.
 */
public final class HighworldLayers {
	public static final int MIN_Y = -64;
	public static final int WORLD_HEIGHT = 512;

	/** Top of the ~10-block raw-metal crust above bedrock. */
	public static final int METAL_TOP_Y = -54;

	/** Top of the ~200-block liquid hydrogen ocean. */
	public static final int HYDROGEN_SEA_LEVEL = 146;

	/** Air column above hydrogen where red lightning strikes (~100 blocks). */
	public static final int LIGHTNING_TOP_Y = 246;

	public static final int SULFIDE_BOTTOM_Y = 246;
	public static final int SULFIDE_TOP_Y = 266;

	public static final int AMMONIA_BOTTOM_Y = 266;
	public static final int AMMONIA_TOP_Y = 296;

	/** Open air above ammonia before build limit (~50 blocks of clear sky). */
	public static final int OPEN_AIR_TOP_Y = 346;

	public static final int HIGHWORLD_IRIDIUM_PIECES = 3;

	private HighworldLayers() {
	}
}
