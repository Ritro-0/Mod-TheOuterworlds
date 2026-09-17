package com.theouterworld.block;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;

public class FrozenAttachedStemBlock extends AttachedStemBlock {
	public FrozenAttachedStemBlock(
		ResourceKey<Block> stem,
		ResourceKey<Block> fruit,
		ResourceKey<Item> seed,
		TagKey<Block> supportBlocks,
		Properties properties
	) {
		super(stem, fruit, seed, supportBlocks, properties);
	}
}
