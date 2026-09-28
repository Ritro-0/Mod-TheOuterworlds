package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.theouterworld.block.WeaverNetBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Shows whatever the Weavers have tucked into a net, bundled down in the weave. */
public class WeaverNetRenderer implements BlockEntityRenderer<WeaverNetBlockEntity, WeaverNetRenderer.WeaverNetRenderState> {
	private static final float ITEM_SCALE = 0.4F;
	private static final float BUNDLE_RADIUS = 0.18F;
	private static final float BUNDLE_Y = 0.42F;

	private final ItemModelResolver itemModelResolver;

	public WeaverNetRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public WeaverNetRenderState createRenderState() {
		return new WeaverNetRenderState();
	}

	@Override
	public void extractRenderState(
		WeaverNetBlockEntity blockEntity,
		WeaverNetRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		int seed = (int) blockEntity.getBlockPos().asLong();
		state.items.clear();
		for (int slot = 0; slot < blockEntity.getContainerSize(); slot++) {
			ItemStack stack = blockEntity.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			ItemStackRenderState itemState = new ItemStackRenderState();
			this.itemModelResolver.updateForTopItem(
				itemState,
				stack,
				ItemDisplayContext.GROUND,
				blockEntity.getLevel(),
				null,
				seed + slot
			);
			state.items.add(itemState);
		}
		state.seed = seed;
	}

	@Override
	public void submit(WeaverNetRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		List<ItemStackRenderState> items = state.items;
		for (int i = 0; i < items.size(); i++) {
			double angle = i * 2.399963 + (state.seed & 7) * 0.4;
			poseStack.pushPose();
			poseStack.translate(
				0.5F + (float) Math.cos(angle) * BUNDLE_RADIUS,
				BUNDLE_Y + i * 0.04F,
				0.5F + (float) Math.sin(angle) * BUNDLE_RADIUS
			);
			poseStack.rotateDegrees(Axis.YP, (float) Math.toDegrees(angle));
			poseStack.rotateDegrees(Axis.XP, 90.0F);
			poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
			items.get(i).submit(poseStack, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	public static class WeaverNetRenderState extends BlockEntityRenderState {
		public final List<ItemStackRenderState> items = new ArrayList<>();
		public int seed;
	}
}
