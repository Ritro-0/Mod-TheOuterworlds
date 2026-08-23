package com.theouterworld.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class OxidizedBasaltHoeItem extends HoeItem {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> HOE_MINEABLE = BlockTags.MINEABLE_WITH_HOE;
    
    public OxidizedBasaltHoeItem(ResourceKey<net.minecraft.world.item.Item> registryKey, Properties settings) {
        super(
            ModToolMaterials.OXIDIZED_BASALT_TOOLS,
            0.0f, // attack damage (wood hoe base - 0, shown as 1 in game)
            -3.0f, // attack speed (standard for hoes)
            computeSettings(settings)
        );
    }
    
    private static Properties computeSettings(Properties settings) {
        // Set durability - HoeItem already handles the tool component via .hoe() in constructor
        return settings.durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
        // Return the mining speed from ToolMaterial when block is mineable, like Paxels does
        return state.is(HOE_MINEABLE) ? ModToolMaterials.OXIDIZED_BASALT_TOOLS_MINING_SPEED : 1.0f;
    }
}
