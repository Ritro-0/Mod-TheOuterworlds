package com.theouterworld.item;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class ModArmorMaterials {
	private ModArmorMaterials() {}

	//Netherite-level durability multiplier with iron defense values
	public static final int REDSTEEL_BASE_DURABILITY = 37;

	public static final TagKey<Item> REPAIRS_REDSTEEL = TagKey.create(
		Registries.ITEM,
		OuterWorldMod.id("repairs_redsteel")
	);

	public static final TagKey<Item> REPAIRS_NICKEL = TagKey.create(
		Registries.ITEM,
		OuterWorldMod.id("repairs_nickel")
	);

	public static final TagKey<Item> REPAIRS_OPAL_LENS = TagKey.create(
		Registries.ITEM,
		OuterWorldMod.id("repairs_opal_lens")
	);

	public static final ResourceKey<EquipmentAsset> REDSTEEL_EQUIPMENT = ResourceKey.create(
		EquipmentAssets.ROOT_ID,
		OuterWorldMod.id("redsteel")
	);

	//Iron defense, enchantability, netherite durability, toughness, knockback resistance.
	public static final ArmorMaterial REDSTEEL = new ArmorMaterial(
		REDSTEEL_BASE_DURABILITY,
		ArmorMaterials.makeDefense(2, 5, 6, 2, 5),
		9,
		SoundEvents.ARMOR_EQUIP_IRON,
		3.0F,
		0.1F,
		REPAIRS_REDSTEEL,
		REDSTEEL_EQUIPMENT
	);

	public static final int NICKEL_BASE_DURABILITY = 15;

	public static final ResourceKey<EquipmentAsset> NICKEL_EQUIPMENT = ResourceKey.create(
		EquipmentAssets.ROOT_ID,
		OuterWorldMod.id("nickel")
	);

	public static final ArmorMaterial NICKEL = new ArmorMaterial(
		NICKEL_BASE_DURABILITY,
		ArmorMaterials.makeDefense(2, 5, 6, 2, 5),
		9,
		SoundEvents.ARMOR_EQUIP_IRON,
		0.0F,
		0.0F,
		REPAIRS_NICKEL,
		NICKEL_EQUIPMENT
	);

	public static final int OPAL_LENS_BASE_DURABILITY = 7;

	public static final ResourceKey<EquipmentAsset> OPAL_LENS_EQUIPMENT = ResourceKey.create(
		EquipmentAssets.ROOT_ID,
		OuterWorldMod.id("opal_lens")
	);

	public static final ArmorMaterial OPAL_LENS = new ArmorMaterial(
		OPAL_LENS_BASE_DURABILITY,
		ArmorMaterials.makeDefense(0, 0, 0, 2, 0),
		25,
		SoundEvents.ARMOR_EQUIP_GOLD,
		0.0F,
		0.0F,
		REPAIRS_OPAL_LENS,
		OPAL_LENS_EQUIPMENT
	);

	public static final int KHARAX_BELT_BASE_DURABILITY = 12;

	public static final TagKey<Item> REPAIRS_KHARAX_BELT = TagKey.create(
		Registries.ITEM,
		OuterWorldMod.id("repairs_kharax_belt")
	);

	public static final ResourceKey<EquipmentAsset> KHARAX_BELT_EQUIPMENT = ResourceKey.create(
		EquipmentAssets.ROOT_ID,
		OuterWorldMod.id("kharax_belt")
	);

	public static final ArmorMaterial KHARAX_BELT = new ArmorMaterial(
		KHARAX_BELT_BASE_DURABILITY,
		ArmorMaterials.makeDefense(0, 0, 1, 0, 0),
		8,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		0.0F,
		0.0F,
		REPAIRS_KHARAX_BELT,
		KHARAX_BELT_EQUIPMENT
	);
}
