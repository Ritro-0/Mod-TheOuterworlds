package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.entity.OceanVentEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** The vent is a fog of particles. The marker itself is not drawn. */
public class OceanVentRenderer extends EntityRenderer<OceanVentEntity, EntityRenderState> {
	public OceanVentRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
	}
}
