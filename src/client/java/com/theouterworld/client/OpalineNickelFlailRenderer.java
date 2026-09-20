package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.OpalineNickelFlailEntity;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OpalineNickelFlailRenderer extends EntityRenderer<OpalineNickelFlailEntity, OpalineNickelFlailRenderer.FlailRenderState> {
	private static final Identifier NICKEL_TEXTURE = OuterWorldMod.id("textures/block/nickel_block.png");
	private static final Identifier OPAL_TEXTURE = OuterWorldMod.id("textures/block/opal_block.png");

	public OpalineNickelFlailRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public FlailRenderState createRenderState() {
		return new FlailRenderState();
	}

	@Override
	protected AABB getBoundingBoxForCulling(OpalineNickelFlailEntity entity, float partialTicks) {
		AABB box = super.getBoundingBoxForCulling(entity, partialTicks);
		Entity owner = entity.getOwner();
		if (owner != null) {
			return box.minmax(owner.getBoundingBox());
		}
		return box;
	}

	@Override
	public void extractRenderState(OpalineNickelFlailEntity entity, FlailRenderState state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		state.yRot = Mth.lerp(tickProgress, entity.yRotO, entity.getYRot());
		state.xRot = Mth.lerp(tickProgress, entity.xRotO, entity.getXRot());
		state.spin = (entity.tickCount + tickProgress) * 18.0F;
		state.shadowRadius = 0.2F;
		extractChainState(entity, state, tickProgress);
	}

	private void extractChainState(OpalineNickelFlailEntity entity, FlailRenderState state, float tickProgress) {
		Entity owner = entity.getOwner();
		if (owner == null || !owner.isAlive()) {
			state.leashStates = null;
			return;
		}

		if (state.leashStates == null
			|| state.leashStates.size() != 1
			|| !(state.leashStates.getFirst() instanceof CharcoalLeashState)) {
			state.leashStates = new ArrayList<>(1);
			state.leashStates.add(new CharcoalLeashState());
		}

		CharcoalLeashState leash = (CharcoalLeashState) state.leashStates.getFirst();
		Vec3 offset = new Vec3(0.0, entity.getBbHeight() * 0.5, 0.0);
		leash.offset = offset;
		leash.start = entity.getPosition(tickProgress).add(offset);
		leash.end = getChainHoldPosition(owner, entity.getThrownHand(), tickProgress);
		leash.slack = true;

		BlockPos startPos = BlockPos.containing(entity.getEyePosition(tickProgress));
		BlockPos endPos = BlockPos.containing(owner.getEyePosition(tickProgress));
		leash.startBlockLight = getBlockLightLevel(entity, startPos);
		leash.endBlockLight = entity.level().getBrightness(LightLayer.BLOCK, endPos);
		leash.startSkyLight = entity.level().getBrightness(LightLayer.SKY, startPos);
		leash.endSkyLight = entity.level().getBrightness(LightLayer.SKY, endPos);
	}

	private Vec3 getChainHoldPosition(Entity owner, InteractionHand thrownHand, float tickProgress) {
		if (!(owner instanceof Player player)) {
			return owner.getRopeHoldPosition(tickProgress);
		}

		HumanoidArm arm = getHoldingArm(player, thrownHand);
		float armSign = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float swing = player.getSwingAnimation(tickProgress);
		float bob = Mth.sin(Mth.sqrt(swing) * (float) Math.PI);

		Minecraft minecraft = Minecraft.getInstance();
		if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson()
			&& player == minecraft.player) {
			return getFirstPersonHandPos(player, armSign, bob, tickProgress);
		}
		return getThirdPersonHandPos(player, armSign, tickProgress);
	}

	private static HumanoidArm getHoldingArm(Player player, InteractionHand thrownHand) {
		HumanoidArm main = player.getMainArm();
		return thrownHand == InteractionHand.MAIN_HAND ? main : main.getOpposite();
	}

	/** Matches vanilla fishing-line third-person hand attachment, but for either arm. */
	private static Vec3 getThirdPersonHandPos(Player player, float armSign, float tickProgress) {
		float bodyRot = Mth.lerp(tickProgress, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180.0F);
		double sin = Mth.sin(bodyRot);
		double cos = Mth.cos(bodyRot);
		float scale = player.getScale();
		double side = armSign * 0.35 * scale;
		double forward = 0.8 * scale;
		float crouch = player.isCrouching() ? -0.1875F : 0.0F;
		return player.getEyePosition(tickProgress).add(
			-cos * side - sin * forward,
			crouch - 0.45 * scale,
			-sin * side + cos * forward
		);
	}

	/** Matches vanilla fishing-line first-person attachment for the active arm. */
	private Vec3 getFirstPersonHandPos(Player player, float armSign, float bob, float tickProgress) {
		float fov = this.entityRenderDispatcher.options.fov().get().intValue();
		double fovScale = 960.0 / fov;
		Vec3 handOffset = this.entityRenderDispatcher.camera.getNearPlane(fov)
			.getPointOnPlane(armSign * 0.525F, -0.1F)
			.scale(fovScale)
			.yRot(bob * 0.5F)
			.xRot(-bob * 0.7F);
		return player.getEyePosition(tickProgress).add(handOffset);
	}

	@Override
	public void submit(FlailRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, -state.yRot);
		poseStack.rotateDegrees(Axis.XP, state.xRot);
		poseStack.rotateDegrees(Axis.YP, state.spin);
		poseStack.translate(0.0F, -0.15F, 0.0F);
		poseStack.scale(1.6F, 1.6F, 1.6F);

		int light = state.lightCoords;
		int overlay = OverlayTexture.NO_OVERLAY;
		// armorCutoutNoCull: Blockbench meshes often need both sides visible.
		queue.submitCustomGeometry(poseStack, RenderTypes.armorCutoutNoCull(NICKEL_TEXTURE), (pose, consumer) ->
			writeMesh(pose, consumer, OpalineNickelFlailModelData.NICKEL, light, overlay)
		);
		queue.submitCustomGeometry(poseStack, RenderTypes.armorCutoutNoCull(OPAL_TEXTURE), (pose, consumer) ->
			writeMesh(pose, consumer, OpalineNickelFlailModelData.OPAL, light, overlay)
		);
		poseStack.popPose();
		super.submit(state, poseStack, queue, camera);
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

	public static class FlailRenderState extends EntityRenderState {
		public float yRot;
		public float xRot;
		public float spin;
	}

	/** Marker so the leash renderer can tint this chain charcoal instead of lead-brown. */
	public static class CharcoalLeashState extends EntityRenderState.LeashState {
	}
}
