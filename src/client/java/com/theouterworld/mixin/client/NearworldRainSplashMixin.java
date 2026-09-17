package com.theouterworld.mixin.client;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Custom rain sheets only — skip vanilla splash droplets in Nearworld / Farworld.
 */
@Mixin(ClientLevel.class)
public class NearworldRainSplashMixin {
	@Inject(method = "tickWeatherEffects", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$skipVanillaRainSplash(CallbackInfo ci) {
		ClientLevel level = (ClientLevel) (Object) this;
		if (ModDimensions.isNearworld(level.dimension()) || ModDimensions.isFarworld(level.dimension())) {
			ci.cancel();
		}
	}
}
