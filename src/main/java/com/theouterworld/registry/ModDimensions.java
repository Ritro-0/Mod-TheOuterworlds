package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.config.OuterworldConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensions {
	public static final ResourceKey<Level> OUTERWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("outerworld")
	);

	public static final ResourceKey<Level> INNERWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("innerworld")
	);

	/** Alias used by ported Outerworld systems that still reference the old key name. */
	public static final ResourceKey<Level> OUTER_WORLD_WORLD_KEY = OUTERWORLD_WORLD_KEY;

	public static final ResourceKey<DimensionType> OUTERWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		OUTERWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> INNERWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		INNERWORLD_WORLD_KEY.identifier()
	);

	public static boolean isOuterworld(ResourceKey<Level> dimension) {
		return OUTERWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isInnerworld(ResourceKey<Level> dimension) {
		return INNERWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isLowGravity(ResourceKey<Level> dimension) {
		return isOuterworld(dimension) || isInnerworld(dimension);
	}

	public static double gravityMultiplier(ResourceKey<Level> dimension) {
		if (isInnerworld(dimension)) {
			return OuterworldConfig.INNERWORLD_GRAVITY_MULTIPLIER;
		}
		if (isOuterworld(dimension)) {
			return OuterworldConfig.GRAVITY_MULTIPLIER;
		}
		return 1.0;
	}

	public static void registerModDimensions() {
		OuterWorldMod.LOGGER.info("Registering dimensions for {}", OuterWorldMod.MOD_ID);
	}
}
