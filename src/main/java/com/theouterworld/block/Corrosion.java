package com.theouterworld.block;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class Corrosion {
	private static final Map<Block, Block> CORRODED_BY_BLOCK = new HashMap<>();
	private static final Map<Block, Block> ORIGINAL_BY_CORRODED = new HashMap<>();
	private static final Map<Block, Block> WAXED_BY_CORRODED = new HashMap<>();

	private Corrosion() {
	}

	public static void register(Block original, Block corroded) {
		CORRODED_BY_BLOCK.put(original, corroded);
		ORIGINAL_BY_CORRODED.put(corroded, original);
	}

	public static void registerWaxed(Block corroded, Block waxed) {
		WAXED_BY_CORRODED.put(corroded, waxed);
	}

	public static boolean isCorroded(Block block) {
		return ORIGINAL_BY_CORRODED.containsKey(block);
	}

	public static boolean isCorroded(BlockState state) {
		return isCorroded(state.getBlock());
	}

	@Nullable
	public static BlockState getCorroded(BlockState state) {
		Block corroded = CORRODED_BY_BLOCK.get(state.getBlock());
		return corroded == null ? null : corroded.withPropertiesOf(state);
	}

	@Nullable
	public static BlockState getUncorroded(BlockState state) {
		Block original = ORIGINAL_BY_CORRODED.get(state.getBlock());
		return original == null ? null : original.withPropertiesOf(state);
	}

	@Nullable
	public static BlockState getWaxed(BlockState state) {
		Block waxed = WAXED_BY_CORRODED.get(state.getBlock());
		return waxed == null ? null : waxed.withPropertiesOf(state);
	}

	public static Map<Block, Block> mappings() {
		return CORRODED_BY_BLOCK;
	}

	public static boolean tryApply(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack) {
		BlockState corroded = getCorroded(state);
		if (corroded == null) {
			return false;
		}
		if (!world.isClientSide()) {
			world.setBlockAndUpdate(pos, corroded);
			world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, corroded));
			world.levelEvent(player, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, pos, 0);
			world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 0.8f);
			if (player == null || !player.isCreative()) {
				stack.shrink(1);
			}
		}
		return true;
	}

	public static boolean tryRemove(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack) {
		BlockState original = getUncorroded(state);
		if (original == null) {
			return false;
		}
		if (!world.isClientSide()) {
			world.setBlockAndUpdate(pos, original);
			world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, original));
			world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
			world.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0);
			if (player != null && !player.isCreative()) {
				stack.hurtAndBreak(1, player, player.getUsedItemHand());
			}
		}
		return true;
	}
}
