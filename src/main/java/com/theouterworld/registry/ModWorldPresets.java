package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public final class ModWorldPresets {
	public static final ResourceKey<WorldPreset> OUTERWORLD = ResourceKey.create(
		Registries.WORLD_PRESET,
		OuterWorldMod.id("outerworld")
	);

	/** Marker file written into a world's root when created with the Outerworld preset. */
	public static final String PRESET_MARKER_FILE = "theouterworlds_outerworld_preset";

	private ModWorldPresets() {
	}
}
