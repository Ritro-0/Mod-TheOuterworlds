package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.util.GlassHelmetUtil;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class GlassHelmetOverlay {
	private static final Identifier HUD_ID = OuterWorldMod.id("glass_helmet_overlay");

	private GlassHelmetOverlay() {}

	public static void register() {
		HudElementRegistry.addLast(HUD_ID, GlassHelmetOverlay::extractRenderState);
	}

	private static void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}

		ItemStack head = client.player.getItemBySlot(EquipmentSlot.HEAD);
		if (!GlassHelmetUtil.isGlassHelmetItem(head)) {
			return;
		}

		Identifier overlayTexture = GlassHelmetUtil.getGlassTextureId(head);
		if (overlayTexture == null) {
			return;
		}

		int width = client.getWindow().getGuiScaledWidth();
		int height = client.getWindow().getGuiScaledHeight();
		int alpha = 90;
		int color = (alpha << 24) | 0xFFFFFF;

		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			overlayTexture,
			0, 0,
			0.0f, 0.0f,
			width, height,
			width, height,
			color
		);
	}
}
