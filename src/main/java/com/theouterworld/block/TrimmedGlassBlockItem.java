package com.theouterworld.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Named like the glass it was forged from. The trim line comes from the trim component. */
public class TrimmedGlassBlockItem extends BlockItem {
	public TrimmedGlassBlockItem(TrimmedGlassBlock block, Properties properties) {
		super(block, properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		TrimmedGlassBlock.Kind kind = TrimmedGlassBlock.kindOf(stack);
		if (kind == null) {
			kind = TrimmedGlassBlock.Kind.GLASS;
		}
		return Component.translatable(kind.descriptionId());
	}
}
