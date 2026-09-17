package com.theouterworld.mixin;

import com.theouterworld.block.AerogelCloudBlock;
import com.theouterworld.item.AerostatBalloonItem;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Balloon: zero gravity while held, then apply dimension-independent lift.
 * Clouds: tiny downward pull (quicksand) when not ballooning.
 */
@Mixin(LivingEntity.class)
public class LivingEntityCloudGravityMixin {
	@Inject(method = "getEffectiveGravity", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$balloonOrCloudGravity(CallbackInfoReturnable<Double> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (AerostatBalloonItem.isActive(self)) {
			cir.setReturnValue(0.0);
			return;
		}
		if (AerogelCloudBlock.isInCloud(self)) {
			cir.setReturnValue(0.008);
		}
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void theouterworlds$balloonLift(CallbackInfo ci) {
		AerostatBalloonItem.tickHeld((LivingEntity) (Object) this);
	}
}
