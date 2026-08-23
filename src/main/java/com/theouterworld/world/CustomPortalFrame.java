package com.theouterworld.world;

import com.theouterworld.block.FrozenNetherPortalBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModTags;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portal-frame detection matching Vanilla Tweaks "Custom Nether Portals":
 * flood-fill a 2D plane from the ignition point, allow mixed obsidian/crying obsidian
 * frames, and accept non-rectangular enclosed shapes.
 */
public final class CustomPortalFrame {
	/** Default min obsidian in the frame (Vanilla Tweaks: 10). */
	public static final int MIN_SIZE = 10;
	/** Default max obsidian in the frame (Vanilla Tweaks: 84). */
	public static final int MAX_SIZE = 84;
	private static final int MAX_VISITED = 1024;

	private CustomPortalFrame() {
	}

	public static boolean tryFill(Level level, BlockPos origin) {
		Result alongX = flood(level, origin, Direction.Axis.X);
		if (alongX != null) {
			alongX.place(level);
			return true;
		}
		Result alongZ = flood(level, origin, Direction.Axis.Z);
		if (alongZ != null) {
			alongZ.place(level);
			return true;
		}
		return false;
	}

	private static Result flood(Level level, BlockPos origin, Direction.Axis portalAxis) {
		Direction[] neighbors = neighbors(portalAxis);
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> interior = new ArrayList<>();
		int frameSize = 0;

		queue.add(origin.immutable());
		while (!queue.isEmpty()) {
			BlockPos pos = queue.removeFirst();
			if (!visited.add(pos)) {
				continue;
			}
			if (visited.size() > MAX_VISITED || !isInWorld(level, pos)) {
				return null;
			}

			BlockState state = level.getBlockState(pos);
			if (isFrame(state)) {
				frameSize++;
				if (frameSize > MAX_SIZE) {
					return null;
				}
				continue;
			}
			if (!isInterior(state)) {
				return null;
			}
			interior.add(pos);
			for (Direction direction : neighbors) {
				BlockPos next = pos.relative(direction);
				if (!visited.contains(next)) {
					queue.add(next);
				}
			}
		}

		if (frameSize < MIN_SIZE || interior.isEmpty()) {
			return null;
		}
		return new Result(portalAxis, interior);
	}

	private static Direction[] neighbors(Direction.Axis portalAxis) {
		if (portalAxis == Direction.Axis.X) {
			return new Direction[] {Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP};
		}
		return new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
	}

	private static boolean isFrame(BlockState state) {
		return state.is(ModTags.PORTAL_FRAME);
	}

	private static boolean isInterior(BlockState state) {
		return state.isAir() || state.getBlock() instanceof BaseFireBlock;
	}

	private static boolean isInWorld(Level level, BlockPos pos) {
		int y = pos.getY();
		return y >= level.getMinY() && y <= level.getMaxY() && level.hasChunkAt(pos);
	}

	private record Result(Direction.Axis axis, List<BlockPos> interior) {
		private void place(Level level) {
			BlockState portal = ModBlocks.FROZEN_NETHER_PORTAL.defaultBlockState()
				.setValue(FrozenNetherPortalBlock.AXIS, axis);
			for (BlockPos pos : interior) {
				level.setBlock(pos, portal, Block.UPDATE_ALL);
			}
		}
	}
}
