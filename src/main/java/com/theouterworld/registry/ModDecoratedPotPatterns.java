package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;

public final class ModDecoratedPotPatterns {
	public static final ResourceKey<DecoratedPotPattern> ROVER = ResourceKey.create(
		Registries.DECORATED_POT_PATTERN,
		OuterWorldMod.id("rover")
	);

	public static final ResourceKey<Item> ROVER_POTTERY_SHERD = ResourceKey.create(
		Registries.ITEM,
		OuterWorldMod.id("rover_pottery_sherd")
	);

	private ModDecoratedPotPatterns() {}

	public static void register() {
		Registry.register(
			BuiltInRegistries.DECORATED_POT_PATTERN,
			ROVER,
			new DecoratedPotPattern(OuterWorldMod.id("rover_pottery_pattern"))
		);
	}
}
