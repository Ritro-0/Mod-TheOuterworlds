package com.theouterworld.mixin;

import com.theouterworld.util.JarositeProtection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityJarositeFireMixin {
	@Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$jarositeFireImmune(CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if (self instanceof LivingEntity living && JarositeProtection.hasJarositeTrim(living)) {
			cir.setReturnValue(true);
			return;
		}
		if (self instanceof ItemEntity item && JarositeProtection.isProtectedItem(item.getItem())) {
			cir.setReturnValue(true);
		}
	}
}
