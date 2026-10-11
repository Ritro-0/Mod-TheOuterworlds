package com.theouterworld.mixin;

import com.theouterworld.block.ReinforcedTintedGlassPaneBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla walls always connect to glass panes and iron bars, because those are
 * {@code IronBarsBlock}s. The reinforced tinted glass pane is a thin flat slab with no sturdy side
 * faces, so walls get the same unconditional connection, but only against its two thin end faces
 * (not the wide front and back).
 */
@Mixin(WallBlock.class)
public class WallBlockPaneConnectionMixin {
	@Inject(method = "connectsTo", at = @At("RETURN"), cancellable = true, require = 1)
	private void theouterworlds$connectToReinforcedPane(
		BlockState state, boolean faceSolid, Direction direction, CallbackInfoReturnable<Boolean> cir
	) {
		// Only the two thin end faces connect: those lie on the axis perpendicular to the pane's facing.
		if (state.getBlock() instanceof ReinforcedTintedGlassPaneBlock
			&& direction.getAxis() != state.getValue(ReinforcedTintedGlassPaneBlock.FACING).getAxis()) {
			cir.setReturnValue(true);
		}
	}
}
