package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;

public class OxidizedBasaltPebbleFeature implements Feature {
	public static final MapCodec<OxidizedBasaltPebbleFeature> CODEC = MapCodec.unit(OxidizedBasaltPebbleFeature::new);

    public OxidizedBasaltPebbleFeature() {
    }

	@Override
	public MapCodec<OxidizedBasaltPebbleFeature> codec() {
		return CODEC;
	}

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {

        int surfaceY = world.getHeight(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            origin.getX(),
            origin.getZ()
        );

        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        BlockState currentState = world.getBlockState(surfacePos);

        if (currentState.isAir() || currentState.canBeReplaced()) {
            BlockPos belowPos = surfacePos.below();
            BlockState belowState = world.getBlockState(belowPos);

            if (!belowState.isAir() && belowState.isRedstoneConductor(world, belowPos)) {
                BlockState pebbleState = ModBlocks.OXIDIZED_BASALT_PEBBLE.defaultBlockState();
                world.setBlock(surfacePos, pebbleState, 3);
                return true;
            }
        }

        return false;
    }
}
