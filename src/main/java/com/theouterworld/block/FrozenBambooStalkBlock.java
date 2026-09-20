package com.theouterworld.block;

import net.minecraft.world.level.block.BonemealSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class FrozenBambooStalkBlock extends BambooStalkBlock {
	public FrozenBambooStalkBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return false;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		return false;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return false;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
	}

	@Override
	protected void growBamboo(BlockState state, Level level, BlockPos pos, RandomSource random, int height) {
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		if (!fluidState.isEmpty()) {
			return null;
		}

		BlockState belowState = context.getLevel().getBlockState(context.getClickedPos().below());
		if (belowState.is(BlockTags.SUPPORTS_BAMBOO)) {
			if (belowState.is(Blocks.BAMBOO_SAPLING) || belowState.is(ModBlocks.FROZEN_BAMBOO_SAPLING)) {
				return this.defaultBlockState().setValue(AGE, 0);
			} else if (belowState.is(Blocks.BAMBOO) || belowState.is(ModBlocks.FROZEN_BAMBOO)) {
				int age = belowState.hasProperty(AGE) && belowState.getValue(AGE) > 0 ? 1 : 0;
				return this.defaultBlockState().setValue(AGE, age);
			} else {
				BlockState aboveState = context.getLevel().getBlockState(context.getClickedPos().above());
				if (aboveState.is(Blocks.BAMBOO) || aboveState.is(ModBlocks.FROZEN_BAMBOO)) {
					return this.defaultBlockState().setValue(AGE, aboveState.getValue(AGE));
				}
				return ModBlocks.FROZEN_BAMBOO_SAPLING.defaultBlockState();
			}
		}
		return null;
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction directionToNeighbour,
		BlockPos neighbourPos,
		BlockState neighbourState,
		RandomSource random
	) {
		if (!state.canSurvive(level, pos)) {
			ticks.scheduleTick(pos, this, 1);
		}
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	protected int getHeightAboveUpToMax(BlockGetter level, BlockPos pos) {
		int height = 0;
		while (height < 16 && isBamboo(level.getBlockState(pos.above(height + 1)))) {
			height++;
		}
		return height;
	}

	@Override
	protected int getHeightBelowUpToMax(BlockGetter level, BlockPos pos) {
		int height = 0;
		while (height < 16 && isBamboo(level.getBlockState(pos.below(height + 1)))) {
			height++;
		}
		return height;
	}

	private static boolean isBamboo(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.BAMBOO || block == ModBlocks.FROZEN_BAMBOO;
	}
}
