package com.theouterworld.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.item.DoggyGlassItem;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

/** Glass helmet on the wolf's head. Wolf armor stays on the body and is not drawn here. */
public class DoggyGlassWolfLayer extends RenderLayer<WolfRenderState, WolfModel> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
		"theouterworlds",
		"textures/entity/equipment/wolf_body/doggy_helmet.png"
	);
	private static final String[] BODY_PARTS = {
		"body",
		"upper_body",
		"right_hind_leg",
		"left_hind_leg",
		"right_front_leg",
		"left_front_leg",
		"tail"
	};

	private final AdultWolfModel model;
	private final AdultWolfModel trimModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	public DoggyGlassWolfLayer(RenderLayerParent<WolfRenderState, WolfModel> renderer, EquipmentLayerRenderer equipmentRenderer) {
		super(renderer);
		this.equipmentRenderer = equipmentRenderer;
		// Past the wolf armor shell (0.2) so this helmet does not share that mesh.
		this.model = bakeHelmet();
		this.trimModel = bakeHelmet();
		// Trim sheets are laid out for a humanoid helmet. Sample each of those face tiles flat on the dog's glass.
		layTrimFlat(this.trimModel.root().getChild("head"));
	}

	private static AdultWolfModel bakeHelmet() {
		AdultWolfModel model = new AdultWolfModel(
			LayerDefinition.create(AdultWolfModel.createBodyLayer(new CubeDeformation(0.4F)), 64, 32).bakeRoot()
		);
		for (String part : BODY_PARTS) {
			model.root().getChild(part).visible = false;
		}
		return model;
	}

	/**
	 * Humanoid helmet trim faces on a 64x32 sheet, in the same order {@code ModelPart.Cube} writes them.
	 * Each doggy-glass face shows that whole tile instead of a slice of the wolf unwrap.
	 */
	private static void layTrimFlat(ModelPart head) {
		head.visit(new PoseStack(), (pose, path, index, cube) -> {
			ModelPart.Polygon[] polygons = cube.polygons;
			for (int i = 0; i < polygons.length; i++) {
				ModelPart.Polygon polygon = polygons[i];
				float[] uv = helmetFaceUv(facing(polygon.normal()));
				ModelPart.Vertex[] source = polygon.vertices();
				ModelPart.Vertex[] copy = new ModelPart.Vertex[source.length];
				for (int vertex = 0; vertex < source.length; vertex++) {
					ModelPart.Vertex from = source[vertex];
					copy[vertex] = new ModelPart.Vertex(from.x(), from.y(), from.z(), 0.0F, 0.0F);
				}
				polygons[i] = new ModelPart.Polygon(copy, uv[0], uv[1], uv[2], uv[3], 64.0F, 32.0F, false, facing(polygon.normal()));
			}
		});
	}

	private static float[] helmetFaceUv(Direction direction) {
		return switch (direction) {
			case DOWN -> new float[] {8.0F, 0.0F, 16.0F, 8.0F};
			case UP -> new float[] {16.0F, 8.0F, 24.0F, 0.0F};
			case NORTH -> new float[] {8.0F, 8.0F, 16.0F, 16.0F};
			case SOUTH -> new float[] {24.0F, 8.0F, 32.0F, 16.0F};
			case WEST -> new float[] {0.0F, 8.0F, 8.0F, 16.0F};
			case EAST -> new float[] {16.0F, 8.0F, 24.0F, 16.0F};
		};
	}

	private static Direction facing(Vector3fc normal) {
		Direction closest = Direction.NORTH;
		float best = Float.NEGATIVE_INFINITY;
		for (Direction direction : Direction.values()) {
			Vector3fc unit = direction.getUnitVec3f();
			float dot = unit.x() * normal.x() + unit.y() * normal.y() + unit.z() * normal.z();
			if (dot > best) {
				best = dot;
				closest = direction;
			}
		}
		return closest;
	}

	@Override
	public void submit(
		com.mojang.blaze3d.vertex.PoseStack poseStack,
		SubmitNodeCollector collector,
		int lightCoords,
		WolfRenderState state,
		float yRot,
		float xRot
	) {
		if (state.isBaby) {
			return;
		}
		ItemStack stack = ((DoggyGlassCarrier) state).theouterworlds$doggyGlass();
		if (!DoggyGlassItem.isDoggyGlass(stack)) {
			return;
		}
		int tint = ARGB.opaque(DoggyGlassItem.tintRgb(DoggyGlassItem.kindOf(stack)));
		collector.order(1).submitModel(
			this.model,
			state,
			poseStack,
			RenderTypes.entityTranslucent(TEXTURE),
			lightCoords,
			OverlayTexture.NO_OVERLAY,
			tint,
			null,
			state.outlineColor
		);
		if (stack.get(DataComponents.TRIM) != null) {
			this.equipmentRenderer.renderLayers(
				EquipmentClientInfo.LayerType.WOLF_BODY,
				DoggyGlassItem.EQUIPMENT_ASSET,
				this.trimModel,
				state,
				stack,
				poseStack,
				collector,
				lightCoords,
				null,
				state.outlineColor,
				2
			);
		}
	}
}
