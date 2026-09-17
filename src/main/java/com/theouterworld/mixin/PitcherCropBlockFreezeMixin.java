package com.theouterworld.mixin;

import com.theouterworld.world.CropFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PitcherCropBlock.class)
public class PitcherCropBlockFreezeMixin {
	@Inject(method = "grow", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeOnGrow(ServerLevel level, BlockState state, BlockPos pos, int increase, CallbackInfo ci) {
		if (CropFreeze.tryFreezePitcherGrowth(level, state, pos, increase)) {
			ci.cancel();
		}
	}
}
