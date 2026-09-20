package com.theouterworld.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class OxidizedBasaltHoeItem extends Item {
	public OxidizedBasaltHoeItem(ResourceKey<Item> registryKey, Item.Properties settings) {
		super(settings
			.durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
			.hoe(ModToolMaterials.OXIDIZED_BASALT_TOOLS, 0.0f, -3.0f));
	}
}
