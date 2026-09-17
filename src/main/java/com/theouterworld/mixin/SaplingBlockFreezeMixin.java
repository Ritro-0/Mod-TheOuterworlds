package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public class SaplingBlockFreezeMixin {
	@Inject(method = "advanceTree", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeSapling(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfo ci) {
		if (PlantFreeze.tryFreezeSapling(level, pos, state)) {
			ci.cancel();
		}
	}
}
