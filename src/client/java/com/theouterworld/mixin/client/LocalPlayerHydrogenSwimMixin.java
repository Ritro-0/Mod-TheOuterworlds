package com.theouterworld.mixin.client;

import com.theouterworld.registry.ModTags;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla swim-sprint cancel checks {@code isInWater()}. Hydrogen / helium are not water, so sprint
 * would immediately stop as soon as the swim pose started.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerHydrogenSwimMixin {
	@Shadow
	private boolean isSprintingPossible(boolean allowedInShallowWater) {
		throw new AssertionError();
	}

	@Inject(method = "shouldStopSwimSprinting", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$keepSprintInHydrogen(CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer self = (LocalPlayer) (Object) this;
		if (self.getFluidHeight(ModTags.LIQUID_HYDROGEN) <= 0.0
			&& self.getFluidHeight(ModTags.LIQUID_HELIUM) <= 0.0
			&& self.getFluidHeight(ModTags.LIQUID_AMMONIA) <= 0.0
			&& self.getFluidHeight(ModTags.LIQUID_METHANE) <= 0.0) {
			return;
		}

		boolean stop = !this.isSprintingPossible(true)
			|| !self.input.hasForwardImpulse() && !self.onGround() && !self.input.keyPresses.shift();
		cir.setReturnValue(stop);
	}
}
