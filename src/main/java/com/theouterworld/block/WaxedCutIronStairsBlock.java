package com.theouterworld.block;

import net.minecraft.tags.ItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedCutIronStairsBlock extends StairBlock {
    private final Block unwaxedVersion;

    public WaxedCutIronStairsBlock(Block unwaxedVersion, BlockState baseBlockState, Properties settings) {
        super(baseBlockState, settings);
        this.unwaxedVersion = unwaxedVersion;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        
        if (stack.is(ItemTags.AXES)) {
            if (world instanceof ServerLevel serverWorld) {
                world.setBlockAndUpdate(pos, unwaxedVersion.withPropertiesOf(state));
                world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                world.playSound(null, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, player.getUsedItemHand());
                }
                
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }

    public Block getUnwaxedVersion() {
        return unwaxedVersion;
    }
}

