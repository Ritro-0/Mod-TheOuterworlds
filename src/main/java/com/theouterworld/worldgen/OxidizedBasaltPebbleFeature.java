package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class OxidizedBasaltPebbleFeature extends Feature<NoneFeatureConfiguration> {
    public OxidizedBasaltPebbleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();

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
