package com.theouterworld.mixin;

import com.theouterworld.world.PlantFreeze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingBlockFreezeMixin {
	@Inject(method = "growBamboo", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$freezeBambooSapling(Level level, BlockPos pos, CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel && PlantFreeze.tryFreezeBambooSapling(serverLevel, pos, level.getBlockState(pos))) {
			ci.cancel();
		}
	}
}
