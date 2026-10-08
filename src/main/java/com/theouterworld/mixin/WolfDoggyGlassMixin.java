package com.theouterworld.mixin;

import com.theouterworld.item.DoggyGlassItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfDoggyGlassMixin {
	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$equipDoggyGlass(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		Wolf wolf = (Wolf) (Object) this;
		ItemStack stack = player.getItemInHand(hand);
		if (!DoggyGlassItem.isDoggyGlass(stack) || !wolf.isTame() || wolf.isBaby() || !wolf.isOwnedBy(player)) {
			return;
		}
		if (!wolf.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			return;
		}
		if (!wolf.level().isClientSide()) {
			wolf.setItemSlot(EquipmentSlot.HEAD, stack.copyWithCount(1));
			wolf.setGuaranteedDrop(EquipmentSlot.HEAD);
			stack.consume(1, player);
		}
		cir.setReturnValue(InteractionResult.SUCCESS);
	}
}
