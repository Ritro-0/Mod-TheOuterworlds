package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.network.SunDeathSyncPacket;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.SunDeathSequence;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Client visuals for The Sun death sequence: heat overlay, whiteout, punchline text.
 */
public final class SunDeathVisualEffects {
	private static final Identifier HEAT_OVERLAY = OuterWorldMod.id("textures/misc/irradiating_heat_overlay.png");
	private static final Identifier HUD_ID = OuterWorldMod.id("sun_death_overlay");
	private static final Component PUNCHLINE = Component.translatable("gui.theouterworlds.sun.punchline");

	private static int serverTicks = -1;
	private static int localTicks = -1;

	private SunDeathVisualEffects() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(SunDeathSyncPacket.ID, (packet, context) -> {
			serverTicks = packet.ticks();
			localTicks = packet.ticks();
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level == null || client.player == null || !ModDimensions.isSun(client.level.dimension())) {
				serverTicks = -1;
				localTicks = -1;
				return;
			}
			if (client.player.isCreative() || client.player.isSpectator()) {
				serverTicks = -1;
				localTicks = -1;
				return;
			}
			if (localTicks >= 0) {
				localTicks++;
			}
		});
		HudElementRegistry.addLast(HUD_ID, SunDeathVisualEffects::extractOverlay);
	}

	private static void extractOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null || !ModDimensions.isSun(client.level.dimension())) {
			return;
		}
		if (client.player.isCreative() || client.player.isSpectator()) {
			return;
		}
		int ticks = localTicks >= 0 ? localTicks : serverTicks;
		if (ticks < 0) {
			return;
		}

		int width = client.getWindow().getGuiScaledWidth();
		int height = client.getWindow().getGuiScaledHeight();

		if (ticks >= SunDeathSequence.GRACE_TICKS) {
			float burnProgress = Mth.clamp(
				(ticks - SunDeathSequence.GRACE_TICKS) / (float) SunDeathSequence.BURN_TICKS,
				0.0F,
				1.0F
			);
			// Much faster and stronger than heated-world overlay.
			float intensity = Math.min(1.0F, burnProgress * 1.75F);
			int tintAlpha = (int) (intensity * 160.0F);
			graphics.fill(0, 0, width, height, (tintAlpha << 24) | 0xFF4A00);
			int textureAlpha = (int) (intensity * 255.0F);
			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				HEAT_OVERLAY,
				0, 0,
				0.0F, 0.0F,
				width, height,
				256, 256,
				(textureAlpha << 24) | 0xFFFFFF
			);
		}

		int whiteStart = SunDeathSequence.GRACE_TICKS + SunDeathSequence.BURN_TICKS;
		if (ticks >= whiteStart) {
			float whiteProgress = Mth.clamp(
				(ticks - whiteStart) / (float) SunDeathSequence.WHITE_TICKS,
				0.0F,
				1.0F
			);
			int whiteAlpha = (int) (whiteProgress * 255.0F);
			graphics.fill(0, 0, width, height, (whiteAlpha << 24) | 0xFFFFFF);
		}

		int textStart = whiteStart + SunDeathSequence.WHITE_TICKS;
		if (ticks >= textStart) {
			graphics.fill(0, 0, width, height, 0xFFFFFFFF);
			int textWidth = client.font.width(PUNCHLINE);
			graphics.text(
				client.font,
				PUNCHLINE,
				(width - textWidth) / 2,
				height / 2 - 4,
				0xFF000000,
				false
			);
		}
	}
}
