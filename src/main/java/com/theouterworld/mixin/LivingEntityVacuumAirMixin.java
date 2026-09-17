package com.theouterworld.mixin;

import com.theouterworld.world.VacuumSuffocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityVacuumAirMixin {
	@Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$holdAirInVacuum(int currentSupply, CallbackInfoReturnable<Integer> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (VacuumSuffocation.shouldSuffocate(self)) {
			cir.setReturnValue(currentSupply);
		}
	}
}
