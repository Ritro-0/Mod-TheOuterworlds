package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.theouterworld.block.RiftBlock;
import com.theouterworld.block.RiftBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RiftBlockEntityRenderer implements BlockEntityRenderer<RiftBlockEntity, RiftBlockEntityRenderer.RiftRenderState> {
	public RiftBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public RiftRenderState createRenderState() {
		return new RiftRenderState();
	}

	@Override
	public void extractRenderState(
		RiftBlockEntity blockEntity,
		RiftRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		Level level = blockEntity.getLevel();
		BlockPos pos = blockEntity.getBlockPos();
		BlockState blockState = blockEntity.getBlockState();
		state.tickProgress = tickProgress;
		state.gameTime = level != null ? level.getGameTime() : 0L;
		state.upperHalf = blockState.hasProperty(RiftBlock.HALF) && blockState.getValue(RiftBlock.HALF) == DoubleBlockHalf.UPPER;
		state.anchorPos = state.upperHalf ? pos.below() : pos;
		state.movingBlock = createMovingBlock(pos, blockState, level);
	}

	@Override
	public void submit(RiftRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState cameraState) {
		if (state.movingBlock == null) {
			return;
		}

		float t = state.gameTime + state.tickProgress;
		int seed = state.anchorPos.hashCode();
		float bob = Mth.sin(t * 0.13F + seed * 0.11F) * 0.04F + Mth.sin(t * 0.07F + seed * 0.19F) * 0.02F;
		float swayX = Mth.sin(t * 0.09F + seed * 0.13F) * 0.04F;
		float swayZ = Mth.cos(t * 0.11F + seed * 0.17F) * 0.04F;
		float tiltZ = Mth.sin(t * 0.08F + seed * 0.05F) * 5.5F;
		float tiltX = Mth.cos(t * 0.06F + seed * 0.08F) * 3.5F;
		float pivotY = state.upperHalf ? 0.0F : 1.0F;

		poseStack.pushPose();
		poseStack.translate(swayX, bob - 0.18F, swayZ);
		poseStack.translate(0.5, pivotY, 0.5);
		poseStack.rotateDegrees(Axis.ZP, tiltZ);
		poseStack.rotateDegrees(Axis.XP, tiltX);
		poseStack.translate(-0.5, -pivotY, -0.5);
		queue.submitMovingBlock(poseStack, state.movingBlock, 0);
		poseStack.popPose();
	}

	@Nullable
	private static MovingBlockRenderState createMovingBlock(BlockPos pos, BlockState blockState, @Nullable Level level) {
		if (!(level instanceof ClientLevel clientLevel)) {
			return null;
		}
		MovingBlockRenderState moving = new MovingBlockRenderState();
		moving.randomSeedPos = pos;
		moving.blockPos = pos;
		moving.blockState = blockState;
		moving.biome = clientLevel.getBiome(pos);
		moving.cardinalLighting = clientLevel.cardinalLighting();
		moving.lightEngine = clientLevel.getLightEngine();
		return moving;
	}

	public static class RiftRenderState extends BlockEntityRenderState {
		public float tickProgress;
		public long gameTime;
		public boolean upperHalf;
		public BlockPos anchorPos = BlockPos.ZERO;
		@Nullable
		public MovingBlockRenderState movingBlock;
	}
}
