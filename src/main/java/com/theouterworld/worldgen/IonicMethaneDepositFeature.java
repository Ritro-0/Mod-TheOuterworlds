package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.world.EdgeworldLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.FluidState;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Rare thin ionic-methane lenses floating a few blocks above the iron/nickel crust
 * inside the liquid-methane ocean (size 1–3, at most 12 above metal top).
 */
public class IonicMethaneDepositFeature implements Feature {
	public static final MapCodec<IonicMethaneDepositFeature> CODEC = MapCodec.unit(IonicMethaneDepositFeature::new);

	private static final int MIN_ABOVE_METAL = 3;
	private static final int MAX_ABOVE_METAL = 12;

	public IonicMethaneDepositFeature() {
	}

	@Override
	public MapCodec<IonicMethaneDepositFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		int x = origin.getX() + random.nextInt(16);
		int z = origin.getZ() + random.nextInt(16);
		int y = EdgeworldLayers.METAL_TOP_Y + MIN_ABOVE_METAL + random.nextInt(MAX_ABOVE_METAL - MIN_ABOVE_METAL + 1);
		cursor.set(x, y, z);
		if (!isLiquidMethane(world.getFluidState(cursor))) {
			return false;
		}

		int size = 1;
		float roll = random.nextFloat();
		if (roll < 0.20F) {
			size = 2;
		} else if (roll < 0.28F) {
			size = 3;
		}
		return placeBlob(world, cursor.immutable(), size, random);
	}

	private static boolean placeBlob(WorldGenLevel world, BlockPos center, int size, RandomSource random) {
		BlockState ionic = ModBlocks.IONIC_METHANE.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int placed = 0;

		for (int i = 0; i < size * 6 && placed < size; i++) {
			int dx = size == 1 ? 0 : random.nextInt(3) - 1;
			int dy = size == 1 ? 0 : random.nextInt(3) - 1;
			int dz = size == 1 ? 0 : random.nextInt(3) - 1;
			cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
			if (cursor.getY() < EdgeworldLayers.METAL_TOP_Y + MIN_ABOVE_METAL
				|| cursor.getY() > EdgeworldLayers.METAL_TOP_Y + MAX_ABOVE_METAL) {
				continue;
			}
			if (!isLiquidMethane(world.getFluidState(cursor))) {
				continue;
			}
			world.setBlock(cursor, ionic, 2);
			placed++;
		}

		if (placed == 0 && isLiquidMethane(world.getFluidState(center))) {
			world.setBlock(center, ionic, 2);
			return true;
		}
		return placed > 0;
	}

	private static boolean isLiquidMethane(FluidState fluid) {
		return fluid.is(ModFluids.LIQUID_METHANE) || fluid.is(ModFluids.FLOWING_LIQUID_METHANE);
	}
}
