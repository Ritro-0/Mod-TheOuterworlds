package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Shared two-block iron golem statue. Collision matches a living iron golem
 * (1.4 wide, 2.7 tall); the model is split across the lower and upper halves.
 */
public abstract class AbstractIronGolemStatueBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	// Living iron golem hitbox: 1.4 x 2.7, centered on the column.
	private static final VoxelShape SHAPE_LOWER = Shapes.box(-0.2, 0.0, -0.2, 1.2, 1.0, 1.2);
	private static final VoxelShape SHAPE_UPPER = Shapes.box(-0.2, 0.0, -0.2, 1.2, 1.7, 1.2);

	private static final int SILENT_FLAGS = Block.UPDATE_CLIENTS;

	protected AbstractIronGolemStatueBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(WATERLOGGED, false)
			.setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, HALF);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		Level world = ctx.getLevel();
		if (pos.getY() >= world.getMaxY() || !world.getBlockState(pos.above()).canBeReplaced(ctx)) {
			return null;
		}
		FluidState fluidState = world.getFluidState(pos);
		return this.defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection().getOpposite())
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
			.setValue(HALF, DoubleBlockHalf.LOWER);
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
		super.onPlace(state, world, pos, oldState, moved);
		if (!world.isClientSide() && !moved) {
			tryPlaceMissingUpper(world, pos, state);
		}
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockState below = world.getBlockState(pos.below());
			return isStatue(below) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
		}
		BlockState above = world.getBlockState(pos.above());
		return (isStatue(above) && above.getValue(HALF) == DoubleBlockHalf.UPPER) || above.canBeReplaced();
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
		if (state.getValue(WATERLOGGED)) {
			scheduledTickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}

		DoubleBlockHalf half = state.getValue(HALF);
		if (direction.getAxis() == Direction.Axis.Y) {
			boolean lookingAtPartner =
				(half == DoubleBlockHalf.LOWER && direction == Direction.UP)
					|| (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN);
			if (lookingAtPartner) {
				if (half == DoubleBlockHalf.LOWER && neighborState.canBeReplaced()) {
					scheduledTickView.scheduleTick(pos, this, 1);
				} else if (!isPartner(neighborState, half)) {
					return Blocks.AIR.defaultBlockState();
				}
			}
		}
		return super.updateShape(state, world, scheduledTickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		tryPlaceMissingUpper(world, pos, state);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!world.isClientSide()) {
			DoubleBlockHalf half = state.getValue(HALF);
			if (half == DoubleBlockHalf.UPPER) {
				BlockPos below = pos.below();
				BlockState belowState = world.getBlockState(below);
				if (isStatue(belowState) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER) {
					world.destroyBlock(below, !player.isCreative());
				}
			} else {
				BlockPos above = pos.above();
				BlockState aboveState = world.getBlockState(above);
				if (isStatue(aboveState) && aboveState.getValue(HALF) == DoubleBlockHalf.UPPER) {
					world.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? SHAPE_UPPER : SHAPE_LOWER;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new IronGolemStatueBlockEntity(pos, state);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	public static boolean isStatue(BlockState state) {
		return state.getBlock() instanceof AbstractIronGolemStatueBlock && state.hasProperty(HALF);
	}

	public static BlockPos lowerPos(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
	}

	@Nullable
	public static IronGolemStatueBlockEntity getStatueEntity(Level world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(lowerPos(pos, state)) instanceof IronGolemStatueBlockEntity statueEntity) {
			return statueEntity;
		}
		return null;
	}

	protected static void tryPlaceMissingUpper(Level world, BlockPos pos, BlockState state) {
		if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
			return;
		}
		BlockPos above = pos.above();
		BlockState aboveState = world.getBlockState(above);
		if (!aboveState.canBeReplaced()) {
			return;
		}
		FluidState fluid = world.getFluidState(above);
		world.setBlock(
			above,
			state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(WATERLOGGED, fluid.getType() == Fluids.WATER),
			Block.UPDATE_ALL
		);
	}

	protected static void replaceBothHalves(Level world, BlockPos pos, BlockState current, Block newBlock) {
		BlockPos lower = lowerPos(pos, current);
		BlockPos upper = lower.above();
		BlockState lowerState = world.getBlockState(lower);
		BlockState upperState = world.getBlockState(upper);

		if (isStatue(lowerState)) {
			world.setBlock(lower, newBlock.withPropertiesOf(lowerState), SILENT_FLAGS);
		}
		if (isStatue(upperState)) {
			world.setBlock(upper, newBlock.withPropertiesOf(upperState), Block.UPDATE_ALL);
		} else {
			tryPlaceMissingUpper(world, lower, newBlock.withPropertiesOf(lowerState));
		}
	}

	protected static void removeBothHalves(Level world, BlockPos pos, BlockState state) {
		BlockPos lower = lowerPos(pos, state);
		BlockPos upper = lower.above();
		if (isStatue(world.getBlockState(upper))) {
			world.setBlock(upper, Blocks.AIR.defaultBlockState(), SILENT_FLAGS);
		}
		world.setBlock(lower, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
	}

	private static boolean isPartner(BlockState neighborState, DoubleBlockHalf ourHalf) {
		if (!isStatue(neighborState)) {
			return false;
		}
		return neighborState.getValue(HALF) != ourHalf;
	}
}
