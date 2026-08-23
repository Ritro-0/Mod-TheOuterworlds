package com.theouterworld.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizedBasaltSwordItem extends Item {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SWORD_MINEABLE = BlockTags.SWORD_EFFICIENT;
    private final net.minecraft.world.item.ToolMaterial tier;

    public OxidizedBasaltSwordItem(ResourceKey<Item> registryKey, Item.Properties settings) {
        super(computeSettings(settings));
        this.tier = ModToolMaterials.OXIDIZED_BASALT_TOOLS;
    }
    
    private static Item.Properties computeSettings(Item.Properties settings) {
        return settings
            .durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
            .tool(
                ModToolMaterials.OXIDIZED_BASALT_TOOLS,
                BlockTags.SWORD_EFFICIENT,
                3.0f, // attack damage (wood sword base)
                -2.4f, // attack speed (standard for swords)
                0.0f   // default damage blocked
            );
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(SWORD_MINEABLE) ? ModToolMaterials.OXIDIZED_BASALT_TOOLS_MINING_SPEED : 1.0f;
    }
    
    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity user) {
        return !state.requiresCorrectToolForDrops() || state.is(SWORD_MINEABLE);
    }
}

