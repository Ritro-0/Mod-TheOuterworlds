package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CocoaBlock.class)
public class CocoaBlockFreezeMixin {
	@Inject(
		method = "randomTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
		),
		cancellable = true,
		require = 1
	)
	private void theouterworlds$freezeOnRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		int age = state.getValue(CocoaBlock.AGE);
		BlockState vanillaNext = state.setValue(CocoaBlock.AGE, age + 1);
		level.setBlock(pos, PlantFreeze.maybeFreezeCocoa(level, vanillaNext), Block.UPDATE_CLIENTS);
		ci.cancel();
	}

	@Inject(
		method = "performBonemeal",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
		),
		cancellable = true,
		require = 1
	)
	private void theouterworlds$freezeOnBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source, CallbackInfo ci) {
		int age = state.getValue(CocoaBlock.AGE);
		BlockState vanillaNext = state.setValue(CocoaBlock.AGE, age + 1);
		level.setBlock(pos, PlantFreeze.maybeFreezeCocoa(level, vanillaNext), Block.UPDATE_CLIENTS);
		ci.cancel();
	}
}
