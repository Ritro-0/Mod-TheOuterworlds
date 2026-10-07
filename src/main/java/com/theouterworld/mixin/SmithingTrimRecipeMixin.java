package com.theouterworld.mixin;

import com.theouterworld.block.TrimmedGlassBlock;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {
	@Inject(method = "applyTrim", at = @At("HEAD"), cancellable = true)
	private static void theouterworlds$trimGlass(
		ItemStack base,
		ItemStack addition,
		Holder<TrimPattern> pattern,
		CallbackInfoReturnable<ItemStack> cir
	) {
		ItemStack trimmed = TrimmedGlassBlock.smith(base, addition, pattern);
		if (trimmed != null) {
			cir.setReturnValue(trimmed);
		}
	}
}
