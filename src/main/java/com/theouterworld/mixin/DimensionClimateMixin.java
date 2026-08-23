package com.theouterworld.mixin;

import com.theouterworld.world.DimensionClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
public class DimensionClimateMixin {
	@ModifyVariable(
		method = {
			"setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
			"setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"
		},
		at = @At("HEAD"),
		argsOnly = true
	)
	private BlockState theouterworlds$convertClimate(BlockState state, BlockPos pos) {
		return DimensionClimate.convert((Level) (Object) this, pos, state);
	}
}
