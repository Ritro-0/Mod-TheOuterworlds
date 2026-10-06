package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.FrostworldJellyEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class FrostworldJellyRenderer extends OceanCreatureRenderer<FrostworldJellyEntity> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/entity/jelly.png");
	private static final RenderType CUTOUT = RenderTypes.entityCutout(TEXTURE);
	private final FrostworldJellyModel model = new FrostworldJellyModel();

	public FrostworldJellyRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(FrostworldJellyEntity entity, State state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.shadowRadius = 0.4F;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		this.poseCreature(state, poseStack, false, false);
		this.model.cap.xRot = Mth.sin(state.ageInTicks * 0.045F) * 0.14F;
		this.model.cap.zRot = Mth.sin(state.ageInTicks * 0.037F + 1.2F) * 0.24F;
		this.model.cap.yRot = Mth.sin(state.ageInTicks * 0.028F + 0.4F) * 0.08F;
		queue.submitModelPart(this.model.root, poseStack, CUTOUT, state.lightCoords, overlay(state), null);
		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
	}
}
