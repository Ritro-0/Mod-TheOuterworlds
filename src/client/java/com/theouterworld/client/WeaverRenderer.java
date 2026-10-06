package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.WeaverEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Renders the Blockbench weaver with a three-leg walk, swaying arms and antennae,
 * and a neck-pivoted head that can look around independently of the body.
 */
public class WeaverRenderer extends EntityRenderer<WeaverEntity, WeaverRenderer.WeaverRenderState> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/entity/weaver.png");
	/** Front arms pitch forward by this much while a block is in the hands. */
	private static final float CARRY_PITCH = 52.0F;
	/** Shoulder to the palm, along the arm. */
	private static final float CARRY_REACH = 0.78F;

	private final ItemModelResolver itemModelResolver;

	public WeaverRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public WeaverRenderState createRenderState() {
		return new WeaverRenderState();
	}

	@Override
	public void extractRenderState(WeaverEntity entity, WeaverRenderState state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.bodyRot = Mth.rotLerp(tickProgress, entity.yBodyRotO, entity.yBodyRot);
		state.headRot = Mth.rotLerp(tickProgress, entity.yHeadRotO, entity.yHeadRot);
		state.xRot = Mth.lerp(tickProgress, entity.xRotO, entity.getXRot());
		state.walkAnimationPos = entity.walkAnimation.position(tickProgress);
		state.walkAnimationSpeed = entity.walkAnimation.speed(tickProgress);
		state.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
		state.ageInTicks = entity.tickCount + tickProgress;
		state.leapPose = entity.getLeapPose(tickProgress);
		state.airborneAmount = entity.getAirborneAmount(tickProgress);
		state.aggressive = entity.isClientAggressive();
		state.inspectAmount = entity.getInspectAmount(tickProgress);
		state.sleeping = entity.isSleeping();
		state.baby = entity.isBaby();
		state.flipProgress = entity.getFlipProgress();
		state.shadowRadius = entity.isBaby() ? 0.3F : 0.55F;
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
	public void submit(WeaverRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, 180.0F - state.bodyRot);
		if (state.flipProgress > 0.001F) {
			rotateAround(poseStack, 0.0F, 0.85F, 0.0F, -360.0F * state.flipProgress, 0.0F, 0.0F);
		}
		if (state.baby) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		if (state.sleeping) {
			// The body already leans about 40° forward. This pitch lays that axis
			// flat on the mattress, shell down, head at one end.
			poseStack.translate(0.0F, -0.42F, 0.05F);
			rotateAround(poseStack, 0.0F, 0.72F, 0.15F, -48.0F, 0.0F, 0.0F);
		}

		float speed = state.sleeping ? 0.0F : state.walkAnimationSpeed;
		float cycle = state.walkAnimationPos * 0.6662F;
		float air = state.airborneAmount;
		float launch = state.leapPose;
		float ground = 1.0F - air;
		float walk = speed * ground;
		// Rock the torso on the hip joint. Translating the whole mesh dropped the feet into the floor.
		float hipBob = state.sleeping ? 0.0F : Mth.sin(state.ageInTicks * 0.12F) * 5.0F * (1.0F - speed);

		int light = state.lightCoords;
		int overlay = OverlayTexture.pack(0.0F, state.hasRedOverlay);

		poseStack.pushPose();
		rotateAround(
			poseStack,
			0.0F,
			WeaverModelData.Pivot.LEFT_LEG_Y,
			0.0F,
			hipBob + Math.max(0.0F, launch) * 10.0F,
			0.0F,
			0.0F
		);
		submitMesh(poseStack, queue, WeaverModelData.BODY, light, overlay);
		submitMesh(poseStack, queue, WeaverModelData.NECK, light, overlay);

		// Curiosity pose: the head cocks down and rolls slowly from side to side.
		float inspect = state.inspectAmount;
		float netHeadYaw = Mth.wrapDegrees(state.headRot - state.bodyRot)
			+ Mth.sin(state.ageInTicks * 0.14F) * 9.0F * inspect;
		float headPitch = state.xRot + inspect * 20.0F;
		float headRoll = Mth.sin(state.ageInTicks * 0.22F) * 16.0F * inspect;
		poseStack.pushPose();
		rotateAround(
			poseStack,
			WeaverModelData.Pivot.HEAD_X,
			WeaverModelData.Pivot.HEAD_Y,
			WeaverModelData.Pivot.HEAD_Z,
			headPitch,
			netHeadYaw,
			headRoll
		);
		submitMesh(poseStack, queue, WeaverModelData.HEAD, light, overlay);
		submitAntennae(poseStack, queue, state, light, overlay);
		poseStack.popPose();

		float tuck = state.sleeping ? 0.85F : Math.max(0.0F, -launch) * air;
		float extend = Math.max(0.0F, launch) * air;
		float charge = state.aggressive ? 1.0F : 0.0F;
		boolean carrying = !state.carriedItem.isEmpty();
		float leftPitch = carrying ? CARRY_PITCH : Mth.sin(cycle) * 32.0F * walk - charge * 25.0F + tuck * 18.0F;
		float rightPitch = carrying ? CARRY_PITCH : Mth.sin(cycle + Mth.PI) * 32.0F * walk - charge * 25.0F + tuck * 18.0F;
		float leftRoll = carrying ? 16.0F : Mth.sin(cycle * 0.5F) * 8.0F * walk;
		float rightRoll = carrying ? -16.0F : Mth.sin(cycle * 0.5F + Mth.PI) * 8.0F * walk;
		if (carrying) {
			poseStack.pushPose();
			poseStack.translate(0.0F, WeaverModelData.Pivot.LEFT_FRONT_ARM_Y, WeaverModelData.Pivot.LEFT_FRONT_ARM_Z);
			poseStack.rotateDegrees(Axis.XP, CARRY_PITCH);
			poseStack.translate(0.0F, -CARRY_REACH, -0.06F);
			poseStack.scale(1.15F, 1.15F, 1.15F);
			state.carriedItem.submit(poseStack, queue, light, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		submitArm(
			poseStack, queue, WeaverModelData.LEFT_FRONT_ARM,
			WeaverModelData.Pivot.LEFT_FRONT_ARM_X, WeaverModelData.Pivot.LEFT_FRONT_ARM_Y, WeaverModelData.Pivot.LEFT_FRONT_ARM_Z,
			leftPitch,
			leftRoll,
			light, overlay
		);
		submitArm(
			poseStack, queue, WeaverModelData.RIGHT_FRONT_ARM,
			WeaverModelData.Pivot.RIGHT_FRONT_ARM_X, WeaverModelData.Pivot.RIGHT_FRONT_ARM_Y, WeaverModelData.Pivot.RIGHT_FRONT_ARM_Z,
			rightPitch,
			rightRoll,
			light, overlay
		);
		submitArm(
			poseStack, queue, WeaverModelData.LEFT_BACK_ARM,
			WeaverModelData.Pivot.LEFT_BACK_ARM_X, WeaverModelData.Pivot.LEFT_BACK_ARM_Y, WeaverModelData.Pivot.LEFT_BACK_ARM_Z,
			Mth.sin(cycle + Mth.PI) * 22.0F * walk + charge * 12.0F,
			Mth.cos(cycle) * 10.0F * walk,
			light, overlay
		);
		submitArm(
			poseStack, queue, WeaverModelData.RIGHT_BACK_ARM,
			WeaverModelData.Pivot.RIGHT_BACK_ARM_X, WeaverModelData.Pivot.RIGHT_BACK_ARM_Y, WeaverModelData.Pivot.RIGHT_BACK_ARM_Z,
			Mth.sin(cycle) * 22.0F * walk + charge * 12.0F,
			Mth.cos(cycle + Mth.PI) * 10.0F * walk,
			light, overlay
		);
		poseStack.popPose();

		submitLeg(
			poseStack, queue,
			WeaverModelData.LEFT_LEG, WeaverModelData.LEFT_FOOT,
			WeaverModelData.Pivot.LEFT_LEG_X, WeaverModelData.Pivot.LEFT_LEG_Y, WeaverModelData.Pivot.LEFT_LEG_Z,
			WeaverModelData.Pivot.LEFT_FOOT_X, WeaverModelData.Pivot.LEFT_FOOT_Y, WeaverModelData.Pivot.LEFT_FOOT_Z,
			legSwing(cycle, walk, tuck, extend),
			light, overlay
		);
		submitLeg(
			poseStack, queue,
			WeaverModelData.RIGHT_LEG, WeaverModelData.RIGHT_FOOT,
			WeaverModelData.Pivot.RIGHT_LEG_X, WeaverModelData.Pivot.RIGHT_LEG_Y, WeaverModelData.Pivot.RIGHT_LEG_Z,
			WeaverModelData.Pivot.RIGHT_FOOT_X, WeaverModelData.Pivot.RIGHT_FOOT_Y, WeaverModelData.Pivot.RIGHT_FOOT_Z,
			legSwing(cycle + Mth.TWO_PI / 3.0F, walk, tuck, extend),
			light, overlay
		);
		submitLeg(
			poseStack, queue,
			WeaverModelData.MIDDLE_LEG, WeaverModelData.MIDDLE_FOOT,
			WeaverModelData.Pivot.MIDDLE_LEG_X, WeaverModelData.Pivot.MIDDLE_LEG_Y, WeaverModelData.Pivot.MIDDLE_LEG_Z,
			WeaverModelData.Pivot.MIDDLE_FOOT_X, WeaverModelData.Pivot.MIDDLE_FOOT_Y, WeaverModelData.Pivot.MIDDLE_FOOT_Z,
			legSwing(cycle + 2.0F * Mth.TWO_PI / 3.0F, walk, tuck, extend),
			light, overlay
		);

		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
	}

	private static float legSwing(float cycle, float walk, float tuck, float extend) {
		return -Mth.sin(cycle) * 38.0F * walk + tuck * 42.0F - extend * 18.0F;
	}

	private static void submitAntennae(
		PoseStack poseStack,
		SubmitNodeCollector queue,
		WeaverRenderState state,
		int light,
		int overlay
	) {
		float age = state.ageInTicks;
		float walk = state.walkAnimationSpeed * (1.0F - state.airborneAmount);
		// Antennae work the air harder while the weaver is sizing something up.
		float probe = 1.0F + state.inspectAmount * 1.6F;
		float leftBaseX = (Mth.sin(age * 0.13F) * 10.0F + Mth.sin(age * 0.21F) * 4.0F * walk) * probe;
		float leftBaseZ = Mth.cos(age * 0.11F) * 7.0F * probe;
		float leftTipX = Mth.sin(age * 0.19F + 0.6F) * 14.0F * probe;
		float leftTipZ = Mth.cos(age * 0.17F) * 10.0F * probe;
		float rightBaseX = (Mth.sin(age * 0.13F + 1.1F) * 10.0F + Mth.sin(age * 0.21F + 0.8F) * 4.0F * walk) * probe;
		float rightBaseZ = Mth.cos(age * 0.11F + 0.9F) * 7.0F * probe;
		float rightTipX = Mth.sin(age * 0.19F + 1.7F) * 14.0F * probe;
		float rightTipZ = Mth.cos(age * 0.17F + 1.2F) * 10.0F * probe;

		submitAntenna(
			poseStack, queue,
			WeaverModelData.LEFT_ANTENNA_BASE, WeaverModelData.LEFT_ANTENNA_TIP,
			WeaverModelData.Pivot.LEFT_ANTENNA_BASE_X, WeaverModelData.Pivot.LEFT_ANTENNA_BASE_Y, WeaverModelData.Pivot.LEFT_ANTENNA_BASE_Z,
			WeaverModelData.Pivot.LEFT_ANTENNA_TIP_X, WeaverModelData.Pivot.LEFT_ANTENNA_TIP_Y, WeaverModelData.Pivot.LEFT_ANTENNA_TIP_Z,
			leftBaseX, leftBaseZ, leftTipX, leftTipZ, light, overlay
		);
		submitAntenna(
			poseStack, queue,
			WeaverModelData.RIGHT_ANTENNA_BASE, WeaverModelData.RIGHT_ANTENNA_TIP,
			WeaverModelData.Pivot.RIGHT_ANTENNA_BASE_X, WeaverModelData.Pivot.RIGHT_ANTENNA_BASE_Y, WeaverModelData.Pivot.RIGHT_ANTENNA_BASE_Z,
			WeaverModelData.Pivot.RIGHT_ANTENNA_TIP_X, WeaverModelData.Pivot.RIGHT_ANTENNA_TIP_Y, WeaverModelData.Pivot.RIGHT_ANTENNA_TIP_Z,
			rightBaseX, rightBaseZ, rightTipX, rightTipZ, light, overlay
		);
	}

	private static void submitAntenna(
		PoseStack poseStack,
		SubmitNodeCollector queue,
		float[] baseMesh,
		float[] tipMesh,
		float baseX,
		float baseY,
		float baseZ,
		float tipX,
		float tipY,
		float tipZ,
		float baseRotX,
		float baseRotZ,
		float tipRotX,
		float tipRotZ,
		int light,
		int overlay
	) {
		poseStack.pushPose();
		rotateAround(poseStack, baseX, baseY, baseZ, baseRotX, 0.0F, baseRotZ);
		submitMesh(poseStack, queue, baseMesh, light, overlay);
		poseStack.pushPose();
		rotateAround(poseStack, tipX, tipY, tipZ, tipRotX, 0.0F, tipRotZ);
		submitMesh(poseStack, queue, tipMesh, light, overlay);
		poseStack.popPose();
		poseStack.popPose();
	}

	private static void submitLeg(
		PoseStack poseStack,
		SubmitNodeCollector queue,
		float[] legMesh,
		float[] footMesh,
		float legX,
		float legY,
		float legZ,
		float footX,
		float footY,
		float footZ,
		float legRot,
		int light,
		int overlay
	) {
		poseStack.pushPose();
		rotateAround(poseStack, legX, legY, legZ, legRot, 0.0F, 0.0F);
		submitMesh(poseStack, queue, legMesh, light, overlay);
		poseStack.pushPose();
		rotateAround(poseStack, footX, footY, footZ, -legRot * 0.55F, 0.0F, 0.0F);
		submitMesh(poseStack, queue, footMesh, light, overlay);
		poseStack.popPose();
		poseStack.popPose();
	}

	private static void submitArm(
		PoseStack poseStack,
		SubmitNodeCollector queue,
		float[] mesh,
		float x,
		float y,
		float z,
		float rotX,
		float rotZ,
		int light,
		int overlay
	) {
		poseStack.pushPose();
		rotateAround(poseStack, x, y, z, rotX, 0.0F, rotZ);
		submitMesh(poseStack, queue, mesh, light, overlay);
		poseStack.popPose();
	}

	private static void rotateAround(
		PoseStack poseStack,
		float x,
		float y,
		float z,
		float rotXDeg,
		float rotYDeg,
		float rotZDeg
	) {
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

	public static class WeaverRenderState extends EntityRenderState {
		public float bodyRot;
		public float headRot;
		public float xRot;
		public float walkAnimationPos;
		public float walkAnimationSpeed;
		public boolean hasRedOverlay;
		public boolean aggressive;
		public float leapPose;
		public float airborneAmount;
		public float inspectAmount;
		public boolean sleeping;
		public boolean baby;
		public float flipProgress;
		public final ItemStackRenderState carriedItem = new ItemStackRenderState();
	}
}
