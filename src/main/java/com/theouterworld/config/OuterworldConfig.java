package com.theouterworld.config;

/**
 * Hardcoded gravity multipliers.
 * Outerworld (Mars): 3.71 / 9.81 ≈ 0.377
 * Innerworld (Moon): 1.62 / 9.81 ≈ 0.165
 */
public class OuterworldConfig {
	public static final double GRAVITY_MULTIPLIER = 0.377;
	public static final double INNERWORLD_GRAVITY_MULTIPLIER = 0.165;

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
