package com.theouterworld.block;

import com.theouterworld.registry.ModFluids;
import com.theouterworld.world.SolarPlasmaPulse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Solar plasma: no collision, full outline/hitbox for creative targeting,
 * and air-exposed sources drive the expand/retract pulse.
 */
public class SolarPlasmaLiquidBlock extends LiquidBlock {
	private static final int TICK_INTERVAL = 2;

	public SolarPlasmaLiquidBlock(FlowingFluid fluid, Properties properties) {
		super(fluid, properties);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.block();
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide() && SolarPlasmaPulse.isAirExposedSource(level, pos)) {
			level.scheduleTick(pos, this, TICK_INTERVAL);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		FluidState fluid = state.getFluidState();
		if (fluid.isSource()) {
			if (SolarPlasmaPulse.isAirExposedSource(level, pos)) {
				SolarPlasmaPulse.pulseFromSource(level, pos);
				level.scheduleTick(pos, this, TICK_INTERVAL);
			}
			return;
		}
		if (this.isSamePlasma(fluid)) {
			SolarPlasmaPulse.tickSurge(level, pos);
		}
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
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
		if (!level.isClientSide() && SolarPlasmaPulse.isAirExposedSource(level, pos)) {
			level.scheduleTick(pos, this, TICK_INTERVAL);
		}
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
		if (level instanceof Level world && !world.isClientSide()
			&& SolarPlasmaPulse.isAirExposedSource(world, pos)) {
			ticks.scheduleTick(pos, this, TICK_INTERVAL);
		}
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockState result = super.playerWillDestroy(level, pos, state, player);
		if (!level.isClientSide()) {
			SolarPlasmaPulse.wakeNeighbors(level, pos);
		}
		return result;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		SolarPlasmaPulse.wakeNeighbors(level, pos);
	}

	private boolean isSamePlasma(FluidState fluid) {
		return fluid.getType() == ModFluids.SOLAR_PLASMA || fluid.getType() == ModFluids.FLOWING_SOLAR_PLASMA;
	}
}
