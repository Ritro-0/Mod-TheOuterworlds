package com.theouterworld.item;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizedBasaltShovelItem extends Item {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SHOVEL_MINEABLE = BlockTags.MINEABLE_WITH_SHOVEL;
    private static final Map<Block, BlockState> FLATTENABLES = Shovel.getFlattenables();
    
    public OxidizedBasaltShovelItem(ResourceKey<Item> registryKey, Item.Properties settings) {
        super(computeSettings(settings));
    }
    
    private static Item.Properties computeSettings(Item.Properties settings) {
        // Use .shovel() method like Paxels does
        return settings
            .durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
            .shovel(
                ModToolMaterials.OXIDIZED_BASALT_TOOLS,
                1.5f, // attack damage (wood shovel base)
                -3.0f // attack speed (standard for shovels)
            );
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Return the mining speed from ToolMaterial when block is mineable, like Paxels does
        return state.is(SHOVEL_MINEABLE) ? ModToolMaterials.OXIDIZED_BASALT_TOOLS_MINING_SPEED : 1.0f;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        
        // Handle path creation (like vanilla shovels)
        if (context.getClickedFace() != Direction.DOWN && FLATTENABLES.containsKey(state.getBlock()) && world.getBlockState(pos.above()).isAir()) {
            world.playSound(context.getPlayer(), pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide()) {
                world.setBlock(pos, FLATTENABLES.get(state.getBlock()), 11);
                if (context.getPlayer() != null) {
                    context.getItemInHand().hurtAndBreak(1, context.getPlayer(), net.minecraft.world.InteractionHand.MAIN_HAND);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    // Inner class to access protected PATH_STATES field, like Paxels does
    private static final class Shovel extends ShovelItem {
        public static Map<Block, BlockState> getFlattenables() {
            return ShovelItem.FLATTENABLES;
        }
        
        private Shovel(net.minecraft.world.item.ToolMaterial tier, float attackDamage, float attackSpeed, Properties settings) {
            super(tier, attackDamage, attackSpeed, settings);
        }
    }
}

