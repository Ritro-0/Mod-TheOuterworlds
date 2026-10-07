package com.theouterworld.mixin;

import com.theouterworld.util.AcidBreath;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityAcidBreathMixin {
	@Inject(method = "decreaseAirSupply", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$acidMembraneBreath(int currentSupply, CallbackInfoReturnable<Integer> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!AcidBreath.slowsDrowning(self)) {
			return;
		}
		// Skip every other loss so a full breath lasts twice as long.
		if ((self.tickCount & 1) == 0) {
			cir.setReturnValue(currentSupply);
		}
	}
}
