package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Arrow.class)
public class ArrowEntityGravityMixin {
	@ModifyConstant(
		method = "tick()V",
		constant = @Constant(doubleValue = -0.05D)
	)
	private double modifyArrowGravity(double original) {
		Arrow instance = (Arrow) (Object) this;

		Level world = ((EntityAccessor) instance).accessor$getWorld();
		if (world == null) {
			return original;
		}

		ResourceKey<Level> dimensionKey = world.dimension();
		double multiplier = ModDimensions.gravityMultiplier(dimensionKey);
		if (multiplier != 1.0) {
			return original * multiplier;
		}

		return original;
	}
}
