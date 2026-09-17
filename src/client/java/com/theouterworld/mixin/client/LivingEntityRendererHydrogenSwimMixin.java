package com.theouterworld.mixin.client;

import com.theouterworld.registry.ModTags;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Avatar swim pitch only follows look angle when {@code isInWater} is true.
 * Hydrogen / helium are not water, so without this the body stays locked at -90°.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererHydrogenSwimMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void theouterworlds$tiltSwimInHydrogen(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		if (entity.getFluidHeight(ModTags.LIQUID_HYDROGEN) > 0.0
			|| entity.getFluidHeight(ModTags.LIQUID_HELIUM) > 0.0
			|| entity.getFluidHeight(ModTags.LIQUID_AMMONIA) > 0.0
			|| entity.getFluidHeight(ModTags.LIQUID_METHANE) > 0.0) {
			state.isInWater = true;
		}
	}
}
