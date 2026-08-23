package com.theouterworld.block;

import com.theouterworld.world.DimensionClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * Liquid mercury that hardens into cinnabar on contact with sulfur, like lava and water.
 */
public class MercuryLiquidBlock extends LiquidBlock {
	public MercuryLiquidBlock(FlowingFluid fluid, Properties properties) {
		super(fluid, properties);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (DimensionClimate.tryConvertMercuryToCinnabar(level, pos)) {
			return;
		}
		super.onPlace(state, level, pos, oldState, movedByPiston);
	}

	@Override
	protected void neighborChanged(
		BlockState state,
		Level level,
		BlockPos pos,
		Block block,
		@Nullable Orientation orientation,
		boolean movedByPiston
	) {
		if (DimensionClimate.tryConvertMercuryToCinnabar(level, pos)) {
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
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
		if (level instanceof Level world && DimensionClimate.tryConvertMercuryToCinnabar(world, pos)) {
			return world.getBlockState(pos);
		}
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}
}
