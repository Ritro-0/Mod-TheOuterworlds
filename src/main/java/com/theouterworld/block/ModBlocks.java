package com.theouterworld.block;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.registry.ModTrimMaterials;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

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
				.pushReaction(PushReaction.DESTROY)
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

	public static final Block RAW_OLIVINE = registerBlock(
		"raw_olivine",
		key -> new Block(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_GREEN)
				.strength(1.5f, 1.5f)
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
				.strength(1.5f, 1.5f)
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
				.strength(1.5f)
				.lightLevel(state -> 1)
				.pushReaction(PushReaction.DESTROY)
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
				.strength(1.5f)
				.lightLevel(state -> 2)
				.pushReaction(PushReaction.DESTROY)
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
				.strength(1.5f)
				.lightLevel(state -> 5)
				.pushReaction(PushReaction.DESTROY)
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
				.pushReaction(PushReaction.DESTROY)
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
				.pushReaction(PushReaction.DESTROY)
				.overrideDescription("block.theouterworlds.olivine_torch")
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
				.isViewBlocking((state, level, pos) -> false)
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

	public static final Block KNAPPING_TABLE = registerBlock(
		"knapping_table",
		key -> new KnappingTableBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.STONE)
				.strength(2.5f, 2.5f)
				.sound(SoundType.WOOD)
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
				.pushReaction(PushReaction.DESTROY)
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
				.pushReaction(PushReaction.DESTROY)
				.noLootTable()
				.liquid()
				.randomTicks()
				.sound(SoundType.EMPTY)
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
				.noCollision()
				.pushReaction(PushReaction.DESTROY)
		)
	);

	public static final Block RIFT = registerBlock(
		"rift",
		key -> new RiftBlock(
			BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_PURPLE)
				.noCollision()
				.noOcclusion()
				.strength(0.3f)
				.lightLevel(state -> 11)
				.sound(SoundType.GLASS)
				.pushReaction(PushReaction.BLOCK)
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
				.lightLevel(state -> state.getValue(RiftPadBlock.POWERED) ? 11 : 4)
				.pushReaction(PushReaction.BLOCK)
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
				.pushReaction(PushReaction.DESTROY)
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
				.pushReaction(PushReaction.BLOCK)
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

		OuterWorldMod.LOGGER.info("Registered oxidizable iron blocks");
	}
}