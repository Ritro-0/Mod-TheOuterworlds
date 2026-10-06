package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shared pose for Blockbench ocean creatures: yaw, optional swim pitch, and the vanilla model flip.
 */
public abstract class OceanCreatureRenderer<T extends LivingEntity> extends EntityRenderer<T, OceanCreatureRenderer.State> {
	protected static final int FULL_BRIGHT = 0xF000F0;

	protected OceanCreatureRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T entity, State state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.bodyRot = Mth.rotLerp(tickProgress, entity.yBodyRotO, entity.yBodyRot);
		state.xRot = entity.getViewXRot(tickProgress);
		state.ageInTicks = entity.tickCount + tickProgress;
		state.inWater = entity.isInWater();
		state.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
		state.deathTime = entity.deathTime;
		state.swimSpeed = (float) entity.getDeltaMovement().horizontalDistance();
	}

	protected void poseCreature(State state, PoseStack poseStack, boolean flopOnLand, boolean pitchInWater) {
		this.poseCreature(state, poseStack, flopOnLand, pitchInWater, false);
	}

	/**
	 * @param leadWithPositiveZ the feeder's nose is the model +Z end, so it faces the opposite yaw from the other fish
	 */
	protected void poseCreature(State state, PoseStack poseStack, boolean flopOnLand, boolean pitchInWater, boolean leadWithPositiveZ) {
		float yaw = leadWithPositiveZ ? -state.bodyRot : 180.0F - state.bodyRot;
		poseStack.rotateDegrees(Axis.YP, yaw);
		if (state.deathTime > 0) {
			float fall = (state.deathTime - 1.0F) / 20.0F * 1.6F;
			fall = Mth.sqrt(fall);
			if (fall > 1.0F) {
				fall = 1.0F;
			}
			poseStack.rotateDegrees(Axis.ZP, fall * 90.0F);
		} else if (flopOnLand && !state.inWater) {
			poseStack.translate(0.1F, 0.1F, -0.1F);
			poseStack.rotateDegrees(Axis.ZP, 90.0F);
		} else if (pitchInWater && state.inWater) {
			poseStack.rotateDegrees(Axis.XP, state.xRot);
		}
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0F, -1.501F, 0.0F);
	}

	protected static int overlay(State state) {
		return OverlayTexture.pack(0.0F, state.hasRedOverlay);
	}

	public static class State extends EntityRenderState {
		public float bodyRot;
		public float xRot;
		public boolean inWater;
		public boolean hasRedOverlay;
		public float deathTime;
		public float swimSpeed;
	}
}
