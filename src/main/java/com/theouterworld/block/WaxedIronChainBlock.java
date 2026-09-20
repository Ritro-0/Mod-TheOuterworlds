package com.theouterworld.block;

import net.minecraft.tags.ItemTags;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedIronChainBlock extends ChainBlock {
    private final Block unwaxedVersion;

    public WaxedIronChainBlock(Block unwaxedVersion, Properties settings) {
        super(settings);
        this.unwaxedVersion = unwaxedVersion;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, net.minecraft.world.entity.player.Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        
        if (stack.is(ItemTags.AXES)) {
            if (world instanceof ServerLevel serverWorld) {
                // In Outerworld: return to unaffected iron chain (which will oxidize)
                // Outside Outerworld: return to vanilla chain
                Block targetBlock;
                if (serverWorld.dimension().equals(ModDimensions.OUTER_WORLD_WORLD_KEY)) {
                    targetBlock = unwaxedVersion; // UNAFFECTED_IRON_CHAIN in Outerworld
                } else {
                    // Return to vanilla chain outside Outerworld
                    targetBlock = Blocks.IRON_CHAIN;
                }
                
                BlockState newState = targetBlock.defaultBlockState();
                if (newState.hasProperty(AXIS)) {
                    newState = newState.setValue(AXIS, state.getValue(AXIS));
                }
                world.setBlockAndUpdate(pos, newState);
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

