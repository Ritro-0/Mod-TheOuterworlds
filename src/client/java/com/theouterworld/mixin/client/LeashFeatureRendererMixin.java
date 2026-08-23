package com.theouterworld.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.theouterworld.client.OpalineNickelFlailRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.LeashFeatureRenderer;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LeashFeatureRenderer.class)
public class LeashFeatureRendererMixin {
	private static final float VANILLA_LEASH_RED = 0.5F;
	private static final float CHARCOAL_RED = 0.25F;
	private static final float CHARCOAL_GREEN = 0.25F;
	private static final float CHARCOAL_BLUE = 0.27F;

	@Redirect(
		method = "addVertexPair",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
		)
	)
	private static VertexConsumer theouterworlds$tintFlailChain(
		VertexConsumer consumer,
		float red,
		float green,
		float blue,
		float alpha,
		VertexConsumer vertices,
		Matrix4fc pose,
		float dx,
		float dy,
		float dz,
		float width,
		float xOffset,
		float zOffset,
		int index,
		boolean reverse,
		EntityRenderState.LeashState leashState
	) {
		if (leashState instanceof OpalineNickelFlailRenderer.CharcoalLeashState) {
			float stripe = red / VANILLA_LEASH_RED;
			return consumer.setColor(CHARCOAL_RED * stripe, CHARCOAL_GREEN * stripe, CHARCOAL_BLUE * stripe, alpha);
		}
		return consumer.setColor(red, green, blue, alpha);
	}
}
