package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingOlivineBlock extends Block {
	public static final int GROWTH_CHANCE = 5;
	private static final Direction[] DIRECTIONS = Direction.values();

	public BuddingOlivineBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(GROWTH_CHANCE) != 0) {
			return;
		}

		Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
		BlockPos growPos = pos.relative(direction);
		BlockState facingState = level.getBlockState(growPos);
		Block next = null;

		if (canClusterGrowAtState(facingState)) {
			next = ModBlocks.OLIVINE_CRYSTAL_0;
		} else if (facingState.is(ModBlocks.OLIVINE_CRYSTAL_0) && facingState.getValue(AmethystClusterBlock.FACING) == direction) {
			next = ModBlocks.OLIVINE_CRYSTAL_1;
		} else if (facingState.is(ModBlocks.OLIVINE_CRYSTAL_1) && facingState.getValue(AmethystClusterBlock.FACING) == direction) {
			next = ModBlocks.OLIVINE_CRYSTAL_2;
		}

		if (next != null) {
			BlockState placed = next.defaultBlockState()
				.setValue(AmethystClusterBlock.FACING, direction)
				.setValue(AmethystClusterBlock.WATERLOGGED, facingState.getFluidState().getType() == Fluids.WATER);
			level.setBlockAndUpdate(growPos, placed);
		}
	}

	public static boolean canClusterGrowAtState(BlockState state) {
		return state.isAir() || (state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8);
	}
}
