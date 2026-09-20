package com.theouterworld.mixin.client;

import com.theouterworld.client.DeepworldAtmosphere;
import com.theouterworld.client.EdgeworldAtmosphere;
import com.theouterworld.client.FarworldAtmosphere;
import com.theouterworld.client.HighworldAtmosphere;
import com.theouterworld.client.NearworldAtmosphere;
import com.theouterworld.registry.ModDimensions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.world.level.Level;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tints lightning bolts sulfuric yellow in Nearworld, deep red in Highworld,
 * orange in Deepworld.
 */
@Mixin(LightningBoltRenderer.class)
public class NearworldLightningColorMixin {
	@Inject(method = "quad", at = @At("HEAD"), cancellable = true)
	private static void theouterworlds$tintLightning(
		Matrix4fc pose,
		VertexConsumer buffer,
		float segmentStartX,
		float segmentStartZ,
		float segmentEndX,
		float segmentEndZ,
		int currentSegment,
		float topRadius,
		float bottomRadius,
		boolean rightXPositive,
		boolean rightZPositive,
		boolean leftXPositive,
		boolean leftZPositive,
		CallbackInfo ci
	) {
		Level level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}

		float r;
		float g;
		float b;
		if (ModDimensions.isNearworld(level.dimension())) {
			r = NearworldAtmosphere.LIGHTNING_RED;
			g = NearworldAtmosphere.LIGHTNING_GREEN;
			b = NearworldAtmosphere.LIGHTNING_BLUE;
		} else if (ModDimensions.isHighworld(level.dimension())) {
			r = HighworldAtmosphere.LIGHTNING_RED;
			g = HighworldAtmosphere.LIGHTNING_GREEN;
			b = HighworldAtmosphere.LIGHTNING_BLUE;
		} else if (ModDimensions.isDeepworld(level.dimension())) {
			r = DeepworldAtmosphere.LIGHTNING_RED;
			g = DeepworldAtmosphere.LIGHTNING_GREEN;
			b = DeepworldAtmosphere.LIGHTNING_BLUE;
		} else if (ModDimensions.isFarworld(level.dimension())) {
			r = FarworldAtmosphere.LIGHTNING_RED;
			g = FarworldAtmosphere.LIGHTNING_GREEN;
			b = FarworldAtmosphere.LIGHTNING_BLUE;
		} else if (ModDimensions.isEdgeworld(level.dimension())) {
			r = EdgeworldAtmosphere.LIGHTNING_RED;
			g = EdgeworldAtmosphere.LIGHTNING_GREEN;
			b = EdgeworldAtmosphere.LIGHTNING_BLUE;
		} else {
			return;
		}

		buffer.addVertex(
			pose,
			segmentStartX + (rightXPositive ? bottomRadius : -bottomRadius),
			currentSegment * 16,
			segmentStartZ + (rightZPositive ? bottomRadius : -bottomRadius)
		).setColor(r, g, b, 0.3F);
		buffer.addVertex(
			pose,
			segmentEndX + (rightXPositive ? topRadius : -topRadius),
			(currentSegment + 1) * 16,
			segmentEndZ + (rightZPositive ? topRadius : -topRadius)
		).setColor(r, g, b, 0.3F);
		buffer.addVertex(
			pose,
			segmentEndX + (leftXPositive ? topRadius : -topRadius),
			(currentSegment + 1) * 16,
			segmentEndZ + (leftZPositive ? topRadius : -topRadius)
		).setColor(r, g, b, 0.3F);
		buffer.addVertex(
			pose,
			segmentStartX + (leftXPositive ? bottomRadius : -bottomRadius),
			currentSegment * 16,
			segmentStartZ + (leftZPositive ? bottomRadius : -bottomRadius)
		).setColor(r, g, b, 0.3F);
		ci.cancel();
	}
}
