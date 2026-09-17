package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityGravityMixin {
	@Redirect(
		method = "*",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;getDefaultGravity()D"
		)
	)
	private double redirectGetGravity(Entity instance) {
		double originalGravity = ((EntityAccessor) instance).invokeGetGravity();

		Level world = ((EntityAccessor) instance).accessor$getWorld();
		if (world == null) {
			return originalGravity;
		}

		ResourceKey<Level> dimensionKey = world.dimension();
		double multiplier = ModDimensions.gravityMultiplier(dimensionKey);
		if (multiplier != 1.0) {
			return originalGravity * multiplier;
		}

		return originalGravity;
	}
}
