package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Hanging opal lantern. The hook attaches to whichever face it was placed on,
 * including walls and the floor.
 */
public class TholinOpalReflectorBlock extends Block {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public TholinOpalReflectorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction attached = context.getClickedFace().getOpposite();
		if (canAttach(context.getLevel(), context.getClickedPos(), attached)) {
			return this.defaultBlockState().setValue(FACING, attached);
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			if (canAttach(context.getLevel(), context.getClickedPos(), direction)) {
				return this.defaultBlockState().setValue(FACING, direction);
			}
		}
		return null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return canAttach(level, pos, state.getValue(FACING));
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		RandomSource random
	) {
		if (!state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return state;
	}

	private static boolean canAttach(LevelReader level, BlockPos pos, Direction facing) {
		BlockPos support = pos.relative(facing);
		if (level.getBlockState(support).getBlock() instanceof TholinThreadBlock) {
			return true;
		}
		return Block.canSupportCenter(level, support, facing.getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case DOWN -> box(6.0, 0.0, 6.0, 10.0, 11.0, 10.0);
			case NORTH -> box(6.0, 6.0, 0.0, 10.0, 10.0, 11.0);
			case SOUTH -> box(6.0, 6.0, 5.0, 10.0, 10.0, 16.0);
			case WEST -> box(0.0, 6.0, 6.0, 11.0, 10.0, 10.0);
			case EAST -> box(5.0, 6.0, 6.0, 16.0, 10.0, 10.0);
			case UP -> box(6.0, 5.0, 6.0, 10.0, 16.0, 10.0);
		};
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
