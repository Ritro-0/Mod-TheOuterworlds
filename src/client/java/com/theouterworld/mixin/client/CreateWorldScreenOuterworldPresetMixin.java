package com.theouterworld.mixin.client;

import com.theouterworld.registry.ModWorldPresets;
import com.theouterworld.world.OuterworldWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Writes the Outerworld preset marker before the integrated server starts loading the
 * new world. Writing at RETURN is too late — SERVER_STARTED already ran by then.
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenOuterworldPresetMixin {
	@Shadow
	public abstract WorldCreationUiState getUiState();

	@Inject(method = "createNewWorld", at = @At("HEAD"))
	private void theouterworlds$markOuterworldPresetEarly(
		net.minecraft.core.LayeredRegistryAccess<net.minecraft.server.RegistryLayer> registries,
		net.minecraft.world.level.storage.LevelDataAndDimensions.WorldDataAndGenSettings worldData,
		java.util.Optional<net.minecraft.world.level.gamerules.GameRules> gameRules,
		CallbackInfoReturnable<Boolean> cir
	) {
		boolean isOuterworldPreset = getUiState().getWorldType().preset()
			.unwrapKey()
			.filter(ModWorldPresets.OUTERWORLD::equals)
			.isPresent();
		if (!isOuterworldPreset) {
			return;
		}

		OuterworldWorldType.writePresetMarker(
			Minecraft.getInstance().getLevelSource().getBaseDir().resolve(getUiState().getTargetFolder())
		);
	}
}
