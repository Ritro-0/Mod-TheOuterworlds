package com.theouterworld.block;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.registry.ModTrimMaterials;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.effect.MobEffects;

import java.util.function.Function;

public class ModBlocks {
	public static final Block REGOLITH = registerBlock(
		"regolith",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_ORANGE)
				.strength(0.5f, 0.5f)
				.sound(SoundType.SAND)
		)
	);

	public static final Block REGOLITH_FARMLAND = registerBlock(
		"regolith_farmland",
		key -> new RegolithFarmlandBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_ORANGE)
				.sound(SoundType.SAND)
		)
	);

	public static final Block SUSPICIOUS_REGOLITH = registerBlock(
		"suspicious_regolith",
		key -> new BrushableBlock(
			REGOLITH,
			SoundEvents.BRUSH_SAND,
			SoundEvents.BRUSH_SAND_COMPLETED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_ORANGE)
				.strength(0.25f)
				.sound(SoundType.SUSPICIOUS_SAND)
				.pushReaction(PushReaction.POPPED)
		)
	);

	public static final Block OXIDIZED_BASALT = registerBlock(
		"oxidized_basalt",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GRAY)
				.strength(1.25f, 4.2f)
				.sound(SoundType.BASALT)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block SULFURIC_BASALT = registerBlock(
		"sulfuric_basalt",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_YELLOW)
				.strength(1.25f, 4.2f)
				.sound(SoundType.BASALT)
				.requiresCorrectToolForDrops()
		)
	);

	/** Titan organic polymer — very soft “stone” of Amberworld. */
	public static final Block THOLIN = registerBlock(
		"tholin",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_ORANGE)
				.strength(0.4f, 0.4f)
				.sound(SoundType.GRAVEL)
		)
	);

	/** Soft tholin dust / soil covering Amberworld's surface. */
	public static final Block THOLINIC_REGOLITH = registerBlock(
		"tholinic_regolith",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_ORANGE)
				.strength(0.25f, 0.25f)
				.sound(SoundType.SAND)
		)
	);

	public static final Block OXIDIZED_BASALT_PEBBLE = registerBlock(
		"oxidized_basalt_pebble",
		key -> new OxidizedBasaltPebbleBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GRAY)
				.strength(1.25f, 4.2f)
				.sound(SoundType.BASALT)
				.noOcclusion()
		)
	);

	// Anorthosite - Outer World deepslate equivalent
	public static final Block ANORTHOSITE = registerBlock(
		"anorthosite",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(3.0f, 6.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block ANORTHOSITE_COPPER_ORE = registerBlock(
		"anorthosite_copper_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5f, 3.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block ANORTHOSITE_GOLD_ORE = registerBlock(
		"anorthosite_gold_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5f, 3.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block ANORTHOSITE_OXIDIZED_IRON_ORE = registerBlock(
		"anorthosite_oxidized_iron_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5f, 3.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block ANORTHOSITE_REDSTONE_ORE = registerBlock(
		"anorthosite_redstone_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5f, 3.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block RAW_HEMATITE = registerBlock(
		"raw_hematite",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_RED)
				.strength(5.0f, 6.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block RAW_OSMIUM = registerBlock(
		"raw_osmium",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.strength(5.0f, 6.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block RAW_OLIVINE = registerBlock(
		"raw_olivine",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GREEN)
				.strength(1.5f, 1200.0f)
				.sound(SoundType.AMETHYST)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block RAW_OLIVINE_BUD = registerBlock(
		"raw_olivine_bud",
		key -> new BuddingOlivineBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GREEN)
				.strength(1.5f, 1200.0f)
				.sound(SoundType.AMETHYST)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block OLIVINE_CRYSTAL_0 = registerBlock(
		"olivine_crystal_0",
		key -> new AmethystClusterBlock(
			3.0f,
			4.0f,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GREEN)
				.forceSolidOn()
				.noOcclusion()
				.sound(SoundType.AMETHYST_CLUSTER)
				.strength(1.5f, 1200.0f)
				.lightLevel(state -> 1)
				.pushReaction(PushReaction.POPPED)
		)
	);

	public static final Block OLIVINE_CRYSTAL_1 = registerBlock(
		"olivine_crystal_1",
		key -> new AmethystClusterBlock(
			5.0f,
			5.0f,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GREEN)
				.forceSolidOn()
				.noOcclusion()
				.sound(SoundType.AMETHYST_CLUSTER)
				.strength(1.5f, 1200.0f)
				.lightLevel(state -> 2)
				.pushReaction(PushReaction.POPPED)
		)
	);

	public static final Block OLIVINE_CRYSTAL_2 = registerBlock(
		"olivine_crystal_2",
		key -> new AmethystClusterBlock(
			7.0f,
			3.0f,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GREEN)
				.forceSolidOn()
				.noOcclusion()
				.sound(SoundType.AMETHYST_CLUSTER)
				.strength(1.5f, 1200.0f)
				.lightLevel(state -> 5)
				.pushReaction(PushReaction.POPPED)
		),
		properties -> properties.trimMaterial(ModTrimMaterials.OLIVINE)
	);

	public static final Block OLIVINE_TORCH = registerBlockOnly(
		"olivine_torch",
		key -> new TorchBlock(
			ParticleTypes.COPPER_FIRE_FLAME,
			BlockBehaviour.Properties.of()
				.setId(key)
				.noCollision()
				.instabreak()
				.lightLevel(state -> 14)
				.sound(SoundType.WOOD)
				.pushReaction(PushReaction.POPPED)
		)
	);

	public static final Block OLIVINE_WALL_TORCH = registerBlockOnly(
		"olivine_wall_torch",
		key -> new WallTorchBlock(
			ParticleTypes.COPPER_FIRE_FLAME,
			BlockBehaviour.Properties.of()
				.setId(key)
				.noCollision()
				.instabreak()
				.lightLevel(state -> 14)
				.sound(SoundType.WOOD)
				.pushReaction(PushReaction.POPPED)
				.overrideDescription("block.theouterworlds.olivine_torch")
		)
	);

	public static final Block UNLIT_TORCH = registerBlockOnly(
		"unlit_torch",
		key -> new UnlitTorchBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)
				.setId(key)
				.lightLevel(state -> 0)
		)
	);

	public static final Block UNLIT_WALL_TORCH = registerBlockOnly(
		"unlit_wall_torch",
		key -> new UnlitWallTorchBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH)
				.setId(key)
				.lightLevel(state -> 0)
				.overrideDescription("block.theouterworlds.unlit_torch")
		)
	);

	public static final Block RAW_JAROSITE = registerFireproofBlock(
		"raw_jarosite",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.GOLD)
				.strength(5.0f, 1200.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block RAW_PERCHLORATE = registerBlock(
		"raw_perchlorate",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.SNOW)
				.strength(5.0f, 6.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false)
		)
	);

	public static final Block PERCHLORATE_CHARGE = registerBlock(
		"perchlorate_charge",
		key -> new PerchlorateChargeBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).setId(key)
		)
	);

	public static final Block OXIDIZED_BASALT_OPAL_ORE = registerBlock(
		"oxidized_basalt_opal_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GRAY)
				.strength(3.0f, 3.0f)
				.sound(SoundType.BASALT)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block ANORTHOSITE_OPAL_ORE = registerBlock(
		"anorthosite_opal_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5f, 3.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block ANORTHOSITE_NICKEL_ORE = registerBlock(
		"anorthosite_nickel_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5f, 3.0f)
				.sound(SoundType.DEEPSLATE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block KNAPPING_TABLE = registerBlock(
		"knapping_table",
		key -> new KnappingTableBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.STONE)
				.strength(2.5f, 2.5f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block PROCESSOR = registerBlock(
		"processor",
		key -> new ProcessorBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.STONE)
				.strength(3.5f, 3.5f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block FORGE_PLATE = registerBlock(
		"forge_plate",
		key -> new ForgePlateBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.STONE)
				.strength(1.5f, 6.0f)
				.sound(SoundType.BASALT)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block ASTRAL_TELESCOPE = registerBlock(
		"astral_telescope",
		key -> new AstralTelescopeBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.METAL)
				.strength(2.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block DRY_ICE = registerBlock(
		"dry_ice",
		key -> new DryIceBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.SNOW)
				.strength(0.4f, 0.4f)
				.friction(0.996f)
				.sound(SoundType.GLASS)
		)
	);

	public static final Block CARBONIC_ICE = registerBlock(
		"carbonic_ice",
		key -> new DryIceBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.strength(0.65f, 0.65f)
				.friction(0.989f)
				.sound(SoundType.GLASS)
		)
	);

	public static final Block NITROGEN_ICE = registerBlock(
		"nitrogen_ice",
		key -> new DryIceBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.strength(0.65f, 0.65f)
				.friction(0.989f)
				.sound(SoundType.GLASS)
		)
	);

	/** Makemake crust ice — slippery, always drops, any pickaxe. */
	public static final Block METHANE_ICE = registerBlock(
		"methane_ice",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.SNOW)
				.strength(0.4f, 0.4f)
				.friction(0.996f)
				.sound(SoundType.GLASS)
		)
	);

	public static final Block ICE = registerBlock(
		"ice",
		key -> new AgingIceBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.ICE)
				.friction(0.98f)
				.strength(0.5f)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn((state, world, pos, type) -> false)
				.isRedstoneConductor((state, world, pos) -> false)
		)
	);

	public static final Block PACKED_ICE = registerBlock(
		"packed_ice",
		key -> new AgingIceBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.ICE)
				.friction(0.98f)
				.strength(0.5f)
				.sound(SoundType.GLASS)
		)
	);

	public static final Block BLUE_ICE = registerBlock(
		"blue_ice",
		key -> new AgingIceBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.ICE)
				.friction(0.989f)
				.strength(2.8f)
				.sound(SoundType.GLASS)
		)
	);

	public static final Block GYPSUM_BLOCK = registerBlock(
		"gypsum_block",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.sound(SoundType.DRIPSTONE_BLOCK)
				.requiresCorrectToolForDrops()
				.strength(1.5f, 1.0f)
		)
	);

	public static final Block GYPSUM_SPIKE = registerBlock(
		"gypsum_spike",
		key -> new PointedDripstoneBlock(
			GYPSUM_BLOCK.defaultBlockState(),
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.forceSolidOn()
				.noOcclusion()
				.sound(SoundType.POINTED_DRIPSTONE)
				.randomTicks()
				.strength(1.5f, 3.0f)
				.dynamicShape()
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.POPPED)
				.isRedstoneConductor((state, world, pos) -> false)
		)
	);

	public static final Block KHARAX_SHED = registerBlock(
		"kharax_shed",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_RED)
				.strength(0.8f, 0.8f)
				.sound(SoundType.SLIME_BLOCK)
				.friction(0.8f)
		)
	);

	public static final Block KHARAX_SPORE = registerBlock(
		"kharax_spore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.GOLD)
				.strength(0.4f, 0.4f)
				.sound(SoundType.SHROOMLIGHT)
				.lightLevel(state -> 5)
		)
	);

	public static final Block MERCURY = registerBlockOnly(
		"mercury",
		key -> new MercuryLiquidBlock(
			ModFluids.MERCURY,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.METAL)
				.replaceable()
				.noCollision()
				.strength(100.0f)
				.pushReaction(PushReaction.POPPED)
				.noLootTable()
				.liquid()
				.randomTicks()
				.sound(SoundType.EMPTY)
		)
	);

	public static final Block LIQUID_HYDROGEN = registerBlockOnly(
		"liquid_hydrogen",
		key -> new LiquidBlock(
			ModFluids.LIQUID_HYDROGEN,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.replaceable()
				.noCollision()
				.strength(100.0f)
				.pushReaction(PushReaction.POPPED)
				.noLootTable()
				.liquid()
				.lightLevel(state -> 2)
				.sound(SoundType.EMPTY)
		)
	);

	public static final Block LIQUID_HELIUM = registerBlockOnly(
		"liquid_helium",
		key -> new LiquidBlock(
			ModFluids.LIQUID_HELIUM,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.replaceable()
				.noCollision()
				.strength(100.0f)
				.pushReaction(PushReaction.POPPED)
				.noLootTable()
				.liquid()
				.lightLevel(state -> 2)
				.sound(SoundType.EMPTY)
		)
	);

	public static final Block LIQUID_AMMONIA = registerBlockOnly(
		"liquid_ammonia",
		key -> new LiquidBlock(
			ModFluids.LIQUID_AMMONIA,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.replaceable()
				.noCollision()
				.strength(100.0f)
				.pushReaction(PushReaction.POPPED)
				.noLootTable()
				.liquid()
				.lightLevel(state -> 2)
				.sound(SoundType.EMPTY)
		)
	);

	public static final Block METALLIC_HYDROGEN = registerBlockOnly(
		"metallic_hydrogen",
		key -> new MetallicHydrogenBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.noCollision()
				.noOcclusion()
				.strength(0.5f, 0.5f)
				.pushReaction(PushReaction.POPPED)
				.lightLevel(state -> 15)
				.sound(SoundType.GLASS)
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false)
		)
	);

	public static final Block METALLIC_HELIUM = registerBlockOnly(
		"metallic_helium",
		key -> new MetallicHeliumBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.noCollision()
				.noOcclusion()
				.strength(0.5f, 0.5f)
				.pushReaction(PushReaction.POPPED)
				.lightLevel(state -> 15)
				.sound(SoundType.GLASS)
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false)
		)
	);

	public static final Block HYDROGEN_CRYSTAL = registerBlock(
		"hydrogen_crystal",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.strength(50.0f, 1200.0f)
				.sound(SoundType.GLASS)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> 15)
		)
	);

	public static final Block HELIUM_CRYSTAL = registerBlock(
		"helium_crystal",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.strength(50.0f, 1200.0f)
				.sound(SoundType.GLASS)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> 15)
		)
	);

	public static final Block IONIC_AMMONIA = registerBlockOnly(
		"ionic_ammonia",
		key -> new IonicAmmoniaBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.noCollision()
				.noOcclusion()
				.strength(0.5f, 0.5f)
				.pushReaction(PushReaction.POPPED)
				.lightLevel(state -> 15)
				.sound(SoundType.GLASS)
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false)
		)
	);

	public static final Block IONIC_CRYSTAL = registerBlock(
		"ionic_crystal",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.strength(50.0f, 1200.0f)
				.sound(SoundType.GLASS)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> 15)
		)
	);

	public static final Block LIQUID_METHANE = registerBlockOnly(
		"liquid_methane",
		key -> new LiquidBlock(
			ModFluids.LIQUID_METHANE,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_CYAN)
				.replaceable()
				.noCollision()
				.strength(100.0f)
				.pushReaction(PushReaction.POPPED)
				.noLootTable()
				.liquid()
				.lightLevel(state -> 2)
				.sound(SoundType.EMPTY)
		)
	);

	public static final Block SOLAR_PLASMA = registerBlockOnly(
		"solar_plasma",
		key -> new SolarPlasmaLiquidBlock(
			ModFluids.SOLAR_PLASMA,
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_ORANGE)
				.replaceable()
				.noCollision()
				.strength(100.0f)
				.pushReaction(PushReaction.POPPED)
				.noLootTable()
				.liquid()
				.lightLevel(state -> 15)
				.sound(SoundType.EMPTY)
		)
	);

	public static final Block IONIC_METHANE = registerBlockOnly(
		"ionic_methane",
		key -> new IonicMethaneBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_CYAN)
				.noCollision()
				.noOcclusion()
				.strength(0.5f, 0.5f)
				.pushReaction(PushReaction.POPPED)
				.lightLevel(state -> 15)
				.sound(SoundType.GLASS)
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false)
		)
	);

	public static final Block METHANE_CRYSTAL = registerBlock(
		"methane_crystal",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_CYAN)
				.strength(50.0f, 1200.0f)
				.sound(SoundType.GLASS)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> 15)
		)
	);

	/** Sulfide cloud deck: sink very slowly, Nausea II when head is inside. */
	public static final Block SULFIDE_CLOUD = registerBlockOnly(
		"sulfide_cloud",
		key -> new AerogelCloudBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_YELLOW)
				.strength(0.2f, 0.2f)
				.sound(SoundType.WOOL)
				.noCollision()
				.noOcclusion()
				.noLootTable()
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false),
			0.88F,
			0.028,
			0.07,
			1
		)
	);

	/** Ammonia cloud deck: sinks a bit faster, Nausea I when head is inside. */
	public static final Block AMMONIA_CLOUD = registerBlockOnly(
		"ammonia_cloud",
		key -> new AerogelCloudBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.strength(0.15f, 0.15f)
				.sound(SoundType.WOOL)
				.noCollision()
				.noOcclusion()
				.noLootTable()
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false),
			0.86F,
			0.04,
			0.065,
			0
		)
	);

	/** Methane cloud deck: similar sink to ammonia, Nausea I when head is inside. */
	public static final Block METHANE_CLOUD = registerBlockOnly(
		"methane_cloud",
		key -> new AerogelCloudBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_CYAN)
				.strength(0.15f, 0.15f)
				.sound(SoundType.WOOL)
				.noCollision()
				.noOcclusion()
				.noLootTable()
				.isViewBlocking((state, level, pos, nearPlane) -> false)
				.isSuffocating((state, level, pos) -> false),
			0.86F,
			0.04,
			0.065,
			1
		)
	);

	public static final Block RAW_NICKEL_BLOCK = registerBlock(
		"raw_nickel_block",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.METAL)
				.strength(5.0f, 6.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block MERCURY_BLOCK = registerBlock(
		"mercury_block",
		key -> new MercuryBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.METAL)
				.strength(0.5f, 0.5f)
				.sound(SoundType.METAL)
				.randomTicks()
		)
	);

	public static final Block FROZEN_WHEAT = registerBlockOnly(
		"frozen_wheat",
		key -> new FrozenCropBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).setId(key),
			"frozen_wheat_seeds"
		)
	);

	public static final Block FROZEN_CARROTS = registerBlockOnly(
		"frozen_carrots",
		key -> new FrozenCropBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS).setId(key),
			"frozen_carrot"
		)
	);

	public static final Block FROZEN_POTATOES = registerBlockOnly(
		"frozen_potatoes",
		key -> new FrozenCropBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.POTATOES).setId(key),
			"frozen_potato"
		)
	);

	public static final Block FROZEN_BEETROOTS = registerBlockOnly(
		"frozen_beetroots",
		key -> new FrozenBeetrootBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.BEETROOTS).setId(key)
		)
	);

	public static final Block FROZEN_TORCHFLOWER_CROP = registerBlockOnly(
		"frozen_torchflower_crop",
		key -> new FrozenTorchflowerCropBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.TORCHFLOWER_CROP).setId(key)
		)
	);

	public static final Block FROZEN_PITCHER_CROP = registerBlockOnly(
		"frozen_pitcher_crop",
		key -> new FrozenPitcherCropBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.PITCHER_CROP).setId(key)
		)
	);

	public static final Block FROZEN_TORCHFLOWER = registerBlock(
		"frozen_torchflower",
		key -> new FlowerBlock(
			MobEffects.NIGHT_VISION,
			4.0F,
			BlockBehaviour.Properties.ofFullCopy(Blocks.TORCHFLOWER).setId(key)
		)
	);

	public static final Block FROZEN_OAK_SAPLING = frozenSapling("frozen_oak_sapling", TreeGrower.OAK, Blocks.OAK_SAPLING);
	public static final Block FROZEN_SPRUCE_SAPLING = frozenSapling("frozen_spruce_sapling", TreeGrower.SPRUCE, Blocks.SPRUCE_SAPLING);
	public static final Block FROZEN_BIRCH_SAPLING = frozenSapling("frozen_birch_sapling", TreeGrower.BIRCH, Blocks.BIRCH_SAPLING);
	public static final Block FROZEN_JUNGLE_SAPLING = frozenSapling("frozen_jungle_sapling", TreeGrower.JUNGLE, Blocks.JUNGLE_SAPLING);
	public static final Block FROZEN_ACACIA_SAPLING = frozenSapling("frozen_acacia_sapling", TreeGrower.ACACIA, Blocks.ACACIA_SAPLING);
	public static final Block FROZEN_DARK_OAK_SAPLING = frozenSapling("frozen_dark_oak_sapling", TreeGrower.DARK_OAK, Blocks.DARK_OAK_SAPLING);
	public static final Block FROZEN_CHERRY_SAPLING = frozenSapling("frozen_cherry_sapling", TreeGrower.CHERRY, Blocks.CHERRY_SAPLING);
	public static final Block FROZEN_PALE_OAK_SAPLING = frozenSapling("frozen_pale_oak_sapling", TreeGrower.PALE_OAK, Blocks.PALE_OAK_SAPLING);

	public static final Block FROZEN_MANGROVE_PROPAGULE = registerBlock(
		"frozen_mangrove_propagule",
		key -> new FrozenMangrovePropaguleBlock(
			TreeGrower.MANGROVE,
			BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_PROPAGULE).setId(key)
		)
	);

	public static final Block FROZEN_AZALEA = registerBlock(
		"frozen_azalea",
		key -> new FrozenAzaleaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AZALEA).setId(key))
	);

	public static final Block FROZEN_FLOWERING_AZALEA = registerBlock(
		"frozen_flowering_azalea",
		key -> new FrozenAzaleaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWERING_AZALEA).setId(key))
	);

	public static final Block FROZEN_RED_MUSHROOM = registerBlock(
		"frozen_red_mushroom",
		key -> new FrozenMushroomBlock(
			TreeFeatures.HUGE_RED_MUSHROOM,
			BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM).setId(key)
		)
	);

	public static final Block FROZEN_BROWN_MUSHROOM = registerBlock(
		"frozen_brown_mushroom",
		key -> new FrozenMushroomBlock(
			TreeFeatures.HUGE_BROWN_MUSHROOM,
			BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).setId(key)
		)
	);

	public static final Block FROZEN_CRIMSON_FUNGUS = registerBlock(
		"frozen_crimson_fungus",
		key -> new FrozenNetherFungusBlock(
			TreeFeatures.CRIMSON_FUNGUS_PLANTED,
			Blocks.CRIMSON_NYLIUM,
			BlockTags.SUPPORTS_CRIMSON_FUNGUS,
			BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_FUNGUS).setId(key)
		)
	);

	public static final Block FROZEN_WARPED_FUNGUS = registerBlock(
		"frozen_warped_fungus",
		key -> new FrozenNetherFungusBlock(
			TreeFeatures.WARPED_FUNGUS_PLANTED,
			Blocks.WARPED_NYLIUM,
			BlockTags.SUPPORTS_WARPED_FUNGUS,
			BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_FUNGUS).setId(key)
		)
	);

	public static final Block FROZEN_COCOA = registerBlockOnly(
		"frozen_cocoa",
		key -> new FrozenCocoaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COCOA).setId(key))
	);

	public static final Block FROZEN_MELON = registerBlock(
		"frozen_melon",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MELON).setId(key))
	);

	public static final Block FROZEN_PUMPKIN = registerBlock(
		"frozen_pumpkin",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN).setId(key))
	);

	public static final Block FROZEN_PUMPKIN_STEM = registerBlockOnly(
		"frozen_pumpkin_stem",
		key -> new FrozenStemBlock(
			blockKey("frozen_pumpkin"),
			blockKey("frozen_attached_pumpkin_stem"),
			itemKey("frozen_pumpkin_seeds"),
			BlockTags.SUPPORTS_PUMPKIN_STEM,
			BlockTags.SUPPORTS_PUMPKIN_STEM_FRUIT,
			BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN_STEM).setId(key)
		)
	);

	public static final Block FROZEN_MELON_STEM = registerBlockOnly(
		"frozen_melon_stem",
		key -> new FrozenStemBlock(
			blockKey("frozen_melon"),
			blockKey("frozen_attached_melon_stem"),
			itemKey("frozen_melon_seeds"),
			BlockTags.SUPPORTS_MELON_STEM,
			BlockTags.SUPPORTS_MELON_STEM_FRUIT,
			BlockBehaviour.Properties.ofFullCopy(Blocks.MELON_STEM).setId(key)
		)
	);

	public static final Block FROZEN_ATTACHED_PUMPKIN_STEM = registerBlockOnly(
		"frozen_attached_pumpkin_stem",
		key -> new FrozenAttachedStemBlock(
			blockKey("frozen_pumpkin_stem"),
			blockKey("frozen_pumpkin"),
			itemKey("frozen_pumpkin_seeds"),
			BlockTags.SUPPORTS_PUMPKIN_STEM,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_PUMPKIN_STEM).setId(key)
		)
	);

	public static final Block FROZEN_ATTACHED_MELON_STEM = registerBlockOnly(
		"frozen_attached_melon_stem",
		key -> new FrozenAttachedStemBlock(
			blockKey("frozen_melon_stem"),
			blockKey("frozen_melon"),
			itemKey("frozen_melon_seeds"),
			BlockTags.SUPPORTS_MELON_STEM,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_MELON_STEM).setId(key)
		)
	);

	public static final Block FROZEN_BAMBOO_SAPLING = registerBlockOnly(
		"frozen_bamboo_sapling",
		key -> new FrozenBambooSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_SAPLING).setId(key))
	);

	public static final Block FROZEN_BAMBOO = registerBlock(
		"frozen_bamboo",
		key -> new FrozenBambooStalkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO).setId(key))
	);

	public static final Block FROZEN_FIREFLY_BUSH = registerBlock(
		"frozen_firefly_bush",
		key -> new FrozenFireflyBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FIREFLY_BUSH).setId(key))
	);

	public static final Block FROZEN_LANTERN = registerOxidizableFrozenLantern(
		"frozen_lantern",
		Blocks.LANTERN,
		WeatheringCopper.WeatherState.UNAFFECTED,
		OxidizableLanternAging.Kind.IRON
	);
	public static final Block FROZEN_EXPOSED_LANTERN = registerOxidizableFrozenLantern(
		"frozen_exposed_lantern",
		Blocks.LANTERN,
		WeatheringCopper.WeatherState.EXPOSED,
		OxidizableLanternAging.Kind.IRON
	);
	public static final Block FROZEN_WEATHERED_LANTERN = registerOxidizableFrozenLantern(
		"frozen_weathered_lantern",
		Blocks.LANTERN,
		WeatheringCopper.WeatherState.WEATHERED,
		OxidizableLanternAging.Kind.IRON
	);
	public static final Block FROZEN_OXIDIZED_LANTERN = registerOxidizableFrozenLantern(
		"frozen_oxidized_lantern",
		Blocks.LANTERN,
		WeatheringCopper.WeatherState.OXIDIZED,
		OxidizableLanternAging.Kind.IRON
	);
	public static final Block FROZEN_WAXED_LANTERN = registerFrozenLantern("frozen_waxed_lantern", Blocks.LANTERN);
	public static final Block FROZEN_WAXED_EXPOSED_LANTERN = registerFrozenLantern("frozen_waxed_exposed_lantern", Blocks.LANTERN);
	public static final Block FROZEN_WAXED_WEATHERED_LANTERN = registerFrozenLantern("frozen_waxed_weathered_lantern", Blocks.LANTERN);
	public static final Block FROZEN_WAXED_OXIDIZED_LANTERN = registerFrozenLantern("frozen_waxed_oxidized_lantern", Blocks.LANTERN);
	public static final Block FROZEN_SOUL_LANTERN = registerFrozenLantern("frozen_soul_lantern", Blocks.SOUL_LANTERN);
	public static final Block FROZEN_COPPER_LANTERN = registerOxidizableFrozenLantern(
		"frozen_copper_lantern",
		Blocks.COPPER_LANTERN.weathering().unaffected(),
		WeatheringCopper.WeatherState.UNAFFECTED,
		OxidizableLanternAging.Kind.COPPER
	);
	public static final Block FROZEN_EXPOSED_COPPER_LANTERN = registerOxidizableFrozenLantern(
		"frozen_exposed_copper_lantern",
		Blocks.COPPER_LANTERN.weathering().exposed(),
		WeatheringCopper.WeatherState.EXPOSED,
		OxidizableLanternAging.Kind.COPPER
	);
	public static final Block FROZEN_WEATHERED_COPPER_LANTERN = registerOxidizableFrozenLantern(
		"frozen_weathered_copper_lantern",
		Blocks.COPPER_LANTERN.weathering().weathered(),
		WeatheringCopper.WeatherState.WEATHERED,
		OxidizableLanternAging.Kind.COPPER
	);
	public static final Block FROZEN_OXIDIZED_COPPER_LANTERN = registerOxidizableFrozenLantern(
		"frozen_oxidized_copper_lantern",
		Blocks.COPPER_LANTERN.weathering().oxidized(),
		WeatheringCopper.WeatherState.OXIDIZED,
		OxidizableLanternAging.Kind.COPPER
	);
	public static final Block FROZEN_WAXED_COPPER_LANTERN = registerFrozenLantern(
		"frozen_waxed_copper_lantern",
		Blocks.COPPER_LANTERN.waxed().unaffected()
	);
	public static final Block FROZEN_WAXED_EXPOSED_COPPER_LANTERN = registerFrozenLantern(
		"frozen_waxed_exposed_copper_lantern",
		Blocks.COPPER_LANTERN.waxed().exposed()
	);
	public static final Block FROZEN_WAXED_WEATHERED_COPPER_LANTERN = registerFrozenLantern(
		"frozen_waxed_weathered_copper_lantern",
		Blocks.COPPER_LANTERN.waxed().weathered()
	);
	public static final Block FROZEN_WAXED_OXIDIZED_COPPER_LANTERN = registerFrozenLantern(
		"frozen_waxed_oxidized_copper_lantern",
		Blocks.COPPER_LANTERN.waxed().oxidized()
	);

	public static final Block FROZEN_NETHER_PORTAL = registerBlock(
		"frozen_nether_portal",
		key -> new FrozenNetherPortalBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.strength(0.3f)
				.sound(SoundType.GLASS)
				.lightLevel(state -> 11)
				.noOcclusion()
				.pushReaction(PushReaction.POPPED)
		)
	);

	public static final Block FROZEN_MAGMA = registerBlock(
		"frozen_magma",
		key -> new Block(
			BlockBehaviour.Properties.ofFullCopy(Blocks.MAGMA_BLOCK)
				.setId(key)
				.mapColor(MapColor.COLOR_CYAN)
				.lightLevel(state -> 0)
				.isValidSpawn((state, world, pos, type) -> false)
		)
	);

	/** Creative-only: never drops in survival. Do not re-add a loot table. */
	public static final Block RIFT = registerBlock(
		"rift",
		key -> new RiftBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.noCollision()
				.noOcclusion()
				.strength(-1.0f, 3600000.0f)
				.lightLevel(state -> 11)
				.sound(SoundType.GLASS)
				.pushReaction(PushReaction.IMMOVEABLE)
				.noLootTable()
		)
	);

	public static final Block RIFT_CHARGE = registerBlock(
		"rift_charge",
		key -> new RiftChargeBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.strength(5.0f, 1200.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.lightLevel(state -> state.getValue(RiftChargeBlock.POWERED) ? 11 : 4)
				.pushReaction(PushReaction.IMMOVEABLE)
		)
	);

	public static final Block RIFT_PAD = registerBlock(
		"rift_pad",
		key -> new RiftPadBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.strength(5.0f, 1200.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.lightLevel(state -> 8)
				.pushReaction(PushReaction.IMMOVEABLE)
		)
	);

	public static final Block BEACON_CONCENTRATOR = registerBlock(
		"beacon_concentrator",
		key -> new BeaconConcentratorBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.instabreak()
				.sound(SoundType.AMETHYST)
				.noOcclusion()
				.lightLevel(state -> state.getValue(BeaconConcentratorBlock.ACTIVE) ? 11 : 0)
				.pushReaction(PushReaction.POPPED)
		)
	);

	public static final Block CONCENTRATED_BEACON_BEAM = registerBlockOnly(
		"concentrated_beacon_beam",
		key -> new ConcentratedBeaconBeamBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.strength(-1.0f, 3600000.0f)
				.noCollision()
				.noOcclusion()
				.pushReaction(PushReaction.IMMOVEABLE)
		)
	);

	public static final Block PYROXENITE = registerStone("pyroxenite", MapColor.TERRACOTTA_BROWN);
	public static final Block PYROXENITE_STAIRS = registerStairs("pyroxenite_stairs", PYROXENITE);
	public static final Block PYROXENITE_SLAB = registerSlab("pyroxenite_slab", PYROXENITE);
	public static final Block PYROXENITE_WALL = registerWall("pyroxenite_wall", PYROXENITE);
	public static final Block POLISHED_PYROXENITE = registerBlock(
		"polished_pyroxenite",
		key -> new Block(stoneProperties(key, MapColor.TERRACOTTA_BROWN).lightLevel(state -> 4))
	);
	public static final Block POLISHED_PYROXENITE_STAIRS = registerBlock(
		"polished_pyroxenite_stairs",
		key -> new StairBlock(POLISHED_PYROXENITE.defaultBlockState(), stoneProperties(key, MapColor.TERRACOTTA_BROWN).lightLevel(state -> 4))
	);
	public static final Block POLISHED_PYROXENITE_SLAB = registerBlock(
		"polished_pyroxenite_slab",
		key -> new SlabBlock(stoneProperties(key, MapColor.TERRACOTTA_BROWN).lightLevel(state -> 4))
	);
	public static final Block POLISHED_PYROXENITE_WALL = registerBlock(
		"polished_pyroxenite_wall",
		key -> new WallBlock(stoneProperties(key, MapColor.TERRACOTTA_BROWN).lightLevel(state -> 4))
	);
	public static final Block ANHYDRITE = registerStone("anhydrite", MapColor.COLOR_LIGHT_GRAY);
	public static final Block ANHYDRITE_STAIRS = registerStairs("anhydrite_stairs", ANHYDRITE);
	public static final Block ANHYDRITE_SLAB = registerSlab("anhydrite_slab", ANHYDRITE);
	public static final Block ANHYDRITE_WALL = registerWall("anhydrite_wall", ANHYDRITE);
	public static final Block POLISHED_ANHYDRITE = registerStone("polished_anhydrite", MapColor.COLOR_LIGHT_GRAY);
	public static final Block POLISHED_ANHYDRITE_STAIRS = registerStairs("polished_anhydrite_stairs", POLISHED_ANHYDRITE);
	public static final Block POLISHED_ANHYDRITE_SLAB = registerSlab("polished_anhydrite_slab", POLISHED_ANHYDRITE);
	public static final Block POLISHED_ANHYDRITE_WALL = registerWall("polished_anhydrite_wall", POLISHED_ANHYDRITE);
	public static final Block PYROXENITE_NICKEL_ORE = registerBlock(
		"pyroxenite_nickel_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_BROWN)
				.strength(3.0f, 3.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);
	public static final Block NORITE = registerStone("norite", MapColor.COLOR_BLACK);
	public static final Block NORITE_STAIRS = registerStairs("norite_stairs", NORITE);
	public static final Block NORITE_SLAB = registerSlab("norite_slab", NORITE);
	public static final Block NORITE_WALL = registerWall("norite_wall", NORITE);
	public static final Block GABBRO = registerStone("gabbro", MapColor.COLOR_GRAY);
	public static final Block GABBRO_STAIRS = registerStairs("gabbro_stairs", GABBRO);
	public static final Block GABBRO_SLAB = registerSlab("gabbro_slab", GABBRO);
	public static final Block GABBRO_WALL = registerWall("gabbro_wall", GABBRO);
	public static final Block ANORTHOSITIC_REGOLITH = registerBlock(
		"anorthositic_regolith",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.SNOW)
				.strength(0.5f, 0.5f)
				.sound(SoundType.SAND)
		)
	);
	public static final Block SALT_BLOCK = registerBlock(
		"salt_block",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.SNOW)
				.strength(0.8f, 0.8f)
				.sound(SoundType.SAND)
				.requiresCorrectToolForDrops()
		)
	);
	public static final Block ENSTATITE = registerStone("enstatite", MapColor.TERRACOTTA_GRAY);
	public static final Block ENSTATITE_STAIRS = registerStairs("enstatite_stairs", ENSTATITE);
	public static final Block ENSTATITE_SLAB = registerSlab("enstatite_slab", ENSTATITE);
	public static final Block ENSTATITE_WALL = registerWall("enstatite_wall", ENSTATITE);
	public static final Block ENSTATITE_DIAMOND_ORE = registerBlock(
		"enstatite_diamond_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_GRAY)
				.strength(4.5f, 3.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);
	public static final Block KOMATIITE = registerStone("komatiite", MapColor.TERRACOTTA_ORANGE);
	public static final Block KOMATIITE_STAIRS = registerStairs("komatiite_stairs", KOMATIITE);
	public static final Block KOMATIITE_SLAB = registerSlab("komatiite_slab", KOMATIITE);
	public static final Block KOMATIITE_WALL = registerWall("komatiite_wall", KOMATIITE);
	public static final Block KOMATIITE_DIAMOND_ORE = registerBlock(
		"komatiite_diamond_ore",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_ORANGE)
				.strength(3.0f, 3.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);
	public static final Block MAGNESIAN_REGOLITH = registerBlock(
		"magnesian_regolith",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
				.strength(0.5f, 0.5f)
				.sound(SoundType.SAND)
		)
	);
	public static final Block GRAPHITE = registerBlock(
		"graphite",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_BLACK)
				.strength(3.0f, 3.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
		)
	);
	public static final Block OPAL_BLOCK = registerBlock(
		"opal_block",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PINK)
				.strength(1.5f, 1.5f)
				.sound(SoundType.AMETHYST)
				.lightLevel(state -> 15)
				.requiresCorrectToolForDrops()
		)
	);
	public static final Block NICKEL_BLOCK = registerBlock(
		"nickel_block",
		key -> new Block(nickelProperties(key))
	);
	public static final Block CUT_NICKEL = registerBlock(
		"cut_nickel",
		key -> new Block(nickelProperties(key))
	);
	public static final Block CUT_NICKEL_STAIRS = registerBlock(
		"cut_nickel_stairs",
		key -> new StairBlock(CUT_NICKEL.defaultBlockState(), nickelProperties(key))
	);
	public static final Block CUT_NICKEL_SLAB = registerBlock(
		"cut_nickel_slab",
		key -> new SlabBlock(nickelProperties(key))
	);
	public static final Block NICKEL_GRATE = registerBlock(
		"nickel_grate",
		key -> new Block(nickelProperties(key).noOcclusion())
	);
	public static final Block NICKEL_BULB = registerBlock(
		"nickel_bulb",
		key -> new NickelBulbBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.METAL)
				.strength(3.0f, 3.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> state.getValue(NickelBulbBlock.LIT) ? 1 : 0)
		)
	);
	public static final Block NICKEL_DOOR = registerBlock(
		"nickel_door",
		key -> new DoorBlock(BlockSetType.IRON, nickelProperties(key).noOcclusion())
	);
	public static final Block NICKEL_TRAPDOOR = registerBlock(
		"nickel_trapdoor",
		key -> new TrapDoorBlock(BlockSetType.IRON, nickelProperties(key).noOcclusion())
	);
	public static final Block NICKEL_CHAIN = registerBlock(
		"nickel_chain",
		key -> new ChainBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.METAL)
				.strength(5.0f, 6.0f)
				.sound(SoundType.CHAIN)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);
	public static final Block NICKEL_BARS = registerBlock(
		"nickel_bars",
		key -> new IronBarsBlock(nickelProperties(key).noOcclusion())
	);
	public static final Block REINFORCED_TINTED_GLASS_PANE = registerBlock(
		"reinforced_tinted_glass_pane",
		key -> new ReinforcedTintedGlassPaneBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.TINTED_GLASS)
				.setId(key)
				.noOcclusion()
		)
	);

	// Oxidizable Iron blocks - oxidize only in Outerworld dimension
	
	// UNAFFECTED stage clones of vanilla blocks (hidden - no BlockItems)
	// These replace vanilla blocks when placed in Outerworld
	// They drop vanilla items via loot tables
	public static final Block UNAFFECTED_IRON = registerBlockOnly(
		"unaffected_iron",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);
	
	public static final Block EXPOSED_IRON = registerBlock(
		"exposed_iron",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_IRON = registerBlock(
		"weathered_iron",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_IRON = registerBlock(
		"oxidized_iron",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	// Waxed iron blocks
	public static final Block WAXED_EXPOSED_IRON = registerBlock(
		"waxed_exposed_iron",
		key -> new WaxedIronBlock(
			EXPOSED_IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block WAXED_WEATHERED_IRON = registerBlock(
		"waxed_weathered_iron",
		key -> new WaxedIronBlock(
			WEATHERED_IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	public static final Block WAXED_OXIDIZED_IRON = registerBlock(
		"waxed_oxidized_iron",
		key -> new WaxedIronBlock(
			OXIDIZED_IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	// Waxed vanilla iron - looks like vanilla but won't oxidize in Outerworld
	public static final Block WAXED_IRON = registerBlock(
		"waxed_iron",
		key -> new WaxedIronBlock(
			UNAFFECTED_IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	// Iron Bulb blocks
	public static final Block IRON_BULB = registerBlock(
		"iron_bulb",
		key -> new OxidizableIronBulbBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(3.0f, 3.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
				.lightLevel(state -> state.getValue(OxidizableIronBulbBlock.LIT) ? 15 : 0)
		)
	);

	public static final Block EXPOSED_IRON_BULB = registerBlock(
		"exposed_iron_bulb",
		key -> new OxidizableIronBulbBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(3.0f, 3.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
				.lightLevel(state -> state.getValue(OxidizableIronBulbBlock.LIT) ? 12 : 0)
		)
	);

	public static final Block WEATHERED_IRON_BULB = registerBlock(
		"weathered_iron_bulb",
		key -> new OxidizableIronBulbBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(3.0f, 3.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
				.lightLevel(state -> state.getValue(OxidizableIronBulbBlock.LIT) ? 8 : 0)
		)
	);

	public static final Block OXIDIZED_IRON_BULB = registerBlock(
		"oxidized_iron_bulb",
		key -> new OxidizableIronBulbBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(3.0f, 3.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> state.getValue(OxidizableIronBulbBlock.LIT) ? 4 : 0)
		)
	);

	public static final Block WAXED_IRON_BULB = registerBlock("waxed_iron_bulb",
		key -> new WaxedIronBulbBlock(IRON_BULB, BlockBehaviour.Properties.of().setId(key).strength(3.0f, 3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(WaxedIronBulbBlock.LIT) ? 15 : 0)));

	public static final Block WAXED_EXPOSED_IRON_BULB = registerBlock("waxed_exposed_iron_bulb",
		key -> new WaxedIronBulbBlock(EXPOSED_IRON_BULB, BlockBehaviour.Properties.of().setId(key).strength(3.0f, 3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(WaxedIronBulbBlock.LIT) ? 12 : 0)));

	public static final Block WAXED_WEATHERED_IRON_BULB = registerBlock("waxed_weathered_iron_bulb",
		key -> new WaxedIronBulbBlock(WEATHERED_IRON_BULB, BlockBehaviour.Properties.of().setId(key).strength(3.0f, 3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(WaxedIronBulbBlock.LIT) ? 8 : 0)));

	public static final Block WAXED_OXIDIZED_IRON_BULB = registerBlock("waxed_oxidized_iron_bulb",
		key -> new WaxedIronBulbBlock(OXIDIZED_IRON_BULB, BlockBehaviour.Properties.of().setId(key).strength(3.0f, 3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(WaxedIronBulbBlock.LIT) ? 4 : 0)));

	// Iron Chain blocks
	public static final Block UNAFFECTED_IRON_CHAIN = registerBlockOnly(
		"unaffected_iron_chain",
		key -> new OxidizableIronChainBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.CHAIN)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);
	
	public static final Block EXPOSED_IRON_CHAIN = registerBlock(
		"exposed_iron_chain",
		key -> new OxidizableIronChainBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.CHAIN)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_IRON_CHAIN = registerBlock(
		"weathered_iron_chain",
		key -> new OxidizableIronChainBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.CHAIN)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_IRON_CHAIN = registerBlock(
		"oxidized_iron_chain",
		key -> new OxidizableIronChainBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.CHAIN)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block WAXED_EXPOSED_IRON_CHAIN = registerBlock("waxed_exposed_iron_chain",
		key -> new WaxedIronChainBlock(EXPOSED_IRON_CHAIN, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.CHAIN).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_WEATHERED_IRON_CHAIN = registerBlock("waxed_weathered_iron_chain",
		key -> new WaxedIronChainBlock(WEATHERED_IRON_CHAIN, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.CHAIN).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_OXIDIZED_IRON_CHAIN = registerBlock("waxed_oxidized_iron_chain",
		key -> new WaxedIronChainBlock(OXIDIZED_IRON_CHAIN, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.CHAIN).requiresCorrectToolForDrops().noOcclusion()));

	// Waxed vanilla iron chain - looks like vanilla but won't oxidize in Outerworld
	public static final Block WAXED_IRON_CHAIN = registerBlock("waxed_iron_chain",
		key -> new WaxedIronChainBlock(UNAFFECTED_IRON_CHAIN, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.CHAIN).requiresCorrectToolForDrops().noOcclusion()));

	// Iron Door blocks
	public static final Block UNAFFECTED_IRON_DOOR = registerBlockOnly(
		"unaffected_iron_door",
		key -> new OxidizableIronDoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);
	
	public static final Block EXPOSED_IRON_DOOR = registerBlock(
		"exposed_iron_door",
		key -> new OxidizableIronDoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_IRON_DOOR = registerBlock(
		"weathered_iron_door",
		key -> new OxidizableIronDoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_IRON_DOOR = registerBlock(
		"oxidized_iron_door",
		key -> new OxidizableIronDoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block WAXED_EXPOSED_IRON_DOOR = registerBlock("waxed_exposed_iron_door",
		key -> new WaxedIronDoorBlock(EXPOSED_IRON_DOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_WEATHERED_IRON_DOOR = registerBlock("waxed_weathered_iron_door",
		key -> new WaxedIronDoorBlock(WEATHERED_IRON_DOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_OXIDIZED_IRON_DOOR = registerBlock("waxed_oxidized_iron_door",
		key -> new WaxedIronDoorBlock(OXIDIZED_IRON_DOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// Waxed vanilla iron door - looks like vanilla but won't oxidize in Outerworld
	public static final Block WAXED_IRON_DOOR = registerBlock("waxed_iron_door",
		key -> new WaxedIronDoorBlock(UNAFFECTED_IRON_DOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// Iron Trapdoor blocks
	public static final Block UNAFFECTED_IRON_TRAPDOOR = registerBlockOnly(
		"unaffected_iron_trapdoor",
		key -> new OxidizableIronTrapdoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);
	
	public static final Block EXPOSED_IRON_TRAPDOOR = registerBlock(
		"exposed_iron_trapdoor",
		key -> new OxidizableIronTrapdoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_IRON_TRAPDOOR = registerBlock(
		"weathered_iron_trapdoor",
		key -> new OxidizableIronTrapdoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_IRON_TRAPDOOR = registerBlock(
		"oxidized_iron_trapdoor",
		key -> new OxidizableIronTrapdoorBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block WAXED_EXPOSED_IRON_TRAPDOOR = registerBlock("waxed_exposed_iron_trapdoor",
		key -> new WaxedIronTrapdoorBlock(EXPOSED_IRON_TRAPDOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_WEATHERED_IRON_TRAPDOOR = registerBlock("waxed_weathered_iron_trapdoor",
		key -> new WaxedIronTrapdoorBlock(WEATHERED_IRON_TRAPDOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_OXIDIZED_IRON_TRAPDOOR = registerBlock("waxed_oxidized_iron_trapdoor",
		key -> new WaxedIronTrapdoorBlock(OXIDIZED_IRON_TRAPDOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// Waxed vanilla iron trapdoor - looks like vanilla but won't oxidize in Outerworld
	public static final Block WAXED_IRON_TRAPDOOR = registerBlock("waxed_iron_trapdoor",
		key -> new WaxedIronTrapdoorBlock(UNAFFECTED_IRON_TRAPDOOR, net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// Iron Grate blocks
	public static final Block IRON_GRATE = registerBlock(
		"iron_grate",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block EXPOSED_IRON_GRATE = registerBlock(
		"exposed_iron_grate",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_IRON_GRATE = registerBlock(
		"weathered_iron_grate",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_IRON_GRATE = registerBlock(
		"oxidized_iron_grate",
		key -> new OxidizableIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block WAXED_IRON_GRATE = registerBlock("waxed_iron_grate",
		key -> new WaxedIronBlock(IRON_GRATE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_EXPOSED_IRON_GRATE = registerBlock("waxed_exposed_iron_grate",
		key -> new WaxedIronBlock(EXPOSED_IRON_GRATE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_WEATHERED_IRON_GRATE = registerBlock("waxed_weathered_iron_grate",
		key -> new WaxedIronBlock(WEATHERED_IRON_GRATE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_OXIDIZED_IRON_GRATE = registerBlock("waxed_oxidized_iron_grate",
		key -> new WaxedIronBlock(OXIDIZED_IRON_GRATE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// Iron Bars blocks
	public static final Block UNAFFECTED_IRON_BARS = registerBlockOnly(
		"unaffected_iron_bars",
		key -> new OxidizableIronBarsBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block EXPOSED_IRON_BARS = registerBlock(
		"exposed_iron_bars",
		key -> new OxidizableIronBarsBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_IRON_BARS = registerBlock(
		"weathered_iron_bars",
		key -> new OxidizableIronBarsBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_IRON_BARS = registerBlock(
		"oxidized_iron_bars",
		key -> new OxidizableIronBarsBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block WAXED_EXPOSED_IRON_BARS = registerBlock("waxed_exposed_iron_bars",
		key -> new WaxedIronBarsBlock(EXPOSED_IRON_BARS, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_WEATHERED_IRON_BARS = registerBlock("waxed_weathered_iron_bars",
		key -> new WaxedIronBarsBlock(WEATHERED_IRON_BARS, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_OXIDIZED_IRON_BARS = registerBlock("waxed_oxidized_iron_bars",
		key -> new WaxedIronBarsBlock(OXIDIZED_IRON_BARS, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// Waxed vanilla iron bars - looks like vanilla but won't oxidize in Outerworld
	public static final Block WAXED_IRON_BARS = registerBlock("waxed_iron_bars",
		key -> new WaxedIronBarsBlock(UNAFFECTED_IRON_BARS, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block EXPOSED_LANTERN = registerLitIronLantern(
		"exposed_lantern",
		WeatheringCopper.WeatherState.EXPOSED,
		Blocks.COPPER_LANTERN.weathering().exposed()
	);
	public static final Block WEATHERED_LANTERN = registerLitIronLantern(
		"weathered_lantern",
		WeatheringCopper.WeatherState.WEATHERED,
		Blocks.COPPER_LANTERN.weathering().weathered()
	);
	public static final Block OXIDIZED_LANTERN = registerLitIronLantern(
		"oxidized_lantern",
		WeatheringCopper.WeatherState.OXIDIZED,
		Blocks.COPPER_LANTERN.weathering().oxidized()
	);
	public static final Block WAXED_LANTERN = registerWaxedLantern("waxed_lantern", Blocks.LANTERN);
	public static final Block WAXED_EXPOSED_LANTERN = registerWaxedLantern("waxed_exposed_lantern", Blocks.COPPER_LANTERN.weathering().exposed());
	public static final Block WAXED_WEATHERED_LANTERN = registerWaxedLantern("waxed_weathered_lantern", Blocks.COPPER_LANTERN.weathering().weathered());
	public static final Block WAXED_OXIDIZED_LANTERN = registerWaxedLantern("waxed_oxidized_lantern", Blocks.COPPER_LANTERN.weathering().oxidized());

	// ============= IRON GOLEM STATUES =============
	// Petrified iron golems that can be scraped to de-oxidize and eventually reanimate
	
	public static final Block IRON_GOLEM_STATUE = registerBlock(
		"iron_golem_statue",
		key -> new IronGolemStatueBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block EXPOSED_IRON_GOLEM_STATUE = registerBlock(
		"exposed_iron_golem_statue",
		key -> new IronGolemStatueBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block WEATHERED_IRON_GOLEM_STATUE = registerBlock(
		"weathered_iron_golem_statue",
		key -> new IronGolemStatueBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	public static final Block OXIDIZED_IRON_GOLEM_STATUE = registerBlock(
		"oxidized_iron_golem_statue",
		key -> new IronGolemStatueBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.noOcclusion()
		)
	);

	// Waxed iron golem statues
	public static final Block WAXED_IRON_GOLEM_STATUE = registerBlock("waxed_iron_golem_statue",
		key -> new WaxedIronGolemStatueBlock(IRON_GOLEM_STATUE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_EXPOSED_IRON_GOLEM_STATUE = registerBlock("waxed_exposed_iron_golem_statue",
		key -> new WaxedIronGolemStatueBlock(EXPOSED_IRON_GOLEM_STATUE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_WEATHERED_IRON_GOLEM_STATUE = registerBlock("waxed_weathered_iron_golem_statue",
		key -> new WaxedIronGolemStatueBlock(WEATHERED_IRON_GOLEM_STATUE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	public static final Block WAXED_OXIDIZED_IRON_GOLEM_STATUE = registerBlock("waxed_oxidized_iron_golem_statue",
		key -> new WaxedIronGolemStatueBlock(OXIDIZED_IRON_GOLEM_STATUE, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

	// ============= CUT IRON BLOCKS =============
	// Oxidizable decorative cut iron blocks with slab and stair variants
	
	public static final Block CUT_IRON = registerBlock(
		"cut_iron",
		key -> new OxidizableCutIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block EXPOSED_CUT_IRON = registerBlock(
		"exposed_cut_iron",
		key -> new OxidizableCutIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_CUT_IRON = registerBlock(
		"weathered_cut_iron",
		key -> new OxidizableCutIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_CUT_IRON = registerBlock(
		"oxidized_cut_iron",
		key -> new OxidizableCutIronBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	// Waxed Cut Iron Blocks
	public static final Block WAXED_CUT_IRON = registerBlock("waxed_cut_iron",
		key -> new WaxedCutIronBlock(CUT_IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_EXPOSED_CUT_IRON = registerBlock("waxed_exposed_cut_iron",
		key -> new WaxedCutIronBlock(EXPOSED_CUT_IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_WEATHERED_CUT_IRON = registerBlock("waxed_weathered_cut_iron",
		key -> new WaxedCutIronBlock(WEATHERED_CUT_IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_OXIDIZED_CUT_IRON = registerBlock("waxed_oxidized_cut_iron",
		key -> new WaxedCutIronBlock(OXIDIZED_CUT_IRON, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	// Cut Iron Slabs
	public static final Block CUT_IRON_SLAB = registerBlock(
		"cut_iron_slab",
		key -> new OxidizableCutIronSlabBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block EXPOSED_CUT_IRON_SLAB = registerBlock(
		"exposed_cut_iron_slab",
		key -> new OxidizableCutIronSlabBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_CUT_IRON_SLAB = registerBlock(
		"weathered_cut_iron_slab",
		key -> new OxidizableCutIronSlabBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_CUT_IRON_SLAB = registerBlock(
		"oxidized_cut_iron_slab",
		key -> new OxidizableCutIronSlabBlock(
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	// Waxed Cut Iron Slabs
	public static final Block WAXED_CUT_IRON_SLAB = registerBlock("waxed_cut_iron_slab",
		key -> new WaxedCutIronSlabBlock(CUT_IRON_SLAB, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_EXPOSED_CUT_IRON_SLAB = registerBlock("waxed_exposed_cut_iron_slab",
		key -> new WaxedCutIronSlabBlock(EXPOSED_CUT_IRON_SLAB, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_WEATHERED_CUT_IRON_SLAB = registerBlock("waxed_weathered_cut_iron_slab",
		key -> new WaxedCutIronSlabBlock(WEATHERED_CUT_IRON_SLAB, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_OXIDIZED_CUT_IRON_SLAB = registerBlock("waxed_oxidized_cut_iron_slab",
		key -> new WaxedCutIronSlabBlock(OXIDIZED_CUT_IRON_SLAB, BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	// Cut Iron Stairs
	public static final Block CUT_IRON_STAIRS = registerBlock(
		"cut_iron_stairs",
		key -> new OxidizableCutIronStairsBlock(
			CUT_IRON.defaultBlockState(),
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block EXPOSED_CUT_IRON_STAIRS = registerBlock(
		"exposed_cut_iron_stairs",
		key -> new OxidizableCutIronStairsBlock(
			EXPOSED_CUT_IRON.defaultBlockState(),
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block WEATHERED_CUT_IRON_STAIRS = registerBlock(
		"weathered_cut_iron_stairs",
		key -> new OxidizableCutIronStairsBlock(
			WEATHERED_CUT_IRON.defaultBlockState(),
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
				.randomTicks()
		)
	);

	public static final Block OXIDIZED_CUT_IRON_STAIRS = registerBlock(
		"oxidized_cut_iron_stairs",
		key -> new OxidizableCutIronStairsBlock(
			OXIDIZED_CUT_IRON.defaultBlockState(),
			net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.of()
				.setId(key)
				.strength(5.0f, 6.0f)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops()
		)
	);

	// Waxed Cut Iron Stairs
	public static final Block WAXED_CUT_IRON_STAIRS = registerBlock("waxed_cut_iron_stairs",
		key -> new WaxedCutIronStairsBlock(CUT_IRON_STAIRS, CUT_IRON.defaultBlockState(), BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_EXPOSED_CUT_IRON_STAIRS = registerBlock("waxed_exposed_cut_iron_stairs",
		key -> new WaxedCutIronStairsBlock(EXPOSED_CUT_IRON_STAIRS, EXPOSED_CUT_IRON.defaultBlockState(), BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_WEATHERED_CUT_IRON_STAIRS = registerBlock("waxed_weathered_cut_iron_stairs",
		key -> new WaxedCutIronStairsBlock(WEATHERED_CUT_IRON_STAIRS, WEATHERED_CUT_IRON.defaultBlockState(), BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	public static final Block WAXED_OXIDIZED_CUT_IRON_STAIRS = registerBlock("waxed_oxidized_cut_iron_stairs",
		key -> new WaxedCutIronStairsBlock(OXIDIZED_CUT_IRON_STAIRS, OXIDIZED_CUT_IRON.defaultBlockState(), BlockBehaviour.Properties.of().setId(key).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));

	private static BlockBehaviour.Properties stoneProperties(ResourceKey<Block> key, MapColor color) {
		return BlockBehaviour.Properties.of()
			.setId(key)
			.mapColor(color)
			.strength(1.5f, 6.0f)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops();
	}

	private static BlockBehaviour.Properties nickelProperties(ResourceKey<Block> key) {
		return BlockBehaviour.Properties.of()
			.setId(key)
			.mapColor(MapColor.METAL)
			.strength(5.0f, 6.0f)
			.sound(SoundType.METAL)
			.requiresCorrectToolForDrops();
	}

	private static ResourceKey<Block> blockKey(String name) {
		return ResourceKey.create(Registries.BLOCK, OuterWorldMod.id(name));
	}

	private static ResourceKey<Item> itemKey(String name) {
		return ResourceKey.create(Registries.ITEM, OuterWorldMod.id(name));
	}

	private static Block registerFrozenLantern(String name, Block vanilla) {
		return registerBlock(
			name,
			key -> new FrozenLanternBlock(
				BlockBehaviour.Properties.ofFullCopy(vanilla).setId(key).lightLevel(state -> 0)
			)
		);
	}

	private static Block registerOxidizableFrozenLantern(
		String name,
		Block vanilla,
		WeatheringCopper.WeatherState age,
		OxidizableLanternAging.Kind kind
	) {
		return registerBlock(
			name,
			key -> {
				BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(vanilla)
					.setId(key)
					.lightLevel(state -> 0);
				if (age != WeatheringCopper.WeatherState.OXIDIZED) {
					properties = properties.randomTicks();
				}
				return new OxidizableFrozenLanternBlock(age, kind, properties);
			}
		);
	}

	private static Block registerLitIronLantern(String name, WeatheringCopper.WeatherState age, Block copperStage) {
		return registerBlock(
			name,
			key -> {
				BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(copperStage).setId(key);
				if (age != WeatheringCopper.WeatherState.OXIDIZED) {
					properties = properties.randomTicks();
				}
				return new OxidizableLanternBlock(age, OxidizableLanternAging.Kind.IRON, true, properties);
			}
		);
	}

	private static Block registerWaxedLantern(String name, Block vanilla) {
		return registerBlock(
			name,
			key -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(vanilla).setId(key))
		);
	}

	private static Block registerStone(String name, MapColor color) {
		return registerBlock(name, key -> new Block(stoneProperties(key, color)));
	}

	private static Block registerStairs(String name, Block base) {
		return registerBlock(name, key -> new StairBlock(base.defaultBlockState(), stoneProperties(key, base.defaultMapColor())));
	}

	private static Block registerSlab(String name, Block base) {
		return registerBlock(name, key -> new SlabBlock(stoneProperties(key, base.defaultMapColor())));
	}

	private static Block registerWall(String name, Block base) {
		return registerBlock(name, key -> new WallBlock(stoneProperties(key, base.defaultMapColor())));
	}

	private static Block frozenSapling(String name, TreeGrower grower, Block vanilla) {
		return registerBlock(
			name,
			key -> new FrozenSaplingBlock(grower, BlockBehaviour.Properties.ofFullCopy(vanilla).setId(key))
		);
	}

	private static Block registerBlock(String name, Function<ResourceKey<Block>, Block> factory) {
		return registerBlock(name, factory, properties -> properties);
	}

	private static Block registerBlock(String name, Function<ResourceKey<Block>, Block> factory, Function<Item.Properties, Item.Properties> itemProperties) {
		Identifier id = OuterWorldMod.id(name);
		ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
		Block block = factory.apply(key);
		Registry.register(BuiltInRegistries.BLOCK, id, block);

		ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
		BlockItem item = new BlockItem(block, itemProperties.apply(new Item.Properties().setId(itemKey)));
		Registry.register(BuiltInRegistries.ITEM, id, item);
		return block;
	}

	private static Block registerBlockOnly(String name, Function<ResourceKey<Block>, Block> factory) {
		Identifier id = OuterWorldMod.id(name);
		ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
		Block block = factory.apply(key);
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		return block;
	}

	private static Block registerFireproofBlock(String name, Function<ResourceKey<Block>, Block> factory) {
		Identifier id = OuterWorldMod.id(name);
		ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
		Block block = factory.apply(key);
		Registry.register(BuiltInRegistries.BLOCK, id, block);

		ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
		BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).fireResistant());
		Registry.register(BuiltInRegistries.ITEM, id, item);
		return block;
	}

	public static void registerModBlocks() {
		OuterWorldMod.LOGGER.info("Registering blocks for {}", OuterWorldMod.MOD_ID);

		BlockTransformerHelper.registerTilling(REGOLITH, REGOLITH_FARMLAND);

		if (ICE instanceof AgingIceBlock ice) {
			ice.setNextStage(PACKED_ICE);
		}
		if (PACKED_ICE instanceof AgingIceBlock packedIce) {
			packedIce.setNextStage(BLUE_ICE);
		}

		if (UNAFFECTED_IRON instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_IRON);
		if (EXPOSED_IRON instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON);
		if (WEATHERED_IRON instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON);
		if (OXIDIZED_IRON instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON);

		if (IRON_BULB instanceof OxidizableIronBulbBlock b) b.setWaxedVersion(WAXED_IRON_BULB);
		if (EXPOSED_IRON_BULB instanceof OxidizableIronBulbBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_BULB);
		if (WEATHERED_IRON_BULB instanceof OxidizableIronBulbBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_BULB);
		if (OXIDIZED_IRON_BULB instanceof OxidizableIronBulbBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_BULB);

		if (UNAFFECTED_IRON_CHAIN instanceof OxidizableIronChainBlock b) b.setWaxedVersion(WAXED_IRON_CHAIN);
		if (EXPOSED_IRON_CHAIN instanceof OxidizableIronChainBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_CHAIN);
		if (WEATHERED_IRON_CHAIN instanceof OxidizableIronChainBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_CHAIN);
		if (OXIDIZED_IRON_CHAIN instanceof OxidizableIronChainBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_CHAIN);

		if (UNAFFECTED_IRON_DOOR instanceof OxidizableIronDoorBlock b) b.setWaxedVersion(WAXED_IRON_DOOR);
		if (EXPOSED_IRON_DOOR instanceof OxidizableIronDoorBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_DOOR);
		if (WEATHERED_IRON_DOOR instanceof OxidizableIronDoorBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_DOOR);
		if (OXIDIZED_IRON_DOOR instanceof OxidizableIronDoorBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_DOOR);

		if (UNAFFECTED_IRON_TRAPDOOR instanceof OxidizableIronTrapdoorBlock b) b.setWaxedVersion(WAXED_IRON_TRAPDOOR);
		if (EXPOSED_IRON_TRAPDOOR instanceof OxidizableIronTrapdoorBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_TRAPDOOR);
		if (WEATHERED_IRON_TRAPDOOR instanceof OxidizableIronTrapdoorBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_TRAPDOOR);
		if (OXIDIZED_IRON_TRAPDOOR instanceof OxidizableIronTrapdoorBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_TRAPDOOR);

		if (IRON_GRATE instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_IRON_GRATE);
		if (EXPOSED_IRON_GRATE instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_GRATE);
		if (WEATHERED_IRON_GRATE instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_GRATE);
		if (OXIDIZED_IRON_GRATE instanceof OxidizableIronBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_GRATE);

		if (UNAFFECTED_IRON_BARS instanceof OxidizableIronBarsBlock b) b.setWaxedVersion(WAXED_IRON_BARS);
		if (EXPOSED_IRON_BARS instanceof OxidizableIronBarsBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_BARS);
		if (WEATHERED_IRON_BARS instanceof OxidizableIronBarsBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_BARS);
		if (OXIDIZED_IRON_BARS instanceof OxidizableIronBarsBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_BARS);

		if (IRON_GOLEM_STATUE instanceof IronGolemStatueBlock b) b.setWaxedVersion(WAXED_IRON_GOLEM_STATUE);
		if (EXPOSED_IRON_GOLEM_STATUE instanceof IronGolemStatueBlock b) b.setWaxedVersion(WAXED_EXPOSED_IRON_GOLEM_STATUE);
		if (WEATHERED_IRON_GOLEM_STATUE instanceof IronGolemStatueBlock b) b.setWaxedVersion(WAXED_WEATHERED_IRON_GOLEM_STATUE);
		if (OXIDIZED_IRON_GOLEM_STATUE instanceof IronGolemStatueBlock b) b.setWaxedVersion(WAXED_OXIDIZED_IRON_GOLEM_STATUE);

		if (CUT_IRON instanceof OxidizableCutIronBlock b) b.setWaxedVersion(WAXED_CUT_IRON);
		if (EXPOSED_CUT_IRON instanceof OxidizableCutIronBlock b) b.setWaxedVersion(WAXED_EXPOSED_CUT_IRON);
		if (WEATHERED_CUT_IRON instanceof OxidizableCutIronBlock b) b.setWaxedVersion(WAXED_WEATHERED_CUT_IRON);
		if (OXIDIZED_CUT_IRON instanceof OxidizableCutIronBlock b) b.setWaxedVersion(WAXED_OXIDIZED_CUT_IRON);

		if (CUT_IRON_SLAB instanceof OxidizableCutIronSlabBlock b) b.setWaxedVersion(WAXED_CUT_IRON_SLAB);
		if (EXPOSED_CUT_IRON_SLAB instanceof OxidizableCutIronSlabBlock b) b.setWaxedVersion(WAXED_EXPOSED_CUT_IRON_SLAB);
		if (WEATHERED_CUT_IRON_SLAB instanceof OxidizableCutIronSlabBlock b) b.setWaxedVersion(WAXED_WEATHERED_CUT_IRON_SLAB);
		if (OXIDIZED_CUT_IRON_SLAB instanceof OxidizableCutIronSlabBlock b) b.setWaxedVersion(WAXED_OXIDIZED_CUT_IRON_SLAB);

		if (CUT_IRON_STAIRS instanceof OxidizableCutIronStairsBlock b) b.setWaxedVersion(WAXED_CUT_IRON_STAIRS);
		if (EXPOSED_CUT_IRON_STAIRS instanceof OxidizableCutIronStairsBlock b) b.setWaxedVersion(WAXED_EXPOSED_CUT_IRON_STAIRS);
		if (WEATHERED_CUT_IRON_STAIRS instanceof OxidizableCutIronStairsBlock b) b.setWaxedVersion(WAXED_WEATHERED_CUT_IRON_STAIRS);
		if (OXIDIZED_CUT_IRON_STAIRS instanceof OxidizableCutIronStairsBlock b) b.setWaxedVersion(WAXED_OXIDIZED_CUT_IRON_STAIRS);

		OxidizableBlocksRegistry.registerNextStage(UNAFFECTED_IRON, EXPOSED_IRON);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON, WEATHERED_IRON);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON, OXIDIZED_IRON);

		OxidizableBlocksRegistry.registerNextStage(IRON_BULB, EXPOSED_IRON_BULB);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_BULB, WEATHERED_IRON_BULB);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_BULB, OXIDIZED_IRON_BULB);

		OxidizableBlocksRegistry.registerNextStage(UNAFFECTED_IRON_CHAIN, EXPOSED_IRON_CHAIN);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_CHAIN, WEATHERED_IRON_CHAIN);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_CHAIN, OXIDIZED_IRON_CHAIN);

		OxidizableBlocksRegistry.registerNextStage(UNAFFECTED_IRON_DOOR, EXPOSED_IRON_DOOR);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_DOOR, WEATHERED_IRON_DOOR);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_DOOR, OXIDIZED_IRON_DOOR);

		OxidizableBlocksRegistry.registerNextStage(UNAFFECTED_IRON_TRAPDOOR, EXPOSED_IRON_TRAPDOOR);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_TRAPDOOR, WEATHERED_IRON_TRAPDOOR);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_TRAPDOOR, OXIDIZED_IRON_TRAPDOOR);

		OxidizableBlocksRegistry.registerNextStage(IRON_GRATE, EXPOSED_IRON_GRATE);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_GRATE, WEATHERED_IRON_GRATE);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_GRATE, OXIDIZED_IRON_GRATE);

		OxidizableBlocksRegistry.registerNextStage(UNAFFECTED_IRON_BARS, EXPOSED_IRON_BARS);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_BARS, WEATHERED_IRON_BARS);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_BARS, OXIDIZED_IRON_BARS);

		OxidizableBlocksRegistry.registerNextStage(IRON_GOLEM_STATUE, EXPOSED_IRON_GOLEM_STATUE);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_IRON_GOLEM_STATUE, WEATHERED_IRON_GOLEM_STATUE);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_IRON_GOLEM_STATUE, OXIDIZED_IRON_GOLEM_STATUE);

		OxidizableBlocksRegistry.registerNextStage(CUT_IRON, EXPOSED_CUT_IRON);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_CUT_IRON, WEATHERED_CUT_IRON);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_CUT_IRON, OXIDIZED_CUT_IRON);

		OxidizableBlocksRegistry.registerNextStage(CUT_IRON_SLAB, EXPOSED_CUT_IRON_SLAB);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_CUT_IRON_SLAB, WEATHERED_CUT_IRON_SLAB);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_CUT_IRON_SLAB, OXIDIZED_CUT_IRON_SLAB);

		OxidizableBlocksRegistry.registerNextStage(CUT_IRON_STAIRS, EXPOSED_CUT_IRON_STAIRS);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_CUT_IRON_STAIRS, WEATHERED_CUT_IRON_STAIRS);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_CUT_IRON_STAIRS, OXIDIZED_CUT_IRON_STAIRS);

		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON, WAXED_EXPOSED_IRON);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON, WAXED_WEATHERED_IRON);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON, WAXED_OXIDIZED_IRON);

		OxidizableBlocksRegistry.registerWaxable(IRON_BULB, WAXED_IRON_BULB);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_BULB, WAXED_EXPOSED_IRON_BULB);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_BULB, WAXED_WEATHERED_IRON_BULB);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_BULB, WAXED_OXIDIZED_IRON_BULB);

		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_CHAIN, WAXED_EXPOSED_IRON_CHAIN);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_CHAIN, WAXED_WEATHERED_IRON_CHAIN);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_CHAIN, WAXED_OXIDIZED_IRON_CHAIN);

		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_DOOR, WAXED_EXPOSED_IRON_DOOR);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_DOOR, WAXED_WEATHERED_IRON_DOOR);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_DOOR, WAXED_OXIDIZED_IRON_DOOR);

		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_TRAPDOOR, WAXED_EXPOSED_IRON_TRAPDOOR);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_TRAPDOOR, WAXED_WEATHERED_IRON_TRAPDOOR);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_TRAPDOOR, WAXED_OXIDIZED_IRON_TRAPDOOR);

		OxidizableBlocksRegistry.registerWaxable(IRON_GRATE, WAXED_IRON_GRATE);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_GRATE, WAXED_EXPOSED_IRON_GRATE);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_GRATE, WAXED_WEATHERED_IRON_GRATE);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_GRATE, WAXED_OXIDIZED_IRON_GRATE);

		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_BARS, WAXED_EXPOSED_IRON_BARS);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_BARS, WAXED_WEATHERED_IRON_BARS);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_BARS, WAXED_OXIDIZED_IRON_BARS);

		OxidizableBlocksRegistry.registerWaxable(IRON_GOLEM_STATUE, WAXED_IRON_GOLEM_STATUE);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_IRON_GOLEM_STATUE, WAXED_EXPOSED_IRON_GOLEM_STATUE);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_IRON_GOLEM_STATUE, WAXED_WEATHERED_IRON_GOLEM_STATUE);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_IRON_GOLEM_STATUE, WAXED_OXIDIZED_IRON_GOLEM_STATUE);

		OxidizableBlocksRegistry.registerWaxable(CUT_IRON, WAXED_CUT_IRON);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_CUT_IRON, WAXED_EXPOSED_CUT_IRON);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_CUT_IRON, WAXED_WEATHERED_CUT_IRON);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_CUT_IRON, WAXED_OXIDIZED_CUT_IRON);

		OxidizableBlocksRegistry.registerWaxable(CUT_IRON_SLAB, WAXED_CUT_IRON_SLAB);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_CUT_IRON_SLAB, WAXED_EXPOSED_CUT_IRON_SLAB);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_CUT_IRON_SLAB, WAXED_WEATHERED_CUT_IRON_SLAB);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_CUT_IRON_SLAB, WAXED_OXIDIZED_CUT_IRON_SLAB);

		OxidizableBlocksRegistry.registerWaxable(CUT_IRON_STAIRS, WAXED_CUT_IRON_STAIRS);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_CUT_IRON_STAIRS, WAXED_EXPOSED_CUT_IRON_STAIRS);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_CUT_IRON_STAIRS, WAXED_WEATHERED_CUT_IRON_STAIRS);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_CUT_IRON_STAIRS, WAXED_OXIDIZED_CUT_IRON_STAIRS);

		OxidizableBlocksRegistry.registerWaxable(Blocks.IRON_BLOCK, WAXED_IRON);
		OxidizableBlocksRegistry.registerWaxable(Blocks.IRON_DOOR, WAXED_IRON_DOOR);
		OxidizableBlocksRegistry.registerWaxable(Blocks.IRON_TRAPDOOR, WAXED_IRON_TRAPDOOR);
		OxidizableBlocksRegistry.registerWaxable(Blocks.IRON_BARS, WAXED_IRON_BARS);
		OxidizableBlocksRegistry.registerWaxable(Blocks.IRON_CHAIN, WAXED_IRON_CHAIN);

		OxidizableBlocksRegistry.registerNextStage(FROZEN_LANTERN, FROZEN_EXPOSED_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(FROZEN_EXPOSED_LANTERN, FROZEN_WEATHERED_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(FROZEN_WEATHERED_LANTERN, FROZEN_OXIDIZED_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(Blocks.LANTERN, EXPOSED_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(EXPOSED_LANTERN, WEATHERED_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(WEATHERED_LANTERN, OXIDIZED_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(FROZEN_COPPER_LANTERN, FROZEN_EXPOSED_COPPER_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(FROZEN_EXPOSED_COPPER_LANTERN, FROZEN_WEATHERED_COPPER_LANTERN);
		OxidizableBlocksRegistry.registerNextStage(FROZEN_WEATHERED_COPPER_LANTERN, FROZEN_OXIDIZED_COPPER_LANTERN);

		OxidizableBlocksRegistry.registerWaxable(Blocks.LANTERN, WAXED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(EXPOSED_LANTERN, WAXED_EXPOSED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(WEATHERED_LANTERN, WAXED_WEATHERED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(OXIDIZED_LANTERN, WAXED_OXIDIZED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_LANTERN, FROZEN_WAXED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_EXPOSED_LANTERN, FROZEN_WAXED_EXPOSED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_WEATHERED_LANTERN, FROZEN_WAXED_WEATHERED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_OXIDIZED_LANTERN, FROZEN_WAXED_OXIDIZED_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_COPPER_LANTERN, FROZEN_WAXED_COPPER_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_EXPOSED_COPPER_LANTERN, FROZEN_WAXED_EXPOSED_COPPER_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_WEATHERED_COPPER_LANTERN, FROZEN_WAXED_WEATHERED_COPPER_LANTERN);
		OxidizableBlocksRegistry.registerWaxable(FROZEN_OXIDIZED_COPPER_LANTERN, FROZEN_WAXED_OXIDIZED_COPPER_LANTERN);

		OuterWorldMod.LOGGER.info("Registered oxidizable iron blocks");
	}
}