package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.DimensionClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla only lights nether portals in the Overworld and Nether.
 * Allow the same ignition check here so flint-and-steel can target a frame,
 * then freeze / snuff whatever actually gets placed.
 */
@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
	@Inject(method = "inPortalDimension", at = @At("HEAD"), cancellable = true, require = 1)
	private static void theouterworlds$allowFrozenPortalIgnition(Level level, CallbackInfoReturnable<Boolean> cir) {
		if (ModDimensions.isLowGravity(level.dimension())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "onPlace", at = @At("RETURN"))
	private void theouterworlds$snuffOrFreeze(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
		if (level.isClientSide() || !ModDimensions.isLowGravity(level.dimension())) {
			return;
		}
		BlockState current = level.getBlockState(pos);
		if (!(current.getBlock() instanceof BaseFireBlock)) {
			return;
		}
		if (DimensionClimate.trySpawnFrozenPortal(level, pos)) {
			return;
		}
		if (level.getBlockState(pos).getBlock() instanceof BaseFireBlock) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
	}
}
