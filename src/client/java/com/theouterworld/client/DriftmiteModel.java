package com.theouterworld.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Blockbench driftmite. {@code light} is the yellow lure cube and is the only fullbright part.
 * {@code tail} and {@code dorsal} are the flat fin planes.
 */
public final class DriftmiteModel {
	public final ModelPart root;
	public final ModelPart body;
	public final ModelPart tail;
	public final ModelPart dorsal;
	public final ModelPart light;

	public DriftmiteModel() {
		this.root = createBodyLayer().bakeRoot();
		this.body = this.root.getChild("body");
		this.tail = this.root.getChild("tail");
		this.dorsal = this.root.getChild("dorsal");
		this.light = this.root.getChild("light");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(0, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 3.0F, 4.0F)
				.texOffs(14, 5).addBox(-1.0F, -4.0F, -3.0F, 2.0F, 3.0F, 2.0F)
				.texOffs(14, 0).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 2.0F, 3.0F),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		body.addOrReplaceChild(
			"shaft",
			CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, -2.0F, 0.0F, 1.0F, 1.0F, 3.0F),
			PartPose.offsetAndRotation(-0.5F, -5.0F, -6.0F, -0.9163F, 0.0F, 0.0F)
		);
		body.addOrReplaceChild(
			"right_legs",
			CubeListBuilder.create()
				.texOffs(20, 21).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F)
				.texOffs(16, 21).addBox(-1.0F, -2.0F, -3.0F, 1.0F, 2.0F, 1.0F),
			PartPose.offsetAndRotation(2.1F, -0.1F, 1.0F, 0.0F, 0.0F, -0.5672F)
		);
		body.addOrReplaceChild(
			"left_legs",
			CubeListBuilder.create()
				.texOffs(20, 18).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F)
				.texOffs(16, 18).addBox(-1.0F, -2.0F, 3.0F, 1.0F, 2.0F, 1.0F),
			PartPose.offsetAndRotation(-1.2F, 0.4F, -2.0F, 0.0F, 0.0F, 0.5672F)
		);
		root.addOrReplaceChild(
			"dorsal",
			CubeListBuilder.create().texOffs(12, 11).addBox(0.0F, -1.0F, -3.0F, 0.0F, 1.0F, 6.0F),
			PartPose.offset(0.0F, 20.0F, 0.0F)
		);
		root.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 0.0F, 0.0F, 4.0F, 7.0F),
			PartPose.offset(0.0F, 21.5F, 3.0F)
		);
		root.addOrReplaceChild(
			"light",
			CubeListBuilder.create().texOffs(8, 18).addBox(0.0F, -2.0F, 1.0F, 2.0F, 2.0F, 2.0F),
			PartPose.offsetAndRotation(-1.0F, 16.5F, -7.2F, -0.9163F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(mesh, 32, 32);
	}
}
