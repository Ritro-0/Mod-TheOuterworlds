package com.theouterworld.mixin;

import com.theouterworld.world.WeaverColonyHarm;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public class FlowingFluidAnchorMixin {
	@Inject(method = "spreadTo", at = @At("HEAD"))
	private void theouterworlds$anchorFlow(
		LevelAccessor level,
		BlockPos pos,
		BlockState state,
		Direction direction,
		FluidState target,
		CallbackInfo ci
	) {
		if (level instanceof ServerLevel server) {
			WeaverColonyHarm.onFluidSpread(server, pos, direction, target.getType());
		}
	}
}
