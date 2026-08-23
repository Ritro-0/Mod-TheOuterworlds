package com.theouterworld.mixin;

import com.theouterworld.util.JarositeProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityJarositeMixin {
	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$protectJarositeItems(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (JarositeProtection.isProtectedDamage(source) && JarositeProtection.isProtectedItem(self.getItem())) {
			cir.setReturnValue(false);
		}
	}
}
