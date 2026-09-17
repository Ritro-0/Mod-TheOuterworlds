package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.worldgen.StretchZDensityFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModDensityFunctions {
	private ModDensityFunctions() {
	}

	public static void register() {
		Registry.register(
			BuiltInRegistries.DENSITY_FUNCTION_TYPE,
			OuterWorldMod.id("stretch_z"),
			StretchZDensityFunction.CODEC.codec()
		);
		OuterWorldMod.LOGGER.info("Registered density functions for {}", OuterWorldMod.MOD_ID);
	}
}
