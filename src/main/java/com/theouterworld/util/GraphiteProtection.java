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
 * Iridium trim on a piece that does not already block heat counts the same way.
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
		return countGraphiteTrimPieces(entity) >= HEAT_SHIELD_PIECES || IridiumTrim.hasEffectivePiece(entity);
	}

	/** Nearworld: three graphite trims, or iridium trims on pieces that do not already block heat. */
	public static boolean hasPressureResistance(LivingEntity entity) {
		return countPressurePieces(entity) >= PRESSURE_RESISTANCE_PIECES;
	}

	public static int countPressurePieces(LivingEntity entity) {
		int count = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
				continue;
			}
			ItemStack stack = entity.getItemBySlot(slot);
			if (hasGraphiteTrim(stack) || IridiumTrim.isEffective(stack)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Emberworld (Io): need graphite-trimmed Redsteel chest, legs, and boots.
	 * Helmet slot stays free for glass / Opal Lens.
	 */
	public static boolean hasEmberworldPressureResistance(LivingEntity entity) {
		return emberSlot(entity.getItemBySlot(EquipmentSlot.CHEST), ModItems.REDSTEEL_CHESTPLATE)
			&& emberSlot(entity.getItemBySlot(EquipmentSlot.LEGS), ModItems.REDSTEEL_LEGGINGS)
			&& emberSlot(entity.getItemBySlot(EquipmentSlot.FEET), ModItems.REDSTEEL_BOOTS);
	}

	private static boolean emberSlot(ItemStack stack, Item item) {
		return isGraphiteTrimmedRedsteel(stack, item) || IridiumTrim.isEffective(stack);
	}

	private static boolean isGraphiteTrimmedRedsteel(ItemStack stack, Item item) {
		return !stack.isEmpty() && stack.is(item) && hasGraphiteTrim(stack);
	}
}
