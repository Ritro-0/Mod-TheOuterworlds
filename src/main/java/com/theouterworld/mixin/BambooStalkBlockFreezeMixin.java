package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooStalkBlock.class)
public class BambooStalkBlockFreezeMixin {
	@Inject(method = "growBamboo", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeBambooStalk(BlockState state, Level level, BlockPos pos, RandomSource random, int height, CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel && PlantFreeze.tryFreezeBambooStalk(serverLevel, pos, state)) {
			ci.cancel();
		}
	}
}
