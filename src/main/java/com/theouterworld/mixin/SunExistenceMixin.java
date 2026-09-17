package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.SunExistenceGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hard-block illegal placements on The Sun (smoke puff, no block).
 */
@Mixin(Level.class)
public class SunExistenceMixin {
	@Inject(
		method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void theouterworlds$blockSunPlacement3(
		BlockPos pos,
		BlockState state,
		int flags,
		CallbackInfoReturnable<Boolean> cir
	) {
		theouterworlds$rejectIllegalSunBlock(pos, state, cir);
	}

	@Inject(
		method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void theouterworlds$blockSunPlacement4(
		BlockPos pos,
		BlockState state,
		int flags,
		int maxUpdateDepth,
		CallbackInfoReturnable<Boolean> cir
	) {
		theouterworlds$rejectIllegalSunBlock(pos, state, cir);
	}

	@Unique
	private void theouterworlds$rejectIllegalSunBlock(
		BlockPos pos,
		BlockState state,
		CallbackInfoReturnable<Boolean> cir
	) {
		Level self = (Level) (Object) this;
		if (self.isClientSide() || !ModDimensions.isSun(self.dimension())) {
			return;
		}
		if (SunExistenceGuard.isAllowedBlock(state)) {
			return;
		}
		if (self instanceof ServerLevel server) {
			server.sendParticles(
				ParticleTypes.LARGE_SMOKE,
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				6,
				0.2,
				0.3,
				0.2,
				0.02
			);
		}
		cir.setReturnValue(false);
	}
}
