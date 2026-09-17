package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Scales leftover vanilla elytra air drag to the world's atmosphere and gravity.
 * Vacuums fully cancel vanilla air friction; Venus / gas giants add drag.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityElytraFrictionMixin {
	private static final double BASE_FRICTION_XZ = 0.99D;
	private static final double BASE_FRICTION_Y = 0.98D;

	@Inject(method = "aiStep()V", at = @At("TAIL"))
	private void theouterworlds$adjustElytraFriction(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!((EntityAccessor) self).invokeGetFlag(7)) {
			return;
		}

		Level world = ((EntityAccessor) self).accessor$getWorld();
		if (world == null) {
			return;
		}

		double dragFraction = ModDimensions.elytraDragFraction(world.dimension());
		if (Math.abs(dragFraction - 1.0D) < 0.001D) {
			return;
		}

		Vec3 v = self.getDeltaMovement();
		if (dragFraction <= 0.0D) {
			// Undo vanilla elytra air friction completely in vacuum.
			self.setDeltaMovement(v.x / BASE_FRICTION_XZ, v.y / BASE_FRICTION_Y, v.z / BASE_FRICTION_XZ);
			return;
		}

		double newFrictionXZ = clampFriction(1.0D - (1.0D - BASE_FRICTION_XZ) * dragFraction);
		double newFrictionY = clampFriction(1.0D - (1.0D - BASE_FRICTION_Y) * dragFraction);
		self.setDeltaMovement(
			v.x * (newFrictionXZ / BASE_FRICTION_XZ),
			v.y * (newFrictionY / BASE_FRICTION_Y),
			v.z * (newFrictionXZ / BASE_FRICTION_XZ)
		);
	}

	private static double clampFriction(double friction) {
		return Math.max(0.50D, Math.min(0.99999D, friction));
	}
}
