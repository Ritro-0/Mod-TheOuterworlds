package com.theouterworld.mixin;

import com.theouterworld.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public class StickIngredientMixin {
	@Shadow
	@Final
	private HolderSet<Item> values;

	@Inject(method = "test", at = @At("RETURN"), cancellable = true)
	private void theouterworlds$interchangeSticks(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() || input.isEmpty()) {
			return;
		}
		if (isStickFamily(input.getItem()) && acceptsAnyStick()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "acceptsItem", at = @At("RETURN"), cancellable = true)
	private void theouterworlds$interchangeStickHolders(Holder<Item> item, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() || item == null) {
			return;
		}
		if (isStickFamily(item.value()) && acceptsAnyStick()) {
			cir.setReturnValue(true);
		}
	}

	private static boolean isStickFamily(Item item) {
		return item == Items.STICK || item == ModItems.RUST_SPLINT;
	}

	private boolean acceptsAnyStick() {
		return this.values.contains(Items.STICK.builtInRegistryHolder())
			|| this.values.contains(ModItems.RUST_SPLINT.builtInRegistryHolder());
	}
}
