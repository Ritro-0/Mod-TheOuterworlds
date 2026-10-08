package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.theouterworld.block.ProcessorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Sits whatever the Processor is working on (or has finished) in the yellow bowl at its tip. */
public class ProcessorRenderer implements BlockEntityRenderer<ProcessorBlockEntity, ProcessorRenderer.ProcessorRenderState> {
	/** Top of the bowl floor in the block model (y = 11.25 px). */
	private static final float BOWL_FLOOR_Y = 11.25F / 16.0F;
	/** The bowl's inner walls are 6 px apart; keep the spinning item just inside them. */
	private static final float BOWL_INNER_WIDTH = 5.5F / 16.0F;
	private static final float SPIN_DEGREES_PER_TICK = 1.5F;
	/** Ticks per full turn, so the wrapped game time never makes the spin jump. */
	private static final long SPIN_PERIOD = 240L;

	private final ItemModelResolver itemModelResolver;

	public ProcessorRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public ProcessorRenderState createRenderState() {
		return new ProcessorRenderState();
	}

	@Override
	public void extractRenderState(
		ProcessorBlockEntity blockEntity,
		ProcessorRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		ItemStack stack = blockEntity.getDisplayedStack();
		int seed = (int) blockEntity.getBlockPos().asLong();
		this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, blockEntity.getLevel(), null, seed);
		long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
		state.spin = ((time % SPIN_PERIOD) + tickProgress) * SPIN_DEGREES_PER_TICK + (seed & 63);
	}

	@Override
	public void submit(ProcessorRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		if (state.item.isEmpty()) {
			return;
		}
		AABB box = state.item.getModelBoundingBox();
		double sweep = Math.sqrt(box.getXsize() * box.getXsize() + box.getZsize() * box.getZsize());
		float scale = sweep > BOWL_INNER_WIDTH ? (float) (BOWL_INNER_WIDTH / sweep) : 1.0F;

		poseStack.pushPose();
		poseStack.translate(0.5F, BOWL_FLOOR_Y, 0.5F);
		poseStack.rotateDegrees(Axis.YP, state.spin);
		poseStack.scale(scale, scale, scale);
		Vec3 center = box.getCenter();
		poseStack.translate((float) -center.x, (float) -box.minY, (float) -center.z);
		state.item.submit(poseStack, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	public static class ProcessorRenderState extends BlockEntityRenderState {
		public final ItemStackRenderState item = new ItemStackRenderState();
		public float spin;
	}
}
