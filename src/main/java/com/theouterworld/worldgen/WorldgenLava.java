package com.theouterworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Places source lava during worldgen and wakes it so it can actually flow.
 * {@code scheduleTick} alone is dropped or spent before neighbors exist;
 * post-processing re-ticks the fluid after the chunk is fully built.
 */
public final class WorldgenLava {
	private WorldgenLava() {
	}

	public static void place(WorldGenLevel world, BlockPos pos) {
		place(world, pos, true);
	}

	public static void place(WorldGenLevel world, BlockPos pos, boolean startFlowing) {
		if (world.getBlockState(pos).is(Blocks.BEDROCK)) {
			return;
		}
		world.setBlock(pos, Blocks.LAVA.defaultBlockState(), 2);
		if (startFlowing) {
			BlockPos frozen = pos.immutable();
			world.scheduleTick(frozen, Fluids.LAVA, 0);
			world.getChunk(frozen).markPosForPostProcessing(frozen);
		}
	}
}
