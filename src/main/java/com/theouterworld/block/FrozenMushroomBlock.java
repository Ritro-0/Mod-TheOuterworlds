package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class FrozenMushroomBlock extends MushroomBlock {
	public FrozenMushroomBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
		super(feature, properties);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return false;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
	}

	@Override
	public boolean growMushroom(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
		return false;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return false;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return false;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
	}
}
