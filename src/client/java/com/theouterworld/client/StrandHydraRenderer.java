package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.StrandHydraEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class StrandHydraRenderer extends OceanCreatureRenderer<StrandHydraEntity> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/entity/strand_hydra.png");
	private static final RenderType CUTOUT = RenderTypes.entityCutout(TEXTURE);
	private final StrandHydraModel model = new StrandHydraModel();

	public StrandHydraRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(StrandHydraEntity entity, State state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.shadowRadius = 0.5F;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		this.poseCreature(state, poseStack, false, false);
		ModelPart[] tentacles = this.model.tentacles;
		for (int i = 0; i < tentacles.length; i++) {
			tentacles[i].xRot = StrandHydraEntity.swayX(i, state.ageInTicks);
			tentacles[i].zRot = StrandHydraEntity.swayZ(i, state.ageInTicks);
			tentacles[i].yRot = 0.0F;
		}
		queue.submitModelPart(this.model.root, poseStack, CUTOUT, state.lightCoords, overlay(state), null);
		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
	}
}
