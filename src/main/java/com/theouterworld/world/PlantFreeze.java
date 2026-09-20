package com.theouterworld.world;

import com.theouterworld.block.FrozenStemBlock;
import com.theouterworld.block.FrozenAzaleaBlock;
import com.theouterworld.block.FrozenBambooSaplingBlock;
import com.theouterworld.block.FrozenBambooStalkBlock;
import com.theouterworld.block.FrozenCocoaBlock;
import com.theouterworld.block.FrozenMangrovePropaguleBlock;
import com.theouterworld.block.FrozenMushroomBlock;
import com.theouterworld.block.FrozenNetherFungusBlock;
import com.theouterworld.block.FrozenSaplingBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

/**
 * In the Outerworld and Innerworld, growing plants can freeze instead of completing a growth stage.
 * Firefly bushes do not grow, so they freeze immediately when placed.
 */
public final class PlantFreeze {
	/** 50% chance for saplings, azalea, bamboo shoots, mushrooms, and nether fungi. */
	public static final int SAPLING_CHANCE = 2;

	private PlantFreeze() {
	}

	public static void register() {
	}

	public static boolean inCold(Level level) {
		return !level.isClientSide() && ModDimensions.shouldFreezePlants(level.dimension());
	}

	public static boolean tryFreezeSapling(ServerLevel level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (!inCold(level) || isAlreadyFrozen(block)) {
			return false;
		}
		if (block instanceof MangrovePropaguleBlock && state.getValue(MangrovePropaguleBlock.HANGING)) {
			return false;
		}
		Block frozen = frozenSaplingOf(block);
		if (frozen == null || !rollSapling(level)) {
			return false;
		}
		BlockState next = copySharedProperties(state, frozen.defaultBlockState());
		if (next.hasProperty(SaplingBlock.STAGE) && state.getValue(SaplingBlock.STAGE) == 0) {
			next = next.setValue(SaplingBlock.STAGE, 1);
		}
		level.setBlock(pos, next, Block.UPDATE_CLIENTS);
		return true;
	}

	public static boolean tryFreezeHangingPropagule(ServerLevel level, BlockPos pos, BlockState state) {
		if (!inCold(level) || isAlreadyFrozen(state.getBlock())) {
			return false;
		}
		if (!(state.getBlock() instanceof MangrovePropaguleBlock) || !state.getValue(MangrovePropaguleBlock.HANGING)) {
			return false;
		}
		int age = state.getValue(MangrovePropaguleBlock.AGE);
		if (age >= MangrovePropaguleBlock.MAX_AGE || !rollSapling(level)) {
			return false;
		}
		BlockState next = copySharedProperties(state, ModBlocks.FROZEN_MANGROVE_PROPAGULE.defaultBlockState())
			.setValue(MangrovePropaguleBlock.AGE, age + 1)
			.setValue(MangrovePropaguleBlock.HANGING, true);
		level.setBlock(pos, next, Block.UPDATE_CLIENTS);
		return true;
	}

	public static boolean tryFreezeAzalea(ServerLevel level, BlockPos pos, BlockState state) {
		if (!inCold(level) || state.getBlock() instanceof FrozenAzaleaBlock || !rollSapling(level)) {
			return false;
		}
		Block frozen = state.is(Blocks.FLOWERING_AZALEA) ? ModBlocks.FROZEN_FLOWERING_AZALEA : ModBlocks.FROZEN_AZALEA;
		if (!state.is(Blocks.AZALEA) && !state.is(Blocks.FLOWERING_AZALEA)) {
			return false;
		}
		level.setBlock(pos, frozen.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}

	public static boolean tryFreezeMushroom(ServerLevel level, BlockPos pos, BlockState state) {
		if (!inCold(level) || state.getBlock() instanceof FrozenMushroomBlock || !rollSapling(level)) {
			return false;
		}
		Block frozen = frozenMushroomOf(state.getBlock());
		if (frozen == null) {
			return false;
		}
		level.setBlock(pos, frozen.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}

	public static boolean tryFreezeFungus(ServerLevel level, BlockPos pos, BlockState state) {
		if (!inCold(level) || state.getBlock() instanceof FrozenNetherFungusBlock || !rollSapling(level)) {
			return false;
		}
		Block frozen = frozenFungusOf(state.getBlock());
		if (frozen == null) {
			return false;
		}
		level.setBlock(pos, frozen.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}

	public static BlockState maybeFreezeCocoa(Level level, BlockState vanillaNext) {
		if (!inCold(level) || vanillaNext.getBlock() instanceof FrozenCocoaBlock || !roll(level)) {
			return vanillaNext;
		}
		return copySharedProperties(vanillaNext, ModBlocks.FROZEN_COCOA.defaultBlockState());
	}

	public static boolean tryFreezeBambooSapling(ServerLevel level, BlockPos pos, BlockState state) {
		if (!inCold(level) || state.getBlock() instanceof FrozenBambooSaplingBlock || !rollSapling(level)) {
			return false;
		}
		if (!state.is(Blocks.BAMBOO_SAPLING)) {
			return false;
		}
		level.setBlock(pos, ModBlocks.FROZEN_BAMBOO_SAPLING.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}

	public static boolean tryFreezeBambooStalk(ServerLevel level, BlockPos pos, BlockState state) {
		if (!inCold(level) || state.getBlock() instanceof FrozenBambooStalkBlock || !roll(level)) {
			return false;
		}
		if (!state.is(Blocks.BAMBOO)) {
			return false;
		}
		BlockState frozen = copySharedProperties(state, ModBlocks.FROZEN_BAMBOO.defaultBlockState())
			.setValue(net.minecraft.world.level.block.BambooStalkBlock.STAGE, 1);
		level.setBlock(pos, frozen, Block.UPDATE_CLIENTS);
		return true;
	}

	public static boolean tryPlaceStemFruit(
		StemBlock stem,
		ServerLevel level,
		BlockPos pos,
		RandomSource random,
		ResourceKey<Block> fruit,
		ResourceKey<Block> attachedStem,
		TagKey<Block> fruitSupportBlocks
	) {
		if (!inCold(level)) {
			return false;
		}
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockPos relative = pos.relative(direction);
		BlockState below = level.getBlockState(relative.below());
		if (!level.getBlockState(relative).isAir() || !below.is(fruitSupportBlocks)) {
			return false;
		}
		Registry<Block> blocks = level.registryAccess().lookupOrThrow(Registries.BLOCK);
		Optional<Block> fruitBlock = blocks.getOptional(fruit);
		Optional<Block> attached = blocks.getOptional(attachedStem);
		if (fruitBlock.isEmpty() || attached.isEmpty()) {
			return false;
		}
		Block placedFruit = fruitBlock.get();
		if (roll(level)) {
			Block frozen = frozenFruitOf(placedFruit);
			if (frozen != null) {
				placedFruit = frozen;
			}
		}
		level.setBlockAndUpdate(relative, placedFruit.defaultBlockState());
		Block attachedBlock = frozenAttachedOf(attached.get());
		level.setBlockAndUpdate(pos, attachedBlock.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
		return true;
	}

	public static BlockState maybeFreezeStem(Level level, BlockState from, BlockState vanillaTo) {
		if (!inCold(level) || from.getBlock() instanceof FrozenStemBlock) {
			return vanillaTo;
		}
		Block frozen = frozenStemOf(from.getBlock());
		if (frozen == null) {
			return vanillaTo;
		}
		int fromAge = from.getValue(StemBlock.AGE);
		int toAge = vanillaTo.getValue(StemBlock.AGE);
		for (int age = fromAge + 1; age <= toAge; age++) {
			if (roll(level)) {
				return frozen.defaultBlockState().setValue(StemBlock.AGE, age);
			}
		}
		return vanillaTo;
	}

	private static boolean rollSapling(Level level) {
		return ModDimensions.isAirless(level.dimension()) || level.getRandom().nextInt(SAPLING_CHANCE) == 0;
	}

	private static boolean roll(Level level) {
		return ModDimensions.isAirless(level.dimension()) || level.getRandom().nextInt(CropFreeze.DEFAULT_CHANCE) == 0;
	}

	private static boolean isAlreadyFrozen(Block block) {
		return block instanceof FrozenSaplingBlock
			|| block instanceof FrozenMangrovePropaguleBlock
			|| block instanceof FrozenAzaleaBlock
			|| block instanceof FrozenMushroomBlock
			|| block instanceof FrozenNetherFungusBlock
			|| block instanceof FrozenCocoaBlock
			|| block instanceof FrozenBambooSaplingBlock
			|| block instanceof FrozenBambooStalkBlock
			|| block instanceof FrozenStemBlock;
	}

	private static Block frozenSaplingOf(Block block) {
		if (block == Blocks.OAK_SAPLING) return ModBlocks.FROZEN_OAK_SAPLING;
		if (block == Blocks.SPRUCE_SAPLING) return ModBlocks.FROZEN_SPRUCE_SAPLING;
		if (block == Blocks.BIRCH_SAPLING) return ModBlocks.FROZEN_BIRCH_SAPLING;
		if (block == Blocks.JUNGLE_SAPLING) return ModBlocks.FROZEN_JUNGLE_SAPLING;
		if (block == Blocks.ACACIA_SAPLING) return ModBlocks.FROZEN_ACACIA_SAPLING;
		if (block == Blocks.DARK_OAK_SAPLING) return ModBlocks.FROZEN_DARK_OAK_SAPLING;
		if (block == Blocks.CHERRY_SAPLING) return ModBlocks.FROZEN_CHERRY_SAPLING;
		if (block == Blocks.PALE_OAK_SAPLING) return ModBlocks.FROZEN_PALE_OAK_SAPLING;
		if (block == Blocks.MANGROVE_PROPAGULE) return ModBlocks.FROZEN_MANGROVE_PROPAGULE;
		return null;
	}

	private static Block frozenMushroomOf(Block block) {
		if (block == Blocks.RED_MUSHROOM) return ModBlocks.FROZEN_RED_MUSHROOM;
		if (block == Blocks.BROWN_MUSHROOM) return ModBlocks.FROZEN_BROWN_MUSHROOM;
		return null;
	}

	private static Block frozenFungusOf(Block block) {
		if (block == Blocks.CRIMSON_FUNGUS) return ModBlocks.FROZEN_CRIMSON_FUNGUS;
		if (block == Blocks.WARPED_FUNGUS) return ModBlocks.FROZEN_WARPED_FUNGUS;
		return null;
	}

	private static Block frozenFruitOf(Block block) {
		if (block == Blocks.MELON) return ModBlocks.FROZEN_MELON;
		if (block == Blocks.PUMPKIN) return ModBlocks.FROZEN_PUMPKIN;
		return null;
	}

	private static Block frozenStemOf(Block block) {
		if (block == Blocks.PUMPKIN_STEM) return ModBlocks.FROZEN_PUMPKIN_STEM;
		if (block == Blocks.MELON_STEM) return ModBlocks.FROZEN_MELON_STEM;
		return null;
	}

	private static Block frozenAttachedOf(Block block) {
		if (block == Blocks.ATTACHED_PUMPKIN_STEM) return ModBlocks.FROZEN_ATTACHED_PUMPKIN_STEM;
		if (block == Blocks.ATTACHED_MELON_STEM) return ModBlocks.FROZEN_ATTACHED_MELON_STEM;
		return block;
	}

	private static BlockState copySharedProperties(BlockState from, BlockState to) {
		BlockState result = to;
		for (Property<?> property : from.getProperties()) {
			if (result.hasProperty(property)) {
				result = copyProperty(from, result, property);
			}
		}
		return result;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static BlockState copyProperty(BlockState from, BlockState to, Property property) {
		return to.setValue(property, from.getValue(property));
	}
}
