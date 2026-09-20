package com.theouterworld.mixin.client;

import com.theouterworld.client.AmberworldAtmosphere;
import com.theouterworld.client.NearworldAtmosphere;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nearworld / Amberworld keep a sulfuric-yellow sky/fog tint without rain collapsing
 * view distance into a wall of haze.
 */
@Mixin(AtmosphericFogEnvironment.class)
public class NearworldFogMixin {
	@Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
	private void theouterworlds$nearworldHazeColor(
		ClientLevel level,
		Camera camera,
		int renderDistance,
		float partialTicks,
		CallbackInfoReturnable<Vector3fc> cir
	) {
		if (level == null) {
			return;
		}
		if (ModDimensions.isAmberworld(level.dimension())) {
			cir.setReturnValue(ARGB.vector3fFromRGB24(AmberworldAtmosphere.HAZE_COLOR));
			return;
		}
		if (ModDimensions.isNearworld(level.dimension())) {
			cir.setReturnValue(ARGB.vector3fFromRGB24(NearworldAtmosphere.HAZE_COLOR));
		}
	}

	@Inject(method = "setupFog", at = @At("RETURN"))
	private void theouterworlds$nearworldHazeDistance(
		FogData fog,
		Camera camera,
		ClientLevel level,
		float renderDistance,
		DeltaTracker deltaTracker,
		CallbackInfo ci
	) {
		if (level == null) {
			return;
		}
		boolean amber = ModDimensions.isAmberworld(level.dimension());
		boolean near = ModDimensions.isNearworld(level.dimension());
		if (!amber && !near) {
			return;
		}
		// Perpetual rain would otherwise pull fog in to the camera. Keep a long yellow wash.
		float fogStart = amber ? AmberworldAtmosphere.FOG_START : NearworldAtmosphere.FOG_START;
		float fogEnd = amber ? AmberworldAtmosphere.FOG_END : NearworldAtmosphere.FOG_END;
		float skyFogEnd = amber ? AmberworldAtmosphere.SKY_FOG_END : NearworldAtmosphere.SKY_FOG_END;
		fog.environmentalStart = fogStart;
		fog.environmentalEnd = fogEnd;
		fog.skyEnd = Math.max(fog.skyEnd, skyFogEnd);
	}
}
