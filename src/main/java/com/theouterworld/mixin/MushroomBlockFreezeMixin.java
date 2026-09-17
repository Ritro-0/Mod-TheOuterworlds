package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MushroomBlock.class)
public class MushroomBlockFreezeMixin {
	@Inject(method = "growMushroom", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeMushroom(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
		if (PlantFreeze.tryFreezeMushroom(level, pos, state)) {
			cir.setReturnValue(true);
		}
	}
}
