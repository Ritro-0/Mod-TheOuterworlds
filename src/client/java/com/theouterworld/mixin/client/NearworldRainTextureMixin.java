package com.theouterworld.mixin.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Nearworld uses sulfuric rain; Farworld uses diamond rain.
 */
@Mixin(WeatherEffectRenderer.class)
public class NearworldRainTextureMixin {
	@Unique
	private static final Identifier VANILLA_RAIN = Identifier.withDefaultNamespace("textures/environment/rain.png");
	@Unique
	private static final Identifier SULFURIC_RAIN = OuterWorldMod.id("textures/environment/sulfuric_rain.png");
	@Unique
	private static final Identifier DIAMOND_RAIN = OuterWorldMod.id("textures/environment/diamond_rain.png");

	@Redirect(
		method = "prepare",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/texture/TextureManager;getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;"
		)
	)
	private AbstractTexture theouterworlds$customRain(TextureManager manager, Identifier location) {
		if (location.equals(VANILLA_RAIN)) {
			Level level = Minecraft.getInstance().level;
			if (level != null && ModDimensions.isNearworld(level.dimension())) {
				return manager.getTexture(SULFURIC_RAIN);
			}
			if (level != null && ModDimensions.isFarworld(level.dimension())) {
				return manager.getTexture(DIAMOND_RAIN);
			}
		}
		return manager.getTexture(location);
	}
}
