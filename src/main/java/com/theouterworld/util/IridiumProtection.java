package com.theouterworld.util;

import com.theouterworld.item.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Iridium armor: any single piece grants heat/pressure immunity in Inner/Nearworld.
 * Highworld survival and liquid hydrogen require {@link #countIridiumPieces} ≥ 3.
 */
public final class IridiumProtection {
	private IridiumProtection() {
	}

	public static boolean isIridiumArmor(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		return item == ModItems.IRIDIUM_HELMET
			|| item == ModItems.IRIDIUM_CHESTPLATE
			|| item == ModItems.IRIDIUM_LEGGINGS
			|| item == ModItems.IRIDIUM_BOOTS;
	}

	public static int countIridiumPieces(LivingEntity entity) {
		int count = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
				&& isIridiumArmor(entity.getItemBySlot(slot))) {
				count++;
			}
		}
		return count;
	}

	public static boolean hasIridiumArmor(LivingEntity entity) {
		return countIridiumPieces(entity) > 0;
	}
}
