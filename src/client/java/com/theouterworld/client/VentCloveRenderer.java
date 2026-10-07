package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.VentCloveBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws the planes of {@code block/vent_clove.json} one by one, each leaning and fluttering
 * in a slow current. Bases stay planted; only the tops drift.
 */
public class VentCloveRenderer implements BlockEntityRenderer<VentCloveBlockEntity, VentCloveRenderer.VentCloveRenderState> {
	private static final Identifier TEXTURE = OuterWorldMod.id("textures/block/vent_clove.png");

	/**
	 * Mirrors the Blockbench elements: from, to, Y rotation and its origin, then the two
	 * visible faces' UVs (east/west for planes across X, north/south for planes across Z).
	 */
	private static final Plane[] PLANES = {
		new Plane(0, 0, 0, 0, 8, 16, 0, 0, 0, new float[] {0, 0, 4, 2}, new float[] {0, 2, 4, 4}),
		new Plane(0, 0, 0, 16, 8, 0, 0, 0, 0, new float[] {0, 4, 4, 6}, new float[] {4, 0, 8, 2}),
		new Plane(16, 0, 16, 16, 8, 32, -180, 16, 16, new float[] {4, 2, 8, 4}, new float[] {4, 4, 8, 6}),
		new Plane(16, 0, 16, 32, 8, 16, -180, 16, 16, new float[] {0, 6, 4, 8}, new float[] {4, 6, 8, 8}),
		new Plane(3, 0, 3, 13, 11, 3, 0, 0, 0, new float[] {0, 8, 2.5F, 10.75F}, new float[] {8, 0, 10.5F, 2.75F}),
		new Plane(3, 0, 13, 13, 11, 13, 0, 0, 0, new float[] {2.5F, 8, 5, 10.75F}, new float[] {8, 2.75F, 10.5F, 5.5F}),
		new Plane(3, 0, 13, 13, 11, 13, 90, 8, 8, new float[] {5, 8, 7.5F, 10.75F}, new float[] {8, 5.5F, 10.5F, 8.25F}),
		new Plane(3, 0, 3, 13, 11, 3, 90, 8, 8, new float[] {7.5F, 8.25F, 10, 11}, new float[] {10, 8.25F, 12.5F, 11})
	};

	public VentCloveRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public VentCloveRenderState createRenderState() {
		return new VentCloveRenderState();
	}

	@Override
	public void extractRenderState(
		VentCloveBlockEntity blockEntity,
		VentCloveRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.time = blockEntity.getLevel() != null
			? (float) (blockEntity.getLevel().getGameTime() % 240000L) + tickProgress
			: 0.0F;
	}

	@Override
	public void submit(VentCloveRenderState state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
		float t = state.time;
		// One current for the whole seabed; its heading wanders slowly so the lean is never fixed.
		float heading = 0.9F + Mth.sin(t * 0.0009F) * 0.8F;
		float flowX = Mth.cos(heading);
		float flowZ = Mth.sin(heading);
		int bx = state.blockPos.getX();
		int bz = state.blockPos.getZ();
		float jitter = (Mth.murmurHash3Mixer(bx * 31 + bz * 17 + state.blockPos.getY()) & 1023) / 1023.0F * Mth.TWO_PI;

		float[] shearX = new float[PLANES.length];
		float[] shearZ = new float[PLANES.length];
		for (int i = 0; i < PLANES.length; i++) {
			Plane plane = PLANES[i];
			float along = (bx + plane.pivotX / 16.0F) * flowX + (bz + plane.pivotZ / 16.0F) * flowZ;
			// Swells roll downstream across the vent field; each plane also flutters on its own beat.
			float swell = Mth.sin(t * 0.018F - along * 0.35F + i * 0.6F);
			float lean = 4.0F + swell * 5.0F;
			float flutter = Mth.sin(t * 0.045F + jitter + i * 2.1F) * 2.0F;
			float leanTan = (float) Math.tan(Math.toRadians(lean));
			float flutterTan = (float) Math.tan(Math.toRadians(flutter));
			shearX[i] = leanTan * flowX - flutterTan * flowZ;
			shearZ[i] = leanTan * flowZ + flutterTan * flowX;
		}

		int light = state.lightCoords;
		queue.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(TEXTURE), (pose, consumer) -> {
			for (int i = 0; i < PLANES.length; i++) {
				PLANES[i].write(pose, consumer, shearX[i], shearZ[i], light);
			}
		});
	}

	private static final class Plane {
		private final float x1;
		private final float y1;
		private final float z1;
		private final float x2;
		private final float y2;
		private final float z2;
		private final float cos;
		private final float sin;
		private final float originX;
		private final float originZ;
		private final float[] uvA;
		private final float[] uvB;
		final float pivotX;
		final float pivotZ;

		Plane(
			float x1, float y1, float z1, float x2, float y2, float z2,
			float yRot, float originX, float originZ,
			float[] uvA, float[] uvB
		) {
			this.x1 = x1;
			this.y1 = y1;
			this.z1 = z1;
			this.x2 = x2;
			this.y2 = y2;
			this.z2 = z2;
			float rad = yRot * Mth.DEG_TO_RAD;
			this.cos = Mth.cos(rad);
			this.sin = Mth.sin(rad);
			this.originX = originX;
			this.originZ = originZ;
			this.uvA = uvA;
			this.uvB = uvB;
			float midX = (x1 + x2) * 0.5F - originX;
			float midZ = (z1 + z2) * 0.5F - originZ;
			this.pivotX = originX + midX * this.cos + midZ * this.sin;
			this.pivotZ = originZ - midX * this.sin + midZ * this.cos;
		}

		void write(PoseStack.Pose pose, VertexConsumer consumer, float shearX, float shearZ, int light) {
			if (this.x1 == this.x2) {
				float x = this.x1;
				// East face, then west face; each is wound to face outward.
				quad(pose, consumer, light, shearX, shearZ, this.uvA, 1, 0, 0,
					x, this.z2, x, this.z1);
				quad(pose, consumer, light, shearX, shearZ, this.uvB, -1, 0, 0,
					x, this.z1, x, this.z2);
			} else {
				float z = this.z1;
				// North face, then south face.
				quad(pose, consumer, light, shearX, shearZ, this.uvA, 0, 0, -1,
					this.x2, z, this.x1, z);
				quad(pose, consumer, light, shearX, shearZ, this.uvB, 0, 0, 1,
					this.x1, z, this.x2, z);
			}
		}

		/** Left edge (as seen from the face) at {@code (leftX, leftZ)}, right edge at {@code (rightX, rightZ)}. */
		private void quad(
			PoseStack.Pose pose, VertexConsumer consumer, int light, float shearX, float shearZ,
			float[] uv, float nx, float ny, float nz,
			float leftX, float leftZ, float rightX, float rightZ
		) {
			float u1 = uv[0] / 16.0F;
			float v1 = uv[1] / 16.0F;
			float u2 = uv[2] / 16.0F;
			float v2 = uv[3] / 16.0F;
			float rnx = nx * this.cos + nz * this.sin;
			float rnz = -nx * this.sin + nz * this.cos;
			vertex(pose, consumer, leftX, this.y2, leftZ, u1, v1, rnx, ny, rnz, shearX, shearZ, light);
			vertex(pose, consumer, leftX, this.y1, leftZ, u1, v2, rnx, ny, rnz, shearX, shearZ, light);
			vertex(pose, consumer, rightX, this.y1, rightZ, u2, v2, rnx, ny, rnz, shearX, shearZ, light);
			vertex(pose, consumer, rightX, this.y2, rightZ, u2, v1, rnx, ny, rnz, shearX, shearZ, light);
		}

		private void vertex(
			PoseStack.Pose pose, VertexConsumer consumer,
			float x, float y, float z, float u, float v,
			float nx, float ny, float nz,
			float shearX, float shearZ, int light
		) {
			float dx = x - this.originX;
			float dz = z - this.originZ;
			float rx = this.originX + dx * this.cos + dz * this.sin;
			float rz = this.originZ - dx * this.sin + dz * this.cos;
			float px = (rx + shearX * y) / 16.0F;
			float pz = (rz + shearZ * y) / 16.0F;
			consumer.addVertex(pose, px, y / 16.0F, pz)
				.setColor(255, 255, 255, 255)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, nx, ny, nz);
		}
	}

	public static class VentCloveRenderState extends BlockEntityRenderState {
		public float time;
	}
}
