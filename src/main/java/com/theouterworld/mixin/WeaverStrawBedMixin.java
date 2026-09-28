package com.theouterworld.mixin;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Straw beds fall apart the moment their occupant gets up. The Anchors use them as
 * permanent bunks, so a Weaver leaving one has to leave the weave intact.
 *
 * <p>The call sits inside a lambda in {@code stopSleeping}, hence the wildcard target.
 */
@Mixin(LivingEntity.class)
public class WeaverStrawBedMixin {
	@Redirect(
		method = "*",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/AbstractBedBlock;onStopSleeping(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
		),
		require = 1
	)
	private void theouterworlds$keepWeaverBedIntact(AbstractBedBlock bed, Level level, BlockPos pos) {
		if ((Object) this instanceof WeaverEntity) {
			return;
		}
		bed.onStopSleeping(level, pos);
	}
}
