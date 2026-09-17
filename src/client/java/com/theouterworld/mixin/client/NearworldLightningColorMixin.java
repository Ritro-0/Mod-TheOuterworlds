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
	@Inject(method = "quad", at = @At("HEAD"), cancellable = true, require = 0)
	private static void theouterworlds$tintLightning(
		Matrix4fc pose,
		VertexConsumer buffer,
		float xo0,
		float zo0,
		int h,
		float xo1,
		float zo1,
		float boltRed,
		float boltGreen,
		float boltBlue,
		float rr1,
		float rr2,
		boolean px1,
		boolean pz1,
		boolean px2,
		boolean pz2,
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

		buffer.addVertex(pose, xo0 + (px1 ? rr2 : -rr2), h * 16, zo0 + (pz1 ? rr2 : -rr2)).setColor(r, g, b, 0.3F);
		buffer.addVertex(pose, xo1 + (px1 ? rr1 : -rr1), (h + 1) * 16, zo1 + (pz1 ? rr1 : -rr1)).setColor(r, g, b, 0.3F);
		buffer.addVertex(pose, xo1 + (px2 ? rr1 : -rr1), (h + 1) * 16, zo1 + (pz2 ? rr1 : -rr1)).setColor(r, g, b, 0.3F);
		buffer.addVertex(pose, xo0 + (px2 ? rr2 : -rr2), h * 16, zo0 + (pz2 ? rr2 : -rr2)).setColor(r, g, b, 0.3F);
		ci.cancel();
	}
}
