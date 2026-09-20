package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Flat reinforced tinted-glass pane. Always a single centered face (no connection
 * geometry); horizontal facing points the north face of the model toward the player.
 * Blocks light the same way as tinted glass.
 */
public class ReinforcedTintedGlassPaneBlock extends TransparentBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	/** Matches the model pane at Z 7–9 (north/south). */
	private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0.0, 0.0, 7.0, 16.0, 16.0, 9.0);
	/** Rotated pane for east/west facing. */
	private static final VoxelShape SHAPE_EAST_WEST = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 16.0);

	public ReinforcedTintedGlassPaneBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case EAST, WEST -> SHAPE_EAST_WEST;
			default -> SHAPE_NORTH_SOUTH;
		};
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return false;
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 15;
	}
}
