package com.theouterworld.config;

/**
 * Hardcoded gravity and atmosphere for each world, relative to Earth.
 * Gravity: Mars 0.377g, Moon 0.165g, Mercury 0.37g, Venus 0.90g, Jupiter 2.5g.
 * Air density: Mars ~0.02, Moon/Mercury vacuum, Venus ~55×, Jupiter crushing.
 */
public class OuterworldConfig {
	public static final double GRAVITY_MULTIPLIER = 0.377;
	public static final double MOON_GRAVITY_MULTIPLIER = 0.165;
	public static final double INNERWORLD_GRAVITY_MULTIPLIER = 0.37;
	public static final double NEARWORLD_GRAVITY_MULTIPLIER = 0.90;
	public static final double HIGHWORLD_GRAVITY_MULTIPLIER = 2.5;
	/** Saturn — near-Earth gravity under a thick envelope. */
	public static final double DEEPWORLD_GRAVITY_MULTIPLIER = 1.1;
	/** Uranus — slightly under Earth gravity. */
	public static final double FARWORLD_GRAVITY_MULTIPLIER = 0.89;

	/** Earth-relative surface air density (1.0 = Overworld). */
	public static final double OUTERWORLD_ATMOSPHERE_DENSITY = 0.02;
	public static final double MOON_ATMOSPHERE_DENSITY = 0.0;
	public static final double INNERWORLD_ATMOSPHERE_DENSITY = 0.0;
	public static final double NEARWORLD_ATMOSPHERE_DENSITY = 55.0;
	/** Dense Jovian envelope — heavy elytra drag under 2.5g. */
	public static final double HIGHWORLD_ATMOSPHERE_DENSITY = 40.0;
	/** Dense Saturnian envelope — still thick, less crushing than Jupiter. */
	public static final double DEEPWORLD_ATMOSPHERE_DENSITY = 20.0;
	/** Dense ice-giant envelope. */
	public static final double FARWORLD_ATMOSPHERE_DENSITY = 15.0;
	/** Neptune — 1.14g under a thick blue envelope. */
	public static final double EDGEWORLD_GRAVITY_MULTIPLIER = 1.14;
	public static final double EDGEWORLD_ATMOSPHERE_DENSITY = 12.0;
	/** Io (Emberworld) — ~0.183g, near-vacuum sulfur surface. */
	public static final double EMBERWORLD_GRAVITY_MULTIPLIER = 0.183;
	public static final double EMBERWORLD_ATMOSPHERE_DENSITY = 0.0;
	/** Europa (Frostworld) — ~0.134g, near-vacuum ice shell. */
	public static final double FROSTWORLD_GRAVITY_MULTIPLIER = 0.134;
	public static final double FROSTWORLD_ATMOSPHERE_DENSITY = 0.0;
	/** Titan (Amberworld) — ~0.14g under a thick, breathable haze. */
	public static final double AMBERWORLD_GRAVITY_MULTIPLIER = 0.14;
	public static final double AMBERWORLD_ATMOSPHERE_DENSITY = 1.45;
	/** Hyperion (Spongeworld) — tiny rubble-pile ice sponge in vacuum. */
	public static final double SPONGEWORLD_GRAVITY_MULTIPLIER = 0.05;
	public static final double SPONGEWORLD_ATMOSPHERE_DENSITY = 0.0;
	/** Phobos & Deimos (Potatoworlds) — twin potato moons in vacuum. */
	public static final double POTATOWORLDS_GRAVITY_MULTIPLIER = 0.04;
	public static final double POTATOWORLDS_ATMOSPHERE_DENSITY = 0.0;
	/** Ceres (Wanderlands) — dwarf planet in vacuum (~0.029g). */
	public static final double WANDERLANDS_GRAVITY_MULTIPLIER = 0.04;
	public static final double WANDERLANDS_ATMOSPHERE_DENSITY = 0.0;
	/** Pluto (Beyondlands) — ~0.063g vacuum. */
	public static final double BEYONDLANDS_GRAVITY_MULTIPLIER = 0.063;
	public static final double BEYONDLANDS_ATMOSPHERE_DENSITY = 0.0;
	/** Charon (Beyondlands II) — ~0.029g vacuum. */
	public static final double BEYONDLANDS_II_GRAVITY_MULTIPLIER = 0.04;
	public static final double BEYONDLANDS_II_ATMOSPHERE_DENSITY = 0.0;
	/** Haumea (Spinlands) — ~0.044g vacuum. */
	public static final double SPINLANDS_GRAVITY_MULTIPLIER = 0.044;
	public static final double SPINLANDS_ATMOSPHERE_DENSITY = 0.0;
	/** Makemake (Scarletlands) — ~0.04g vacuum. Physics floor is 0.04g. */
	public static final double SCARLETLANDS_GRAVITY_MULTIPLIER = 0.04;
	public static final double SCARLETLANDS_ATMOSPHERE_DENSITY = 0.0;
	/** Eris (Lonelands) — ~0.08g vacuum. */
	public static final double LONELANDS_GRAVITY_MULTIPLIER = 0.08;
	public static final double LONELANDS_ATMOSPHERE_DENSITY = 0.0;
	/** The Sun — crushing surface gravity under plasma. */
	public static final double SUN_GRAVITY_MULTIPLIER = 27.9;
	public static final double SUN_ATMOSPHERE_DENSITY = 0.0;
	/** Below this, entity gravity in this pack stops applying reliably. */
	public static final double MIN_GRAVITY_MULTIPLIER = 0.04;

	private static final OuterworldConfig INSTANCE = new OuterworldConfig();

	public double gravityMultiplier = GRAVITY_MULTIPLIER;

	private OuterworldConfig() {
	}

	public static void register() {
	}

	public static OuterworldConfig get() {
		return INSTANCE;
	}
}
