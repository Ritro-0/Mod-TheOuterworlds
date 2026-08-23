package com.theouterworld.util;

import com.theouterworld.registry.ModTags;
import com.theouterworld.registry.ModTrimMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class JarositeProtection {
	private JarositeProtection() {
	}

	public static boolean isProtectedDamage(DamageSource source) {
		return source.is(ModTags.JAROSITE_IMMUNE);
	}

	public static boolean isProtectedItem(ItemStack stack) {
		if (stack.is(ModTags.JAROSITE_PROTECTED)) {
			return true;
		}
		return hasJarositeTrim(stack);
	}

	public static boolean hasJarositeTrim(ItemStack stack) {
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		return trim != null && trim.material().is(ModTrimMaterials.JAROSITE);
	}

	public static boolean hasJarositeTrim(LivingEntity entity) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && hasJarositeTrim(entity.getItemBySlot(slot))) {
				return true;
			}
		}
		return false;
	}
}
