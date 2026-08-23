package com.theouterworld.item;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizedBasaltAxeItem extends Item {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> AXE_MINEABLE = BlockTags.MINEABLE_WITH_AXE;
    private static final Map<Block, Block> STRIPPED_BLOCKS = Axe.getStrippables();
    
    public OxidizedBasaltAxeItem(ResourceKey<Item> registryKey, Item.Properties settings) {
        super(computeSettings(settings));
    }
    
    private static Item.Properties computeSettings(Item.Properties settings) {
        // Use .axe() method like Paxels does
        return settings
            .durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
            .axe(
                ModToolMaterials.OXIDIZED_BASALT_TOOLS,
                6.0f, // attack damage (wood axe base)
                -3.2f // attack speed (standard for axes)
            );
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Return the mining speed from ToolMaterial when block is mineable, like Paxels does
        return state.is(AXE_MINEABLE) ? ModToolMaterials.OXIDIZED_BASALT_TOOLS_MINING_SPEED : 1.0f;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Player player = context.getPlayer();
        
        // Cancel if player is holding shield in offhand (like vanilla axes)
        if (player != null && context.getHand() == InteractionHand.MAIN_HAND && player.getOffhandItem().is(net.minecraft.world.item.Items.SHIELD) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        
        // Handle wood stripping (like vanilla axes)
        if (STRIPPED_BLOCKS.containsKey(state.getBlock())) {
            world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide()) {
                BlockState strippedState = STRIPPED_BLOCKS.get(state.getBlock()).defaultBlockState();
                // Preserve axis property if present
                if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                    strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                }
                world.setBlock(pos, strippedState, 11);
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        // Handle campfire extinguishing (like vanilla axes)
        if (state.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
            world.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false), 11);
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    // Inner class to access protected STRIPPED_BLOCKS field, like Paxels does
    private static final class Axe extends AxeItem {
        public static Map<Block, Block> getStrippables() {
            return AxeItem.STRIPPABLES;
        }
        
        private Axe(net.minecraft.world.item.ToolMaterial tier, float attackDamage, float attackSpeed, Properties settings) {
            super(tier, attackDamage, attackSpeed, settings);
        }
    }
}

