package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.DriftmiteEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class DriftmiteRenderer extends OceanCreatureRenderer<DriftmiteEntity> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/entity/driftmite.png");
	private static final RenderType CUTOUT = RenderTypes.entityCutout(TEXTURE);
	private final DriftmiteModel model = new DriftmiteModel();

	public DriftmiteRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(DriftmiteEntity entity, State state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.shadowRadius = 0.3F;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		this.poseCreature(state, poseStack, true, true);
		float wag = Mth.sin(state.ageInTicks * 0.6F) * (0.4F + Mth.clamp(state.swimSpeed * 8.0F, 0.0F, 0.75F));
		this.model.tail.yRot = -wag;
		this.model.tail.xRot = 0.0F;
		this.model.dorsal.xRot = 0.0F;
		this.model.dorsal.yRot = 0.0F;
		this.model.dorsal.zRot = wag;
		int overlay = overlay(state);
		int light = state.lightCoords;
		queue.submitModelPart(this.model.body, poseStack, CUTOUT, light, overlay, null);
		queue.submitModelPart(this.model.dorsal, poseStack, CUTOUT, light, overlay, null);
		queue.submitModelPart(this.model.tail, poseStack, CUTOUT, light, overlay, null);
		queue.submitModelPart(this.model.light, poseStack, CUTOUT, FULL_BRIGHT, overlay, null);
		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
	}
}
