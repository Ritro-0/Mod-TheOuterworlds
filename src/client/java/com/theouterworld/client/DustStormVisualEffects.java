package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GlassHelmetUtil;
import com.theouterworld.weather.DustStormManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Client-side visual effects for dust storms:
 * - FOV zoom effect (like powdered snow)
 * - Overlay rendering (like powdered snow overlay)
 */
public class DustStormVisualEffects {
	private static final Identifier DUSTSTORM_OVERLAY = OuterWorldMod.id("textures/misc/duststorm_overlay.png");
	private static final Identifier HUD_ID = OuterWorldMod.id("dust_storm_overlay");

	/** Seconds for the overlay to reach full strength while stood in the open. */
	private static final float RAMP_SECONDS = 6.0f;
	/** Replay viewers need it promptly; a six second build-up is useless when scrubbing. */
	private static final float REPLAY_RAMP_SECONDS = 0.75f;

	private static float exposure = 0.0f;
	private static long lastUpdateNanos = 0L;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(DustStormVisualEffects::update);
		HudElementRegistry.addLast(HUD_ID, DustStormVisualEffects::extractOverlay);
	}

	/**
	 * Advance the exposure ramp on wall-clock time rather than ticks, and drive it from both the
	 * client tick and the HUD pass. During a replay the tick rate follows playback speed and stops
	 * entirely while paused, which previously left the overlay stuck near zero.
	 */
	private static void update(Minecraft client) {
		long now = System.nanoTime();
		float elapsed = lastUpdateNanos == 0L ? 0.0f : (now - lastUpdateNanos) / 1_000_000_000.0f;
		lastUpdateNanos = now;
		elapsed = Mth.clamp(elapsed, 0.0f, 0.25f);

		Entity camera = client.getCameraEntity();
		if (client.level == null || camera == null) {
			// No world loaded: leave the player's own FOV option alone.
			exposure = 0.0f;
			return;
		}

		boolean exposed = client.level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)
			&& DustStormClientState.isActive()
			&& !isProtected(camera)
			&& !InteriorShelterClient.isInterior(camera.getX(), camera.getEyeY(), camera.getZ())
			&& DustStormManager.isOpenToSky(
				client.level,
				BlockPos.containing(camera.getX(), camera.getEyeY(), camera.getZ())
			);

		float rampSeconds = FlashbackTimeline.isInReplay() ? REPLAY_RAMP_SECONDS : RAMP_SECONDS;
		float step = elapsed / rampSeconds;
		exposure = exposed
			? Math.min(1.0f, exposure + step)
			: Math.max(0.0f, exposure - step);

		applyFov(client, exposure);
	}

	private static boolean isProtected(Entity camera) {
		return camera instanceof LivingEntity living && GlassHelmetUtil.isWearingDustStormProtection(living);
	}

	private static void applyFov(Minecraft client, float intensity) {
		double desired = 1.0 - (intensity * intensity * 0.4);
		if (Math.abs(client.options.fovEffectScale().get() - desired) > 0.001) {
			client.options.fovEffectScale().set(desired);
		}
	}

	private static void extractOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}

		update(client);

		if (!client.level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}

		float intensity = Mth.clamp(exposure, 0f, 1f);
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

}
