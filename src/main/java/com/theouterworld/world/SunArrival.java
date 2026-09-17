package com.theouterworld.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places the single orange stained-glass pane and returns the stand position on The Sun.
 */
public final class SunArrival {
	private SunArrival() {
	}

	public static BlockPos prepareLanding(ServerLevel world, int x, int z) {
		int standY = SunTerrain.surfaceY(world.getMinY());
		BlockPos glass = new BlockPos(x, standY, z);
		BlockPos stand = glass.above();
		world.setBlockAndUpdate(glass, Blocks.STAINED_GLASS_PANE.pick(DyeColor.ORANGE).defaultBlockState());
		if (!world.getBlockState(stand).isAir()) {
			world.setBlockAndUpdate(stand, Blocks.AIR.defaultBlockState());
		}
		BlockPos head = stand.above();
		if (!world.getBlockState(head).isAir()) {
			world.setBlockAndUpdate(head, Blocks.AIR.defaultBlockState());
		}
		return stand;
	}

	public static void onArrived(ServerPlayer player) {
		SunDeathSequence.markArrived(player);
	}

	public static boolean isSpawnGlass(BlockState state) {
		return state.is(Blocks.STAINED_GLASS_PANE.pick(DyeColor.ORANGE));
	}
}
