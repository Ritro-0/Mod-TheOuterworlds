package com.theouterworld.block;

import net.minecraft.world.level.block.BonemealSource;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FrozenStemBlock extends StemBlock {
	public FrozenStemBlock(
		ResourceKey<Block> fruit,
		ResourceKey<Block> attachedStem,
		ResourceKey<Item> seed,
		TagKey<Block> stemSupportBlocks,
		TagKey<Block> fruitSupportBlocks,
		Properties properties
	) {
		super(fruit, attachedStem, seed, stemSupportBlocks, fruitSupportBlocks, properties);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return false;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		return false;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return false;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
	}
}
