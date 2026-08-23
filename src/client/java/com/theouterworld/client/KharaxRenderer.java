package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.KharaxEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Renders the Blockbench kharax mesh with a hop-walk: hind legs kick, body rises, front legs plant.
 */
public class KharaxRenderer extends EntityRenderer<KharaxEntity, KharaxRenderer.KharaxRenderState> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/entity/kharax.png");

	public KharaxRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public KharaxRenderState createRenderState() {
		return new KharaxRenderState();
	}

	@Override
	public void extractRenderState(KharaxEntity entity, KharaxRenderState state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.bodyRot = Mth.rotLerp(tickProgress, entity.yBodyRotO, entity.yBodyRot);
		state.walkAnimationPos = entity.walkAnimation.position(tickProgress);
		state.walkAnimationSpeed = entity.walkAnimation.speed(tickProgress);
		state.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
		state.shadowRadius = 0.65F;
		state.warning = entity.isWarning();
		state.ageInTicks = entity.tickCount + tickProgress;
	}

	@Override
	public void submit(KharaxRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));

		float speed = state.walkAnimationSpeed;
		float cycle = state.walkAnimationPos * 0.7F;
		float hopWave = Mth.sin(cycle);
		float hop = Math.max(0.0F, hopWave) * speed;
		float plant = Math.max(0.0F, -hopWave) * speed;
		float idle = Mth.sin(state.ageInTicks * 0.12F) * 0.02F * (1.0F - speed);

		float warn = state.warning ? 1.0F : 0.0F;
		float rub = state.warning ? Mth.sin(state.ageInTicks * 1.8F) : 0.0F;
		float headShake = state.warning ? Mth.sin(state.ageInTicks * 2.6F) * 22.0F : 0.0F;

		poseStack.translate(0.0F, hop * 0.18F + idle + warn * 0.22F, 0.0F);
		poseStack.mulPose(Axis.XP.rotationDegrees(hop * 14.0F - plant * 8.0F - warn * 18.0F));

		int light = state.lightCoords;
		int overlay = state.hasRedOverlay ? OverlayTexture.pack(0, 10) : OverlayTexture.NO_OVERLAY;

		submitMesh(poseStack, queue, KharaxModelData.BODY, light, overlay);

		poseStack.pushPose();
		rotateAround(
			poseStack,
			KharaxModelData.Pivot.HEAD_X,
			KharaxModelData.Pivot.HEAD_Y,
			KharaxModelData.Pivot.HEAD_Z,
			hop * 8.0F - plant * 12.0F + warn * 12.0F,
			headShake
		);
		submitMesh(poseStack, queue, KharaxModelData.HEAD, light, overlay);
		poseStack.popPose();

		float hindThigh = hop * -58.0F + plant * 18.0F + warn * -35.0F;
		float hindShin = hop * 38.0F - plant * 12.0F + warn * 28.0F;
		submitLimb(
			poseStack,
			queue,
			KharaxModelData.LEFT_THIGH,
			KharaxModelData.LEFT_LEG,
			KharaxModelData.Pivot.LEFT_THIGH_X,
			KharaxModelData.Pivot.LEFT_THIGH_Y,
			KharaxModelData.Pivot.LEFT_THIGH_Z,
			KharaxModelData.Pivot.LEFT_LEG_X,
			KharaxModelData.Pivot.LEFT_LEG_Y,
			KharaxModelData.Pivot.LEFT_LEG_Z,
			hindThigh,
			hindShin,
			light,
			overlay
		);
		submitLimb(
			poseStack,
			queue,
			KharaxModelData.RIGHT_THIGH,
			KharaxModelData.RIGHT_LEG,
			KharaxModelData.Pivot.RIGHT_THIGH_X,
			KharaxModelData.Pivot.RIGHT_THIGH_Y,
			KharaxModelData.Pivot.RIGHT_THIGH_Z,
			KharaxModelData.Pivot.RIGHT_LEG_X,
			KharaxModelData.Pivot.RIGHT_LEG_Y,
			KharaxModelData.Pivot.RIGHT_LEG_Z,
			hindThigh,
			hindShin,
			light,
			overlay
		);

		float frontTricep = plant * 42.0F - hop * 22.0F + warn * (55.0F + rub * 18.0F);
		float frontArm = plant * 28.0F + hop * 8.0F + warn * (-40.0F + rub * -25.0F);
		float leftRubZ = warn * (rub * 28.0F);
		float rightRubZ = warn * (-rub * 28.0F);
		submitLimb(
			poseStack,
			queue,
			KharaxModelData.LEFT_TRICEP,
			KharaxModelData.LEFT_ARM,
			KharaxModelData.Pivot.LEFT_TRICEP_X,
			KharaxModelData.Pivot.LEFT_TRICEP_Y,
			KharaxModelData.Pivot.LEFT_TRICEP_Z,
			KharaxModelData.Pivot.LEFT_ARM_X,
			KharaxModelData.Pivot.LEFT_ARM_Y,
			KharaxModelData.Pivot.LEFT_ARM_Z,
			frontTricep,
			frontArm,
			leftRubZ,
			light,
			overlay
		);
		submitLimb(
			poseStack,
			queue,
			KharaxModelData.RIGHT_TRICEP,
			KharaxModelData.RIGHT_ARM,
			KharaxModelData.Pivot.RIGHT_TRICEP_X,
			KharaxModelData.Pivot.RIGHT_TRICEP_Y,
			KharaxModelData.Pivot.RIGHT_TRICEP_Z,
			KharaxModelData.Pivot.RIGHT_ARM_X,
			KharaxModelData.Pivot.RIGHT_ARM_Y,
			KharaxModelData.Pivot.RIGHT_ARM_Z,
			frontTricep,
			frontArm,
			rightRubZ,
			light,
			overlay
		);

		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
	}

	private static void submitLimb(
		PoseStack poseStack,
		SubmitNodeCollector queue,
		float[] parentMesh,
		float[] childMesh,
		float parentX,
		float parentY,
		float parentZ,
		float childX,
		float childY,
		float childZ,
		float parentRot,
		float childRot,
		int light,
		int overlay
	) {
		submitLimb(poseStack, queue, parentMesh, childMesh, parentX, parentY, parentZ, childX, childY, childZ, parentRot, childRot, 0.0F, light, overlay);
	}

	private static void submitLimb(
		PoseStack poseStack,
		SubmitNodeCollector queue,
		float[] parentMesh,
		float[] childMesh,
		float parentX,
		float parentY,
		float parentZ,
		float childX,
		float childY,
		float childZ,
		float parentRot,
		float childRot,
		float parentRotZ,
		int light,
		int overlay
	) {
		poseStack.pushPose();
		rotateAround(poseStack, parentX, parentY, parentZ, parentRot, parentRotZ);
		submitMesh(poseStack, queue, parentMesh, light, overlay);
		poseStack.pushPose();
		rotateAround(poseStack, childX, childY, childZ, childRot, 0.0F);
		submitMesh(poseStack, queue, childMesh, light, overlay);
		poseStack.popPose();
		poseStack.popPose();
	}

	private static void rotateAround(PoseStack poseStack, float x, float y, float z, float rotXDeg, float rotZDeg) {
		poseStack.translate(x, y, z);
		if (rotXDeg != 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(rotXDeg));
		}
		if (rotZDeg != 0.0F) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(rotZDeg));
		}
		poseStack.translate(-x, -y, -z);
	}

	private static void submitMesh(PoseStack poseStack, SubmitNodeCollector queue, float[] mesh, int light, int overlay) {
		queue.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, consumer) ->
			writeMesh(pose, consumer, mesh, light, overlay)
		);
	}

	private static void writeMesh(PoseStack.Pose pose, VertexConsumer consumer, float[] data, int light, int overlay) {
		for (int i = 0; i < data.length; i += 8) {
			consumer.addVertex(pose, data[i], data[i + 1], data[i + 2])
				.setColor(255, 255, 255, 255)
				.setUv(data[i + 3], data[i + 4])
				.setOverlay(overlay)
				.setLight(light)
				.setNormal(pose, data[i + 5], data[i + 6], data[i + 7]);
		}
	}

	public static class KharaxRenderState extends EntityRenderState {
		public float bodyRot;
		public float walkAnimationPos;
		public float walkAnimationSpeed;
		public boolean hasRedOverlay;
		public boolean warning;
	}
}
