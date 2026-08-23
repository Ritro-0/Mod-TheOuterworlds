package com.theouterworld.block;

import com.theouterworld.OuterWorldMod;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.function.Function;

public class CorrodedBlocks {
	private static final WeatheringCopper.WeatherState UNAFFECTED = WeatheringCopper.WeatherState.UNAFFECTED;
	private static final WeatheringCopper.WeatherState EXPOSED = WeatheringCopper.WeatherState.EXPOSED;
	private static final WeatheringCopper.WeatherState WEATHERED = WeatheringCopper.WeatherState.WEATHERED;
	private static final WeatheringCopper.WeatherState OXIDIZED = WeatheringCopper.WeatherState.OXIDIZED;

	public static final Block CORRODED_COPPER = copper("corroded_copper", UNAFFECTED, Blocks.COPPER_BLOCK.weathering().unaffected(), true);
	public static final Block CORRODED_EXPOSED_COPPER = copper("corroded_exposed_copper", EXPOSED, Blocks.COPPER_BLOCK.weathering().exposed(), true);
	public static final Block CORRODED_WEATHERED_COPPER = copper("corroded_weathered_copper", WEATHERED, Blocks.COPPER_BLOCK.weathering().weathered(), true);
	public static final Block CORRODED_OXIDIZED_COPPER = copper("corroded_oxidized_copper", OXIDIZED, Blocks.COPPER_BLOCK.weathering().oxidized(), false);

	public static final Block CORRODED_CUT_COPPER = copper("corroded_cut_copper", UNAFFECTED, Blocks.CUT_COPPER.weathering().unaffected(), true);
	public static final Block CORRODED_EXPOSED_CUT_COPPER = copper("corroded_exposed_cut_copper", EXPOSED, Blocks.CUT_COPPER.weathering().exposed(), true);
	public static final Block CORRODED_WEATHERED_CUT_COPPER = copper("corroded_weathered_cut_copper", WEATHERED, Blocks.CUT_COPPER.weathering().weathered(), true);
	public static final Block CORRODED_OXIDIZED_CUT_COPPER = copper("corroded_oxidized_cut_copper", OXIDIZED, Blocks.CUT_COPPER.weathering().oxidized(), false);

	public static final Block CORRODED_CUT_COPPER_STAIRS = copperStairs("corroded_cut_copper_stairs", UNAFFECTED, CORRODED_CUT_COPPER, Blocks.CUT_COPPER_STAIRS.weathering().unaffected(), true);
	public static final Block CORRODED_EXPOSED_CUT_COPPER_STAIRS = copperStairs("corroded_exposed_cut_copper_stairs", EXPOSED, CORRODED_EXPOSED_CUT_COPPER, Blocks.CUT_COPPER_STAIRS.weathering().exposed(), true);
	public static final Block CORRODED_WEATHERED_CUT_COPPER_STAIRS = copperStairs("corroded_weathered_cut_copper_stairs", WEATHERED, CORRODED_WEATHERED_CUT_COPPER, Blocks.CUT_COPPER_STAIRS.weathering().weathered(), true);
	public static final Block CORRODED_OXIDIZED_CUT_COPPER_STAIRS = copperStairs("corroded_oxidized_cut_copper_stairs", OXIDIZED, CORRODED_OXIDIZED_CUT_COPPER, Blocks.CUT_COPPER_STAIRS.weathering().oxidized(), false);

	public static final Block CORRODED_CUT_COPPER_SLAB = copperSlab("corroded_cut_copper_slab", UNAFFECTED, Blocks.CUT_COPPER_SLAB.weathering().unaffected(), true);
	public static final Block CORRODED_EXPOSED_CUT_COPPER_SLAB = copperSlab("corroded_exposed_cut_copper_slab", EXPOSED, Blocks.CUT_COPPER_SLAB.weathering().exposed(), true);
	public static final Block CORRODED_WEATHERED_CUT_COPPER_SLAB = copperSlab("corroded_weathered_cut_copper_slab", WEATHERED, Blocks.CUT_COPPER_SLAB.weathering().weathered(), true);
	public static final Block CORRODED_OXIDIZED_CUT_COPPER_SLAB = copperSlab("corroded_oxidized_cut_copper_slab", OXIDIZED, Blocks.CUT_COPPER_SLAB.weathering().oxidized(), false);

	public static final Block CORRODED_IRON = iron("corroded_iron", UNAFFECTED, true, false);
	public static final Block CORRODED_EXPOSED_IRON = iron("corroded_exposed_iron", EXPOSED, true, false);
	public static final Block CORRODED_WEATHERED_IRON = iron("corroded_weathered_iron", WEATHERED, true, false);
	public static final Block CORRODED_OXIDIZED_IRON = iron("corroded_oxidized_iron", OXIDIZED, false, false);

	public static final Block CORRODED_CUT_IRON = cutIron("corroded_cut_iron", UNAFFECTED, true);
	public static final Block CORRODED_EXPOSED_CUT_IRON = cutIron("corroded_exposed_cut_iron", EXPOSED, true);
	public static final Block CORRODED_WEATHERED_CUT_IRON = cutIron("corroded_weathered_cut_iron", WEATHERED, true);
	public static final Block CORRODED_OXIDIZED_CUT_IRON = cutIron("corroded_oxidized_cut_iron", OXIDIZED, false);

	public static final Block CORRODED_CUT_IRON_SLAB = cutIronSlab("corroded_cut_iron_slab", UNAFFECTED, true);
	public static final Block CORRODED_EXPOSED_CUT_IRON_SLAB = cutIronSlab("corroded_exposed_cut_iron_slab", EXPOSED, true);
	public static final Block CORRODED_WEATHERED_CUT_IRON_SLAB = cutIronSlab("corroded_weathered_cut_iron_slab", WEATHERED, true);
	public static final Block CORRODED_OXIDIZED_CUT_IRON_SLAB = cutIronSlab("corroded_oxidized_cut_iron_slab", OXIDIZED, false);

	public static final Block CORRODED_CUT_IRON_STAIRS = cutIronStairs("corroded_cut_iron_stairs", UNAFFECTED, CORRODED_CUT_IRON, true);
	public static final Block CORRODED_EXPOSED_CUT_IRON_STAIRS = cutIronStairs("corroded_exposed_cut_iron_stairs", EXPOSED, CORRODED_EXPOSED_CUT_IRON, true);
	public static final Block CORRODED_WEATHERED_CUT_IRON_STAIRS = cutIronStairs("corroded_weathered_cut_iron_stairs", WEATHERED, CORRODED_WEATHERED_CUT_IRON, true);
	public static final Block CORRODED_OXIDIZED_CUT_IRON_STAIRS = cutIronStairs("corroded_oxidized_cut_iron_stairs", OXIDIZED, CORRODED_OXIDIZED_CUT_IRON, false);

	public static final Block CORRODED_IRON_GRATE = iron("corroded_iron_grate", UNAFFECTED, true, true);
	public static final Block CORRODED_EXPOSED_IRON_GRATE = iron("corroded_exposed_iron_grate", EXPOSED, true, true);
	public static final Block CORRODED_WEATHERED_IRON_GRATE = iron("corroded_weathered_iron_grate", WEATHERED, true, true);
	public static final Block CORRODED_OXIDIZED_IRON_GRATE = iron("corroded_oxidized_iron_grate", OXIDIZED, false, true);

	public static final Block CORRODED_IRON_BULB = ironBulb("corroded_iron_bulb", UNAFFECTED, 15, true);
	public static final Block CORRODED_EXPOSED_IRON_BULB = ironBulb("corroded_exposed_iron_bulb", EXPOSED, 12, true);
	public static final Block CORRODED_WEATHERED_IRON_BULB = ironBulb("corroded_weathered_iron_bulb", WEATHERED, 8, true);
	public static final Block CORRODED_OXIDIZED_IRON_BULB = ironBulb("corroded_oxidized_iron_bulb", OXIDIZED, 4, false);

	public static final Block CORRODED_IRON_DOOR = ironDoor("corroded_iron_door", UNAFFECTED, true);
	public static final Block CORRODED_EXPOSED_IRON_DOOR = ironDoor("corroded_exposed_iron_door", EXPOSED, true);
	public static final Block CORRODED_WEATHERED_IRON_DOOR = ironDoor("corroded_weathered_iron_door", WEATHERED, true);
	public static final Block CORRODED_OXIDIZED_IRON_DOOR = ironDoor("corroded_oxidized_iron_door", OXIDIZED, false);

	public static final Block CORRODED_IRON_TRAPDOOR = ironTrapdoor("corroded_iron_trapdoor", UNAFFECTED, true);
	public static final Block CORRODED_EXPOSED_IRON_TRAPDOOR = ironTrapdoor("corroded_exposed_iron_trapdoor", EXPOSED, true);
	public static final Block CORRODED_WEATHERED_IRON_TRAPDOOR = ironTrapdoor("corroded_weathered_iron_trapdoor", WEATHERED, true);
	public static final Block CORRODED_OXIDIZED_IRON_TRAPDOOR = ironTrapdoor("corroded_oxidized_iron_trapdoor", OXIDIZED, false);

	public static final Block CORRODED_IRON_CHAIN = ironChain("corroded_iron_chain", UNAFFECTED, true);
	public static final Block CORRODED_EXPOSED_IRON_CHAIN = ironChain("corroded_exposed_iron_chain", EXPOSED, true);
	public static final Block CORRODED_WEATHERED_IRON_CHAIN = ironChain("corroded_weathered_iron_chain", WEATHERED, true);
	public static final Block CORRODED_OXIDIZED_IRON_CHAIN = ironChain("corroded_oxidized_iron_chain", OXIDIZED, false);

	public static final Block CORRODED_IRON_BARS = ironBars("corroded_iron_bars", UNAFFECTED, true);
	public static final Block CORRODED_EXPOSED_IRON_BARS = ironBars("corroded_exposed_iron_bars", EXPOSED, true);
	public static final Block CORRODED_WEATHERED_IRON_BARS = ironBars("corroded_weathered_iron_bars", WEATHERED, true);
	public static final Block CORRODED_OXIDIZED_IRON_BARS = ironBars("corroded_oxidized_iron_bars", OXIDIZED, false);

	private static Block copper(String name, WeatheringCopper.WeatherState state, Block vanilla, boolean tick) {
		return register(name, key -> new FastWeatheringCopperBlock(state, copy(vanilla, key, tick)));
	}

	private static Block copperStairs(String name, WeatheringCopper.WeatherState state, Block base, Block vanilla, boolean tick) {
		return register(name, key -> new FastWeatheringCopperStairsBlock(state, base.defaultBlockState(), copy(vanilla, key, tick)));
	}

	private static Block copperSlab(String name, WeatheringCopper.WeatherState state, Block vanilla, boolean tick) {
		return register(name, key -> new FastWeatheringCopperSlabBlock(state, copy(vanilla, key, tick)));
	}

	private static Block iron(String name, WeatheringCopper.WeatherState state, boolean tick, boolean grate) {
		return register(name, key -> new OxidizableIronBlock(state, metal(key, tick, grate)));
	}

	private static Block cutIron(String name, WeatheringCopper.WeatherState state, boolean tick) {
		return register(name, key -> new OxidizableCutIronBlock(state, metal(key, tick, false)));
	}

	private static Block cutIronSlab(String name, WeatheringCopper.WeatherState state, boolean tick) {
		return register(name, key -> new OxidizableCutIronSlabBlock(state, metal(key, tick, false)));
	}

	private static Block cutIronStairs(String name, WeatheringCopper.WeatherState state, Block base, boolean tick) {
		return register(name, key -> new OxidizableCutIronStairsBlock(base.defaultBlockState(), state, metal(key, tick, false)));
	}

	private static Block ironBulb(String name, WeatheringCopper.WeatherState state, int litLight, boolean tick) {
		return register(name, key -> new OxidizableIronBulbBlock(state, metal(key, tick, false).strength(3.0f, 3.0f).lightLevel(s -> s.getValue(OxidizableIronBulbBlock.LIT) ? litLight : 0)));
	}

	private static Block ironDoor(String name, WeatheringCopper.WeatherState state, boolean tick) {
		return register(name, key -> new OxidizableIronDoorBlock(state, BlockSetType.IRON, metal(key, tick, true)));
	}

	private static Block ironTrapdoor(String name, WeatheringCopper.WeatherState state, boolean tick) {
		return register(name, key -> new OxidizableIronTrapdoorBlock(state, BlockSetType.IRON, metal(key, tick, true)));
	}

	private static Block ironChain(String name, WeatheringCopper.WeatherState state, boolean tick) {
		return register(name, key -> new OxidizableIronChainBlock(state, metal(key, tick, true).sound(SoundType.CHAIN)));
	}

	private static Block ironBars(String name, WeatheringCopper.WeatherState state, boolean tick) {
		return register(name, key -> new OxidizableIronBarsBlock(state, metal(key, tick, true)));
	}

	private static BlockBehaviour.Properties copy(Block vanilla, ResourceKey<Block> key, boolean tick) {
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(vanilla).setId(key);
		return tick ? properties.randomTicks() : properties;
	}

	private static BlockBehaviour.Properties metal(ResourceKey<Block> key, boolean tick, boolean noOcclusion) {
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
			.setId(key)
			.strength(5.0f, 6.0f)
			.sound(SoundType.METAL)
			.requiresCorrectToolForDrops();
		if (noOcclusion) {
			properties = properties.noOcclusion();
		}
		return tick ? properties.randomTicks() : properties;
	}

	private static Block register(String name, Function<ResourceKey<Block>, Block> factory) {
		Identifier id = OuterWorldMod.id(name);
		ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
		Block block = factory.apply(key);
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
		Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().setId(itemKey)));
		return block;
	}

	public static void register() {
		chain(CORRODED_COPPER, CORRODED_EXPOSED_COPPER, CORRODED_WEATHERED_COPPER, CORRODED_OXIDIZED_COPPER);
		chain(CORRODED_CUT_COPPER, CORRODED_EXPOSED_CUT_COPPER, CORRODED_WEATHERED_CUT_COPPER, CORRODED_OXIDIZED_CUT_COPPER);
		chain(CORRODED_CUT_COPPER_STAIRS, CORRODED_EXPOSED_CUT_COPPER_STAIRS, CORRODED_WEATHERED_CUT_COPPER_STAIRS, CORRODED_OXIDIZED_CUT_COPPER_STAIRS);
		chain(CORRODED_CUT_COPPER_SLAB, CORRODED_EXPOSED_CUT_COPPER_SLAB, CORRODED_WEATHERED_CUT_COPPER_SLAB, CORRODED_OXIDIZED_CUT_COPPER_SLAB);

		chain(CORRODED_IRON, CORRODED_EXPOSED_IRON, CORRODED_WEATHERED_IRON, CORRODED_OXIDIZED_IRON);
		chain(CORRODED_CUT_IRON, CORRODED_EXPOSED_CUT_IRON, CORRODED_WEATHERED_CUT_IRON, CORRODED_OXIDIZED_CUT_IRON);
		chain(CORRODED_CUT_IRON_SLAB, CORRODED_EXPOSED_CUT_IRON_SLAB, CORRODED_WEATHERED_CUT_IRON_SLAB, CORRODED_OXIDIZED_CUT_IRON_SLAB);
		chain(CORRODED_CUT_IRON_STAIRS, CORRODED_EXPOSED_CUT_IRON_STAIRS, CORRODED_WEATHERED_CUT_IRON_STAIRS, CORRODED_OXIDIZED_CUT_IRON_STAIRS);
		chain(CORRODED_IRON_GRATE, CORRODED_EXPOSED_IRON_GRATE, CORRODED_WEATHERED_IRON_GRATE, CORRODED_OXIDIZED_IRON_GRATE);
		chain(CORRODED_IRON_BULB, CORRODED_EXPOSED_IRON_BULB, CORRODED_WEATHERED_IRON_BULB, CORRODED_OXIDIZED_IRON_BULB);
		chain(CORRODED_IRON_DOOR, CORRODED_EXPOSED_IRON_DOOR, CORRODED_WEATHERED_IRON_DOOR, CORRODED_OXIDIZED_IRON_DOOR);
		chain(CORRODED_IRON_TRAPDOOR, CORRODED_EXPOSED_IRON_TRAPDOOR, CORRODED_WEATHERED_IRON_TRAPDOOR, CORRODED_OXIDIZED_IRON_TRAPDOOR);
		chain(CORRODED_IRON_CHAIN, CORRODED_EXPOSED_IRON_CHAIN, CORRODED_WEATHERED_IRON_CHAIN, CORRODED_OXIDIZED_IRON_CHAIN);
		chain(CORRODED_IRON_BARS, CORRODED_EXPOSED_IRON_BARS, CORRODED_WEATHERED_IRON_BARS, CORRODED_OXIDIZED_IRON_BARS);

		wax(CORRODED_COPPER, Blocks.COPPER_BLOCK.waxed().unaffected());
		wax(CORRODED_EXPOSED_COPPER, Blocks.COPPER_BLOCK.waxed().exposed());
		wax(CORRODED_WEATHERED_COPPER, Blocks.COPPER_BLOCK.waxed().weathered());
		wax(CORRODED_OXIDIZED_COPPER, Blocks.COPPER_BLOCK.waxed().oxidized());
		wax(CORRODED_CUT_COPPER, Blocks.CUT_COPPER.waxed().unaffected());
		wax(CORRODED_EXPOSED_CUT_COPPER, Blocks.CUT_COPPER.waxed().exposed());
		wax(CORRODED_WEATHERED_CUT_COPPER, Blocks.CUT_COPPER.waxed().weathered());
		wax(CORRODED_OXIDIZED_CUT_COPPER, Blocks.CUT_COPPER.waxed().oxidized());
		wax(CORRODED_CUT_COPPER_STAIRS, Blocks.CUT_COPPER_STAIRS.waxed().unaffected());
		wax(CORRODED_EXPOSED_CUT_COPPER_STAIRS, Blocks.CUT_COPPER_STAIRS.waxed().exposed());
		wax(CORRODED_WEATHERED_CUT_COPPER_STAIRS, Blocks.CUT_COPPER_STAIRS.waxed().weathered());
		wax(CORRODED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.CUT_COPPER_STAIRS.waxed().oxidized());
		wax(CORRODED_CUT_COPPER_SLAB, Blocks.CUT_COPPER_SLAB.waxed().unaffected());
		wax(CORRODED_EXPOSED_CUT_COPPER_SLAB, Blocks.CUT_COPPER_SLAB.waxed().exposed());
		wax(CORRODED_WEATHERED_CUT_COPPER_SLAB, Blocks.CUT_COPPER_SLAB.waxed().weathered());
		wax(CORRODED_OXIDIZED_CUT_COPPER_SLAB, Blocks.CUT_COPPER_SLAB.waxed().oxidized());

		wax(CORRODED_IRON, ModBlocks.WAXED_IRON);
		wax(CORRODED_EXPOSED_IRON, ModBlocks.WAXED_EXPOSED_IRON);
		wax(CORRODED_WEATHERED_IRON, ModBlocks.WAXED_WEATHERED_IRON);
		wax(CORRODED_OXIDIZED_IRON, ModBlocks.WAXED_OXIDIZED_IRON);
		wax(CORRODED_CUT_IRON, ModBlocks.WAXED_CUT_IRON);
		wax(CORRODED_EXPOSED_CUT_IRON, ModBlocks.WAXED_EXPOSED_CUT_IRON);
		wax(CORRODED_WEATHERED_CUT_IRON, ModBlocks.WAXED_WEATHERED_CUT_IRON);
		wax(CORRODED_OXIDIZED_CUT_IRON, ModBlocks.WAXED_OXIDIZED_CUT_IRON);
		wax(CORRODED_CUT_IRON_SLAB, ModBlocks.WAXED_CUT_IRON_SLAB);
		wax(CORRODED_EXPOSED_CUT_IRON_SLAB, ModBlocks.WAXED_EXPOSED_CUT_IRON_SLAB);
		wax(CORRODED_WEATHERED_CUT_IRON_SLAB, ModBlocks.WAXED_WEATHERED_CUT_IRON_SLAB);
		wax(CORRODED_OXIDIZED_CUT_IRON_SLAB, ModBlocks.WAXED_OXIDIZED_CUT_IRON_SLAB);
		wax(CORRODED_CUT_IRON_STAIRS, ModBlocks.WAXED_CUT_IRON_STAIRS);
		wax(CORRODED_EXPOSED_CUT_IRON_STAIRS, ModBlocks.WAXED_EXPOSED_CUT_IRON_STAIRS);
		wax(CORRODED_WEATHERED_CUT_IRON_STAIRS, ModBlocks.WAXED_WEATHERED_CUT_IRON_STAIRS);
		wax(CORRODED_OXIDIZED_CUT_IRON_STAIRS, ModBlocks.WAXED_OXIDIZED_CUT_IRON_STAIRS);
		wax(CORRODED_IRON_GRATE, ModBlocks.WAXED_IRON_GRATE);
		wax(CORRODED_EXPOSED_IRON_GRATE, ModBlocks.WAXED_EXPOSED_IRON_GRATE);
		wax(CORRODED_WEATHERED_IRON_GRATE, ModBlocks.WAXED_WEATHERED_IRON_GRATE);
		wax(CORRODED_OXIDIZED_IRON_GRATE, ModBlocks.WAXED_OXIDIZED_IRON_GRATE);
		wax(CORRODED_IRON_BULB, ModBlocks.WAXED_IRON_BULB);
		wax(CORRODED_EXPOSED_IRON_BULB, ModBlocks.WAXED_EXPOSED_IRON_BULB);
		wax(CORRODED_WEATHERED_IRON_BULB, ModBlocks.WAXED_WEATHERED_IRON_BULB);
		wax(CORRODED_OXIDIZED_IRON_BULB, ModBlocks.WAXED_OXIDIZED_IRON_BULB);
		wax(CORRODED_IRON_DOOR, ModBlocks.WAXED_IRON_DOOR);
		wax(CORRODED_EXPOSED_IRON_DOOR, ModBlocks.WAXED_EXPOSED_IRON_DOOR);
		wax(CORRODED_WEATHERED_IRON_DOOR, ModBlocks.WAXED_WEATHERED_IRON_DOOR);
		wax(CORRODED_OXIDIZED_IRON_DOOR, ModBlocks.WAXED_OXIDIZED_IRON_DOOR);
		wax(CORRODED_IRON_TRAPDOOR, ModBlocks.WAXED_IRON_TRAPDOOR);
		wax(CORRODED_EXPOSED_IRON_TRAPDOOR, ModBlocks.WAXED_EXPOSED_IRON_TRAPDOOR);
		wax(CORRODED_WEATHERED_IRON_TRAPDOOR, ModBlocks.WAXED_WEATHERED_IRON_TRAPDOOR);
		wax(CORRODED_OXIDIZED_IRON_TRAPDOOR, ModBlocks.WAXED_OXIDIZED_IRON_TRAPDOOR);
		wax(CORRODED_IRON_CHAIN, ModBlocks.WAXED_IRON_CHAIN);
		wax(CORRODED_EXPOSED_IRON_CHAIN, ModBlocks.WAXED_EXPOSED_IRON_CHAIN);
		wax(CORRODED_WEATHERED_IRON_CHAIN, ModBlocks.WAXED_WEATHERED_IRON_CHAIN);
		wax(CORRODED_OXIDIZED_IRON_CHAIN, ModBlocks.WAXED_OXIDIZED_IRON_CHAIN);
		wax(CORRODED_IRON_BARS, ModBlocks.WAXED_IRON_BARS);
		wax(CORRODED_EXPOSED_IRON_BARS, ModBlocks.WAXED_EXPOSED_IRON_BARS);
		wax(CORRODED_WEATHERED_IRON_BARS, ModBlocks.WAXED_WEATHERED_IRON_BARS);
		wax(CORRODED_OXIDIZED_IRON_BARS, ModBlocks.WAXED_OXIDIZED_IRON_BARS);

		pair(Blocks.COPPER_BLOCK.weathering().unaffected(), CORRODED_COPPER);
		pair(Blocks.COPPER_BLOCK.weathering().exposed(), CORRODED_EXPOSED_COPPER);
		pair(Blocks.COPPER_BLOCK.weathering().weathered(), CORRODED_WEATHERED_COPPER);
		pair(Blocks.COPPER_BLOCK.weathering().oxidized(), CORRODED_OXIDIZED_COPPER);
		pair(Blocks.CUT_COPPER.weathering().unaffected(), CORRODED_CUT_COPPER);
		pair(Blocks.CUT_COPPER.weathering().exposed(), CORRODED_EXPOSED_CUT_COPPER);
		pair(Blocks.CUT_COPPER.weathering().weathered(), CORRODED_WEATHERED_CUT_COPPER);
		pair(Blocks.CUT_COPPER.weathering().oxidized(), CORRODED_OXIDIZED_CUT_COPPER);
		pair(Blocks.CUT_COPPER_STAIRS.weathering().unaffected(), CORRODED_CUT_COPPER_STAIRS);
		pair(Blocks.CUT_COPPER_STAIRS.weathering().exposed(), CORRODED_EXPOSED_CUT_COPPER_STAIRS);
		pair(Blocks.CUT_COPPER_STAIRS.weathering().weathered(), CORRODED_WEATHERED_CUT_COPPER_STAIRS);
		pair(Blocks.CUT_COPPER_STAIRS.weathering().oxidized(), CORRODED_OXIDIZED_CUT_COPPER_STAIRS);
		pair(Blocks.CUT_COPPER_SLAB.weathering().unaffected(), CORRODED_CUT_COPPER_SLAB);
		pair(Blocks.CUT_COPPER_SLAB.weathering().exposed(), CORRODED_EXPOSED_CUT_COPPER_SLAB);
		pair(Blocks.CUT_COPPER_SLAB.weathering().weathered(), CORRODED_WEATHERED_CUT_COPPER_SLAB);
		pair(Blocks.CUT_COPPER_SLAB.weathering().oxidized(), CORRODED_OXIDIZED_CUT_COPPER_SLAB);

		pair(Blocks.IRON_BLOCK, CORRODED_IRON);
		pair(ModBlocks.UNAFFECTED_IRON, CORRODED_IRON);
		pair(ModBlocks.EXPOSED_IRON, CORRODED_EXPOSED_IRON);
		pair(ModBlocks.WEATHERED_IRON, CORRODED_WEATHERED_IRON);
		pair(ModBlocks.OXIDIZED_IRON, CORRODED_OXIDIZED_IRON);
		pair(ModBlocks.CUT_IRON, CORRODED_CUT_IRON);
		pair(ModBlocks.EXPOSED_CUT_IRON, CORRODED_EXPOSED_CUT_IRON);
		pair(ModBlocks.WEATHERED_CUT_IRON, CORRODED_WEATHERED_CUT_IRON);
		pair(ModBlocks.OXIDIZED_CUT_IRON, CORRODED_OXIDIZED_CUT_IRON);
		pair(ModBlocks.CUT_IRON_SLAB, CORRODED_CUT_IRON_SLAB);
		pair(ModBlocks.EXPOSED_CUT_IRON_SLAB, CORRODED_EXPOSED_CUT_IRON_SLAB);
		pair(ModBlocks.WEATHERED_CUT_IRON_SLAB, CORRODED_WEATHERED_CUT_IRON_SLAB);
		pair(ModBlocks.OXIDIZED_CUT_IRON_SLAB, CORRODED_OXIDIZED_CUT_IRON_SLAB);
		pair(ModBlocks.CUT_IRON_STAIRS, CORRODED_CUT_IRON_STAIRS);
		pair(ModBlocks.EXPOSED_CUT_IRON_STAIRS, CORRODED_EXPOSED_CUT_IRON_STAIRS);
		pair(ModBlocks.WEATHERED_CUT_IRON_STAIRS, CORRODED_WEATHERED_CUT_IRON_STAIRS);
		pair(ModBlocks.OXIDIZED_CUT_IRON_STAIRS, CORRODED_OXIDIZED_CUT_IRON_STAIRS);
		pair(ModBlocks.IRON_GRATE, CORRODED_IRON_GRATE);
		pair(ModBlocks.EXPOSED_IRON_GRATE, CORRODED_EXPOSED_IRON_GRATE);
		pair(ModBlocks.WEATHERED_IRON_GRATE, CORRODED_WEATHERED_IRON_GRATE);
		pair(ModBlocks.OXIDIZED_IRON_GRATE, CORRODED_OXIDIZED_IRON_GRATE);
		pair(ModBlocks.IRON_BULB, CORRODED_IRON_BULB);
		pair(ModBlocks.EXPOSED_IRON_BULB, CORRODED_EXPOSED_IRON_BULB);
		pair(ModBlocks.WEATHERED_IRON_BULB, CORRODED_WEATHERED_IRON_BULB);
		pair(ModBlocks.OXIDIZED_IRON_BULB, CORRODED_OXIDIZED_IRON_BULB);
		pair(Blocks.IRON_DOOR, CORRODED_IRON_DOOR);
		pair(ModBlocks.UNAFFECTED_IRON_DOOR, CORRODED_IRON_DOOR);
		pair(ModBlocks.EXPOSED_IRON_DOOR, CORRODED_EXPOSED_IRON_DOOR);
		pair(ModBlocks.WEATHERED_IRON_DOOR, CORRODED_WEATHERED_IRON_DOOR);
		pair(ModBlocks.OXIDIZED_IRON_DOOR, CORRODED_OXIDIZED_IRON_DOOR);
		pair(Blocks.IRON_TRAPDOOR, CORRODED_IRON_TRAPDOOR);
		pair(ModBlocks.UNAFFECTED_IRON_TRAPDOOR, CORRODED_IRON_TRAPDOOR);
		pair(ModBlocks.EXPOSED_IRON_TRAPDOOR, CORRODED_EXPOSED_IRON_TRAPDOOR);
		pair(ModBlocks.WEATHERED_IRON_TRAPDOOR, CORRODED_WEATHERED_IRON_TRAPDOOR);
		pair(ModBlocks.OXIDIZED_IRON_TRAPDOOR, CORRODED_OXIDIZED_IRON_TRAPDOOR);
		pair(Blocks.IRON_CHAIN, CORRODED_IRON_CHAIN);
		pair(ModBlocks.UNAFFECTED_IRON_CHAIN, CORRODED_IRON_CHAIN);
		pair(ModBlocks.EXPOSED_IRON_CHAIN, CORRODED_EXPOSED_IRON_CHAIN);
		pair(ModBlocks.WEATHERED_IRON_CHAIN, CORRODED_WEATHERED_IRON_CHAIN);
		pair(ModBlocks.OXIDIZED_IRON_CHAIN, CORRODED_OXIDIZED_IRON_CHAIN);
		pair(Blocks.IRON_BARS, CORRODED_IRON_BARS);
		pair(ModBlocks.UNAFFECTED_IRON_BARS, CORRODED_IRON_BARS);
		pair(ModBlocks.EXPOSED_IRON_BARS, CORRODED_EXPOSED_IRON_BARS);
		pair(ModBlocks.WEATHERED_IRON_BARS, CORRODED_WEATHERED_IRON_BARS);
		pair(ModBlocks.OXIDIZED_IRON_BARS, CORRODED_OXIDIZED_IRON_BARS);

		OuterWorldMod.LOGGER.info("Registered corroded copper and iron blocks");
	}

	private static void chain(Block a, Block b, Block c, Block d) {
		OxidizableBlocksRegistry.registerNextStage(a, b);
		OxidizableBlocksRegistry.registerNextStage(b, c);
		OxidizableBlocksRegistry.registerNextStage(c, d);
	}

	private static void wax(Block corroded, Block waxed) {
		Corrosion.registerWaxed(corroded, waxed);
	}

	private static void pair(Block original, Block corroded) {
		Corrosion.register(original, corroded);
	}
}
