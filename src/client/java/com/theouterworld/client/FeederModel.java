package com.theouterworld.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Blockbench vent_feeder. Parts are siblings so the glowing segment can be lit on its own,
 * but their offsets run nose-to-tail so the renderer can stack them into one worm.
 * {@code light} is the only part drawn fullbright.
 */
public final class FeederModel {
	public final ModelPart root;
	public final ModelPart front;
	public final ModelPart light;
	public final ModelPart mid;
	public final ModelPart rear;

	public FeederModel() {
		this.root = createBodyLayer().bakeRoot();
		this.front = this.root.getChild("front");
		this.light = this.root.getChild("light");
		this.mid = this.root.getChild("mid");
		this.rear = this.root.getChild("rear");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"front",
			CubeListBuilder.create().texOffs(10, 0).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
			PartPose.offset(0.0F, 24.0F, 8.0F)
		);
		PartDefinition light = root.addOrReplaceChild(
			"light",
			CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
			PartPose.offset(0.0F, 0.0F, -4.0F)
		);
		light.addOrReplaceChild(
			"light_r1",
			CubeListBuilder.create().texOffs(10, 5).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
			PartPose.offsetAndRotation(0.0F, -3.3F, 0.7F, 1.1781F, 0.0F, 0.0F)
		);
		light.addOrReplaceChild(
			"light_shaft_r1",
			CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F),
			PartPose.offsetAndRotation(-0.5F, -1.9F, -0.4F, 1.1781F, 0.0F, 0.0F)
		);
		root.addOrReplaceChild(
			"mid",
			CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
			PartPose.offset(0.0F, 0.0F, -4.0F)
		);
		root.addOrReplaceChild(
			"rear",
			CubeListBuilder.create().texOffs(0, 10).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
			PartPose.offset(0.0F, 0.0F, -4.0F)
		);
		return LayerDefinition.create(mesh, 32, 32);
	}
}
