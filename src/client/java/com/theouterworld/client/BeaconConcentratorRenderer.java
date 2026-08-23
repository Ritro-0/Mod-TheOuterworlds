package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.block.BeaconConcentratorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Still, non-rotating concentrated beacon beam. Solid core is 2×2 pixels (1/16 block radius).
 */
public class BeaconConcentratorRenderer implements BlockEntityRenderer<BeaconConcentratorBlockEntity, BeaconRenderState> {
	private static final float STILL_ANIMATION_TIME = 20.0F;
	private static final float TEXTURE_SCALE = 1.0F;
	private static final float SOLID_RADIUS = 1.0F / 16.0F;
	private static final float GLOW_RADIUS = 1.25F / 16.0F;
	private static final int BEAM_HEIGHT = BeaconRenderer.MAX_RENDER_Y;
	private static final int BEAM_COLOR = DyeColor.MAGENTA.getTextureDiffuseColor();

	public BeaconConcentratorRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public BeaconRenderState createRenderState() {
		return new BeaconRenderState();
	}

	@Override
	public void extractRenderState(
		BeaconConcentratorBlockEntity blockEntity,
		BeaconRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.animationTime = STILL_ANIMATION_TIME;
		state.beamRadiusScale = 1.0F;
		if (blockEntity.getLevel() != null && blockEntity.isActive()) {
			state.sections = java.util.List.of(new BeaconRenderState.Section(BEAM_COLOR, BEAM_HEIGHT));
		} else {
			state.sections = java.util.List.of();
		}
	}

	@Override
	public void submit(BeaconRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState cameraState) {
		if (state.sections.isEmpty()) {
			return;
		}
		int yOffset = 0;
		for (BeaconRenderState.Section section : state.sections) {
			BeaconRenderer.submitBeaconBeam(
				poseStack,
				queue,
				BeaconRenderer.BEAM_LOCATION,
				STILL_ANIMATION_TIME,
				TEXTURE_SCALE,
				yOffset,
				section.height(),
				section.color(),
				SOLID_RADIUS,
				GLOW_RADIUS
			);
			yOffset += section.height();
		}
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public boolean shouldRender(BeaconConcentratorBlockEntity blockEntity, Vec3 cameraPos) {
		Vec3 pos = Vec3.atCenterOf(blockEntity.getBlockPos());
		double dx = pos.x - cameraPos.x;
		double dz = pos.z - cameraPos.z;
		int distance = getViewDistance();
		return dx * dx + dz * dz < (double) distance * (double) distance;
	}
}
