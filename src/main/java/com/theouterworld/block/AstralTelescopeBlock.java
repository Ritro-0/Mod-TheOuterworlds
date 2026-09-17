package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import com.theouterworld.screen.AstralTelescopeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AstralTelescopeBlock extends BaseEntityBlock {
	public static final MapCodec<AstralTelescopeBlock> CODEC = simpleCodec(AstralTelescopeBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final Component TITLE = Component.translatable("container.theouterworlds.astral_telescope");
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public AstralTelescopeBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
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
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AstralTelescopeBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (!world.isClientSide()) {
			player.openMenu(state.getMenuProvider(world, pos));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
		return new SimpleMenuProvider(
			(containerId, inventory, player) -> new AstralTelescopeMenu(containerId, inventory, pos),
			TITLE
		);
	}

	/**
	 * True when skylight reaches the block above the telescope, or that block is
	 * transparent / non-full and does not fully occlude the sky path.
	 */
	public static boolean hasSkyAccess(Level world, BlockPos telescopePos) {
		BlockPos above = telescopePos.above();
		if (world.getBrightness(LightLayer.SKY, above) > 0 || world.canSeeSky(above)) {
			return true;
		}
		BlockState aboveState = world.getBlockState(above);
		VoxelShape shape = aboveState.getCollisionShape(world, above);
		boolean nonFull = shape.isEmpty() || !Block.isShapeFullBlock(shape);
		boolean transparent = !aboveState.canOcclude();
		if (!(nonFull || transparent)) {
			return false;
		}
		return world.canSeeSky(above.above()) || world.getBrightness(LightLayer.SKY, above.above()) > 0;
	}
}
