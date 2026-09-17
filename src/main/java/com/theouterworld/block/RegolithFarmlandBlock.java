package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Farmland made from regolith. Crops can be planted on it, but it never hydrates.
 */
public class RegolithFarmlandBlock extends Block {
	private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 15.0);

	public RegolithFarmlandBlock(Properties properties) {
		super(properties);
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
		if (direction == Direction.UP && !state.canSurvive(level, pos)) {
			ticks.scheduleTick(pos, this, 1);
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState above = level.getBlockState(pos.above());
		return !above.isSolid() || maintainsFarmland(level, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return !this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
			? ModBlocks.REGOLITH.defaultBlockState()
			: super.getStateForPlacement(context);
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			turnToRegolith(null, state, level, pos);
		}
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!maintainsFarmland(level, pos)) {
			turnToRegolith(null, state, level, pos);
		}
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		if (level instanceof ServerLevel serverLevel
			&& level.getRandom().nextFloat() < fallDistance - 0.5
			&& entity instanceof LivingEntity
			&& (entity instanceof Player || serverLevel.getGameRules().get(GameRules.MOB_GRIEFING))
			&& entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F
		) {
			turnToRegolith(entity, state, level, pos);
		}
		super.fallOn(level, state, pos, entity, fallDistance);
	}

	public static void turnToRegolith(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
		BlockState dirt = pushEntitiesUp(state, ModBlocks.REGOLITH.defaultBlockState(), level, pos);
		level.setBlockAndUpdate(pos, dirt);
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, dirt));
	}

	private static boolean maintainsFarmland(BlockGetter level, BlockPos pos) {
		return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}
}
