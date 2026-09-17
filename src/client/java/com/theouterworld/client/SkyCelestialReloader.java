package com.theouterworld.client;

/** Signals SkyRenderer to rebuild custom sun/moon GPU buffers after a resource reload. */
public final class SkyCelestialReloader {
	private static boolean stale;

	private SkyCelestialReloader() {
	}

	public static void markStale() {
		stale = true;
	}

	public static boolean consumeStale() {
		if (!stale) {
			return false;
		}
		stale = false;
		return true;
	}
}
