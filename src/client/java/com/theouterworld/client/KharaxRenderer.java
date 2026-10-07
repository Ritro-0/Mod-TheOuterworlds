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
import net.minecraft.client.renderer.item.ItemStackRenderState;
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
		state.scared = entity.isScared();
		state.ageInTicks = entity.tickCount + tickProgress;
		state.hopPose = entity.getHopPose(tickProgress);
		state.airborneAmount = entity.getAirborneAmount(tickProgress);
		state.headRot = Mth.rotLerp(tickProgress, entity.yHeadRotO, entity.yHeadRot);
		state.xRot = Mth.lerp(tickProgress, entity.xRotO, entity.getXRot());
		state.inspectAmount = 0.0F;
		state.carrying = false;
		state.sleeping = false;
	}

	@Override
	public void submit(KharaxRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		submitBody(state, poseStack, queue);
		super.submit(state, poseStack, queue, camera);
	}

	public static void submitBody(KharaxRenderState state, PoseStack poseStack, SubmitNodeCollector queue) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, 180.0F - state.bodyRot);

		float speed = state.walkAnimationSpeed;
		float cycle = state.walkAnimationPos * 0.7F;
		float hopWave = Mth.sin(cycle);
		// While airborne the arc itself drives the pose: legs extend on the way up, tuck on the way
		// down. Both inputs arrive pre-eased, and the two poses cross-fade rather than switching.
		float air = state.airborneAmount;
		float launch = state.hopPose;
		float extend = Math.max(0.0F, launch);
		float tuck = Math.max(0.0F, -launch);
		float hop = Mth.lerp(air, Math.max(0.0F, hopWave) * speed, extend);
		float plant = Math.max(0.0F, -hopWave) * speed * (1.0F - air);
		float idle = Mth.sin(state.ageInTicks * 0.12F) * 0.02F * (1.0F - speed);

		float warn = state.warning ? 1.0F : 0.0F;
		float rub = state.warning ? Mth.sin(state.ageInTicks * 1.8F) : 0.0F;
		float headShake = state.warning ? Mth.sin(state.ageInTicks * 2.6F) * 22.0F : 0.0F;
		float inspect = state.inspectAmount;
		float sleep = state.sleeping ? 1.0F : 0.0F;

		poseStack.translate(0.0F, hop * 0.18F + idle + warn * 0.22F - sleep * 0.14F, 0.0F);
		poseStack.rotateDegrees(Axis.XP,
			hop * 14.0F - plant * 8.0F - warn * 18.0F + launch * 16.0F + inspect * 8.0F + sleep * 16.0F
		);
		if (state.scared) {
			poseStack.rotateDegrees(Axis.ZP, Mth.sin(state.ageInTicks * 1.65F) * 7.0F);
			poseStack.rotateDegrees(Axis.XP, Mth.sin(state.ageInTicks * 2.2F) * 3.5F);
		}

		int light = state.lightCoords;
		int overlay = OverlayTexture.pack(0.0F, state.hasRedOverlay);

		submitMesh(poseStack, queue, KharaxModelData.BODY, light, overlay);

		float netHeadYaw = Mth.wrapDegrees(state.headRot - state.bodyRot);
		float headPitch = state.xRot + hop * 8.0F - plant * 12.0F + warn * 12.0F;
		float headRoll = headShake;
		if (inspect > 0.0F) {
			netHeadYaw += Mth.sin(state.ageInTicks * 0.14F) * 10.0F * inspect;
			headPitch += 16.0F * inspect;
			headRoll += Mth.sin(state.ageInTicks * 0.22F) * 14.0F * inspect;
		}
		if (state.scared) {
			netHeadYaw += Mth.sin(state.ageInTicks * 0.33F) * 28.0F;
			headRoll += Mth.sin(state.ageInTicks * 1.7F) * 9.0F;
		}
		poseStack.pushPose();
		rotateAround(
			poseStack,
			KharaxModelData.Pivot.HEAD_X,
			KharaxModelData.Pivot.HEAD_Y,
			KharaxModelData.Pivot.HEAD_Z,
			headPitch,
			netHeadYaw,
			headRoll
		);
		submitMesh(poseStack, queue, KharaxModelData.HEAD, light, overlay);
		poseStack.popPose();

		float hindThigh = hop * -58.0F + plant * 18.0F + warn * -35.0F + tuck * -46.0F;
		float hindShin = hop * 38.0F - plant * 12.0F + warn * 28.0F + tuck * 62.0F;
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

		float probe = Mth.sin(state.ageInTicks * 0.19F) * 14.0F * inspect;
		float frontTricep = plant * 42.0F - hop * 22.0F + warn * (55.0F + rub * 18.0F) + tuck * 34.0F
			+ inspect * 26.0F + sleep * 20.0F;
		float frontArm = plant * 28.0F + hop * 8.0F + warn * (-40.0F + rub * -25.0F) + tuck * -30.0F
			- inspect * 12.0F + sleep * 18.0F;
		float leftRubZ = warn * (rub * 28.0F) + probe;
		float rightRubZ = warn * (-rub * 28.0F) - probe;
		if (state.carrying) {
			frontTricep = -42.0F;
			frontArm = 28.0F;
			leftRubZ = 16.0F;
			rightRubZ = -16.0F;
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.92F, -0.48F);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			state.carriedItem.submit(poseStack, queue, light, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
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
		rotateAround(poseStack, x, y, z, rotXDeg, 0.0F, rotZDeg);
	}

	private static void rotateAround(PoseStack poseStack, float x, float y, float z, float rotXDeg, float rotYDeg, float rotZDeg) {
		poseStack.translate(x, y, z);
		if (rotYDeg != 0.0F) {
			poseStack.rotateDegrees(Axis.YP, rotYDeg);
		}
		if (rotXDeg != 0.0F) {
			poseStack.rotateDegrees(Axis.XP, rotXDeg);
		}
		if (rotZDeg != 0.0F) {
			poseStack.rotateDegrees(Axis.ZP, rotZDeg);
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
		public float headRot;
		public float xRot;
		public float walkAnimationPos;
		public float walkAnimationSpeed;
		public boolean hasRedOverlay;
		public boolean warning;
		public boolean scared;
		public boolean carrying;
		public boolean sleeping;
		public float hopPose;
		public float airborneAmount;
		public float inspectAmount;
		public final ItemStackRenderState carriedItem = new ItemStackRenderState();
	}
}
