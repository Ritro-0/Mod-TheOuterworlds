package com.theouterworld.client;

import com.theouterworld.world.HighworldLayers;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector3fc;

/**
 * Highworld (Jupiter) lightning / fog / depth-light helpers.
 */
public final class HighworldAtmosphere {
	/** Deep red lightning. */
	public static final float LIGHTNING_RED = 1.0F;
	public static final float LIGHTNING_GREEN = 0.18F;
	public static final float LIGHTNING_BLUE = 0.12F;

	public static final int HAZE_COLOR = 0xC9A227;

	/**
	 * Y at and below which natural sky light is fully extinguished.
	 * A short drop beneath the sulfide deck so darkness arrives quickly.
	 */
	public static final double FULL_DARK_Y = HighworldLayers.SULFIDE_BOTTOM_Y - 26.0;

	private HighworldAtmosphere() {
	}

	/**
	 * Multiplier for natural sky / haze brightness.
	 * 1 at the ammonia tops, fading through the sulfide deck, then to 0 shortly below.
	 * Block lights are unaffected; night vision is a separate lightmap channel.
	 */
	public static float naturalSkyFactor(double y) {
		if (y >= HighworldLayers.AMMONIA_TOP_Y) {
			return 1.0F;
		}
		if (y >= HighworldLayers.AMMONIA_BOTTOM_Y) {
			float t = (float) ((y - HighworldLayers.AMMONIA_BOTTOM_Y)
				/ (double) (HighworldLayers.AMMONIA_TOP_Y - HighworldLayers.AMMONIA_BOTTOM_Y));
			return Mth.lerp(t, 0.78F, 1.0F);
		}
		if (y >= HighworldLayers.SULFIDE_BOTTOM_Y) {
			float t = (float) ((y - HighworldLayers.SULFIDE_BOTTOM_Y)
				/ (double) (HighworldLayers.SULFIDE_TOP_Y - HighworldLayers.SULFIDE_BOTTOM_Y));
			return Mth.lerp(t, 0.28F, 0.78F);
		}
		if (y <= FULL_DARK_Y) {
			return 0.0F;
		}
		float t = (float) ((y - FULL_DARK_Y) / (HighworldLayers.SULFIDE_BOTTOM_Y - FULL_DARK_Y));
		return Mth.lerp(t, 0.0F, 0.28F);
	}

	/** Dim the amber haze so depth darkening is visible through fog, not only on the hand. */
	public static Vector3fc depthFogColor(Vector3fc baseColor, double y) {
		float factor = naturalSkyFactor(y);
		if (factor >= 0.999F) {
			return baseColor;
		}
		// Keep a tiny residual haze so pitch-black fog doesn't look broken; sky light is already ~0.
		float mix = Mth.clamp(factor, 0.02F, 1.0F);
		return ARGB.scaleRGB(baseColor, mix);
	}

	public static Vector3fc depthSkyColor(Vector3fc baseColor, double y) {
		return depthFogColor(baseColor, y);
	}

	public static Vector3fc depthAmbientColor(Vector3fc baseColor, double y) {
		float factor = naturalSkyFactor(y);
		if (factor >= 0.999F) {
			return baseColor;
		}
		return ARGB.scaleRGB(baseColor, Mth.clamp(factor, 0.0F, 1.0F));
	}
}
