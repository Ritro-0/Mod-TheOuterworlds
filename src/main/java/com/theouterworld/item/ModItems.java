package com.theouterworld.item;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModTrimMaterials;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;

public class ModItems {
	public static final Item OXIDIZED_BASALT_ROCK = registerItem(
		"oxidized_basalt_rock",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_BASALT_PICK = registerItem(
		"oxidized_basalt_pick",
		key -> new OxidizedBasaltPickItem(key, new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_BASALT_SWORD = registerItem(
		"oxidized_basalt_sword",
		key -> new OxidizedBasaltSwordItem(key, new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_BASALT_AXE = registerItem(
		"oxidized_basalt_axe",
		key -> new OxidizedBasaltAxeItem(key, new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_BASALT_PICKAXE = registerItem(
		"oxidized_basalt_pickaxe",
		key -> new OxidizedBasaltPickaxeItem(key, new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_BASALT_SHOVEL = registerItem(
		"oxidized_basalt_shovel",
		key -> new OxidizedBasaltShovelItem(key, new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_BASALT_HOE = registerItem(
		"oxidized_basalt_hoe",
		key -> new OxidizedBasaltHoeItem(key, new Item.Properties().setId(key))
	);

	public static final Item RAW_OXIDIZED_IRON = registerItem(
		"raw_oxidized_iron",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item RUST_SPLINT = registerItem(
		"rust_splint",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item HEMATITE = registerItem(
		"hematite",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item REDSTEEL_INGOT = registerItem(
		"redsteel_ingot",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item REDSTEEL_UPGRADE_SMITHING_TEMPLATE = registerItem(
		"redsteel_upgrade_smithing_template",
		key -> new SmithingTemplateItem(
			Component.translatable(Util.makeDescriptionId("item", OuterWorldMod.id("smithing_template.redsteel_upgrade.applies_to")))
				.withStyle(ChatFormatting.BLUE),
			Component.translatable(Util.makeDescriptionId("item", OuterWorldMod.id("smithing_template.redsteel_upgrade.ingredients")))
				.withStyle(ChatFormatting.BLUE),
			Component.translatable(Util.makeDescriptionId("item", OuterWorldMod.id("smithing_template.redsteel_upgrade.base_slot_description"))),
			Component.translatable(Util.makeDescriptionId("item", OuterWorldMod.id("smithing_template.redsteel_upgrade.additions_slot_description"))),
			List.of(
				Identifier.withDefaultNamespace("container/slot/helmet"),
				Identifier.withDefaultNamespace("container/slot/sword"),
				Identifier.withDefaultNamespace("container/slot/chestplate"),
				Identifier.withDefaultNamespace("container/slot/pickaxe"),
				Identifier.withDefaultNamespace("container/slot/leggings"),
				Identifier.withDefaultNamespace("container/slot/axe"),
				Identifier.withDefaultNamespace("container/slot/boots"),
				Identifier.withDefaultNamespace("container/slot/hoe"),
				Identifier.withDefaultNamespace("container/slot/shovel"),
				Identifier.withDefaultNamespace("container/slot/spear")
			),
			List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
			new Item.Properties().setId(key).rarity(Rarity.UNCOMMON)
		)
	);

	public static final Item REDSTEEL_SWORD = registerItem(
		"redsteel_sword",
		key -> new Item(new Item.Properties().setId(key).sword(ModToolMaterials.REDSTEEL, 3.0F, -2.4F))
	);

	public static final Item REDSTEEL_SHOVEL = registerItem(
		"redsteel_shovel",
		key -> new ShovelItem(ModToolMaterials.REDSTEEL, 1.5F, -3.0F, new Item.Properties().setId(key))
	);

	public static final Item REDSTEEL_PICKAXE = registerItem(
		"redsteel_pickaxe",
		key -> new Item(new Item.Properties().setId(key).pickaxe(ModToolMaterials.REDSTEEL, 1.0F, -2.8F))
	);

	public static final Item REDSTEEL_AXE = registerItem(
		"redsteel_axe",
		key -> new AxeItem(ModToolMaterials.REDSTEEL, 6.0F, -3.1F, new Item.Properties().setId(key))
	);

	public static final Item REDSTEEL_HOE = registerItem(
		"redsteel_hoe",
		key -> new HoeItem(ModToolMaterials.REDSTEEL, -2.0F, -1.0F, new Item.Properties().setId(key))
	);

	public static final Item REDSTEEL_HELMET = registerItem(
		"redsteel_helmet",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.REDSTEEL, ArmorType.HELMET))
	);

	public static final Item REDSTEEL_CHESTPLATE = registerItem(
		"redsteel_chestplate",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.REDSTEEL, ArmorType.CHESTPLATE))
	);

	public static final Item REDSTEEL_LEGGINGS = registerItem(
		"redsteel_leggings",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.REDSTEEL, ArmorType.LEGGINGS))
	);

	public static final Item REDSTEEL_BOOTS = registerItem(
		"redsteel_boots",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.REDSTEEL, ArmorType.BOOTS))
	);

	public static final Item REDSTEEL_SPEAR = registerItem(
		"redsteel_spear",
		key -> new Item(new Item.Properties().setId(key).spear(ModToolMaterials.REDSTEEL, 0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F))
	);

	public static final Item RAW_NICKEL = registerItem(
		"raw_nickel",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item NICKEL_INGOT = registerItem(
		"nickel_ingot",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item NICKEL_NUGGET = registerItem(
		"nickel_nugget",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item NICKEL_SWORD = registerItem(
		"nickel_sword",
		key -> new Item(new Item.Properties().setId(key).sword(ModToolMaterials.NICKEL, 3.0F, -2.4F))
	);

	public static final Item NICKEL_SHOVEL = registerItem(
		"nickel_shovel",
		key -> new ShovelItem(ModToolMaterials.NICKEL, 1.5F, -3.0F, new Item.Properties().setId(key))
	);

	public static final Item NICKEL_PICKAXE = registerItem(
		"nickel_pickaxe",
		key -> new Item(new Item.Properties().setId(key).pickaxe(ModToolMaterials.NICKEL, 1.0F, -2.8F))
	);

	public static final Item NICKEL_AXE = registerItem(
		"nickel_axe",
		key -> new AxeItem(ModToolMaterials.NICKEL, 6.0F, -3.1F, new Item.Properties().setId(key))
	);

	public static final Item NICKEL_HOE = registerItem(
		"nickel_hoe",
		key -> new HoeItem(ModToolMaterials.NICKEL, -2.0F, -1.0F, new Item.Properties().setId(key))
	);

	public static final Item NICKEL_HELMET = registerItem(
		"nickel_helmet",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.NICKEL, ArmorType.HELMET))
	);

	public static final Item NICKEL_CHESTPLATE = registerItem(
		"nickel_chestplate",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.NICKEL, ArmorType.CHESTPLATE))
	);

	public static final Item NICKEL_LEGGINGS = registerItem(
		"nickel_leggings",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.NICKEL, ArmorType.LEGGINGS))
	);

	public static final Item NICKEL_BOOTS = registerItem(
		"nickel_boots",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.NICKEL, ArmorType.BOOTS))
	);

	public static final Item NICKEL_SPEAR = registerItem(
		"nickel_spear",
		key -> new Item(new Item.Properties().setId(key).spear(ModToolMaterials.NICKEL, 0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F))
	);

	public static final Item OPALINE_NICKEL = registerItem(
		"opaline_nickel",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item OPALINE_NICKEL_FLAIL = registerItem(
		"opaline_nickel_flail",
		key -> new OpalineNickelFlailItem(key, new Item.Properties().setId(key))
	);

	public static final Item OPAL_LENS = registerItem(
		"opal_lens",
		key -> new Item(new Item.Properties().setId(key).humanoidArmor(ModArmorMaterials.OPAL_LENS, ArmorType.HELMET))
	);

	public static final Item OLIVINE_TORCH = registerItem(
		"olivine_torch",
		key -> {
			StandingAndWallBlockItem item = new StandingAndWallBlockItem(
				ModBlocks.OLIVINE_TORCH,
				ModBlocks.OLIVINE_WALL_TORCH,
				Direction.DOWN,
				new Item.Properties().setId(key).useBlockDescriptionPrefix()
			);
			item.registerBlocks(Item.BY_BLOCK, item);
			return item;
		}
	);

	public static final Item JAROSITE = registerItem(
		"jarosite",
		key -> new Item(new Item.Properties().setId(key).fireResistant().trimMaterial(ModTrimMaterials.JAROSITE))
	);

	public static final Item JAROSITE_ROCKET = registerItem(
		"jarosite_rocket",
		key -> new FireworkRocketItem(
			new Item.Properties()
				.setId(key)
				.fireResistant()
				.component(DataComponents.FIREWORKS, new Fireworks(6, List.of()))
		)
	);

	public static final Item PERCHLORATE_CRYSTAL = registerItem(
		"perchlorate_crystal",
		key -> new PerchlorateCrystalItem(new Item.Properties().setId(key))
	);

	public static final Item OPAL = registerItem(
		"opal",
		key -> new Item(new Item.Properties().setId(key).trimMaterial(ModTrimMaterials.OPAL))
	);

	public static final Item ROVER_POTTERY_SHERD = registerItem(
		"rover_pottery_sherd",
		key -> new Item(new Item.Properties().setId(key).rarity(Rarity.UNCOMMON))
	);

	public static final Item ORBIT_SMITHING_TEMPLATE = registerItem(
		"orbit_smithing_template",
		key -> SmithingTemplateItem.createArmorTrimTemplate(new Item.Properties().setId(key).rarity(Rarity.UNCOMMON))
	);

	public static final Item MERCURY_BUCKET = registerItem(
		"mercury_bucket",
		key -> new MercuryBucketItem(
			com.theouterworld.registry.ModFluids.MERCURY,
			new Item.Properties().setId(key).craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
		)
	);

	public static final Item KHARAX_SPAWN_EGG = registerItem(
		"kharax_spawn_egg",
		key -> new SpawnEggItem(
			new Item.Properties().setId(key).spawnEgg(com.theouterworld.registry.ModEntities.KHARAX)
		)
	);

	public static final Item KHARAX_CHITIN = registerItem(
		"kharax_chitin",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Identifier KHARAX_BELT_STEP_HEIGHT_ID = OuterWorldMod.id("kharax_belt_step_height");

	/** Player base step height is 0.6; +0.9 yields a 1.5-block step-up. */
	public static final Item KHARAX_BELT = registerItem(
		"kharax_belt",
		key -> {
			ItemAttributeModifiers attributes = ModArmorMaterials.KHARAX_BELT.createAttributes(ArmorType.LEGGINGS)
				.withModifierAdded(
					Attributes.STEP_HEIGHT,
					new AttributeModifier(KHARAX_BELT_STEP_HEIGHT_ID, 0.9, AttributeModifier.Operation.ADD_VALUE),
					EquipmentSlotGroup.LEGS
				);
			return new Item(
				new Item.Properties()
					.setId(key)
					.humanoidArmor(ModArmorMaterials.KHARAX_BELT, ArmorType.LEGGINGS)
					.attributes(attributes)
			);
		}
	);

	private static Item registerItem(String name, Function<ResourceKey<Item>, Item> factory) {
		Identifier id = OuterWorldMod.id(name);
		ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
		Item item = factory.apply(key);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	public static void registerModItems() {
		OuterWorldMod.LOGGER.info("Registering items for {}", OuterWorldMod.MOD_ID);
	}
}
