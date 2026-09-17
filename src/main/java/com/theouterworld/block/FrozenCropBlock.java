package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import com.theouterworld.OuterWorldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A crop that has already frozen. It keeps its current age and does not grow further.
 */
public class FrozenCropBlock extends CropBlock {
	public static final MapCodec<FrozenCropBlock> CODEC = simpleCodec(properties ->
		new FrozenCropBlock(properties, "frozen_wheat_seeds")
	);

	private final String seedId;

	public FrozenCropBlock(Properties properties, String seedId) {
		super(properties);
		this.seedId = seedId;
	}

	@Override
	public MapCodec<? extends CropBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return false;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return false;
	}

	@Override
	public void growCrops(Level level, BlockPos pos, BlockState state) {
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return BuiltInRegistries.ITEM.getValue(OuterWorldMod.id(this.seedId));
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(getBaseSeedId());
	}
}
