package com.theouterworld.mixin;

import com.theouterworld.block.Corrosion;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemCorrosionMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$removeCorrosion(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Level world = context.getLevel();
		BlockState state = world.getBlockState(context.getClickedPos());
		if (Corrosion.tryRemove(world, context.getClickedPos(), state, context.getPlayer(), context.getItemInHand())) {
			cir.setReturnValue(InteractionResult.SUCCESS);
		}
	}
}
