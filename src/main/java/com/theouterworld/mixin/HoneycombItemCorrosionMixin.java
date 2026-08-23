package com.theouterworld.mixin;

import com.theouterworld.block.Corrosion;
import java.util.Optional;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneycombItem.class)
public class HoneycombItemCorrosionMixin {
	@Inject(method = "getWaxed", at = @At("RETURN"), cancellable = true)
	private static void theouterworlds$waxCorroded(BlockState state, CallbackInfoReturnable<Optional<BlockState>> cir) {
		if (cir.getReturnValue().isPresent()) {
			return;
		}
		BlockState waxed = Corrosion.getWaxed(state);
		if (waxed != null) {
			cir.setReturnValue(Optional.of(waxed));
		}
	}
}
