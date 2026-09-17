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

	/** The Moon (former Innerworld). Beacon concentrators travel here. */
	public static final ResourceKey<Level> MOON_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("moon")
	);

	/** The Innerworld — Mercury. */
	public static final ResourceKey<Level> INNERWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("innerworld")
	);

	/** The Nearworld — Venus. */
	public static final ResourceKey<Level> NEARWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("nearworld")
	);

	/** The Highworld — Jupiter. */
	public static final ResourceKey<Level> HIGHWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("highworld")
	);

	/** The Deepworld — Saturn. */
	public static final ResourceKey<Level> DEEPWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("deepworld")
	);

	/** The Farworld — Uranus. */
	public static final ResourceKey<Level> FARWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("farworld")
	);

	/** The Edgeworld — Neptune. */
	public static final ResourceKey<Level> EDGEWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("edgeworld")
	);

	/** The Emberworld — Io, moon of Highworld. */
	public static final ResourceKey<Level> EMBERWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("emberworld")
	);

	/** The Frostworld — Europa, moon of Highworld. */
	public static final ResourceKey<Level> FROSTWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("frostworld")
	);

	/** The Amberworld — Titan, moon of Deepworld. */
	public static final ResourceKey<Level> AMBERWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("amberworld")
	);

	/** The Spongeworld — Hyperion, porous ice moon of Deepworld. */
	public static final ResourceKey<Level> SPONGEWORLD_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("spongeworld")
	);

	/** The Potatoworlds — Phobos & Deimos, twin potato moons of Outerworld. */
	public static final ResourceKey<Level> POTATOWORLDS_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("potatoworlds")
	);

	/** The Wanderlands — Ceres, dwarf planet between Outerworld and Highworld. */
	public static final ResourceKey<Level> WANDERLANDS_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("wanderlands")
	);

	/** The Beyondlands — Pluto. */
	public static final ResourceKey<Level> BEYONDLANDS_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("beyondlands")
	);

	/** The Beyondlands II — Charon. */
	public static final ResourceKey<Level> BEYONDLANDS_II_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("beyondlands_ii")
	);

	/** The Spinlands — Haumea. */
	public static final ResourceKey<Level> SPINLANDS_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("spinlands")
	);

	/** The Scarletlands — Makemake. */
	public static final ResourceKey<Level> SCARLETLANDS_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("scarletlands")
	);

	/** The Lonelands — Eris. */
	public static final ResourceKey<Level> LONELANDS_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("lonelands")
	);

	/** The Sun — visiting is a spectacularly bad idea. */
	public static final ResourceKey<Level> SUN_WORLD_KEY = ResourceKey.create(
		Registries.DIMENSION,
		OuterWorldMod.id("sun")
	);

	/** Alias used by ported Outerworld systems that still reference the old key name. */
	public static final ResourceKey<Level> OUTER_WORLD_WORLD_KEY = OUTERWORLD_WORLD_KEY;

	public static final ResourceKey<DimensionType> OUTERWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		OUTERWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> MOON_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		MOON_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> INNERWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		INNERWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> NEARWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		NEARWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> HIGHWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		HIGHWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> DEEPWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		DEEPWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> FARWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		FARWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> EDGEWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		EDGEWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> EMBERWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		EMBERWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> FROSTWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		FROSTWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> AMBERWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		AMBERWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> SPONGEWORLD_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		SPONGEWORLD_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> POTATOWORLDS_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		POTATOWORLDS_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> WANDERLANDS_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		WANDERLANDS_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> BEYONDLANDS_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		BEYONDLANDS_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> BEYONDLANDS_II_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		BEYONDLANDS_II_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> SPINLANDS_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		SPINLANDS_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> SCARLETLANDS_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		SCARLETLANDS_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> LONELANDS_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		LONELANDS_WORLD_KEY.identifier()
	);

	public static final ResourceKey<DimensionType> SUN_DIMENSION_TYPE = ResourceKey.create(
		Registries.DIMENSION_TYPE,
		SUN_WORLD_KEY.identifier()
	);

	public static boolean isOuterworld(ResourceKey<Level> dimension) {
		return OUTERWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isMoon(ResourceKey<Level> dimension) {
		return MOON_WORLD_KEY.equals(dimension);
	}

	public static boolean isInnerworld(ResourceKey<Level> dimension) {
		return INNERWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isNearworld(ResourceKey<Level> dimension) {
		return NEARWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isHighworld(ResourceKey<Level> dimension) {
		return HIGHWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isDeepworld(ResourceKey<Level> dimension) {
		return DEEPWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isFarworld(ResourceKey<Level> dimension) {
		return FARWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isEdgeworld(ResourceKey<Level> dimension) {
		return EDGEWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isEmberworld(ResourceKey<Level> dimension) {
		return EMBERWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isFrostworld(ResourceKey<Level> dimension) {
		return FROSTWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isAmberworld(ResourceKey<Level> dimension) {
		return AMBERWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isSpongeworld(ResourceKey<Level> dimension) {
		return SPONGEWORLD_WORLD_KEY.equals(dimension);
	}

	public static boolean isPotatoworlds(ResourceKey<Level> dimension) {
		return POTATOWORLDS_WORLD_KEY.equals(dimension);
	}

	public static boolean isWanderlands(ResourceKey<Level> dimension) {
		return WANDERLANDS_WORLD_KEY.equals(dimension);
	}

	public static boolean isBeyondlands(ResourceKey<Level> dimension) {
		return BEYONDLANDS_WORLD_KEY.equals(dimension);
	}

	public static boolean isBeyondlandsIi(ResourceKey<Level> dimension) {
		return BEYONDLANDS_II_WORLD_KEY.equals(dimension);
	}

	public static boolean isSpinlands(ResourceKey<Level> dimension) {
		return SPINLANDS_WORLD_KEY.equals(dimension);
	}

	public static boolean isScarletlands(ResourceKey<Level> dimension) {
		return SCARLETLANDS_WORLD_KEY.equals(dimension);
	}

	public static boolean isLonelands(ResourceKey<Level> dimension) {
		return LONELANDS_WORLD_KEY.equals(dimension);
	}

	public static boolean isSun(ResourceKey<Level> dimension) {
		return SUN_WORLD_KEY.equals(dimension);
	}

	/** Pluto / Charon pair used for high-altitude crossover. */
	public static boolean isBeyondlandsPair(ResourceKey<Level> dimension) {
		return isBeyondlands(dimension) || isBeyondlandsIi(dimension);
	}

	/**
	 * Moons and dwarf planets that require a Quantum Pod (not a bare Rift Pad).
	 * THE Moon is excluded — it remains rift-pad accessible.
	 */
	public static boolean requiresQuantumPod(ResourceKey<Level> dimension) {
		return isWanderlands(dimension)
			|| isBeyondlands(dimension)
			|| isBeyondlandsIi(dimension)
			|| isSpinlands(dimension)
			|| isScarletlands(dimension)
			|| isLonelands(dimension)
			|| isEmberworld(dimension)
			|| isFrostworld(dimension)
			|| isAmberworld(dimension)
			|| isSpongeworld(dimension)
			|| isPotatoworlds(dimension)
			|| isSun(dimension);
	}

	/** Gas-giant cloud decks used for rift-pad arrival. */
	public static boolean isGasGiant(ResourceKey<Level> dimension) {
		return isHighworld(dimension) || isDeepworld(dimension) || isFarworld(dimension) || isEdgeworld(dimension);
	}

	/**
	 * Dimensions where metallic hydrogen stays liquid instead of crystallizing.
	 */
	public static boolean keepsMetallicHydrogen(ResourceKey<Level> dimension) {
		return isHighworld(dimension)
			|| isDeepworld(dimension)
			|| isFarworld(dimension)
			|| isEdgeworld(dimension);
	}

	/**
	 * Dimensions where metallic helium stays liquid instead of crystallizing.
	 */
	public static boolean keepsMetallicHelium(ResourceKey<Level> dimension) {
		return isDeepworld(dimension)
			|| isHighworld(dimension)
			|| isFarworld(dimension)
			|| isEdgeworld(dimension);
	}

	/**
	 * Dimensions where ionic ammonia stays liquid instead of crystallizing.
	 */
	public static boolean keepsIonicAmmonia(ResourceKey<Level> dimension) {
		return isFarworld(dimension)
			|| isHighworld(dimension)
			|| isDeepworld(dimension)
			|| isEdgeworld(dimension);
	}

	/**
	 * Dimensions where ionic methane stays liquid instead of crystallizing.
	 */
	public static boolean keepsIonicMethane(ResourceKey<Level> dimension) {
		return isEdgeworld(dimension)
			|| isFarworld(dimension)
			|| isHighworld(dimension)
			|| isDeepworld(dimension);
	}

	/** Custom planetary dimensions that share climate / light conversion hooks. */
	public static boolean isLowGravity(ResourceKey<Level> dimension) {
		return isOuterworld(dimension)
			|| isMoon(dimension)
			|| isInnerworld(dimension)
			|| isNearworld(dimension)
			|| isHighworld(dimension)
			|| isDeepworld(dimension)
			|| isFarworld(dimension)
			|| isEdgeworld(dimension)
			|| isEmberworld(dimension)
			|| isFrostworld(dimension)
			|| isAmberworld(dimension)
			|| isSpongeworld(dimension)
			|| isPotatoworlds(dimension)
			|| isWanderlands(dimension)
			|| isBeyondlands(dimension)
			|| isBeyondlandsIi(dimension)
			|| isSpinlands(dimension)
			|| isScarletlands(dimension)
			|| isLonelands(dimension)
			|| isSun(dimension);
	}

	/** Freeze water/lava, frozen portals, plant freeze — Outerworld and Moon.
	 * Frostworld keeps a liquid subsurface ocean, so it is not a cold-climate converter. */
	public static boolean isColdClimate(ResourceKey<Level> dimension) {
		return isOuterworld(dimension)
			|| isMoon(dimension)
			|| isPotatoworlds(dimension)
			|| isWanderlands(dimension)
			|| isBeyondlands(dimension)
			|| isBeyondlandsIi(dimension)
			|| isSpinlands(dimension)
			|| isScarletlands(dimension)
			|| isLonelands(dimension);
	}

	/** Melt ice/water on contact, snuff fire — Innerworld (Mercury) and gas giants. */
	public static boolean isHotClimate(ResourceKey<Level> dimension) {
		return isInnerworld(dimension)
			|| isHighworld(dimension)
			|| isDeepworld(dimension)
			|| isFarworld(dimension)
			|| isEdgeworld(dimension);
	}

	/**
	 * Nearworld (Venus) and Emberworld (Io): melt ice/water, keep fire/lava active (ultrawarm), no portals.
	 * Distinct from Innerworld which snuffs fire in vacuum.
	 */
	public static boolean isScorchingClimate(ResourceKey<Level> dimension) {
		return isNearworld(dimension) || isEmberworld(dimension) || isSun(dimension);
	}

	/** Unbreathable atmosphere — helmet required. */
	public static boolean isVacuum(ResourceKey<Level> dimension) {
		return isOuterworld(dimension)
			|| isMoon(dimension)
			|| isInnerworld(dimension)
			|| isNearworld(dimension)
			|| isHighworld(dimension)
			|| isDeepworld(dimension)
			|| isFarworld(dimension)
			|| isEdgeworld(dimension)
			|| isEmberworld(dimension)
			|| isFrostworld(dimension)
			|| isSpongeworld(dimension)
			|| isPotatoworlds(dimension)
			|| isWanderlands(dimension)
			|| isBeyondlands(dimension)
			|| isBeyondlandsIi(dimension)
			|| isSpinlands(dimension)
			|| isScarletlands(dimension)
			|| isLonelands(dimension)
			|| isSun(dimension);
	}

	public static double gravityMultiplier(ResourceKey<Level> dimension) {
		if (isSun(dimension)) {
			return OuterworldConfig.SUN_GRAVITY_MULTIPLIER;
		}
		if (isMoon(dimension)) {
			return OuterworldConfig.MOON_GRAVITY_MULTIPLIER;
		}
		if (isBeyondlands(dimension)) {
			return OuterworldConfig.BEYONDLANDS_GRAVITY_MULTIPLIER;
		}
		if (isBeyondlandsIi(dimension)) {
			return floorCustomGravity(OuterworldConfig.BEYONDLANDS_II_GRAVITY_MULTIPLIER);
		}
		if (isSpinlands(dimension)) {
			return floorCustomGravity(OuterworldConfig.SPINLANDS_GRAVITY_MULTIPLIER);
		}
		if (isScarletlands(dimension)) {
			return floorCustomGravity(OuterworldConfig.SCARLETLANDS_GRAVITY_MULTIPLIER);
		}
		if (isLonelands(dimension)) {
			return floorCustomGravity(OuterworldConfig.LONELANDS_GRAVITY_MULTIPLIER);
		}
		if (isWanderlands(dimension)) {
			return floorCustomGravity(OuterworldConfig.WANDERLANDS_GRAVITY_MULTIPLIER);
		}
		if (isInnerworld(dimension)) {
			return OuterworldConfig.INNERWORLD_GRAVITY_MULTIPLIER;
		}
		if (isNearworld(dimension)) {
			return OuterworldConfig.NEARWORLD_GRAVITY_MULTIPLIER;
		}
		if (isHighworld(dimension)) {
			return OuterworldConfig.HIGHWORLD_GRAVITY_MULTIPLIER;
		}
		if (isDeepworld(dimension)) {
			return OuterworldConfig.DEEPWORLD_GRAVITY_MULTIPLIER;
		}
		if (isFarworld(dimension)) {
			return OuterworldConfig.FARWORLD_GRAVITY_MULTIPLIER;
		}
		if (isEdgeworld(dimension)) {
			return OuterworldConfig.EDGEWORLD_GRAVITY_MULTIPLIER;
		}
		if (isEmberworld(dimension)) {
			return OuterworldConfig.EMBERWORLD_GRAVITY_MULTIPLIER;
		}
		if (isFrostworld(dimension)) {
			return OuterworldConfig.FROSTWORLD_GRAVITY_MULTIPLIER;
		}
		if (isAmberworld(dimension)) {
			return OuterworldConfig.AMBERWORLD_GRAVITY_MULTIPLIER;
		}
		if (isSpongeworld(dimension)) {
			return floorCustomGravity(OuterworldConfig.SPONGEWORLD_GRAVITY_MULTIPLIER);
		}
		if (isPotatoworlds(dimension)) {
			return floorCustomGravity(OuterworldConfig.POTATOWORLDS_GRAVITY_MULTIPLIER);
		}
		if (isOuterworld(dimension)) {
			return floorCustomGravity(OuterworldConfig.GRAVITY_MULTIPLIER);
		}
		return 1.0;
	}

	/** Custom gravity below 0.04g does not apply reliably. */
	private static double floorCustomGravity(double gravity) {
		if (gravity > 0.0 && gravity < 1.0) {
			return Math.max(OuterworldConfig.MIN_GRAVITY_MULTIPLIER, gravity);
		}
		return gravity;
	}

	/** Earth-relative surface air density. 1.0 is Overworld. */
	public static double atmosphereDensity(ResourceKey<Level> dimension) {
		if (isSun(dimension)) {
			return OuterworldConfig.SUN_ATMOSPHERE_DENSITY;
		}
		if (isMoon(dimension)) {
			return OuterworldConfig.MOON_ATMOSPHERE_DENSITY;
		}
		if (isBeyondlands(dimension)) {
			return OuterworldConfig.BEYONDLANDS_ATMOSPHERE_DENSITY;
		}
		if (isBeyondlandsIi(dimension)) {
			return OuterworldConfig.BEYONDLANDS_II_ATMOSPHERE_DENSITY;
		}
		if (isSpinlands(dimension)) {
			return OuterworldConfig.SPINLANDS_ATMOSPHERE_DENSITY;
		}
		if (isScarletlands(dimension)) {
			return OuterworldConfig.SCARLETLANDS_ATMOSPHERE_DENSITY;
		}
		if (isLonelands(dimension)) {
			return OuterworldConfig.LONELANDS_ATMOSPHERE_DENSITY;
		}
		if (isWanderlands(dimension)) {
			return OuterworldConfig.WANDERLANDS_ATMOSPHERE_DENSITY;
		}
		if (isInnerworld(dimension)) {
			return OuterworldConfig.INNERWORLD_ATMOSPHERE_DENSITY;
		}
		if (isNearworld(dimension)) {
			return OuterworldConfig.NEARWORLD_ATMOSPHERE_DENSITY;
		}
		if (isHighworld(dimension)) {
			return OuterworldConfig.HIGHWORLD_ATMOSPHERE_DENSITY;
		}
		if (isDeepworld(dimension)) {
			return OuterworldConfig.DEEPWORLD_ATMOSPHERE_DENSITY;
		}
		if (isFarworld(dimension)) {
			return OuterworldConfig.FARWORLD_ATMOSPHERE_DENSITY;
		}
		if (isEdgeworld(dimension)) {
			return OuterworldConfig.EDGEWORLD_ATMOSPHERE_DENSITY;
		}
		if (isEmberworld(dimension)) {
			return OuterworldConfig.EMBERWORLD_ATMOSPHERE_DENSITY;
		}
		if (isFrostworld(dimension)) {
			return OuterworldConfig.FROSTWORLD_ATMOSPHERE_DENSITY;
		}
		if (isAmberworld(dimension)) {
			return OuterworldConfig.AMBERWORLD_ATMOSPHERE_DENSITY;
		}
		if (isSpongeworld(dimension)) {
			return OuterworldConfig.SPONGEWORLD_ATMOSPHERE_DENSITY;
		}
		if (isPotatoworlds(dimension)) {
			return OuterworldConfig.POTATOWORLDS_ATMOSPHERE_DENSITY;
		}
		if (isOuterworld(dimension)) {
			return OuterworldConfig.OUTERWORLD_ATMOSPHERE_DENSITY;
		}
		return 1.0;
	}

	/**
	 * Vanilla elytra drag to keep, relative to the Overworld (1.0).
	 * Vacuums cancel air friction entirely; thin air glides; dense Venus/gas-giant air adds drag.
	 */
	public static double elytraDragFraction(ResourceKey<Level> dimension) {
		// Titan's haze is thick for survival flavor, but low-g flight should soar —
		// don't punish Amberworld elytra with Nearworld/gas-giant drag math.
		if (isAmberworld(dimension)) {
			double gravity = gravityMultiplier(dimension);
			final double vacuumGlide = 0.00337;
			double air = 0.18;
			double airTerm = vacuumGlide + (1.0 - vacuumGlide) * (air * air);
			return Math.max(0.001, airTerm * (0.70 + 0.30 * gravity));
		}

		double air = atmosphereDensity(dimension);
		double gravity = gravityMultiplier(dimension);
		if (air >= 0.999 && gravity >= 0.999 && !isGasGiant(dimension)) {
			return 1.0;
		}

		// True vacuums (Moon, Mercury, Emberworld, Frostworld): no atmospheric drag.
		if (air <= 0.0) {
			return 0.0;
		}

		final double vacuumGlide = 0.00337;
		double airTerm;
		if (air < 1.0) {
			airTerm = vacuumGlide + (1.0 - vacuumGlide) * (air * air);
		} else {
			airTerm = 1.0 + Math.log10(air);
		}
		return Math.max(0.001, airTerm * (0.70 + 0.30 * gravity));
	}

	public static void registerModDimensions() {
		OuterWorldMod.LOGGER.info("Registering dimensions for {}", OuterWorldMod.MOD_ID);
	}
}
