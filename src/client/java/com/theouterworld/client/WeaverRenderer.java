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
import net.minecraft.core.Direction;
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
	/** Levels the 37.5° body lean; a half roll then puts the shell on the mattress. */
	private static final float SLEEP_PITCH = -37.5F;
	/** Limb pitch that turns a hanging limb back toward the ground once the body lies shell down. */
	private static final float SLEEP_LIMB_FLIP = -180.0F - SLEEP_PITCH;
	/** Lays the upside-down head level as it hangs past the head end of the pad. */
	private static final float SLEEP_HEAD_PITCH = 64.0F;
	/** Shell depth and body front, in model units, after the sleep rotation. */
	private static final float SLEEP_SHELL_DEPTH = 1.101F;
	private static final float SLEEP_BODY_FRONT = 1.19F;
	/** How far the shell front sticks past the pad, leaving room for the head to hang without clipping. */
	private static final float SLEEP_NECK_OVERHANG = 0.14F;
	/** A sleeper stands at the foot-block centre, 0.125 above the mattress; the pad reaches 1.5 ahead. */
	private static final float PAD_TOP_DROP = 0.125F;
	private static final float PAD_HEAD_END = 1.5F;

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
		Direction bed = state.sleeping ? entity.getBedOrientation() : null;
		state.sleepYaw = bed != null ? bed.toYRot() : state.bodyRot;
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
		poseStack.rotateDegrees(Axis.YP, 180.0F - (state.sleeping ? state.sleepYaw : state.bodyRot));
		if (state.flipProgress > 0.001F) {
			rotateAround(poseStack, 0.0F, 0.85F, 0.0F, -360.0F * state.flipProgress, 0.0F, 0.0F);
		}
		float size = state.baby ? 0.5F : 1.0F;
		if (state.sleeping) {
			// Shell on the mattress, body along the pad, neck just past the head end so the head can hang.
			poseStack.translate(
				0.0F,
				SLEEP_SHELL_DEPTH * size - PAD_TOP_DROP + 0.003F,
				-PAD_HEAD_END + (SLEEP_BODY_FRONT - SLEEP_NECK_OVERHANG) * size
			);
		}
		if (state.baby) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		if (state.sleeping) {
			poseStack.rotateDegrees(Axis.ZP, 180.0F);
			poseStack.rotateDegrees(Axis.XP, SLEEP_PITCH);
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
		float breath = Mth.sin(state.ageInTicks * 0.05F);
		if (state.sleeping) {
			netHeadYaw = 0.0F;
			headPitch = SLEEP_HEAD_PITCH + breath * 2.0F;
			headRoll = 0.0F;
		}
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

		float tuck = Math.max(0.0F, -launch) * air;
		float extend = Math.max(0.0F, launch) * air;
		float charge = state.aggressive ? 1.0F : 0.0F;
		boolean carrying = !state.sleeping && !state.carriedItem.isEmpty();
		float leftPitch = carrying ? CARRY_PITCH : Mth.sin(cycle) * 32.0F * walk - charge * 25.0F + tuck * 18.0F;
		float rightPitch = carrying ? CARRY_PITCH : Mth.sin(cycle + Mth.PI) * 32.0F * walk - charge * 25.0F + tuck * 18.0F;
		float leftRoll = carrying ? 16.0F : Mth.sin(cycle * 0.5F) * 8.0F * walk;
		float rightRoll = carrying ? -16.0F : Mth.sin(cycle * 0.5F + Mth.PI) * 8.0F * walk;
		float leftBackPitch = Mth.sin(cycle + Mth.PI) * 22.0F * walk + charge * 12.0F;
		float rightBackPitch = Mth.sin(cycle) * 22.0F * walk + charge * 12.0F;
		float leftBackRoll = Mth.cos(cycle) * 10.0F * walk;
		float rightBackRoll = Mth.cos(cycle + Mth.PI) * 10.0F * walk;
		float leftLegPitch = legSwing(cycle, walk, tuck, extend);
		float rightLegPitch = legSwing(cycle + Mth.TWO_PI / 3.0F, walk, tuck, extend);
		float middleLegPitch = legSwing(cycle + 2.0F * Mth.TWO_PI / 3.0F, walk, tuck, extend);
		float leftLegRoll = 0.0F;
		float rightLegRoll = 0.0F;
		float leftFootPitch = -leftLegPitch * 0.55F;
		float rightFootPitch = -rightLegPitch * 0.55F;
		float middleFootPitch = -middleLegPitch * 0.55F;
		if (state.sleeping) {
			// With the flip pitch, limb angles read like a standing pose turned to face the foot end,
			// so roll splays them over the pad's sides and positive pitch leans them toward the foot.
			// A baby's limbs are too short to clear the edge and splay flatter instead.
			float frontSplay = state.baby ? 60.0F : 25.0F;
			float backSplay = state.baby ? 65.0F : 35.0F;
			float legSplay = state.baby ? 60.0F : 32.0F;
			float kick = Mth.sin(state.ageInTicks * 0.16F) * 16.0F;
			leftPitch = SLEEP_LIMB_FLIP + breath * 3.0F;
			rightPitch = SLEEP_LIMB_FLIP - breath * 3.0F;
			leftRoll = -frontSplay;
			rightRoll = frontSplay;
			leftBackPitch = SLEEP_LIMB_FLIP - 15.0F + breath * 2.0F;
			rightBackPitch = SLEEP_LIMB_FLIP - 15.0F - breath * 2.0F;
			leftBackRoll = -backSplay;
			rightBackRoll = backSplay;
			leftLegPitch = SLEEP_LIMB_FLIP + 20.0F + kick;
			rightLegPitch = SLEEP_LIMB_FLIP + 20.0F - kick;
			leftLegRoll = -legSplay;
			rightLegRoll = legSplay;
			leftFootPitch = -kick * 0.7F;
			rightFootPitch = kick * 0.7F;
			// The middle leg lies back along the belly toward the foot end, foot flat.
			middleLegPitch = SLEEP_LIMB_FLIP + 98.0F;
			middleFootPitch = -98.0F;
		}
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
			leftBackPitch,
			leftBackRoll,
			light, overlay
		);
		submitArm(
			poseStack, queue, WeaverModelData.RIGHT_BACK_ARM,
			WeaverModelData.Pivot.RIGHT_BACK_ARM_X, WeaverModelData.Pivot.RIGHT_BACK_ARM_Y, WeaverModelData.Pivot.RIGHT_BACK_ARM_Z,
			rightBackPitch,
			rightBackRoll,
			light, overlay
		);
		poseStack.popPose();

		submitLeg(
			poseStack, queue,
			WeaverModelData.LEFT_LEG, WeaverModelData.LEFT_FOOT,
			WeaverModelData.Pivot.LEFT_LEG_X, WeaverModelData.Pivot.LEFT_LEG_Y, WeaverModelData.Pivot.LEFT_LEG_Z,
			WeaverModelData.Pivot.LEFT_FOOT_X, WeaverModelData.Pivot.LEFT_FOOT_Y, WeaverModelData.Pivot.LEFT_FOOT_Z,
			leftLegPitch, leftLegRoll, leftFootPitch,
			light, overlay
		);
		submitLeg(
			poseStack, queue,
			WeaverModelData.RIGHT_LEG, WeaverModelData.RIGHT_FOOT,
			WeaverModelData.Pivot.RIGHT_LEG_X, WeaverModelData.Pivot.RIGHT_LEG_Y, WeaverModelData.Pivot.RIGHT_LEG_Z,
			WeaverModelData.Pivot.RIGHT_FOOT_X, WeaverModelData.Pivot.RIGHT_FOOT_Y, WeaverModelData.Pivot.RIGHT_FOOT_Z,
			rightLegPitch, rightLegRoll, rightFootPitch,
			light, overlay
		);
		submitLeg(
			poseStack, queue,
			WeaverModelData.MIDDLE_LEG, WeaverModelData.MIDDLE_FOOT,
			WeaverModelData.Pivot.MIDDLE_LEG_X, WeaverModelData.Pivot.MIDDLE_LEG_Y, WeaverModelData.Pivot.MIDDLE_LEG_Z,
			WeaverModelData.Pivot.MIDDLE_FOOT_X, WeaverModelData.Pivot.MIDDLE_FOOT_Y, WeaverModelData.Pivot.MIDDLE_FOOT_Z,
			middleLegPitch, 0.0F, middleFootPitch,
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
		if (state.sleeping) {
			// The head hangs upside down, so these pitches lay the antennae forward along the floor.
			leftBaseX = -100.0F;
			rightBaseX = -100.0F;
			leftTipX = 0.0F;
			rightTipX = 0.0F;
			leftBaseZ = Mth.sin(age * 0.04F) * 5.0F;
			rightBaseZ = Mth.sin(age * 0.04F + 1.3F) * 5.0F;
			leftTipZ = Mth.sin(age * 0.06F + 0.5F) * 6.0F;
			rightTipZ = Mth.sin(age * 0.06F + 2.0F) * 6.0F;
		}

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
		float legRoll,
		float footRot,
		int light,
		int overlay
	) {
		poseStack.pushPose();
		rotateAround(poseStack, legX, legY, legZ, legRot, 0.0F, legRoll);
		submitMesh(poseStack, queue, legMesh, light, overlay);
		poseStack.pushPose();
		rotateAround(poseStack, footX, footY, footZ, footRot, 0.0F, 0.0F);
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
		/** Faces the head block of the pad being slept in. */
		public float sleepYaw;
		public boolean baby;
		public float flipProgress;
		public final ItemStackRenderState carriedItem = new ItemStackRenderState();
	}
}
