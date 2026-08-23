package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents vanilla rain, snow, and storms in the Outerworld and Innerworld.
 * The Outerworld is Mars — it never rains water. The Innerworld is the Moon.
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

		if (ModDimensions.isOuterworld(world.dimension()) || ModDimensions.isInnerworld(world.dimension())) {
			ci.cancel();
		}
	}

	@Inject(
		method = "setWeatherParameters(IIZZ)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void preventVanillaWeatherSetInOuterworld(
		int clearDuration,
		int rainDuration,
		boolean raining,
		boolean thundering,
		CallbackInfo ci
	) {
		ServerLevel world = (ServerLevel) (Object) this;

		if (ModDimensions.isOuterworld(world.dimension()) || ModDimensions.isInnerworld(world.dimension())) {
			if (raining || thundering) {
				ci.cancel();
			}
		}
	}
}
