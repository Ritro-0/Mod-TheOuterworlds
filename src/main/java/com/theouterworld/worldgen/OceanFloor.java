package com.theouterworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The ice crust is the ocean-floor heightmap, so seabed scans walk down from just under the sea.
 */
public final class OceanFloor {
	public static final int SCAN_TOP = 89;

	private OceanFloor() {
	}

	public static int surfaceY(BlockGetter level, int x, int z) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, SCAN_TOP, z);
		for (int y = SCAN_TOP; y >= -64; y--) {
			pos.setY(y);
			BlockState state = level.getBlockState(pos);
			if (state.isAir() || !state.getFluidState().isEmpty()) {
				continue;
			}
			return y;
		}
		return Integer.MIN_VALUE;
	}
}
