package com.theouterworld.mixin.client;

import java.util.function.UnaryOperator;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Wolf armor has no trim patterns. Doggy Glass uses the humanoid helmet patterns,
 * and only the head of that mesh is drawn.
 */
@Mixin(targets = "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer$TrimTextureKey")
public abstract class DoggyGlassTrimTextureMixin {
	@Shadow
	public abstract EquipmentClientInfo equipmentInfo();

	@Redirect(
		method = "getOrPrepareTexture",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/resources/Identifier;withPath(Ljava/util/function/UnaryOperator;)Lnet/minecraft/resources/Identifier;"
		)
	)
	private Identifier theouterworlds$humanoidTrimOnWolfHelmet(Identifier textureId, UnaryOperator<String> pathMapper) {
		if (theouterworlds$isDoggyGlass()) {
			return textureId.withPath(path -> "trims/entity/humanoid/" + path);
		}
		return textureId.withPath(pathMapper);
	}

	private boolean theouterworlds$isDoggyGlass() {
		for (EquipmentClientInfo.Layer layer : this.equipmentInfo().getLayers(EquipmentClientInfo.LayerType.WOLF_BODY)) {
			if ("doggy_helmet".equals(layer.textureId().getPath())) {
				return true;
			}
		}
		return false;
	}
}
