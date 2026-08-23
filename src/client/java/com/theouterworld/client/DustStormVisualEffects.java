package com.theouterworld.client;

import com.theouterworld.OuterWorldClient;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GlassHelmetUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Client-side visual effects for dust storms:
 * - FOV zoom effect (like powdered snow)
 * - Overlay rendering (like powdered snow overlay)
 */
public class DustStormVisualEffects {
	private static final Identifier DUSTSTORM_OVERLAY = OuterWorldMod.id("textures/misc/duststorm_overlay.png");
	private static final Identifier HUD_ID = OuterWorldMod.id("dust_storm_overlay");

	private static int clientExposureTicks = 0;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) {
				clientExposureTicks = 0;
				return;
			}

			boolean exposed = client.level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)
				&& OuterWorldClient.isDustStormActive
				&& !GlassHelmetUtil.isWearingDustStormProtection(client.player)
				&& !isPlayerSheltered(client);

			if (exposed) {
				if (clientExposureTicks < 120) {
					clientExposureTicks++;
				}
			} else if (clientExposureTicks > 0) {
				// Fade out when entering shelter, putting on a helmet, or the storm ending.
				clientExposureTicks--;
			} else {
				clientExposureTicks = 0;
			}

			float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			float intensity = Mth.clamp((clientExposureTicks + tickDelta) / 120f, 0f, 1f);

			if (intensity > 0f) {
				float fovMultiplier = 1.0f - (intensity * intensity * 0.4f);
				client.options.fovEffectScale().set((double) fovMultiplier);
			} else if (client.options.fovEffectScale().get() != 1.0) {
				client.options.fovEffectScale().set(1.0);
			}
		});

		HudElementRegistry.addLast(HUD_ID, DustStormVisualEffects::extractOverlay);
	}

	private static void extractOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return;
		}

		if (!client.level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}

		float tickDeltaFloat = tickDelta.getGameTimeDeltaPartialTick(false);
		float intensity = Mth.clamp((clientExposureTicks + tickDeltaFloat) / 120f, 0f, 1f);
		if (intensity <= 0f) {
			return;
		}

		int width = client.getWindow().getGuiScaledWidth();
		int height = client.getWindow().getGuiScaledHeight();

		int tintAlpha = (int) (intensity * 120f);
		int tintColor = (tintAlpha << 24) | 0xD2691E;
		graphics.fill(0, 0, width, height, tintColor);

		int textureAlpha = (int) (intensity * 150f);
		int overlayColor = (textureAlpha << 24) | 0xFFFFFF;

		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			DUSTSTORM_OVERLAY,
			0, 0,
			0.0f, 0.0f,
			width, height,
			256, 256,
			overlayColor
		);
	}

	private static boolean isPlayerSheltered(Minecraft client) {
		if (client.player == null) {
			return false;
		}
		return InteriorShelterClient.isInterior(
			client.player.getX(),
			client.player.getY() + 0.5,
			client.player.getZ()
		);
	}
}
