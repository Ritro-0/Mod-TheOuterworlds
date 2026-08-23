package com.theouterworld.mixin;

import com.theouterworld.block.IronGolemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {

	@Redirect(
		method = {"getOrCreateIronGolemFull", "getOrCreateIronGolemBase"},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/pattern/BlockInWorld;hasState(Ljava/util/function/Predicate;)Ljava/util/function/Predicate;"
		),
		require = 1
	)
	private static Predicate<BlockInWorld> theouterworlds$acceptCustomIron(Predicate<BlockState> predicate) {
		if (predicate.test(Blocks.IRON_BLOCK.defaultBlockState())) {
			return BlockInWorld.hasState(IronGolemBlocks::isGolemBody);
		}
		return BlockInWorld.hasState(predicate);
	}

	@Inject(method = "spawnGolemInWorld", at = @At("HEAD"), require = 1)
	private static void theouterworlds$captureIronOxidation(
		Level world,
		BlockPattern.BlockPatternMatch match,
		Entity golem,
		BlockPos pos,
		CallbackInfo ci
	) {
		if (golem instanceof IronGolem) {
			IronGolemBlocks.captureFromPattern(match);
		}
	}
}
