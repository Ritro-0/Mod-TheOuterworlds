package com.theouterworld.util;

import com.theouterworld.registry.ModTrimMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

/**
 * Iridium trim on a piece that does not already fully block heat
 * (Iridium armor, or any Graphite trim) matches Graphite-trimmed Redsteel:
 * one piece blocks heat, and chest / legs / boots count toward pressure.
 */
public final class IridiumTrim {
	private IridiumTrim() {
	}

	public static boolean hasTrim(ItemStack stack) {
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		return trim != null && trim.material().is(ModTrimMaterials.IRIDIUM);
	}

	public static boolean isEffective(ItemStack stack) {
		return hasTrim(stack)
			&& !IridiumProtection.isIridiumArmor(stack)
			&& !GraphiteProtection.hasGraphiteTrim(stack);
	}

	public static boolean hasEffectivePiece(LivingEntity entity) {
		return countEffectivePieces(entity) > 0;
	}

	public static int countEffectivePieces(LivingEntity entity) {
		int count = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && isEffective(entity.getItemBySlot(slot))) {
				count++;
			}
		}
		return count;
	}
}
