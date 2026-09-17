package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OxidizedBasaltPebbleBlock extends Block {
    // Pressure plate-like outline shape (1 pixel high with 1 pixel margins)
    private static final VoxelShape PRESSURE_PLATE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    
    // Full cube for reliable targeting and breaking
    private static final VoxelShape FULL_CUBE = Shapes.block();
    
    // Small collision shape so entities can walk over it easily
    private static final VoxelShape COLLISION_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public OxidizedBasaltPebbleBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return PRESSURE_PLATE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return FULL_CUBE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos below = pos.below();
        return world.getBlockState(below).isFaceSturdy(world, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess ticks,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        BlockState neighborState,
        RandomSource random
    ) {
        if (!state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, world, ticks, pos, direction, neighborPos, neighborState, random);
    }
}
