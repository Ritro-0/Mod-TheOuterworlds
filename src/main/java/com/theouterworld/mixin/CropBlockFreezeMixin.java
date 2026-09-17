package com.theouterworld.mixin;

import com.theouterworld.world.CropFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockFreezeMixin {
	@Shadow
	public abstract int getAge(BlockState state);

	@Shadow
	public abstract int getMaxAge();

	@Shadow
	public abstract BlockState getStateForAge(int age);

	@Shadow
	protected abstract int getBonemealAgeIncrease(Level level);

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
		CropBlock crop = (CropBlock) (Object) this;
		BlockState vanillaNext = this.getStateForAge(this.getAge(state) + 1);
		level.setBlock(pos, CropFreeze.maybeFreeze(crop, level, state, vanillaNext), Block.UPDATE_CLIENTS);
		ci.cancel();
	}

	@Inject(
		method = "growCrops",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
		),
		cancellable = true,
		require = 1
	)
	private void theouterworlds$freezeOnGrowCrops(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
		CropBlock crop = (CropBlock) (Object) this;
		int nextAge = Math.min(this.getMaxAge(), this.getAge(state) + this.getBonemealAgeIncrease(level));
		BlockState vanillaNext = this.getStateForAge(nextAge);
		level.setBlock(pos, CropFreeze.maybeFreeze(crop, level, state, vanillaNext), Block.UPDATE_CLIENTS);
		ci.cancel();
	}
}
