package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ProcessorBlock extends BaseEntityBlock {
    public static final MapCodec<ProcessorBlock> CODEC = simpleCodec(ProcessorBlock::new);
    
    // Custom shape based on the blockbench model
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(4, 0, 4, 12, 2, 12),   // Base
        Block.box(6, 2, 6, 10, 3, 10),   // Lower support
        Block.box(7, 3, 7, 9, 10, 9),    // Central pillar
        Block.box(4, 10, 4, 12, 11, 12), // Top arms
        Block.box(4, 11, 4, 12, 13, 12)  // Top ring
    );

    public ProcessorBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProcessorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return (type == ModBlockEntities.PROCESSOR)
            ? (w, p, s, be) -> ProcessorBlockEntity.tick(w, p, s, (ProcessorBlockEntity) be)
            : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ProcessorBlockEntity processor) {
            player.openMenu(processor);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (!moved) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ProcessorBlockEntity processorEntity) {
                Containers.dropContents(world, pos, processorEntity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}

