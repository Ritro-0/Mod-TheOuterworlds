package com.theouterworld.mixin.client;

import com.theouterworld.item.AerostatBalloonItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hold the aerostat balloon arm straight overhead.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelAerostatMixin<T extends HumanoidRenderState> {
	private static final float ARM_STRAIGHT_UP = -Mth.PI;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
	private void theouterworlds$raiseBalloonArm(T state, CallbackInfo ci) {
		HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
		if (state.rightHandItemStack.getItem() instanceof AerostatBalloonItem) {
			self.rightArm.xRot = ARM_STRAIGHT_UP;
			self.rightArm.yRot = 0.0F;
			self.rightArm.zRot = 0.0F;
		}
		if (state.leftHandItemStack.getItem() instanceof AerostatBalloonItem) {
			self.leftArm.xRot = ARM_STRAIGHT_UP;
			self.leftArm.yRot = 0.0F;
			self.leftArm.zRot = 0.0F;
		}
	}
}
