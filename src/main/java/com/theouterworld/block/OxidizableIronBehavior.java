package com.theouterworld.block;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizableIronBehavior {
    /**
     * Check if a block state is an unwaxed oxidizable iron block
     */
    public static boolean isUnwaxedOxidizableIron(BlockState state) {
        return state.getBlock() instanceof OxidizableIronBlock;
    }

    /**
     * Count nearby unwaxed oxidizable iron blocks at or above the given oxidation level
     * Checks all 26 neighbors (3x3x3 minus center)
     */
    public static int countNearbyUnwaxedOxidizableIron(LevelReader world, BlockPos pos, WeatheringCopper.WeatherState minLevel) {
        int count = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip center
                    
                    mutable.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockState neighborState = world.getBlockState(mutable);
                    
                    if (isUnwaxedOxidizableIron(neighborState)) {
                        OxidizableIronBlock neighborBlock = (OxidizableIronBlock) neighborState.getBlock();
                        if (neighborBlock.getAge().ordinal() >= minLevel.ordinal()) {
                            count++;
                        }
                    }
                }
            }
        }
        
        return count;
    }

    /**
     * Get the oxidation chance based on current level and neighbor count
     * Based on vanilla CopperBlock logic
     */
    public static float getOxidationChance(WeatheringCopper.WeatherState currentLevel, int higherNeighborCount) {
        return getOxidationChance(currentLevel, higherNeighborCount, null);
    }

    public static float getOxidationChance(WeatheringCopper.WeatherState currentLevel, int higherNeighborCount, Block block) {
        if (currentLevel == WeatheringCopper.WeatherState.OXIDIZED) {
            return 0.0f; // Can't oxidize further
        }
        
        float baseChance = 0.05688889f; // Base chance from vanilla
        float neighborMultiplier = 1.0f + (higherNeighborCount * 0.05f); // 5% increase per neighbor
        
        // Stage-specific multipliers (from vanilla)
        float stageMultiplier = switch (currentLevel) {
            case UNAFFECTED -> 1.0f;
            case EXPOSED -> 0.75f;
            case WEATHERED -> 0.5f;
            case OXIDIZED -> 0.0f;
        };

        float chance = baseChance * neighborMultiplier * stageMultiplier;
        if (block != null && Corrosion.isCorroded(block)) {
            chance = Math.min(1.0f, chance * 4.0f);
        }
        return chance;
    }

    /**
     * Get the delay before next oxidation check
     * Based on vanilla CopperBlock delays
     */
    public static int getNextOxidationDelay(WeatheringCopper.WeatherState currentLevel) {
        return switch (currentLevel) {
            case UNAFFECTED -> 50; // ~2.5 seconds
            case EXPOSED -> 100; // ~5 seconds
            case WEATHERED -> 200; // ~10 seconds
            case OXIDIZED -> Integer.MAX_VALUE; // Never
        };
    }

    /**
     * Check if oxidation should occur in this dimension
     * Only oxidizes in the Outerworld dimension
     */
    public static boolean shouldOxidize(ServerLevel world, BlockPos pos) {
        return world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY);
    }

    /**
     * Comparator output for iron bulbs, matching copper bulb light levels per oxidation stage:
     * 15 / 12 / 8 / 4 when lit, 0 when unlit.
     */
    public static int bulbComparatorSignal(boolean lit, WeatheringCopper.WeatherState age) {
        if (!lit) {
            return 0;
        }
        return switch (age) {
            case UNAFFECTED -> 15;
            case EXPOSED -> 12;
            case WEATHERED -> 8;
            case OXIDIZED -> 4;
        };
    }
}

