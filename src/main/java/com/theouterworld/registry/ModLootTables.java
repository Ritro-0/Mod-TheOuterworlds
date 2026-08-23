package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class ModLootTables {
	public static final ResourceKey<LootTable> SUSPICIOUS_REGOLITH = ResourceKey.create(
		Registries.LOOT_TABLE,
		OuterWorldMod.id("archaeology/suspicious_regolith")
	);

	private ModLootTables() {
	}
}
