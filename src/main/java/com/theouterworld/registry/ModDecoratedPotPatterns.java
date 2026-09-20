package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;

/**
 * Rover pot pattern is a datapack registry entry
 * ({@code data/theouterworlds/decorated_pot_pattern/rover.json}).
 * The sherd binds via {@link net.minecraft.world.item.Item.Properties#potPattern}.
 */
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
		// Pattern is loaded from datapack JSON; sherd links via Item.Properties.potPattern.
	}
}
