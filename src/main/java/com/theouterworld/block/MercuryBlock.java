package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.world.DimensionClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Solid mercury that melts into liquid when a block light source is nearby.
 */
public class MercuryBlock extends Block {
	public static final MapCodec<MercuryBlock> CODEC = simpleCodec(MercuryBlock::new);
	private static final int CHECK_DELAY = 8;

	public MercuryBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.is(this) && world instanceof ServerLevel serverLevel) {
			serverLevel.scheduleTick(pos, this, CHECK_DELAY);
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
		ticks.scheduleTick(pos, this, CHECK_DELAY);
		return state;
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		tryMelt(world, pos);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		tryMelt(world, pos);
	}

	private static void tryMelt(ServerLevel world, BlockPos pos) {
		if (!world.getBlockState(pos).is(ModBlocks.MERCURY_BLOCK)) {
			return;
		}
		if (DimensionClimate.isNearMercuryMeltLight(world, pos)) {
			world.setBlock(pos, ModFluids.MERCURY.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL);
		}
	}
}
