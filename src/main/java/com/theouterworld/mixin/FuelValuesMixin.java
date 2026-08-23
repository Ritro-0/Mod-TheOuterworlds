package com.theouterworld.mixin;

import com.theouterworld.item.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FuelValues.Builder.class)
public abstract class FuelValuesMixin {
	@Shadow
	public abstract FuelValues.Builder add(ItemLike item, int burnTime);

	@Inject(method = "build", at = @At("HEAD"))
	private void theouterworlds$addJarositeFuel(CallbackInfoReturnable<FuelValues> cir) {
		this.add(ModItems.JAROSITE, 10000);
	}
}
