package com.theouterworld.client;

import com.theouterworld.world.EdgeworldLayers;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Edgeworld — deep soul-blue sky / lightning / depth light.
 */
public final class EdgeworldAtmosphere {
	/** Deep, soul-penetrating blue lightning. */
	public static final float LIGHTNING_RED = 0.18F;
	public static final float LIGHTNING_GREEN = 0.32F;
	public static final float LIGHTNING_BLUE = 1.0F;

	/** Deep blue haze. */
	public static final int HAZE_COLOR = 0x1A2A6E;

	public static final double FULL_DARK_Y = EdgeworldLayers.METHANE_LOWER_BOTTOM_Y - 26.0;

	private EdgeworldAtmosphere() {
	}

	public static float naturalSkyFactor(double y) {
		if (y >= EdgeworldLayers.METHANE_UPPER_TOP_Y) {
			return 1.0F;
		}
		if (y >= EdgeworldLayers.METHANE_UPPER_BOTTOM_Y) {
			float t = (float) ((y - EdgeworldLayers.METHANE_UPPER_BOTTOM_Y)
				/ (double) (EdgeworldLayers.METHANE_UPPER_TOP_Y - EdgeworldLayers.METHANE_UPPER_BOTTOM_Y));
			return Mth.lerp(t, 0.78F, 1.0F);
		}
		if (y >= EdgeworldLayers.METHANE_MID_BOTTOM_Y) {
			float t = (float) ((y - EdgeworldLayers.METHANE_MID_BOTTOM_Y)
				/ (double) (EdgeworldLayers.METHANE_MID_TOP_Y - EdgeworldLayers.METHANE_MID_BOTTOM_Y));
			return Mth.lerp(t, 0.45F, 0.78F);
		}
		if (y >= EdgeworldLayers.METHANE_LOWER_BOTTOM_Y) {
			float t = (float) ((y - EdgeworldLayers.METHANE_LOWER_BOTTOM_Y)
				/ (double) (EdgeworldLayers.METHANE_LOWER_TOP_Y - EdgeworldLayers.METHANE_LOWER_BOTTOM_Y));
			return Mth.lerp(t, 0.28F, 0.45F);
		}
		if (y <= FULL_DARK_Y) {
			return 0.0F;
		}
		float t = (float) ((y - FULL_DARK_Y) / (EdgeworldLayers.METHANE_LOWER_BOTTOM_Y - FULL_DARK_Y));
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
