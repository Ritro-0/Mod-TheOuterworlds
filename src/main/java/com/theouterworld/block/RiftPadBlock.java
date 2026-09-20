package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.screen.RiftPadMenu;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RiftPadBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final Component TITLE = Component.translatable("container.theouterworlds.rift_pad");
	private static final Component WRONG_DIMENSION = Component.translatable("gui.theouterworlds.rift_pad.wrong_dimension");

	/** Matches the OBJ: base ~20×2×20 and pillar up to y=25. */
	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(-2.0, 0.0, -2.0, 18.0, 2.0, 18.0),
		Block.box(6.0, 1.0, 6.0, 10.0, 25.0, 10.0)
	);

	public RiftPadBlock(Properties properties) {
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
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RiftPadBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		if (world.isClientSide()) {
			return null;
		}
		return type == ModBlockEntities.RIFT_PAD
			? (w, p, s, be) -> RiftPadBlockEntity.tick(w, p, s, (RiftPadBlockEntity) be)
			: null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (!isSupportedDimension(world.dimension())) {
			if (!world.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				serverPlayer.sendSystemMessage(WRONG_DIMENSION, true);
			}
			return InteractionResult.SUCCESS;
		}
		if (!world.isClientSide()) {
			player.openMenu(state.getMenuProvider(world, pos));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
		return new SimpleMenuProvider(
			(containerId, inventory, player) -> new RiftPadMenu(containerId, inventory, pos),
			TITLE
		);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!world.isClientSide() && world.getBlockEntity(pos) instanceof RiftPadBlockEntity pad) {
			pad.onBroken();
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel world, BlockPos pos, boolean moved) {
		// Prefer linked-pos from BE when still available (explosions / setBlock); else playerWillDestroy already ran.
		if (world.getBlockEntity(pos) instanceof RiftPadBlockEntity pad) {
			pad.onBroken();
		}
		super.affectNeighborsAfterRemoval(state, world, pos, moved);
	}

	public static boolean isSupportedDimension(net.minecraft.resources.ResourceKey<Level> dimension) {
		return dimension == Level.OVERWORLD
			|| ModDimensions.isOuterworld(dimension)
			|| ModDimensions.isMoon(dimension)
			|| ModDimensions.isInnerworld(dimension)
			|| ModDimensions.isNearworld(dimension)
			|| ModDimensions.isHighworld(dimension)
			|| ModDimensions.isDeepworld(dimension)
			|| ModDimensions.isFarworld(dimension)
			|| ModDimensions.isEdgeworld(dimension)
			|| ModDimensions.isEmberworld(dimension)
			|| ModDimensions.isFrostworld(dimension)
			|| ModDimensions.isAmberworld(dimension)
			|| ModDimensions.isSpongeworld(dimension)
			|| ModDimensions.isPotatoworlds(dimension)
			|| ModDimensions.isWanderlands(dimension)
			|| ModDimensions.isBeyondlands(dimension)
			|| ModDimensions.isBeyondlandsIi(dimension)
			|| ModDimensions.isSpinlands(dimension)
			|| ModDimensions.isScarletlands(dimension)
			|| ModDimensions.isLonelands(dimension)
			|| ModDimensions.isSun(dimension);
	}

	/** Destinations the bare rift pad may visit (planets + THE Moon). */
	public static boolean isRiftPadDestination(net.minecraft.resources.ResourceKey<Level> dimension) {
		return isSupportedDimension(dimension) && !ModDimensions.requiresQuantumPod(dimension);
	}
}
