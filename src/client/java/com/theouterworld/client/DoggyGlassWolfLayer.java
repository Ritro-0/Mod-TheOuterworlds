package com.theouterworld.client;

import com.theouterworld.item.DoggyGlassItem;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

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
	private final EquipmentLayerRenderer equipmentRenderer;

	public DoggyGlassWolfLayer(RenderLayerParent<WolfRenderState, WolfModel> renderer, EquipmentLayerRenderer equipmentRenderer) {
		super(renderer);
		this.equipmentRenderer = equipmentRenderer;
		// Past the wolf armor shell (0.2) so this helmet does not share that mesh.
		this.model = new AdultWolfModel(
			LayerDefinition.create(AdultWolfModel.createBodyLayer(new CubeDeformation(0.4F)), 64, 32).bakeRoot()
		);
		for (String part : BODY_PARTS) {
			this.model.root().getChild(part).visible = false;
		}
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
				this.model,
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
