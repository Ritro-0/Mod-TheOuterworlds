package com.theouterworld.world;

import com.theouterworld.item.ModItems;
import com.theouterworld.registry.ModLootTables;
import com.theouterworld.registry.ModTags;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

/** Late-game offerings a colony can press into a visitor's hands. The pool itself is a loot table. */
public final class WeaverColonyGifts {
	private WeaverColonyGifts() {
	}

	public static int valueOf(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		int quality = 1;
		if (stack.is(ModTags.WEAVER_CURIOSITY_FINE)) {
			quality = 3;
		}
		if (stack.is(ModTags.WEAVER_CURIOSITY_PRECIOUS)) {
			quality = 8;
		}
		return quality * stack.getCount();
	}

	public static ItemStack roll(ServerLevel level) {
		LootTable table = lootTable(level);
		LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
		List<ItemStack> items = table.getRandomItems(params);
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return new ItemStack(ModItems.IRIDIUM_INGOT);
	}

	private static LootTable lootTable(ServerLevel level) {
		ReloadableServerRegistries.Holder registries = level.getServer().reloadableRegistries();
		return registries.getLootTable(ModLootTables.WEAVER_COLONY_GIFT);
	}
}
