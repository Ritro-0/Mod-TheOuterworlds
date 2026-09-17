package com.theouterworld.block;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FrozenTorchflowerCropBlock extends TorchflowerCropBlock {
	public FrozenTorchflowerCropBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return false;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return false;
	}

	@Override
	public void growCrops(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
	}

	@Override
	public BlockState getStateForAge(int age) {
		return super.getStateForAge(Math.min(age, 1));
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return BuiltInRegistries.ITEM.getValue(OuterWorldMod.id("frozen_torchflower_seeds"));
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(getBaseSeedId());
	}
}
