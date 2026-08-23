package com.theouterworld.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

/**
 * Full iron-block variants that can form an iron golem body, plus helpers to
 * read the rust stage of a constructed golem.
 */
public final class IronGolemBlocks {
	private static final ThreadLocal<PendingSpawn> PENDING = new ThreadLocal<>();

	private IronGolemBlocks() {
	}

	public static boolean isGolemBody(BlockState state) {
		return oxidationOf(state.getBlock()) >= 0;
	}

	public static int oxidationOf(Block block) {
		if (block == Blocks.IRON_BLOCK
			|| block == ModBlocks.UNAFFECTED_IRON
			|| block == ModBlocks.WAXED_IRON) {
			return 0;
		}
		if (block == ModBlocks.EXPOSED_IRON || block == ModBlocks.WAXED_EXPOSED_IRON) {
			return 1;
		}
		if (block == ModBlocks.WEATHERED_IRON || block == ModBlocks.WAXED_WEATHERED_IRON) {
			return 2;
		}
		if (block == ModBlocks.OXIDIZED_IRON || block == ModBlocks.WAXED_OXIDIZED_IRON) {
			return 3;
		}
		return -1;
	}

	public static boolean isWaxedGolemBody(Block block) {
		return block == ModBlocks.WAXED_IRON
			|| block == ModBlocks.WAXED_EXPOSED_IRON
			|| block == ModBlocks.WAXED_WEATHERED_IRON
			|| block == ModBlocks.WAXED_OXIDIZED_IRON;
	}

	public static boolean isCustomGolemBody(Block block) {
		return block != Blocks.IRON_BLOCK && oxidationOf(block) >= 0;
	}

	public static void captureFromPattern(BlockPattern.BlockPatternMatch match) {
		int maxOxidation = 0;
		boolean anyCustom = false;
		int bodyCount = 0;
		int waxedCount = 0;

		for (int x = 0; x < match.getWidth(); x++) {
			for (int y = 0; y < match.getHeight(); y++) {
				for (int z = 0; z < match.getDepth(); z++) {
					BlockInWorld inWorld = match.getBlock(x, y, z);
					Block block = inWorld.getState().getBlock();
					int oxidation = oxidationOf(block);
					if (oxidation < 0) {
						continue;
					}
					bodyCount++;
					maxOxidation = Math.max(maxOxidation, oxidation);
					if (isCustomGolemBody(block)) {
						anyCustom = true;
					}
					if (isWaxedGolemBody(block)) {
						waxedCount++;
					}
				}
			}
		}

		PENDING.set(new PendingSpawn(maxOxidation, bodyCount > 0 && waxedCount == bodyCount, anyCustom));
	}

	public static PendingSpawn takePending() {
		PendingSpawn pending = PENDING.get();
		PENDING.remove();
		return pending;
	}

	public record PendingSpawn(int oxidation, boolean waxed, boolean custom) {
	}
}
