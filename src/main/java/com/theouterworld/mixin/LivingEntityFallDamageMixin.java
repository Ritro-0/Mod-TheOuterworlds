package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityFallDamageMixin {
	@ModifyVariable(method = "calculateFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1)
	private double theouterworlds$scaleFallDistance(double fallDistance) {
		LivingEntity self = (LivingEntity) (Object) this;
		Level level = self.level();
		if (level == null) {
			return fallDistance;
		}
		double gravity = ModDimensions.gravityMultiplier(level.dimension());
		if (gravity >= 1.0) {
			return fallDistance;
		}
		return fallDistance * gravity;
	}
}
