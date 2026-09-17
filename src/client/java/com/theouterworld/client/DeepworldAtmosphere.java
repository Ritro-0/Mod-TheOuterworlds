package com.theouterworld.client;

import com.theouterworld.world.DeepworldLayers;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Deepworld (Saturn) lightning / fog / depth-light helpers.
 * Depth curve matches Highworld Y bands: methane tops → ammonia deck → dark below.
 */
public final class DeepworldAtmosphere {
	/** Orange lightning. */
	public static final float LIGHTNING_RED = 1.0F;
	public static final float LIGHTNING_GREEN = 0.48F;
	public static final float LIGHTNING_BLUE = 0.12F;

	public static final int HAZE_COLOR = 0xD4C48A;

	/** Same absolute depth as Highworld's full-dark cutoff. */
	public static final double FULL_DARK_Y = DeepworldLayers.AMMONIA_BOTTOM_Y - 26.0;

	private DeepworldAtmosphere() {
	}

	/**
	 * Multiplier for natural sky / haze brightness.
	 * 1 at the methane tops, fading through the ammonia deck, then to 0 shortly below.
	 */
	public static float naturalSkyFactor(double y) {
		if (y >= DeepworldLayers.METHANE_TOP_Y) {
			return 1.0F;
		}
		if (y >= DeepworldLayers.METHANE_BOTTOM_Y) {
			float t = (float) ((y - DeepworldLayers.METHANE_BOTTOM_Y)
				/ (double) (DeepworldLayers.METHANE_TOP_Y - DeepworldLayers.METHANE_BOTTOM_Y));
			return Mth.lerp(t, 0.78F, 1.0F);
		}
		if (y >= DeepworldLayers.AMMONIA_BOTTOM_Y) {
			float t = (float) ((y - DeepworldLayers.AMMONIA_BOTTOM_Y)
				/ (double) (DeepworldLayers.AMMONIA_TOP_Y - DeepworldLayers.AMMONIA_BOTTOM_Y));
			return Mth.lerp(t, 0.28F, 0.78F);
		}
		if (y <= FULL_DARK_Y) {
			return 0.0F;
		}
		float t = (float) ((y - FULL_DARK_Y) / (DeepworldLayers.AMMONIA_BOTTOM_Y - FULL_DARK_Y));
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
