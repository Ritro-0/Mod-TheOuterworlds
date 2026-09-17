package com.theouterworld.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

/**
 * Glow overlay for ionic methane deposits.
 */
public final class IonicMethaneGlowRenderer {
	private static final Identifier GLOW_TEXTURE = OuterWorldMod.id("textures/block/methane_still.png");
	private static final RenderType GLOW_LAYER = RenderTypes.eyes(GLOW_TEXTURE);
	private static final int CHUNK_RADIUS = 4;
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final ModelPart CUBE = createGlowCube();

	private static List<BlockPos> glowBlocks = List.of();

	private IonicMethaneGlowRenderer() {
	}

	public static void register() {
		LevelExtractionEvents.END_EXTRACTION.register(IonicMethaneGlowRenderer::extract);
		LevelRenderEvents.COLLECT_SUBMITS.register(IonicMethaneGlowRenderer::submit);
	}

	private static ModelPart createGlowCube() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"cube",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.4F)),
			PartPose.ZERO
		);
		return LayerDefinition.create(mesh, 16, 16).bakeRoot();
	}

	private static void extract(LevelExtractionContext context) {
		ClientLevel level = context.level();
		if (level == null) {
			glowBlocks = List.of();
			return;
		}

		Vec3 camera = context.camera().position();
		int camChunkX = SectionPos.blockToSectionCoord(camera.x);
		int camChunkZ = SectionPos.blockToSectionCoord(camera.z);
		List<BlockPos> found = new ArrayList<>();

		for (int cx = camChunkX - CHUNK_RADIUS; cx <= camChunkX + CHUNK_RADIUS; cx++) {
			for (int cz = camChunkZ - CHUNK_RADIUS; cz <= camChunkZ + CHUNK_RADIUS; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
				if (chunk == null) {
					continue;
				}
				collectFromChunk(chunk, found);
			}
		}

		glowBlocks = found;
	}

	private static void collectFromChunk(LevelChunk chunk, List<BlockPos> found) {
		LevelChunkSection[] sections = chunk.getSections();
		int minSectionY = chunk.getMinSectionY();
		int originX = chunk.getPos().getMinBlockX();
		int originZ = chunk.getPos().getMinBlockZ();

		for (int i = 0; i < sections.length; i++) {
			LevelChunkSection section = sections[i];
			if (section == null || section.hasOnlyAir()) {
				continue;
			}
			if (!section.maybeHas(state -> state.is(ModBlocks.IONIC_METHANE))) {
				continue;
			}

			int originY = SectionPos.sectionToBlockCoord(minSectionY + i);
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						BlockState state = section.getBlockState(x, y, z);
						if (state.is(ModBlocks.IONIC_METHANE)) {
							found.add(new BlockPos(originX + x, originY + y, originZ + z));
						}
					}
				}
			}
		}
	}

	private static void submit(LevelRenderContext context) {
		List<BlockPos> blocks = glowBlocks;
		if (blocks.isEmpty()) {
			return;
		}

		PoseStack matrices = context.poseStack();
		SubmitNodeCollector collector = context.submitNodeCollector();
		Vec3 camera = context.levelState().cameraRenderState.pos;

		for (BlockPos pos : blocks) {
			matrices.pushPose();
			matrices.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
			collector.submitModelPart(
				CUBE,
				matrices,
				GLOW_LAYER,
				FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY,
				null
			);
			matrices.popPose();
		}
	}
}
