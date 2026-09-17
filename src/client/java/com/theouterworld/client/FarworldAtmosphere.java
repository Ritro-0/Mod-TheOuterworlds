package com.theouterworld.client;

import com.theouterworld.world.FarworldLayers;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Farworld lightning / fog / depth-light helpers.
 * Same vertical lighting rules as Highworld, keyed to methane decks.
 */
public final class FarworldAtmosphere {
	/** Light teal lightning. */
	public static final float LIGHTNING_RED = 0.55F;
	public static final float LIGHTNING_GREEN = 0.95F;
	public static final float LIGHTNING_BLUE = 0.90F;

	/** Light teal sky — mostly blue with a slight green shade. */
	public static final int HAZE_COLOR = 0x7EC8D0;

	public static final double FULL_DARK_Y = FarworldLayers.METHANE_LOWER_BOTTOM_Y - 26.0;

	private FarworldAtmosphere() {
	}

	public static float naturalSkyFactor(double y) {
		if (y >= FarworldLayers.METHANE_UPPER_TOP_Y) {
			return 1.0F;
		}
		if (y >= FarworldLayers.METHANE_UPPER_BOTTOM_Y) {
			float t = (float) ((y - FarworldLayers.METHANE_UPPER_BOTTOM_Y)
				/ (double) (FarworldLayers.METHANE_UPPER_TOP_Y - FarworldLayers.METHANE_UPPER_BOTTOM_Y));
			return Mth.lerp(t, 0.78F, 1.0F);
		}
		if (y >= FarworldLayers.METHANE_MID_BOTTOM_Y) {
			float t = (float) ((y - FarworldLayers.METHANE_MID_BOTTOM_Y)
				/ (double) (FarworldLayers.METHANE_MID_TOP_Y - FarworldLayers.METHANE_MID_BOTTOM_Y));
			return Mth.lerp(t, 0.45F, 0.78F);
		}
		if (y >= FarworldLayers.METHANE_LOWER_BOTTOM_Y) {
			float t = (float) ((y - FarworldLayers.METHANE_LOWER_BOTTOM_Y)
				/ (double) (FarworldLayers.METHANE_LOWER_TOP_Y - FarworldLayers.METHANE_LOWER_BOTTOM_Y));
			return Mth.lerp(t, 0.28F, 0.45F);
		}
		if (y <= FULL_DARK_Y) {
			return 0.0F;
		}
		float t = (float) ((y - FULL_DARK_Y) / (FarworldLayers.METHANE_LOWER_BOTTOM_Y - FULL_DARK_Y));
		return Mth.lerp(t, 0.0F, 0.28F);
	}

	public static int depthFogColor(int baseColor, double y) {
		float factor = naturalSkyFactor(y);
		if (factor >= 0.999F) {
			return baseColor;
		}
		float mix = Mth.clamp(factor, 0.02F, 1.0F);
		return ARGB.scaleRGB(baseColor, mix);
	}

	public static int depthSkyColor(int baseColor, double y) {
		return depthFogColor(baseColor, y);
	}

	public static int depthAmbientColor(int baseColor, double y) {
		float factor = naturalSkyFactor(y);
		if (factor >= 0.999F) {
			return baseColor;
		}
		return ARGB.scaleRGB(baseColor, Mth.clamp(factor, 0.0F, 1.0F));
	}
}
