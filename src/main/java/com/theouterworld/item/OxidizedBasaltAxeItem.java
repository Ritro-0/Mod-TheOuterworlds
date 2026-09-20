package com.theouterworld.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class OxidizedBasaltAxeItem extends Item {
	public OxidizedBasaltAxeItem(ResourceKey<Item> registryKey, Item.Properties settings) {
		super(settings
			.durability(ModToolMaterials.OXIDIZED_BASALT_TOOLS_DURABILITY)
			.axe(ModToolMaterials.OXIDIZED_BASALT_TOOLS, 6.0f, -3.2f));
	}
}
