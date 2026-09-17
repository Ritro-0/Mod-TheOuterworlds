package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AzaleaBlock.class)
public class AzaleaBlockFreezeMixin {
	@Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeAzalea(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (PlantFreeze.tryFreezeAzalea(level, pos, state)) {
			ci.cancel();
		}
	}
}
