package com.theouterworld.client;

import com.theouterworld.entity.StrandHydraEntity;
import java.util.EnumSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

/**
 * Blockbench strand hydra. The base stays put; each tentacle is its own part so it can sway.
 */
public final class StrandHydraModel {
	public final ModelPart root;
	public final ModelPart baseTop;
	public final ModelPart[] tentacles = new ModelPart[StrandHydraEntity.TENTACLES.length];

	public StrandHydraModel() {
		this.root = createBodyLayer().bakeRoot();
		this.baseTop = createTopLayer().bakeRoot();
		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i] = this.root.getChild("tentacle" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(0, 0).addBox(
				-8.0F, -9.0F, -8.0F, 16.0F, 9.0F, 16.0F, EnumSet.complementOf(EnumSet.of(Direction.UP))
			),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		StrandHydraEntity.Tentacle[] tentacles = StrandHydraEntity.TENTACLES;
		for (int i = 0; i < tentacles.length; i++) {
			StrandHydraEntity.Tentacle tentacle = tentacles[i];
			root.addOrReplaceChild(
				"tentacle" + i,
				CubeListBuilder.create().texOffs(tentacle.u(), tentacle.v()).addBox(-2.0F, -32.0F, -2.0F, 4.0F, 32.0F, 4.0F),
				PartPose.offset(tentacle.pivotX(), tentacle.pivotY(), tentacle.pivotZ())
			);
		}
		return LayerDefinition.create(mesh, 128, 128);
	}

	/**
	 * The lid under the tentacles. UVs are shifted so that face fills a 16×16 bedrock texture.
	 */
	public static LayerDefinition createTopLayer() {
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild(
			"top",
			CubeListBuilder.create().texOffs(-32, 0).addBox(
				-8.0F, -9.0F, -8.0F, 16.0F, 9.0F, 16.0F, EnumSet.of(Direction.UP)
			),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		return LayerDefinition.create(mesh, 16, 16);
	}
}
