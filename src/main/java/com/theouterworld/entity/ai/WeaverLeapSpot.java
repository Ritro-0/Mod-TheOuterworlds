package com.theouterworld.entity.ai;

import com.theouterworld.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/** Shared rule for every Weaver leap: the landing column is not methane. */
public final class WeaverLeapSpot {
	private WeaverLeapSpot() {
	}

	/**
	 * {@code ground} is the block they fall onto. The three blocks above it are the body.
	 * Any methane in that column is not a landing.
	 */
	public static boolean isDry(BlockGetter level, BlockPos ground) {
		if (methane(level, ground)) {
			return false;
		}
		for (int up = 1; up <= 3; up++) {
			if (methane(level, ground.above(up))) {
				return false;
			}
		}
		return true;
	}

	public static boolean methane(BlockGetter level, BlockPos pos) {
		return level.getFluidState(pos).getType().isSame(ModFluids.LIQUID_METHANE);
	}
}
