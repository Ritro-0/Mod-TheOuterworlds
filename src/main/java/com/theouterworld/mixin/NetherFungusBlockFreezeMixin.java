package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.NetherFungusBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherFungusBlock.class)
public class NetherFungusBlockFreezeMixin {
	@Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeFungus(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source, CallbackInfo ci) {
		if (PlantFreeze.tryFreezeFungus(level, pos, state)) {
			ci.cancel();
		}
	}
}
