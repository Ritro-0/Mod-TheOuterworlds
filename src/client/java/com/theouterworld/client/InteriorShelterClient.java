package com.theouterworld.client;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;

/**
 * Client-side cache of sealed interior cells synced from the server.
 */
public final class InteriorShelterClient {
	private static final LongOpenHashSet INTERIOR_CELLS = new LongOpenHashSet();

	private InteriorShelterClient() {
	}

	public static void replaceAll(long[] cells) {
		INTERIOR_CELLS.clear();
		for (long cell : cells) {
			INTERIOR_CELLS.add(cell);
		}
	}

	public static void clear() {
		INTERIOR_CELLS.clear();
	}

	public static boolean isInterior(BlockPos pos) {
		return INTERIOR_CELLS.contains(pos.asLong());
	}

	public static boolean isInterior(int x, int y, int z) {
		return INTERIOR_CELLS.contains(BlockPos.asLong(x, y, z));
	}

	public static boolean isInterior(double x, double y, double z) {
		return isInterior(BlockPos.containing(x, y, z));
	}

	public static LongOpenHashSet cells() {
		return INTERIOR_CELLS;
	}

	public static int size() {
		return INTERIOR_CELLS.size();
	}
}
