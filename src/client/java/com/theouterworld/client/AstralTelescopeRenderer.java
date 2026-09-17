package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.AstralTelescopeBlock;
import com.theouterworld.block.AstralTelescopeBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AstralTelescopeRenderer implements BlockEntityRenderer<AstralTelescopeBlockEntity, AstralTelescopeRenderer.TelescopeRenderState> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/block/astral_telescope.png");
	/** OBJ tube points northwest; +45° aligns the lens with north (−Z). */
	private static final float MODEL_YAW_OFFSET = 45.0F;

	public AstralTelescopeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public TelescopeRenderState createRenderState() {
		return new TelescopeRenderState();
	}

	@Override
	public void extractRenderState(
		AstralTelescopeBlockEntity blockEntity,
		TelescopeRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.facing = blockEntity.getBlockState().getValue(AstralTelescopeBlock.FACING);
	}

	@Override
	public void submit(TelescopeRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		// Align OBJ (NW) → north, then rotate so FACING is the look direction.
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot() + 180.0F + MODEL_YAW_OFFSET));

		int light = state.lightCoords;
		int overlay = OverlayTexture.NO_OVERLAY;
		queue.submitCustomGeometry(poseStack, RenderTypes.armorCutoutNoCull(TEXTURE), (pose, consumer) ->
			writeMesh(pose, consumer, AstralTelescopeModelData.MESH, light, overlay)
		);
		poseStack.popPose();
	}

	private static void writeMesh(PoseStack.Pose pose, VertexConsumer consumer, float[] data, int light, int overlay) {
		for (int i = 0; i < data.length; i += 8) {
			consumer.addVertex(pose, data[i], data[i + 1], data[i + 2])
				.setColor(255, 255, 255, 255)
				.setUv(data[i + 3], data[i + 4])
				.setOverlay(overlay)
				.setLight(light)
				.setNormal(pose, data[i + 5], data[i + 6], data[i + 7]);
		}
	}

	public static class TelescopeRenderState extends BlockEntityRenderState {
		public Direction facing = Direction.NORTH;
	}
}
