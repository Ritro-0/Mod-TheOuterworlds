package com.theouterworld.mixin.client;

import com.theouterworld.block.ModBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class FrozenStemColorMixin {
	private static final int FROZEN_STEM_COLOR = ARGB.opaque(0x8FCBE0);

	@Inject(method = "createDefault", at = @At("RETURN"))
	private static void theouterworlds$tintFrozenStems(CallbackInfoReturnable<BlockColors> cir) {
		cir.getReturnValue().register(
			List.of(BlockTintSources.constant(FROZEN_STEM_COLOR)),
			ModBlocks.FROZEN_PUMPKIN_STEM,
			ModBlocks.FROZEN_MELON_STEM,
			ModBlocks.FROZEN_ATTACHED_PUMPKIN_STEM,
			ModBlocks.FROZEN_ATTACHED_MELON_STEM
		);
	}
}
