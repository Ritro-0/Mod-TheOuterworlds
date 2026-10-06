package com.theouterworld.mixin;

import com.theouterworld.world.WeaverColonySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class WeaverFurnitureMixin {
	@Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
	private void theouterworlds$weaverPlacedBlock(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		ServerLevel level = (ServerLevel) (Object) this;
		WeaverColonySavedData.get(level).onBlockChanged(level, pos, oldState, newState);
	}
}
