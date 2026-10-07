package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Chain-like tholin cord. The stem follows the clicked face, the same way a chain does.
 * A perpendicular thread, or a reflector hooked onto this block, grows an arm toward that
 * neighbor. A cord with no arms runs the full block. A cord with arms stays half, toward
 * whichever end is joined, and only runs full again when both ends are joined.
 */
public class TholinThreadBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final EnumProperty<Stem> STEM = EnumProperty.create("stem", Stem.class);
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

	public TholinThreadBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(AXIS, Direction.Axis.Y)
				.setValue(STEM, Stem.FULL)
				.setValue(NORTH, false)
				.setValue(EAST, false)
				.setValue(SOUTH, false)
				.setValue(WEST, false)
				.setValue(UP, false)
				.setValue(DOWN, false)
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clicked = context.getClickedFace();
		Direction.Axis axis = clicked.getAxis();
		Direction support = clicked.getOpposite();
		Stem towardSupport = support.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Stem.HALF_TOP : Stem.HALF;
		BlockState state = this.defaultBlockState().setValue(AXIS, axis).setValue(STEM, towardSupport);
		return reconcile(state, context.getLevel(), context.getClickedPos());
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
		return reconcile(state, level, pos);
	}

	/**
	 * Whether the neighbor past one end of the stem joins it. Any thread there does: one on
	 * this axis runs end-to-end, and one on another axis grows a side arm back into this end.
	 * A reflector only joins when its hook is attached to this block.
	 */
	private static boolean linksAlong(BlockState neighbor, Direction towardNeighbor) {
		if (neighbor.getBlock() instanceof TholinOpalReflectorBlock) {
			return neighbor.getValue(TholinOpalReflectorBlock.FACING) == towardNeighbor.getOpposite();
		}
		return neighbor.getBlock() instanceof TholinThreadBlock;
	}

	private static boolean connectsSide(BlockState neighbor, Direction towardNeighbor, Direction.Axis axis) {
		if (towardNeighbor.getAxis() == axis) {
			return false;
		}
		if (neighbor.getBlock() instanceof TholinOpalReflectorBlock) {
			return neighbor.getValue(TholinOpalReflectorBlock.FACING) == towardNeighbor.getOpposite();
		}
		return neighbor.getBlock() instanceof TholinThreadBlock && neighbor.getValue(AXIS) != axis;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (oldState.is(this)) {
			return;
		}
		BlockState reconciled = reconcile(state, level, pos);
		if (!reconciled.equals(state)) {
			level.setBlock(pos, reconciled, Block.UPDATE_CLIENTS);
		}
	}

	private BlockState reconcile(BlockState state, LevelReader level, BlockPos pos) {
		Direction.Axis axis = state.getValue(AXIS);
		boolean anySide = false;
		for (Direction direction : Direction.values()) {
			if (direction.getAxis() == axis) {
				state = state.setValue(side(direction), false);
				continue;
			}
			boolean connected = connectsSide(level.getBlockState(pos.relative(direction)), direction, axis);
			state = state.setValue(side(direction), connected);
			anySide |= connected;
		}
		if (!anySide) {
			return state.setValue(STEM, Stem.FULL);
		}
		Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		Stem stem = state.getValue(STEM);
		boolean negativeSolid = isSolid(level, pos.relative(negative));
		boolean positiveSolid = isSolid(level, pos.relative(positive));
		// A solid block only anchors an end the stem already reaches, so a joint never stretches toward a wall it wasn't hung from.
		boolean negativeAnchored = linksAlong(level.getBlockState(pos.relative(negative)), negative)
			|| (stem != Stem.HALF_TOP && negativeSolid);
		boolean positiveAnchored = linksAlong(level.getBlockState(pos.relative(positive)), positive)
			|| (stem != Stem.HALF && positiveSolid);
		if (negativeAnchored && positiveAnchored) {
			return state.setValue(STEM, Stem.FULL);
		}
		if (negativeAnchored) {
			return state.setValue(STEM, Stem.HALF);
		}
		if (positiveAnchored) {
			return state.setValue(STEM, Stem.HALF_TOP);
		}
		return stem == Stem.FULL ? state.setValue(STEM, Stem.HALF) : state;
	}

	private static boolean isSolid(LevelReader level, BlockPos pos) {
		return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
	}

	private static Direction.Axis rotateAxis(Direction.Axis axis, Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> switch (axis) {
				case X -> Direction.Axis.Z;
				case Z -> Direction.Axis.X;
				case Y -> Direction.Axis.Y;
			};
			default -> axis;
		};
	}

	private static BooleanProperty side(Direction direction) {
		return switch (direction) {
			case NORTH -> NORTH;
			case EAST -> EAST;
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case UP -> UP;
			case DOWN -> DOWN;
		};
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = stemShape(state.getValue(AXIS), state.getValue(STEM));
		for (Direction direction : Direction.values()) {
			if (state.getValue(side(direction))) {
				shape = Shapes.or(shape, armShape(direction));
			}
		}
		return shape;
	}

	private static VoxelShape stemShape(Direction.Axis axis, Stem stem) {
		return switch (axis) {
			case Y -> switch (stem) {
				case FULL -> box(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);
				case HALF -> box(6.5, 0.0, 6.5, 9.5, 9.0, 9.5);
				case HALF_TOP -> box(6.5, 7.0, 6.5, 9.5, 16.0, 9.5);
			};
			case X -> switch (stem) {
				case FULL -> box(0.0, 6.5, 6.5, 16.0, 9.5, 9.5);
				case HALF -> box(0.0, 6.5, 6.5, 9.0, 9.5, 9.5);
				case HALF_TOP -> box(7.0, 6.5, 6.5, 16.0, 9.5, 9.5);
			};
			case Z -> switch (stem) {
				case FULL -> box(6.5, 6.5, 0.0, 9.5, 9.5, 16.0);
				case HALF -> box(6.5, 6.5, 0.0, 9.5, 9.5, 9.0);
				case HALF_TOP -> box(6.5, 6.5, 7.0, 9.5, 9.5, 16.0);
			};
		};
	}

	private static VoxelShape armShape(Direction direction) {
		return switch (direction) {
			case NORTH -> box(6.5, 6.5, 0.0, 9.5, 9.5, 8.0);
			case SOUTH -> box(6.5, 6.5, 8.0, 9.5, 9.5, 16.0);
			case WEST -> box(0.0, 6.5, 6.5, 8.0, 9.5, 9.5);
			case EAST -> box(8.0, 6.5, 6.5, 16.0, 9.5, 9.5);
			case UP -> box(6.5, 8.0, 6.5, 9.5, 16.0, 9.5);
			case DOWN -> box(6.5, 0.0, 6.5, 9.5, 8.0, 9.5);
		};
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		Direction.Axis axis = state.getValue(AXIS);
		Stem stem = state.getValue(STEM);
		if (stem != Stem.FULL && rotation != Rotation.NONE) {
			Direction negativeEnd = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
			if (rotation.rotate(negativeEnd).getAxisDirection() == Direction.AxisDirection.POSITIVE) {
				stem = stem == Stem.HALF ? Stem.HALF_TOP : Stem.HALF;
			}
		}
		BlockState rotated = state.setValue(AXIS, rotateAxis(axis, rotation)).setValue(STEM, stem);
		for (Direction direction : Direction.values()) {
			rotated = rotated.setValue(side(rotation.rotate(direction)), state.getValue(side(direction)));
		}
		return rotated;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE) {
			return state;
		}
		Direction.Axis axis = state.getValue(AXIS);
		Stem stem = state.getValue(STEM);
		boolean flipStem = stem != Stem.FULL
			&& ((mirror == Mirror.LEFT_RIGHT && axis == Direction.Axis.Z)
				|| (mirror == Mirror.FRONT_BACK && axis == Direction.Axis.X));
		if (flipStem) {
			stem = stem == Stem.HALF ? Stem.HALF_TOP : Stem.HALF;
		}
		BlockState mirrored = state.setValue(STEM, stem);
		for (Direction direction : Direction.values()) {
			mirrored = mirrored.setValue(side(mirror.mirror(direction)), state.getValue(side(direction)));
		}
		return mirrored;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, STEM, NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	public enum Stem implements StringRepresentable {
		FULL("full"),
		HALF("half"),
		HALF_TOP("half_top");

		private final String name;

		Stem(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
