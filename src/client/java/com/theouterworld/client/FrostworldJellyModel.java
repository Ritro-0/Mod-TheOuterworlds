package com.theouterworld.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Blockbench frostworld jelly. The stalk stays planted; {@code cap} is what sways.
 */
public final class FrostworldJellyModel {
	public final ModelPart root;
	public final ModelPart cap;

	public FrostworldJellyModel() {
		this.root = createBodyLayer().bakeRoot();
		this.cap = this.root.getChild("cap");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"stalk",
			CubeListBuilder.create().texOffs(24, 25).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		PartDefinition cap = root.addOrReplaceChild(
			"cap",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 7.0F, 10.0F)
				.texOffs(0, 17).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 6.0F, 6.0F)
				.texOffs(24, 17).addBox(-2.0F, -15.0F, -2.0F, 4.0F, 4.0F, 4.0F)
				.texOffs(-4, -6).addBox(-5.0F, -7.0F, -4.0F, 0.0F, 2.0F, 8.0F)
				.texOffs(-4, -6).addBox(5.0F, -7.0F, -4.0F, 0.0F, 2.0F, 8.0F),
			PartPose.offset(0.0F, 14.0F, 0.0F)
		);
		cap.addOrReplaceChild(
			"frill_north",
			CubeListBuilder.create().texOffs(-4, -6).addBox(1.0F, -2.0F, -5.0F, 0.0F, 2.0F, 8.0F),
			PartPose.offsetAndRotation(1.0F, -5.0F, -4.0F, 0.0F, 1.5708F, 0.0F)
		);
		cap.addOrReplaceChild(
			"frill_south",
			CubeListBuilder.create().texOffs(-4, -6).addBox(1.0F, -2.0F, -5.0F, 0.0F, 2.0F, 8.0F),
			PartPose.offsetAndRotation(1.0F, -5.0F, 6.0F, 0.0F, 1.5708F, 0.0F)
		);
		return LayerDefinition.create(mesh, 64, 64);
	}
}
