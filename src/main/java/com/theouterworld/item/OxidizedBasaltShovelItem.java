package com.theouterworld.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class OxidizedBasaltShovelItem extends Item {
	public OxidizedBasaltShovelItem(ResourceKey<Item> registryKey, Item.Properties settings) {
		super(settings
			.durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
			.shovel(ModToolMaterials.OXIDIZED_BASALT_TOOLS, 1.5f, -3.0f));
	}
}
