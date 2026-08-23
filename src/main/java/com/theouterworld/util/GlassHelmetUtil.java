package com.theouterworld.util;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

public final class GlassHelmetUtil {
	private GlassHelmetUtil() {}

	public static void register() {
		Equippable glassHelmet = Equippable.builder(EquipmentSlot.HEAD)
			.setSwappable(true)
			.setDispensable(false)
			.setDamageOnHurt(false)
			.build();

		DefaultItemComponentEvents.MODIFY.register(context -> {
			context.modify(item -> isGlassHelmetItem(item), (builder, item) -> {
				builder.set(DataComponents.EQUIPPABLE, glassHelmet);
			});
		});
	}

	public static boolean isGlassHelmetItem(Item item) {
		if (item == null) {
			return false;
		}
		if (item == Items.GLASS || item == Items.TINTED_GLASS) {
			return true;
		}
		return Items.STAINED_GLASS.asList().contains(item);
	}

	public static boolean isGlassHelmetItem(ItemStack stack) {
		return stack != null && !stack.isEmpty() && isGlassHelmetItem(stack.getItem());
	}

	public static boolean isWearingDustStormProtection(LivingEntity entity) {
		if (entity == null) {
			return false;
		}
		ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
		return isGlassHelmetItem(head) || head.is(com.theouterworld.item.ModItems.OPAL_LENS);
	}

	public static boolean isWearingGlassHelmet(LivingEntity entity) {
		return entity != null && isGlassHelmetItem(entity.getItemBySlot(EquipmentSlot.HEAD));
	}

	public static Identifier getGlassTextureId(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		if (stack.is(Items.GLASS)) {
			return Identifier.fromNamespaceAndPath("minecraft", "textures/block/glass.png");
		}
		if (stack.is(Items.TINTED_GLASS)) {
			return Identifier.fromNamespaceAndPath("minecraft", "textures/block/tinted_glass.png");
		}
		for (DyeColor color : DyeColor.values()) {
			if (stack.is(Items.STAINED_GLASS.pick(color))) {
				return Identifier.fromNamespaceAndPath("minecraft", "textures/block/" + color.getSerializedName() + "_stained_glass.png");
			}
		}
		return null;
	}
}
