package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityElytraFrictionMixin {
	@Inject(method = "aiStep()V", at = @At("TAIL"))
	private void theouterworlds$adjustElytraFriction(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!((EntityAccessor) self).invokeGetFlag(7)) {
			return;
		}

		Level world = ((EntityAccessor) self).accessor$getWorld();
		if (world == null || !ModDimensions.isLowGravity(world.dimension())) {
			return;
		}

		final double desiredFrictionFraction = 0.00337D;
		final double baseFrictionXZ = 0.99D;
		final double baseFrictionY = 0.98D;
		final double newFrictionXZ = 1.0D - (1.0D - baseFrictionXZ) * desiredFrictionFraction;
		final double newFrictionY = 1.0D - (1.0D - baseFrictionY) * desiredFrictionFraction;
		final double compensationXZ = newFrictionXZ / baseFrictionXZ;
		final double compensationY = newFrictionY / baseFrictionY;

		Vec3 v = self.getDeltaMovement();
		self.setDeltaMovement(v.x * compensationXZ, v.y * compensationY, v.z * compensationXZ);
	}
}
