package com.theouterworld.mixin;

import com.theouterworld.item.OpalineNickelFlailItem;
import com.theouterworld.item.OpalineNickelFlailTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A casted flail that is moved into any container other than a player inventory goes back to normal. */
@Mixin(Slot.class)
public abstract class SlotCastFlailMixin {
	@Shadow
	@Final
	public Container container;

	@Inject(method = "set", at = @At("HEAD"))
	private void theouterworlds$resetCastFlail(ItemStack stack, CallbackInfo ci) {
		if (!(this.container instanceof Inventory) && OpalineNickelFlailTracker.isCastFlail(stack)) {
			OpalineNickelFlailItem.setCast(stack, false);
		}
	}
}
