package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents vanilla weather cycle advances in mod dimensions.
 * Outerworld/Moon/Innerworld have no weather; Nearworld forces rain levels separately
 * without mutating the server-global WeatherData shared with the Overworld.
 */
@Mixin(ServerLevel.class)
public class OuterworldWeatherBlockerMixin {
	@Inject(
		method = "advanceWeatherCycle",
		at = @At("HEAD"),
		cancellable = true
	)
	private void preventVanillaWeatherInOuterworld(CallbackInfo ci) {
		ServerLevel world = (ServerLevel) (Object) this;

		if (ModDimensions.isVacuum(world.dimension())) {
			ci.cancel();
		}
	}
}
