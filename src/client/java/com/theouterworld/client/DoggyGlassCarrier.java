package com.theouterworld.client;

import net.minecraft.world.item.ItemStack;

/** Extra render state on a wolf, filled when it is wearing Doggy Glass. */
public interface DoggyGlassCarrier {
	ItemStack theouterworlds$doggyGlass();

	void theouterworlds$doggyGlass(ItemStack stack);
}
