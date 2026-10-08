package com.theouterworld.util;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModTrimMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.Level;

/**
 * Acid trim on a glass helmet doubles how long you can hold your breath
 * in the liquids of Highworld, Deepworld, Farworld, and Edgeworld.
 * The same trim on any other armor piece is only cosmetic.
 */
public final class AcidBreath {
	private AcidBreath() {
	}

	public static boolean slowsDrowning(LivingEntity entity) {
		if (entity == null) {
			return false;
		}
		Level level = entity.level();
		if (level == null || !ModDimensions.isGasGiant(level.dimension())) {
			return false;
		}
		return hasAcidTrimmedGlassHelmet(entity);
	}

	public static boolean hasAcidTrim(ItemStack stack) {
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		return trim != null && trim.material().is(ModTrimMaterials.ACID);
	}

	private static boolean hasAcidTrimmedGlassHelmet(LivingEntity entity) {
		ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
		return (GlassHelmetUtil.isGlassHelmetItem(head) || com.theouterworld.item.DoggyGlassItem.isDoggyGlass(head))
			&& hasAcidTrim(head);
	}
}
