package com.theouterworld.mixin;

import com.theouterworld.util.JarositeProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityJarositeMixin {
	@Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$jarositeTrimImmunity(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (JarositeProtection.isProtectedDamage(source) && JarositeProtection.hasJarositeTrim(self)) {
			cir.setReturnValue(true);
		}
	}
}
