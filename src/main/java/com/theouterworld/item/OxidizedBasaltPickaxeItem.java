package com.theouterworld.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizedBasaltPickaxeItem extends Item {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> PICKAXE_MINEABLE = BlockTags.MINEABLE_WITH_PICKAXE;
    
    public OxidizedBasaltPickaxeItem(ResourceKey<Item> registryKey, Item.Properties settings) {
        super(computeSettings(settings));
    }
    
    private static Item.Properties computeSettings(Item.Properties settings) {
        // Use the settings.pickaxe() method like Paxels does
        return settings
            .durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
            .pickaxe(
                ModToolMaterials.OXIDIZED_BASALT_TOOLS,
                1.0f, // attack damage (wood pickaxe base)
                -2.8f // attack speed (standard for pickaxes)
            );
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Return the mining speed from ToolMaterial when block is mineable, like Paxels does
        return state.is(PICKAXE_MINEABLE) ? ModToolMaterials.OXIDIZED_BASALT_TOOLS_MINING_SPEED : 1.0f;
    }
}

