package com.theouterworld.mixin;

import com.theouterworld.util.GlassHelmetUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorSlot.class)
public abstract class ArmorSlotGlassMixin {
	@Shadow
	@Final
	private EquipmentSlot slot;

	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$allowGlassInHead(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (this.slot == EquipmentSlot.HEAD && GlassHelmetUtil.isGlassHelmetItem(stack)) {
			cir.setReturnValue(true);
		}
	}
}
