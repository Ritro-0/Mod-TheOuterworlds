package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.FeederEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class FeederRenderer extends OceanCreatureRenderer<FeederEntity> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/entity/feeder.png");
	private static final RenderType CUTOUT = RenderTypes.entityCutout(TEXTURE);
	private final FeederModel model = new FeederModel();

	public FeederRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(FeederEntity entity, State state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.shadowRadius = 0.25F;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		this.poseCreature(state, poseStack, true, true, true);
		this.wiggle(state);
		int overlay = overlay(state);
		int light = state.lightCoords;
		queue.submitModelPart(this.model.front, poseStack, CUTOUT, light, overlay, null);
		this.model.front.translateAndRotate(poseStack);
		queue.submitModelPart(this.model.light, poseStack, CUTOUT, FULL_BRIGHT, overlay, null);
		this.model.light.translateAndRotate(poseStack);
		queue.submitModelPart(this.model.mid, poseStack, CUTOUT, light, overlay, null);
		this.model.mid.translateAndRotate(poseStack);
		queue.submitModelPart(this.model.rear, poseStack, CUTOUT, light, overlay, null);
		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
	}

	private void wiggle(State state) {
		float speed = Mth.clamp(state.swimSpeed, 0.0F, 0.45F);
		float time = state.ageInTicks * (0.32F + speed * 1.15F);
		float amp = 0.18F + speed * 0.55F;
		float phase = 0.95F;
		this.bend(this.model.front, time, amp * 0.22F);
		this.bend(this.model.light, time - phase, amp * 0.42F);
		this.bend(this.model.mid, time - phase * 2.0F, amp * 0.72F);
		this.bend(this.model.rear, time - phase * 3.0F, amp * 1.05F);
	}

	private void bend(ModelPart part, float time, float amount) {
		float wave = Mth.sin(time);
		part.yRot = wave * amount;
		part.xRot = wave * amount * 0.28F;
		part.zRot = 0.0F;
	}
}
