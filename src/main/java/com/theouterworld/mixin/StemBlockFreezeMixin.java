package com.theouterworld.mixin;

import com.theouterworld.block.FrozenStemBlock;
import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StemBlock.class)
public class StemBlockFreezeMixin {
	@Shadow
	@Final
	private ResourceKey<Block> fruit;

	@Shadow
	@Final
	private ResourceKey<Block> attachedStem;

	@Shadow
	@Final
	private TagKey<Block> fruitSupportBlocks;

	@Inject(
		method = "randomTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
		),
		cancellable = true,
		require = 1
	)
	private void theouterworlds$freezeStemGrowth(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (!PlantFreeze.inCold(level) || (Object) this instanceof FrozenStemBlock) {
			return;
		}
		BlockState vanillaNext = state.setValue(StemBlock.AGE, state.getValue(StemBlock.AGE) + 1);
		level.setBlock(pos, PlantFreeze.maybeFreezeStem(level, state, vanillaNext), Block.UPDATE_CLIENTS);
		ci.cancel();
	}

	@Inject(
		method = "performBonemeal",
		at = @At("HEAD"),
		cancellable = true,
		require = 1
	)
	private void theouterworlds$freezeStemBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source, CallbackInfo ci) {
		if (!PlantFreeze.inCold(level) || (Object) this instanceof FrozenStemBlock) {
			return;
		}
		int age = Math.min(StemBlock.MAX_AGE, state.getValue(StemBlock.AGE) + Mth.nextInt(random, 2, 5));
		BlockState vanillaNext = state.setValue(StemBlock.AGE, age);
		BlockState next = PlantFreeze.maybeFreezeStem(level, state, vanillaNext);
		level.setBlock(pos, next, Block.UPDATE_CLIENTS);
		if (next.getBlock() == state.getBlock() && next.getValue(StemBlock.AGE) == StemBlock.MAX_AGE) {
			next.randomTick(level, pos, random);
		}
		ci.cancel();
	}

	@Inject(
		method = "randomTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/Direction$Plane;getRandomDirection(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/core/Direction;"
		),
		cancellable = true,
		require = 1
	)
	private void theouterworlds$freezeFruit(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (PlantFreeze.tryPlaceStemFruit((StemBlock) (Object) this, level, pos, random, this.fruit, this.attachedStem, this.fruitSupportBlocks)) {
			ci.cancel();
		}
	}
}
