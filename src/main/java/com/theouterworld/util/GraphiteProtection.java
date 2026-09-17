package com.theouterworld.util;

import com.theouterworld.item.ModItems;
import com.theouterworld.registry.ModTrimMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

/**
 * Graphite armor trims:
 * - 1 piece: full Innerworld/Nearworld/Emberworld heat immunity + wearer fire immunity
 * - 3 pieces: Nearworld pressure resistance
 * - 3 graphite-trimmed Redsteel body pieces (chest/legs/boots): Emberworld pressure resistance
 */
public final class GraphiteProtection {
	public static final int HEAT_SHIELD_PIECES = 1;
	public static final int PRESSURE_RESISTANCE_PIECES = 3;

	private GraphiteProtection() {
	}

	public static boolean hasGraphiteTrim(ItemStack stack) {
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		return trim != null && trim.material().is(ModTrimMaterials.GRAPHITE);
	}

	public static int countGraphiteTrimPieces(LivingEntity entity) {
		int count = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && hasGraphiteTrim(entity.getItemBySlot(slot))) {
				count++;
			}
		}
		return count;
	}

	public static boolean hasHeatShield(LivingEntity entity) {
		return countGraphiteTrimPieces(entity) >= HEAT_SHIELD_PIECES;
	}

	/** Nearworld: need three graphite-trimmed pieces to resist crushing pressure. */
	public static boolean hasPressureResistance(LivingEntity entity) {
		return countGraphiteTrimPieces(entity) >= PRESSURE_RESISTANCE_PIECES;
	}

	/**
	 * Emberworld (Io): need graphite-trimmed Redsteel chest, legs, and boots.
	 * Helmet slot stays free for glass / Opal Lens.
	 */
	public static boolean hasEmberworldPressureResistance(LivingEntity entity) {
		return isGraphiteTrimmedRedsteel(entity.getItemBySlot(EquipmentSlot.CHEST), ModItems.REDSTEEL_CHESTPLATE)
			&& isGraphiteTrimmedRedsteel(entity.getItemBySlot(EquipmentSlot.LEGS), ModItems.REDSTEEL_LEGGINGS)
			&& isGraphiteTrimmedRedsteel(entity.getItemBySlot(EquipmentSlot.FEET), ModItems.REDSTEEL_BOOTS);
	}

	private static boolean isGraphiteTrimmedRedsteel(ItemStack stack, Item item) {
		return !stack.isEmpty() && stack.is(item) && hasGraphiteTrim(stack);
	}
}
