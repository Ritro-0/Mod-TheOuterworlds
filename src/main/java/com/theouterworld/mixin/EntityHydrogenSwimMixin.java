package com.theouterworld.mixin;

import com.theouterworld.registry.ModTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla sprint-swim pose only keys off {@code FluidTags.WATER}.
 * Treat liquid hydrogen and liquid helium the same way.
 */
@Mixin(Entity.class)
public class EntityHydrogenSwimMixin {
	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$swimInHydrogen(CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		TagKey<Fluid> cryogen = cryogenTag(self);
		if (cryogen == null) {
			return;
		}

		boolean eyesInCryogen = self.isEyeInFluid(cryogen);
		if (self.isSwimming()) {
			self.setSwimming(self.isSprinting() && !self.isPassenger());
		} else {
			self.setSwimming(
				self.isSprinting()
					&& eyesInCryogen
					&& !self.isPassenger()
					&& self.level().getFluidState(self.blockPosition()).is(cryogen)
			);
		}
		ci.cancel();
	}

	@Inject(method = "isVisuallyCrawling", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$hydrogenIsNotCrawl(CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if (self.getFluidHeight(ModTags.LIQUID_HYDROGEN) > 0.0
			|| self.getFluidHeight(ModTags.LIQUID_HELIUM) > 0.0
			|| self.getFluidHeight(ModTags.LIQUID_AMMONIA) > 0.0
			|| self.getFluidHeight(ModTags.LIQUID_METHANE) > 0.0) {
			cir.setReturnValue(false);
		}
	}

	private static TagKey<Fluid> cryogenTag(Entity self) {
		if (self.getFluidHeight(ModTags.LIQUID_HYDROGEN) > 0.0) {
			return ModTags.LIQUID_HYDROGEN;
		}
		if (self.getFluidHeight(ModTags.LIQUID_HELIUM) > 0.0) {
			return ModTags.LIQUID_HELIUM;
		}
		if (self.getFluidHeight(ModTags.LIQUID_AMMONIA) > 0.0) {
			return ModTags.LIQUID_AMMONIA;
		}
		if (self.getFluidHeight(ModTags.LIQUID_METHANE) > 0.0) {
			return ModTags.LIQUID_METHANE;
		}
		return null;
	}
}
