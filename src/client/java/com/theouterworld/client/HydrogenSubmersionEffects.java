package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModTags;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Subtle submerged HUD overlay + local ambient FX while the camera is in liquid hydrogen.
 */
public final class HydrogenSubmersionEffects {
	private static final Identifier HUD_ID = OuterWorldMod.id("hydrogen_submersion_overlay");
	private static final Identifier OVERLAY = Identifier.withDefaultNamespace("textures/misc/underwater.png");
	/** Teal wash matching liquid hydrogen (ARGB). */
	private static final int OVERLAY_COLOR = 0x55A8D4FF;
	private static final int AMBIENT_INTERVAL_TICKS = 55;

	private static int ambientCooldown;

	private HydrogenSubmersionEffects() {
	}

	public static void register() {
		HudElementRegistry.addLast(HUD_ID, HydrogenSubmersionEffects::extractOverlay);
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
	}

	private static void extractOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null) {
			return;
		}
		if (!player.isEyeInFluid(ModTags.LIQUID_HYDROGEN)
			&& !player.isEyeInFluid(ModTags.LIQUID_HELIUM)
			&& !player.isEyeInFluid(ModTags.LIQUID_AMMONIA)
			&& !player.isEyeInFluid(ModTags.LIQUID_METHANE)) {
			return;
		}

		int width = client.getWindow().getGuiScaledWidth();
		int height = client.getWindow().getGuiScaledHeight();
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			OVERLAY,
			0,
			0,
			0.0F,
			0.0F,
			width,
			height,
			width,
			height,
			OVERLAY_COLOR
		);
	}

	private static void tick(Minecraft client) {
		Player player = client.player;
		if (player == null || client.isPaused()) {
			return;
		}
		if (!player.isEyeInFluid(ModTags.LIQUID_HYDROGEN)
			&& !player.isEyeInFluid(ModTags.LIQUID_HELIUM)
			&& !player.isEyeInFluid(ModTags.LIQUID_AMMONIA)
			&& !player.isEyeInFluid(ModTags.LIQUID_METHANE)) {
			ambientCooldown = 0;
			return;
		}

		RandomSource random = player.getRandom();
		if (ambientCooldown > 0) {
			ambientCooldown--;
		} else if (random.nextInt(3) == 0) {
			ambientCooldown = AMBIENT_INTERVAL_TICKS + random.nextInt(40);
			player.level().playLocalSound(
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.LAVA_AMBIENT,
				SoundSource.AMBIENT,
				0.18F + random.nextFloat() * 0.1F,
				1.55F + random.nextFloat() * 0.35F,
				false
			);
		}

		if (random.nextInt(8) == 0) {
			player.level().addParticle(
				ParticleTypes.BUBBLE,
				player.getX() + (random.nextDouble() - 0.5) * 0.8,
				player.getEyeY() + (random.nextDouble() - 0.5) * 0.4,
				player.getZ() + (random.nextDouble() - 0.5) * 0.8,
				0.0,
				0.02,
				0.0
			);
		} else if (random.nextInt(14) == 0) {
			player.level().addParticle(
				ParticleTypes.CLOUD,
				player.getX() + (random.nextDouble() - 0.5),
				player.getY() + random.nextDouble(),
				player.getZ() + (random.nextDouble() - 0.5),
				0.0,
				0.01,
				0.0
			);
		}
	}
}
