package com.theouterworld.client;

import com.theouterworld.block.TrimmedGlassBlock;
import com.theouterworld.item.DoggyGlassItem;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.world.item.ItemStack;

/** Glass helmet on the wolf's head. Wolf armor stays on the body and is not drawn here. */
public class DoggyGlassWolfLayer extends RenderLayer<WolfRenderState, WolfModel> {
	/** Plain glass keeps the base texture; every other kind has its own doggy_helmet_<kind> texture. */
	private static final Map<TrimmedGlassBlock.Kind, Identifier> TEXTURES = new EnumMap<>(TrimmedGlassBlock.Kind.class);

	static {
		for (TrimmedGlassBlock.Kind kind : TrimmedGlassBlock.Kind.values()) {
			String name = kind == TrimmedGlassBlock.Kind.GLASS ? "doggy_helmet" : "doggy_helmet_" + kind.getSerializedName();
			TEXTURES.put(kind, Identifier.fromNamespaceAndPath("theouterworlds", "textures/entity/equipment/wolf_body/" + name + ".png"));
		}
	}

	private static final String[] BODY_PARTS = {
		"body",
		"upper_body",
		"right_hind_leg",
		"left_hind_leg",
		"right_front_leg",
		"left_front_leg",
		"tail"
	};
	private static final String[] ROOT_PARTS = {"head", "body", "upper_body", "right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg", "tail"};
	/** The glass is the wolf head inflated by the same 0.4 the model is baked with. */
	private static final float GROW = 0.4F;

	private final AdultWolfModel model;
	private final AdultWolfModel trimModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	public DoggyGlassWolfLayer(RenderLayerParent<WolfRenderState, WolfModel> renderer, EquipmentLayerRenderer equipmentRenderer) {
		super(renderer);
		this.equipmentRenderer = equipmentRenderer;
		this.model = new AdultWolfModel(bakeHelmetRoot());
		this.trimModel = bakeTrimModel();
	}

	private static ModelPart bakeHelmetRoot() {
		ModelPart root = LayerDefinition.create(AdultWolfModel.createBodyLayer(new CubeDeformation(GROW)), 64, 32).bakeRoot();
		for (String part : BODY_PARTS) {
			root.getChild(part).visible = false;
		}
		return root;
	}

	/**
	 * The trim only exists on the front and back of the glass, each one a single 6x6 sheet that maps the whole
	 * trim texture. It is built as real cubes with the right texture offsets (instead of editing the baked
	 * polygons) so renderers that cache cube geometry, like Sodium, draw exactly the same thing.
	 */
	private static AdultWolfModel bakeTrimModel() {
		ModelPart base = bakeHelmetRoot();
		ModelPart baseHead = base.getChild("head");

		// A cube's NORTH face reads u = xOff + 4 .. xOff + 10 and v = yOff + 4 .. yOff + 10 on a 6x6 sheet,
		// so -4/-4 puts it on 0..6. The SOUTH face sits 10 further along, hence -14.
		ModelPart.Cube front = new ModelPart.Cube(-4, -4, -2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, GROW, GROW, GROW, false, 6.0F, 6.0F, Set.of(Direction.NORTH));
		ModelPart.Cube back = new ModelPart.Cube(-14, -4, -2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, GROW, GROW, GROW, false, 6.0F, 6.0F, Set.of(Direction.SOUTH));
		ModelPart realHead = new ModelPart(List.of(front, back), Map.of());
		ModelPart head = new ModelPart(List.of(), new HashMap<>(Map.of("real_head", realHead)));
		head.loadPose(baseHead.storePose());
		head.setInitialPose(baseHead.getInitialPose());

		Map<String, ModelPart> children = new HashMap<>();
		for (String name : ROOT_PARTS) {
			children.put(name, base.getChild(name));
		}
		children.put("head", head);
		return new AdultWolfModel(new ModelPart(List.of(), children));
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
		// The per-kind textures already carry the glass color, so nothing is tinted on top.
		Identifier texture = TEXTURES.get(DoggyGlassItem.kindOf(stack));
		collector.order(1).submitModel(
			this.model,
			state,
			poseStack,
			RenderTypes.entityTranslucent(texture),
			lightCoords,
			OverlayTexture.NO_OVERLAY,
			-1,
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
