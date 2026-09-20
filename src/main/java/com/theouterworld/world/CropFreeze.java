package com.theouterworld.world;

import com.theouterworld.block.FrozenPitcherCropBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * In the Outerworld and Innerworld, crops can freeze instead of completing a growth stage.
 * Frozen plants stop growing and drop frozen produce.
 */
public final class CropFreeze {
	public static final int DEFAULT_CHANCE = 2;
	public static final int WHEAT_CHANCE = 2;
	public static final int TORCHFLOWER_CHANCE = 2;

	private CropFreeze() {
	}

	public static BlockState maybeFreeze(CropBlock crop, Level level, BlockState from, BlockState vanillaTo) {
		if (level.isClientSide() || !ModDimensions.shouldFreezePlants(level.dimension())) {
			return vanillaTo;
		}
		if (!isVanillaCrop(crop)) {
			return vanillaTo;
		}

		int fromAge = crop.getAge(from);
		int toAge = effectiveAge(crop, vanillaTo);
		boolean always = ModDimensions.isAirless(level.dimension());
		int chance = freezeChance(crop);
		for (int age = fromAge + 1; age <= toAge; age++) {
			if (always || level.getRandom().nextInt(chance) == 0) {
				return frozenState(crop, age);
			}
		}
		return vanillaTo;
	}

	public static boolean tryFreezePitcherGrowth(ServerLevel level, BlockState state, BlockPos pos, int increase) {
		if (level.isClientSide() || !ModDimensions.shouldFreezePlants(level.dimension())) {
			return false;
		}
		if (!(state.getBlock() instanceof PitcherCropBlock) || state.getBlock() instanceof FrozenPitcherCropBlock) {
			return false;
		}

		int fromAge = state.getValue(PitcherCropBlock.AGE);
		int targetAge = Math.min(4, fromAge + increase);
		for (int age = fromAge + 1; age <= targetAge; age++) {
			if (!canPitcherGrowTo(level, pos, age)) {
				return false;
			}
			if (ModDimensions.isAirless(level.dimension()) || level.getRandom().nextInt(DEFAULT_CHANCE) == 0) {
				placeFrozenPitcher(level, pos, age);
				return true;
			}
		}
		return false;
	}

	private static boolean isVanillaCrop(CropBlock crop) {
		return crop == Blocks.WHEAT
			|| crop == Blocks.CARROTS
			|| crop == Blocks.POTATOES
			|| crop == Blocks.BEETROOTS
			|| crop == Blocks.TORCHFLOWER_CROP;
	}

	private static int freezeChance(CropBlock crop) {
		if (crop == Blocks.WHEAT) {
			return WHEAT_CHANCE;
		}
		if (crop instanceof TorchflowerCropBlock) {
			return TORCHFLOWER_CHANCE;
		}
		return DEFAULT_CHANCE;
	}

	private static int effectiveAge(CropBlock crop, BlockState vanillaTo) {
		if (vanillaTo.is(Blocks.TORCHFLOWER)) {
			return crop.getMaxAge();
		}
		if (vanillaTo.getBlock() instanceof CropBlock toCrop) {
			return toCrop.getAge(vanillaTo);
		}
		return crop.getMaxAge();
	}

	private static BlockState frozenState(CropBlock crop, int age) {
		if (crop instanceof TorchflowerCropBlock || crop == Blocks.TORCHFLOWER_CROP) {
			if (age >= 2) {
				return ModBlocks.FROZEN_TORCHFLOWER.defaultBlockState();
			}
			return ModBlocks.FROZEN_TORCHFLOWER_CROP.defaultBlockState().setValue(TorchflowerCropBlock.AGE, age);
		}
		if (crop instanceof BeetrootBlock || crop == Blocks.BEETROOTS) {
			return ModBlocks.FROZEN_BEETROOTS.defaultBlockState().setValue(BeetrootBlock.AGE, age);
		}
		if (crop == Blocks.CARROTS) {
			return ModBlocks.FROZEN_CARROTS.defaultBlockState().setValue(CropBlock.AGE, age);
		}
		if (crop == Blocks.POTATOES) {
			return ModBlocks.FROZEN_POTATOES.defaultBlockState().setValue(CropBlock.AGE, age);
		}
		return ModBlocks.FROZEN_WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
	}

	private static boolean canPitcherGrowTo(ServerLevel level, BlockPos pos, int age) {
		if (level.getRawBrightness(pos, 0) < 8) {
			return false;
		}
		if (!level.isInsideBuildHeight(pos.above())) {
			return false;
		}
		if (age >= 3) {
			BlockState above = level.getBlockState(pos.above());
			return above.isAir() || above.is(Blocks.PITCHER_CROP) || above.is(ModBlocks.FROZEN_PITCHER_CROP);
		}
		return true;
	}

	private static void placeFrozenPitcher(ServerLevel level, BlockPos pos, int age) {
		BlockState lower = ModBlocks.FROZEN_PITCHER_CROP.defaultBlockState()
			.setValue(PitcherCropBlock.AGE, age)
			.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
		level.setBlock(pos, lower, Block.UPDATE_CLIENTS);
		if (age >= 3) {
			level.setBlock(
				pos.above(),
				lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
				Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE
			);
		}
	}
}
