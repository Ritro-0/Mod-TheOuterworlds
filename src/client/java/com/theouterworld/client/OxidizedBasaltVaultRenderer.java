package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.OxidizedBasaltVaultBlock;
import com.theouterworld.block.OxidizedBasaltVaultBlockEntity;
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

/**
 * Draws the lid of the Oxidized Basalt Vault. The base and the four stilts are the normal block model; the lid is
 * the same box as in the model json, lifted straight up until it rests on top of the stilts.
 */
public class OxidizedBasaltVaultRenderer implements BlockEntityRenderer<OxidizedBasaltVaultBlockEntity, OxidizedBasaltVaultRenderer.VaultRenderState> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/block/oxidized_basalt_vault.png");

	// Lid box from oxidized_basalt_vault.json (pixels).
	private static final float MIN_X = 1.0F;
	private static final float MIN_Y = 9.1F;
	private static final float MIN_Z = 1.0F;
	private static final float MAX_X = 15.0F;
	private static final float MAX_Y = 14.1F;
	private static final float MAX_Z = 15.0F;
	/** The stilts end at y = 14, so the lid's underside (y = 9.1) has to rise by 4.9 px to sit flat on them. */
	private static final float LIFT_PIXELS = 14.0F - MIN_Y;

	// UVs per face from the model json, in 16-unit texture space: u0, v0, u1, v1.
	private static final float[] UV_NORTH = {3.5F, 3.5F, 7.0F, 4.75F};
	private static final float[] UV_EAST = {0.0F, 3.5F, 3.5F, 4.75F};
	private static final float[] UV_SOUTH = {10.5F, 3.5F, 14.0F, 4.75F};
	private static final float[] UV_WEST = {7.0F, 3.5F, 10.5F, 4.75F};
	private static final float[] UV_UP = {10.5F, 3.5F, 7.0F, 0.0F};
	private static final float[] UV_DOWN = {7.0F, 0.0F, 3.5F, 3.5F};

	public OxidizedBasaltVaultRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public VaultRenderState createRenderState() {
		return new VaultRenderState();
	}

	@Override
	public void extractRenderState(
		OxidizedBasaltVaultBlockEntity blockEntity,
		VaultRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.facing = blockEntity.getBlockState().hasProperty(OxidizedBasaltVaultBlock.FACING)
			? blockEntity.getBlockState().getValue(OxidizedBasaltVaultBlock.FACING)
			: Direction.NORTH;
		float openness = blockEntity.getOpenNess(tickProgress);
		// Same easing the chest lid uses.
		float inverse = 1.0F - openness;
		state.openness = 1.0F - inverse * inverse * inverse;
	}

	@Override
	public void submit(VaultRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		// Block state rotation: north = 0, east = 90, south = 180, west = 270.
		poseStack.rotateDegrees(Axis.YP, (state.facing.toYRot() + 180.0F) % 360.0F);
		poseStack.translate(-0.5F, state.openness * LIFT_PIXELS / 16.0F, -0.5F);

		int light = state.lightCoords;
		queue.submitCustomGeometry(poseStack, RenderTypes.armorCutoutNoCull(TEXTURE), (pose, consumer) -> writeLid(pose, consumer, light));
		poseStack.popPose();
	}

	private static void writeLid(PoseStack.Pose pose, VertexConsumer consumer, int light) {
		float x0 = MIN_X / 16.0F;
		float y0 = MIN_Y / 16.0F;
		float z0 = MIN_Z / 16.0F;
		float x1 = MAX_X / 16.0F;
		float y1 = MAX_Y / 16.0F;
		float z1 = MAX_Z / 16.0F;

		// Vertex order matches the vanilla block model baker so the model json's UVs map identically.
		quad(pose, consumer, light, UV_DOWN, 0, -1, 0,
			x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
		quad(pose, consumer, light, UV_UP, 0, 1, 0,
			x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
		quad(pose, consumer, light, UV_NORTH, 0, 0, -1,
			x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0);
		quad(pose, consumer, light, UV_SOUTH, 0, 0, 1,
			x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
		quad(pose, consumer, light, UV_WEST, -1, 0, 0,
			x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
		quad(pose, consumer, light, UV_EAST, 1, 0, 0,
			x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0);
	}

	private static void quad(
		PoseStack.Pose pose,
		VertexConsumer consumer,
		int light,
		float[] uv,
		float nx,
		float ny,
		float nz,
		float... p
	) {
		float u0 = uv[0] / 16.0F;
		float v0 = uv[1] / 16.0F;
		float u1 = uv[2] / 16.0F;
		float v1 = uv[3] / 16.0F;
		float[] us = {u0, u0, u1, u1};
		float[] vs = {v0, v1, v1, v0};
		for (int i = 0; i < 4; i++) {
			consumer.addVertex(pose, p[i * 3], p[i * 3 + 1], p[i * 3 + 2])
				.setColor(255, 255, 255, 255)
				.setUv(us[i], vs[i])
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, nx, ny, nz);
		}
	}

	public static class VaultRenderState extends BlockEntityRenderState {
		public Direction facing = Direction.NORTH;
		public float openness;
	}
}
