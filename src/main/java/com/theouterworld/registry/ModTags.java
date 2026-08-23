package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
	public static final TagKey<Block> CORRODED_BLOCKS = TagKey.create(Registries.BLOCK, OuterWorldMod.id("corroded"));
	public static final TagKey<Block> PORTAL_FRAME = TagKey.create(Registries.BLOCK, OuterWorldMod.id("portal_frame"));
	public static final TagKey<Block> GYPSUM_REPLACEABLE = TagKey.create(Registries.BLOCK, OuterWorldMod.id("gypsum_replaceable_blocks"));
	public static final TagKey<Block> LAVA_TUBE_REPLACEABLE = TagKey.create(Registries.BLOCK, OuterWorldMod.id("lava_tube_replaceable"));
	public static final TagKey<Block> CONVERTS_MERCURY_TO_CINNABAR = TagKey.create(Registries.BLOCK, OuterWorldMod.id("converts_mercury_to_cinnabar"));
	public static final TagKey<Item> JAROSITE_PROTECTED = TagKey.create(Registries.ITEM, OuterWorldMod.id("jarosite_protected"));
	public static final TagKey<DamageType> JAROSITE_IMMUNE = TagKey.create(Registries.DAMAGE_TYPE, OuterWorldMod.id("jarosite_immune"));
}
