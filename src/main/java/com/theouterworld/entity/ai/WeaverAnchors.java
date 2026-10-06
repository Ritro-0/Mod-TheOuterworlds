package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModTags;
import com.theouterworld.world.WeaverColonySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
	 * structure, not Amberworld's own ground, and not unbreakable. A block that opens a
	 * screen is still clutter, even when it keeps an inventory.
	 */
	public static boolean isLitter(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || state.is(ModTags.WEAVER_ANCHOR_PARTS) || isNativeGround(state)) {
			return false;
		}
		if (state.hasBlockEntity() && !hasScreen(state, level, pos)) {
			return false;
		}
		if (state.getBlock().asItem() == Items.AIR) {
			return false;
		}
		if (state.getDestroySpeed(level, pos) < 0.0F) {
			return false;
		}
		return !(level instanceof ServerLevel server) || !WeaverColonySavedData.get(server).isWeaverPlaced(pos, state);
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

	public static boolean isCuriosity(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Block block = Block.byItem(stack.getItem());
		return block != Blocks.AIR && isCuriosity(block.defaultBlockState());
	}

	/** Crafting tables, chests, and anything else that opens a screen. */
	public static boolean hasScreen(BlockState state, Level level, BlockPos pos) {
		if (state.getMenuProvider(level, pos) != null) {
			return true;
		}
		return level.getBlockEntity(pos) instanceof MenuProvider;
	}

	public static boolean carriedHasScreen(ItemStack stack, Level level, BlockPos at) {
		if (stack.isEmpty()) {
			return false;
		}
		Block block = Block.byItem(stack.getItem());
		if (block == Blocks.AIR) {
			return false;
		}
		BlockState state = block.defaultBlockState();
		if (state.getMenuProvider(level, at) != null) {
			return true;
		}
		if (block instanceof EntityBlock entityBlock) {
			BlockEntity created = entityBlock.newBlockEntity(at, state);
			return created instanceof MenuProvider;
		}
		return false;
	}

	/**
	 * Stone, dirt, regolith, or ore that Amberworld itself does not grow.
	 * Curiosities are judged separately and can still earn a gift.
	 */
	public static boolean isSpecimen(BlockState state) {
		if (isNativeGround(state) || state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
			return false;
		}
		String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
		if (path.contains("tholin")
			|| path.contains("stairs")
			|| path.contains("slab")
			|| path.contains("wall")
			|| path.contains("brick")
			|| path.contains("polished")
			|| path.contains("chiseled")
			|| path.contains("farmland")
			|| path.contains("suspicious")) {
			return false;
		}
		return path.endsWith("_ore")
			|| path.contains("regolith")
			|| path.equals("dirt")
			|| path.equals("coarse_dirt")
			|| path.equals("rooted_dirt")
			|| path.equals("podzol")
			|| path.equals("mycelium")
			|| path.equals("mud")
			|| path.equals("clay")
			|| path.equals("gravel")
			|| path.equals("sand")
			|| path.equals("red_sand")
			|| path.equals("sandstone")
			|| path.equals("red_sandstone")
			|| path.equals("stone")
			|| path.equals("cobblestone")
			|| path.equals("deepslate")
			|| path.equals("cobbled_deepslate")
			|| path.equals("tuff")
			|| path.equals("calcite")
			|| path.equals("dripstone_block")
			|| path.equals("andesite")
			|| path.equals("diorite")
			|| path.equals("granite")
			|| path.equals("netherrack")
			|| path.equals("blackstone")
			|| path.equals("basalt")
			|| path.equals("smooth_basalt")
			|| path.equals("end_stone")
			|| path.equals("soul_sand")
			|| path.equals("soul_soil")
			|| path.equals("anorthosite")
			|| path.equals("norite")
			|| path.equals("gabbro")
			|| path.equals("enstatite")
			|| path.equals("komatiite")
			|| path.equals("pyroxenite")
			|| path.equals("anhydrite")
			|| path.equals("oxidized_basalt")
			|| path.equals("sulfuric_basalt");
	}
}
