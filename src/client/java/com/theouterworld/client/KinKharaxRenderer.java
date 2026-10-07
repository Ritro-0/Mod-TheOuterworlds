package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.entity.KinKharaxEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Adopted Kharax. Same mesh as a wild one, with the Weaver inspect and carry
 * poses retargeted onto the shorter arms, head, and legs.
 */
public class KinKharaxRenderer extends EntityRenderer<KinKharaxEntity, KharaxRenderer.KharaxRenderState> {
	private final ItemModelResolver itemModelResolver;

	public KinKharaxRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public KharaxRenderer.KharaxRenderState createRenderState() {
		return new KharaxRenderer.KharaxRenderState();
	}

	@Override
	public void extractRenderState(KinKharaxEntity entity, KharaxRenderer.KharaxRenderState state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.bodyRot = Mth.rotLerp(tickProgress, entity.yBodyRotO, entity.yBodyRot);
		state.headRot = Mth.rotLerp(tickProgress, entity.yHeadRotO, entity.yHeadRot);
		state.xRot = Mth.lerp(tickProgress, entity.xRotO, entity.getXRot());
		state.walkAnimationPos = entity.walkAnimation.position(tickProgress);
		state.walkAnimationSpeed = entity.walkAnimation.speed(tickProgress);
		state.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
		state.shadowRadius = 0.65F;
		state.warning = false;
		state.scared = false;
		state.ageInTicks = entity.tickCount + tickProgress;
		state.hopPose = entity.getLeapPose(tickProgress);
		state.airborneAmount = entity.getAirborneAmount(tickProgress);
		state.inspectAmount = entity.getInspectAmount(tickProgress);
		state.sleeping = false;
		state.carrying = !entity.getCarriedItem().isEmpty();
		this.itemModelResolver.updateForTopItem(
			state.carriedItem,
			entity.getCarriedItem(),
			ItemDisplayContext.FIXED,
			entity.level(),
			null,
			entity.getId()
		);
	}

	@Override
	public void submit(
		KharaxRenderer.KharaxRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector queue,
		CameraRenderState camera
	) {
		KharaxRenderer.submitBody(state, poseStack, queue);
		super.submit(state, poseStack, queue, camera);
	}
}
