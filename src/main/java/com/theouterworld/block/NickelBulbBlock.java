package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Non-oxidizing nickel bulb. Lit and lit/powered states emit light level 1;
 * a lit bulb outputs a full comparator signal of 15.
 */
public class NickelBulbBlock extends Block {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public NickelBulbBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, POWERED);
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.is(state.getBlock()) && world instanceof ServerLevel serverWorld) {
			serverWorld.scheduleTick(pos, this, 1);
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader world,
		ScheduledTickAccess scheduledTickView,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		RandomSource random
	) {
		if (world instanceof ServerLevel serverWorld) {
			boolean poweredNow = serverWorld.hasNeighborSignal(pos);
			boolean wasPowered = state.getValue(POWERED);
			if (poweredNow && !wasPowered) {
				scheduledTickView.scheduleTick(pos, this, 1);
			}
			if (poweredNow != wasPowered) {
				return state.setValue(POWERED, poweredNow);
			}
		}
		return state;
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (!state.getValue(POWERED) || !world.hasNeighborSignal(pos)) {
			return;
		}
		BlockState newState = state.cycle(LIT);
		world.setBlock(pos, newState, Block.UPDATE_ALL);
		world.playSound(
			null,
			pos,
			newState.getValue(LIT) ? SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON : SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
			SoundSource.BLOCKS,
			0.4F,
			newState.getValue(LIT) ? 0.8F : 1.2F
		);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
		return state.getValue(LIT) ? 15 : 0;
	}
}
