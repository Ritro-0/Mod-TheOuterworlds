package com.theouterworld.mixin.client;

import com.theouterworld.client.DeepworldAtmosphere;
import com.theouterworld.client.EdgeworldAtmosphere;
import com.theouterworld.client.FarworldAtmosphere;
import com.theouterworld.client.HighworldAtmosphere;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Highworld / Deepworld natural light and haze fall off with depth. Block light is untouched;
 * lightning sky-flash stays full strength; night vision is a separate lightmap path.
 */
@Mixin(ClientLevel.class)
public abstract class HighworldSkyLightMixin {
	@Shadow
	private int skyFlashTime;

	@Inject(method = "addEnvironmentAttributeLayers", at = @At("RETURN"))
	private void theouterworlds$highworldDepthSkyLight(
		EnvironmentAttributeSystem.Builder environmentAttributes,
		CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> cir
	) {
		ClientLevel self = (ClientLevel) (Object) this;
		EnvironmentAttributeSystem.Builder builder = cir.getReturnValue();

		builder.addPositionalLayer(
			EnvironmentAttributes.SKY_LIGHT_FACTOR,
			(value, pos, interpolator) -> {
				if (this.skyFlashTime > 0) {
					return value;
				}
				if (ModDimensions.isHighworld(self.dimension())) {
					return value * HighworldAtmosphere.naturalSkyFactor(pos.y);
				}
				if (ModDimensions.isDeepworld(self.dimension())) {
					return value * DeepworldAtmosphere.naturalSkyFactor(pos.y);
				}
				if (ModDimensions.isFarworld(self.dimension())) {
					return value * FarworldAtmosphere.naturalSkyFactor(pos.y);
				}
				if (ModDimensions.isEdgeworld(self.dimension())) {
					return value * EdgeworldAtmosphere.naturalSkyFactor(pos.y);
				}
				return value;
			}
		);
		// Fog/sky/ambient were staying bright amber, so only the hand looked darker.
		builder.addPositionalLayer(
			EnvironmentAttributes.FOG_COLOR,
			(value, pos, interpolator) -> {
				if (this.skyFlashTime > 0) {
					return value;
				}
				if (ModDimensions.isHighworld(self.dimension())) {
					return HighworldAtmosphere.depthFogColor(value, pos.y);
				}
				if (ModDimensions.isDeepworld(self.dimension())) {
					return DeepworldAtmosphere.depthFogColor(value, pos.y);
				}
				if (ModDimensions.isFarworld(self.dimension())) {
					return FarworldAtmosphere.depthFogColor(value, pos.y);
				}
				if (ModDimensions.isEdgeworld(self.dimension())) {
					return EdgeworldAtmosphere.depthFogColor(value, pos.y);
				}
				return value;
			}
		);
		builder.addPositionalLayer(
			EnvironmentAttributes.SKY_COLOR,
			(value, pos, interpolator) -> {
				if (this.skyFlashTime > 0) {
					return value;
				}
				if (ModDimensions.isHighworld(self.dimension())) {
					return HighworldAtmosphere.depthSkyColor(value, pos.y);
				}
				if (ModDimensions.isDeepworld(self.dimension())) {
					return DeepworldAtmosphere.depthSkyColor(value, pos.y);
				}
				if (ModDimensions.isFarworld(self.dimension())) {
					return FarworldAtmosphere.depthSkyColor(value, pos.y);
				}
				if (ModDimensions.isEdgeworld(self.dimension())) {
					return EdgeworldAtmosphere.depthSkyColor(value, pos.y);
				}
				return value;
			}
		);
		builder.addPositionalLayer(
			EnvironmentAttributes.AMBIENT_LIGHT_COLOR,
			(value, pos, interpolator) -> {
				if (this.skyFlashTime > 0) {
					return value;
				}
				if (ModDimensions.isHighworld(self.dimension())) {
					return HighworldAtmosphere.depthAmbientColor(value, pos.y);
				}
				if (ModDimensions.isDeepworld(self.dimension())) {
					return DeepworldAtmosphere.depthAmbientColor(value, pos.y);
				}
				if (ModDimensions.isFarworld(self.dimension())) {
					return FarworldAtmosphere.depthAmbientColor(value, pos.y);
				}
				if (ModDimensions.isEdgeworld(self.dimension())) {
					return EdgeworldAtmosphere.depthAmbientColor(value, pos.y);
				}
				return value;
			}
		);
	}
}
