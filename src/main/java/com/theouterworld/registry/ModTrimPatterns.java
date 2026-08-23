package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public final class ModTrimPatterns {
	public static final ResourceKey<TrimPattern> ORBIT = ResourceKey.create(
		Registries.TRIM_PATTERN,
		OuterWorldMod.id("orbit")
	);

	private ModTrimPatterns() {}
}
