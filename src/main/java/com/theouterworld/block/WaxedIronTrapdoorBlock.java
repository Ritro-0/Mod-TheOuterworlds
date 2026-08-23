package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedIronTrapdoorBlock extends TrapDoorBlock {
    private final Block unwaxedVersion;

    public WaxedIronTrapdoorBlock(Block unwaxedVersion, BlockSetType blockSetType, Properties settings) {
        super(blockSetType, settings);
        this.unwaxedVersion = unwaxedVersion;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, net.minecraft.world.entity.player.Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        
        if (stack.getItem() instanceof AxeItem) {
            if (world instanceof ServerLevel) {
                BlockState newState = unwaxedVersion.defaultBlockState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(OPEN, state.getValue(OPEN))
                    .setValue(HALF, state.getValue(HALF))
                    .setValue(POWERED, state.getValue(POWERED))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                world.setBlockAndUpdate(pos, newState);
                world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                world.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, player.getUsedItemHand());
                }
                
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }
        
        // Call super for normal trapdoor behavior
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    public Block getUnwaxedVersion() {
        return unwaxedVersion;
    }
}

