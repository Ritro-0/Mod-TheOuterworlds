package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import com.theouterworld.mixin.BeaconBlockEntityAccessor;
import com.theouterworld.registry.ModBlockEntities;
import com.theouterworld.world.BeaconConcentratorPortals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BeaconConcentratorBlock extends FallingBlock implements EntityBlock {
	public static final MapCodec<BeaconConcentratorBlock> CODEC = simpleCodec(BeaconConcentratorBlock::new);
	public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;

	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0),
		Block.box(6.0, 3.0, 6.0, 10.0, 8.0, 10.0),
		Block.box(5.0, 8.0, 5.0, 11.0, 13.0, 11.0),
		Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0)
	);

	public BeaconConcentratorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
	}

	@Override
	protected MapCodec<? extends FallingBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public int getDustColor(BlockState state, BlockGetter world, BlockPos pos) {
		return 0x9A5CC6;
	}

	@Override
	protected int getDelayAfterPlace() {
		return 2;
	}

	@Override
	protected void falling(FallingBlockEntity entity) {
		entity.disableDrop();
	}

	@Override
	public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaced, FallingBlockEntity entity) {
		shatter(level, pos, state);
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity entity) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(entity.getBlockState()));
		}
	}

	public static void shatter(Level level, BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel serverLevel) {
			if (state.hasProperty(ACTIVE) && state.getValue(ACTIVE)) {
				BeaconConcentratorPortals.deactivate(serverLevel, pos);
			}
			serverLevel.destroyBlock(pos, false);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
		BeaconConcentratorPortals.deactivate(world, pos);
		super.affectNeighborsAfterRemoval(state, world, pos, moved);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BeaconConcentratorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		if (world.isClientSide()) {
			return null;
		}
		return type == ModBlockEntities.BEACON_CONCENTRATOR
			? (w, p, s, be) -> BeaconConcentratorBlockEntity.tick(w, p, s, (BeaconConcentratorBlockEntity) be)
			: null;
	}

	public static boolean canActivate(ServerLevel world, BlockPos concentratorPos) {
		if (!world.dimension().equals(Level.OVERWORLD)) {
			return false;
		}
		BlockPos beaconPos = concentratorPos.below();
		BlockState below = world.getBlockState(beaconPos);
		if (!below.is(Blocks.BEACON)) {
			return false;
		}
		BlockEntity blockEntity = world.getBlockEntity(beaconPos);
		if (!(blockEntity instanceof BeaconBlockEntity beacon)) {
			return false;
		}
		int levels = ((BeaconBlockEntityAccessor) beacon).theouterworlds$getLevels();
		return levels >= 4 && !beacon.getBeamSections().isEmpty();
	}
}
