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
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

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

	public static final Item OXIDIZED_COIL = registerItem(
		"oxidized_coil",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item OXIDIZED_COIL_BRUSH = registerItem(
		"oxidized_coil_brush",
		key -> new BrushItem(new Item.Properties().setId(key).durability(32))
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
		key -> new Item(new Item.Properties().setId(key).shovel(ModToolMaterials.REDSTEEL, 1.5F, -3.0F))
	);

	public static final Item REDSTEEL_PICKAXE = registerItem(
		"redsteel_pickaxe",
		key -> new Item(new Item.Properties().setId(key).pickaxe(ModToolMaterials.REDSTEEL, 1.0F, -2.8F))
	);

	public static final Item REDSTEEL_AXE = registerItem(
		"redsteel_axe",
		key -> new Item(new Item.Properties().setId(key).axe(ModToolMaterials.REDSTEEL, 6.0F, -3.1F))
	);

	public static final Item REDSTEEL_HOE = registerItem(
		"redsteel_hoe",
		key -> new Item(new Item.Properties().setId(key).hoe(ModToolMaterials.REDSTEEL, -2.0F, -1.0F))
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
		key -> new Item(new Item.Properties().setId(key).shovel(ModToolMaterials.NICKEL, 1.5F, -3.0F))
	);

	public static final Item NICKEL_PICKAXE = registerItem(
		"nickel_pickaxe",
		key -> new Item(new Item.Properties().setId(key).pickaxe(ModToolMaterials.NICKEL, 1.0F, -2.8F))
	);

	public static final Item NICKEL_AXE = registerItem(
		"nickel_axe",
		key -> new Item(new Item.Properties().setId(key).axe(ModToolMaterials.NICKEL, 6.0F, -3.1F))
	);

	public static final Item NICKEL_HOE = registerItem(
		"nickel_hoe",
		key -> new Item(new Item.Properties().setId(key).hoe(ModToolMaterials.NICKEL, -2.0F, -1.0F))
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

	public static final Item OSMIUM_FLAKE = registerItem(
		"osmium_flake",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item IRIDIUM_INGOT = registerItem(
		"iridium_ingot",
		key -> new Item(new Item.Properties().setId(key).fireResistant())
	);

	public static final Item IRIDIUM_NUGGET = registerItem(
		"iridium_nugget",
		key -> new Item(new Item.Properties().setId(key).fireResistant())
	);

	public static final Item IRIDIUM_HELMET = registerItem(
		"iridium_helmet",
		key -> new Item(new Item.Properties().setId(key).fireResistant().humanoidArmor(ModArmorMaterials.IRIDIUM, ArmorType.HELMET))
	);

	public static final Item IRIDIUM_CHESTPLATE = registerItem(
		"iridium_chestplate",
		key -> new Item(new Item.Properties().setId(key).fireResistant().humanoidArmor(ModArmorMaterials.IRIDIUM, ArmorType.CHESTPLATE))
	);

	public static final Item IRIDIUM_LEGGINGS = registerItem(
		"iridium_leggings",
		key -> new Item(new Item.Properties().setId(key).fireResistant().humanoidArmor(ModArmorMaterials.IRIDIUM, ArmorType.LEGGINGS))
	);

	public static final Item IRIDIUM_BOOTS = registerItem(
		"iridium_boots",
		key -> new Item(new Item.Properties().setId(key).fireResistant().humanoidArmor(ModArmorMaterials.IRIDIUM, ArmorType.BOOTS))
	);

	public static final Item EMERGENCY_RETURN_POD = registerItem(
		"emergency_return_pod",
		key -> new EmergencyReturnPodItem(key, new Item.Properties().setId(key))
	);

	public static final Item BROKEN_EMERGENCY_RETURN_POD = registerItem(
		"broken_emergency_return_pod",
		key -> new Item(new Item.Properties().setId(key).stacksTo(1))
	);

	public static final Item QUANTUM_POD = registerItem(
		"quantum_pod",
		key -> new QuantumPodItem(key, new Item.Properties().setId(key))
	);

	public static final Item SALT = registerItem(
		"salt",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item AEROSTAT_BALLOON = registerItem(
		"aerostat_balloon",
		key -> new AerostatBalloonItem(key, new Item.Properties().setId(key))
	);

	public static final Item METALLIC_HYDROGEN = registerItem(
		"metallic_hydrogen",
		key -> new MetallicHydrogenItem(new Item.Properties().setId(key).fireResistant())
	);

	public static final Item METALLIC_HELIUM = registerItem(
		"metallic_helium",
		key -> new MetallicHeliumItem(new Item.Properties().setId(key).fireResistant())
	);

	public static final Item IONIC_AMMONIA = registerItem(
		"ionic_ammonia",
		key -> new IonicAmmoniaItem(new Item.Properties().setId(key).fireResistant())
	);

	public static final Item IONIC_METHANE = registerItem(
		"ionic_methane",
		key -> new IonicMethaneItem(new Item.Properties().setId(key).fireResistant())
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

	public static final Item UNLIT_TORCH = registerItem(
		"unlit_torch",
		key -> {
			StandingAndWallBlockItem item = new StandingAndWallBlockItem(
				ModBlocks.UNLIT_TORCH,
				ModBlocks.UNLIT_WALL_TORCH,
				Direction.DOWN,
				new Item.Properties().setId(key).useBlockDescriptionPrefix()
			);
			item.registerBlocks(Item.BY_BLOCK, item);
			return item;
		}
	);

	public static final Item JAROSITE = registerItem(
		"jarosite",
		key -> new Item(new Item.Properties().setId(key).fireResistant().trimMaterial(ModTrimMaterials.JAROSITE).component(DataComponents.COOKING_FUEL, new CookingFuel(new ResolvableInt.Constant(10000), ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER))))
	);

	public static final Item GRAPHITE_SHARD = registerItem(
		"graphite_shard",
		key -> new Item(new Item.Properties().setId(key).fireResistant().trimMaterial(ModTrimMaterials.GRAPHITE))
	);

	/** Half of one hunger shank (1 / 20 of the hunger bar). */
	private static final FoodProperties FROZEN_SNACK = new FoodProperties.Builder()
		.nutrition(1)
		.saturationModifier(0.1F)
		.build();

	public static final Item FROZEN_WHEAT_SEEDS = registerItem(
		"frozen_wheat_seeds",
		key -> frozenCropSeed(ModBlocks.FROZEN_WHEAT, key)
	);

	public static final Item FROZEN_WHEAT = registerItem(
		"frozen_wheat",
		key -> new Item(new Item.Properties().setId(key).food(FROZEN_SNACK))
	);

	public static final Item FROZEN_CARROT = registerItem(
		"frozen_carrot",
		key -> frozenCropSeed(ModBlocks.FROZEN_CARROTS, key)
	);

	public static final Item FROZEN_POTATO = registerItem(
		"frozen_potato",
		key -> frozenCropSeed(ModBlocks.FROZEN_POTATOES, key)
	);

	public static final Item FROZEN_POISONOUS_POTATO = registerItem(
		"frozen_poisonous_potato",
		key -> new Item(
			new Item.Properties()
				.setId(key)
				.food(FROZEN_SNACK, Consumables.POISONOUS_POTATO)
		)
	);

	public static final Item FROZEN_BEETROOT_SEEDS = registerItem(
		"frozen_beetroot_seeds",
		key -> frozenCropSeed(ModBlocks.FROZEN_BEETROOTS, key)
	);

	public static final Item FROZEN_BEETROOT = registerItem(
		"frozen_beetroot",
		key -> new Item(new Item.Properties().setId(key).food(FROZEN_SNACK))
	);

	public static final Item FROZEN_TORCHFLOWER_SEEDS = registerItem(
		"frozen_torchflower_seeds",
		key -> frozenCropSeed(ModBlocks.FROZEN_TORCHFLOWER_CROP, key)
	);

	public static final Item FROZEN_PITCHER_POD = registerItem(
		"frozen_pitcher_pod",
		key -> frozenCropSeed(ModBlocks.FROZEN_PITCHER_CROP, key)
	);

	public static final Item FROZEN_PITCHER_PLANT = registerItem(
		"frozen_pitcher_plant",
		key -> new Item(new Item.Properties().setId(key))
	);

	public static final Item FROZEN_COCOA_BEANS = registerItem(
		"frozen_cocoa_beans",
		key -> {
			BlockItem item = new BlockItem(
				ModBlocks.FROZEN_COCOA,
				new Item.Properties().setId(key).useItemDescriptionPrefix()
			);
			item.registerBlocks(Item.BY_BLOCK, item);
			return item;
		}
	);

	public static final Item FROZEN_PUMPKIN_SEEDS = registerItem(
		"frozen_pumpkin_seeds",
		key -> frozenCropSeed(ModBlocks.FROZEN_PUMPKIN_STEM, key)
	);

	public static final Item FROZEN_MELON_SEEDS = registerItem(
		"frozen_melon_seeds",
		key -> frozenCropSeed(ModBlocks.FROZEN_MELON_STEM, key)
	);

	public static final Item SURVIVAL_BRICK = registerItem(
		"survival_brick",
		key -> new Item(
			new Item.Properties()
				.setId(key)
				.food(
					new FoodProperties.Builder().nutrition(8).saturationModifier(0.1F).build(),
					Consumables.defaultFood()
						.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 240, 0)))
						.build()
				)
		)
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
		key -> new Item(new Item.Properties().setId(key).rarity(Rarity.UNCOMMON).potPattern(com.theouterworld.registry.ModDecoratedPotPatterns.ROVER))
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

	public static final Item METHANE_BUCKET = registerItem(
		"methane_bucket",
		key -> new BucketItem(
			com.theouterworld.registry.ModFluids.LIQUID_METHANE,
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

	private static Item frozenCropSeed(Block crop, ResourceKey<Item> key) {
		BlockItem item = new BlockItem(
			crop,
			new Item.Properties().setId(key).food(FROZEN_SNACK).useItemDescriptionPrefix()
		);
		item.registerBlocks(Item.BY_BLOCK, item);
		return item;
	}

	private static Item registerItem(String name, Function<ResourceKey<Item>, Item> factory) {
		Identifier id = OuterWorldMod.id(name);
		ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
		Item item = factory.apply(key);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	public static void registerModItems() {
		OuterWorldMod.LOGGER.info("Registering items for {}", OuterWorldMod.MOD_ID);
		Item.BY_BLOCK.put(ModBlocks.FROZEN_ATTACHED_PUMPKIN_STEM, FROZEN_PUMPKIN_SEEDS);
		Item.BY_BLOCK.put(ModBlocks.FROZEN_ATTACHED_MELON_STEM, FROZEN_MELON_SEEDS);
		Item.BY_BLOCK.put(ModBlocks.METALLIC_HYDROGEN, METALLIC_HYDROGEN);
		Item.BY_BLOCK.put(ModBlocks.METALLIC_HELIUM, METALLIC_HELIUM);
		Item.BY_BLOCK.put(ModBlocks.IONIC_AMMONIA, IONIC_AMMONIA);
		Item.BY_BLOCK.put(ModBlocks.IONIC_METHANE, IONIC_METHANE);
	}
}
