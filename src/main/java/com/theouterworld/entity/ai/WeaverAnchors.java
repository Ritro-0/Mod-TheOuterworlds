package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tells a Weaver where its Anchor ends and someone else's clutter begins.
 *
 * <p>Anchors are recognised from the blocks themselves rather than from saved structure
 * bounds, so a hand-built tower counts just as much as a generated one.
 */
public final class WeaverAnchors {
	/** Fibre blocks needed around a spot before the Weavers treat it as part of the Anchor. */
	private static final int ANCHOR_THRESHOLD = 6;
	private static final int ANCHOR_PROBE = 3;

	private WeaverAnchors() {
	}

	/** Terrain the Weavers grew up with, and never bother tidying. */
	public static boolean isNativeGround(BlockState state) {
		return state.isAir()
			|| state.liquid()
			|| state.is(ModBlocks.THOLIN)
			|| state.is(ModBlocks.THOLINIC_REGOLITH)
			|| state.is(ModBlocks.LIQUID_METHANE)
			|| state.is(Blocks.PACKED_ICE)
			|| state.is(Blocks.BEDROCK);
	}

	/**
	 * A block that does not belong in an Anchor and can be carried off: not part of the
	 * structure, not Amberworld's own ground, not unbreakable, and not something with an
	 * inventory of its own.
	 */
	public static boolean isLitter(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || state.is(ModTags.WEAVER_ANCHOR_PARTS) || isNativeGround(state)) {
			return false;
		}
		if (isStashedInNet(level, pos)) {
			return false;
		}
		if (state.hasBlockEntity()) {
			return false;
		}
		if (state.getBlock().asItem() == Items.AIR) {
			return false;
		}
		return state.getDestroySpeed(level, pos) >= 0.0F;
	}

	/** Blocks sitting in a hanging net are stores, not litter. */
	public static boolean isStashedInNet(Level level, BlockPos pos) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (Direction dir : Direction.values()) {
			cursor.setWithOffset(pos, dir);
			if (level.getBlockState(cursor).is(ModBlocks.WEAVER_NET)) {
				return true;
			}
		}
		return false;
	}

	/** True when enough Anchor material surrounds {@code pos} to call it Anchor ground. */
	public static boolean isInsideAnchor(BlockGetter level, BlockPos pos) {
		return countAnchorParts(level, pos, ANCHOR_PROBE, ANCHOR_THRESHOLD) >= ANCHOR_THRESHOLD;
	}

	public static int countAnchorParts(BlockGetter level, BlockPos pos, int radius, int stopAt) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int found = 0;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
					if (level.getBlockState(cursor).is(ModTags.WEAVER_ANCHOR_PARTS)) {
						found++;
						if (found >= stopAt) {
							return found;
						}
					}
				}
			}
		}
		return found;
	}

	/** Worth keeping once inspected, rather than thrown off the tower. */
	public static boolean isCuriosity(BlockState state) {
		return state.is(ModTags.WEAVER_CURIOSITIES);
	}
}
