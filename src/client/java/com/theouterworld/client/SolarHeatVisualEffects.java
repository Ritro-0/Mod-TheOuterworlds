package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.SolarIrradiation;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Screen overlay for solar / ambient heat — Innerworld sun-scorch and Nearworld /
 * Emberworld atmosphere. Ramps with exposure the same way powdered snow / dust storms do.
 */
public final class SolarHeatVisualEffects {
	private static final Identifier HEAT_OVERLAY = OuterWorldMod.id("textures/misc/irradiating_heat_overlay.png");
	private static final Identifier HUD_ID = OuterWorldMod.id("solar_heat_overlay");
	private static final float RAMP_SECONDS = 8.0F;
	private static final float NEARWORLD_RAMP_SECONDS = 5.0F;

	private static float exposure = 0.0F;
	private static long lastUpdateNanos = 0L;

	private SolarHeatVisualEffects() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(SolarHeatVisualEffects::update);
		HudElementRegistry.addLast(HUD_ID, SolarHeatVisualEffects::extractOverlay);
	}

	private static void update(Minecraft client) {
		long now = System.nanoTime();
		float elapsed = lastUpdateNanos == 0L ? 0.0F : (now - lastUpdateNanos) / 1_000_000_000.0F;
		lastUpdateNanos = now;
		elapsed = Mth.clamp(elapsed, 0.0F, 0.25F);

		Entity camera = client.getCameraEntity();
		if (client.level == null || camera == null) {
			exposure = 0.0F;
			return;
		}
		if (!inHeatDimension(client)) {
			exposure = Math.max(0.0F, exposure - elapsed / (RAMP_SECONDS * 0.5F));
			return;
		}

		boolean exposed = false;
		if (camera instanceof LivingEntity living) {
			float heatMult = SolarIrradiation.heatMultiplier(living);
			exposed = heatMult > 0.0F && SolarIrradiation.isExposed(living);
		}

		float ramp = ModDimensions.isNearworld(client.level.dimension()) || ModDimensions.isEmberworld(client.level.dimension())
			? NEARWORLD_RAMP_SECONDS
			: RAMP_SECONDS;
		float step = elapsed / ramp;
		exposure = exposed
			? Math.min(1.0F, exposure + step)
			: Math.max(0.0F, exposure - step * 1.5F);
	}

	private static void extractOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}
		update(client);
		if (!inHeatDimension(client)) {
			return;
		}

		float intensity = Mth.clamp(exposure, 0.0F, 1.0F);
		if (intensity <= 0.01F) {
			return;
		}

		int width = client.getWindow().getGuiScaledWidth();
		int height = client.getWindow().getGuiScaledHeight();

		int tintAlpha = (int) (intensity * 100.0F);
		int tintColor = (tintAlpha << 24) | 0xFF6A1B;
		graphics.fill(0, 0, width, height, tintColor);

		int textureAlpha = (int) (intensity * 200.0F);
		int overlayColor = (textureAlpha << 24) | 0xFFFFFF;
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			HEAT_OVERLAY,
			0, 0,
			0.0F, 0.0F,
			width, height,
			256, 256,
			overlayColor
		);
	}

	private static boolean inHeatDimension(Minecraft client) {
		return client.level != null
			&& (ModDimensions.isInnerworld(client.level.dimension())
			|| ModDimensions.isNearworld(client.level.dimension())
			|| ModDimensions.isEmberworld(client.level.dimension()));
	}
}
