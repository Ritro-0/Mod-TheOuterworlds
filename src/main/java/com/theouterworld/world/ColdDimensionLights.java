package com.theouterworld.world;

import com.theouterworld.block.CorrodedBlocks;
import com.theouterworld.block.FastWeatheringCopperBlock;
import com.theouterworld.block.FastWeatheringCopperSlabBlock;
import com.theouterworld.block.FastWeatheringCopperStairsBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Torches go out and lanterns freeze in vacuum dimensions.
 * Both can only be re-lit outside those dimensions.
 */
public final class ColdDimensionLights {
	private ColdDimensionLights() {
	}

	public static void register() {
	}

	public static boolean isFireSource(ItemStack stack) {
		return stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE);
	}

	public static BlockState convert(BlockState state) {
		Block replacement = replacementOf(state.getBlock());
		return replacement == null ? state : replacement.withPropertiesOf(state);
	}

	public static InteractionResult relightTorch(
		ItemStack stack,
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		InteractionHand hand
	) {
		if (ModDimensions.isLowGravity(level.dimension())) {
			return InteractionResult.FAIL;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		boolean wall = state.hasProperty(HorizontalDirectionalBlock.FACING);
		BlockState support = level.getBlockState(supportPos(pos, state, wall));
		Block floor;
		Block wallBlock;
		if (support.is(Blocks.REDSTONE_BLOCK)) {
			floor = Blocks.REDSTONE_TORCH;
			wallBlock = Blocks.REDSTONE_WALL_TORCH;
		} else if (support.is(BlockTags.SOUL_FIRE_BASE_BLOCKS)) {
			floor = Blocks.SOUL_TORCH;
			wallBlock = Blocks.SOUL_WALL_TORCH;
		} else if (isCopperSupport(support)) {
			floor = Blocks.COPPER_TORCH;
			wallBlock = Blocks.COPPER_WALL_TORCH;
		} else {
			floor = Blocks.TORCH;
			wallBlock = Blocks.WALL_TORCH;
		}
		Block lit = wall ? wallBlock : floor;
		level.setBlock(pos, lit.withPropertiesOf(state), Block.UPDATE_ALL);
		consumeFireSource(stack, player, hand, level, pos);
		return InteractionResult.SUCCESS;
	}

	public static InteractionResult relightLantern(
		ItemStack stack,
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		InteractionHand hand
	) {
		if (ModDimensions.isLowGravity(level.dimension())) {
			return InteractionResult.FAIL;
		}
		Block lit = litLanternOf(state.getBlock());
		if (lit == null) {
			return InteractionResult.FAIL;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		level.setBlock(pos, lit.withPropertiesOf(state), Block.UPDATE_ALL);
		consumeFireSource(stack, player, hand, level, pos);
		return InteractionResult.SUCCESS;
	}

	public static Block litLanternOf(Block frozen) {
		if (frozen == ModBlocks.FROZEN_LANTERN) return Blocks.LANTERN;
		if (frozen == ModBlocks.FROZEN_EXPOSED_LANTERN) return ModBlocks.EXPOSED_LANTERN;
		if (frozen == ModBlocks.FROZEN_WEATHERED_LANTERN) return ModBlocks.WEATHERED_LANTERN;
		if (frozen == ModBlocks.FROZEN_OXIDIZED_LANTERN) return ModBlocks.OXIDIZED_LANTERN;
		if (frozen == ModBlocks.FROZEN_WAXED_LANTERN) return ModBlocks.WAXED_LANTERN;
		if (frozen == ModBlocks.FROZEN_WAXED_EXPOSED_LANTERN) return ModBlocks.WAXED_EXPOSED_LANTERN;
		if (frozen == ModBlocks.FROZEN_WAXED_WEATHERED_LANTERN) return ModBlocks.WAXED_WEATHERED_LANTERN;
		if (frozen == ModBlocks.FROZEN_WAXED_OXIDIZED_LANTERN) return ModBlocks.WAXED_OXIDIZED_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_LANTERN) return CorrodedBlocks.CORRODED_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_EXPOSED_LANTERN) return CorrodedBlocks.CORRODED_EXPOSED_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_WEATHERED_LANTERN) return CorrodedBlocks.CORRODED_WEATHERED_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_OXIDIZED_LANTERN) return CorrodedBlocks.CORRODED_OXIDIZED_LANTERN;
		if (frozen == ModBlocks.FROZEN_SOUL_LANTERN) return Blocks.SOUL_LANTERN;
		if (frozen == ModBlocks.FROZEN_COPPER_LANTERN) return Blocks.COPPER_LANTERN.weathering().unaffected();
		if (frozen == ModBlocks.FROZEN_EXPOSED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.weathering().exposed();
		if (frozen == ModBlocks.FROZEN_WEATHERED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.weathering().weathered();
		if (frozen == ModBlocks.FROZEN_OXIDIZED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.weathering().oxidized();
		if (frozen == ModBlocks.FROZEN_WAXED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.waxed().unaffected();
		if (frozen == ModBlocks.FROZEN_WAXED_EXPOSED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.waxed().exposed();
		if (frozen == ModBlocks.FROZEN_WAXED_WEATHERED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.waxed().weathered();
		if (frozen == ModBlocks.FROZEN_WAXED_OXIDIZED_COPPER_LANTERN) return Blocks.COPPER_LANTERN.waxed().oxidized();
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_COPPER_LANTERN) return CorrodedBlocks.CORRODED_COPPER_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_EXPOSED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_EXPOSED_COPPER_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_WEATHERED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_WEATHERED_COPPER_LANTERN;
		if (frozen == CorrodedBlocks.CORRODED_FROZEN_OXIDIZED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_OXIDIZED_COPPER_LANTERN;
		return null;
	}

	private static Block replacementOf(Block placed) {
		if (isFloorTorch(placed)) {
			return ModBlocks.UNLIT_TORCH;
		}
		if (isWallTorch(placed)) {
			return ModBlocks.UNLIT_WALL_TORCH;
		}
		return frozenLanternOf(placed);
	}

	private static Block frozenLanternOf(Block placed) {
		if (placed == Blocks.LANTERN) return ModBlocks.FROZEN_LANTERN;
		if (placed == ModBlocks.EXPOSED_LANTERN) return ModBlocks.FROZEN_EXPOSED_LANTERN;
		if (placed == ModBlocks.WEATHERED_LANTERN) return ModBlocks.FROZEN_WEATHERED_LANTERN;
		if (placed == ModBlocks.OXIDIZED_LANTERN) return ModBlocks.FROZEN_OXIDIZED_LANTERN;
		if (placed == ModBlocks.WAXED_LANTERN) return ModBlocks.FROZEN_WAXED_LANTERN;
		if (placed == ModBlocks.WAXED_EXPOSED_LANTERN) return ModBlocks.FROZEN_WAXED_EXPOSED_LANTERN;
		if (placed == ModBlocks.WAXED_WEATHERED_LANTERN) return ModBlocks.FROZEN_WAXED_WEATHERED_LANTERN;
		if (placed == ModBlocks.WAXED_OXIDIZED_LANTERN) return ModBlocks.FROZEN_WAXED_OXIDIZED_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_EXPOSED_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_EXPOSED_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_WEATHERED_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_WEATHERED_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_OXIDIZED_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_OXIDIZED_LANTERN;
		if (placed == Blocks.SOUL_LANTERN) return ModBlocks.FROZEN_SOUL_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.weathering().unaffected()) return ModBlocks.FROZEN_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.weathering().exposed()) return ModBlocks.FROZEN_EXPOSED_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.weathering().weathered()) return ModBlocks.FROZEN_WEATHERED_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.weathering().oxidized()) return ModBlocks.FROZEN_OXIDIZED_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.waxed().unaffected()) return ModBlocks.FROZEN_WAXED_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.waxed().exposed()) return ModBlocks.FROZEN_WAXED_EXPOSED_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.waxed().weathered()) return ModBlocks.FROZEN_WAXED_WEATHERED_COPPER_LANTERN;
		if (placed == Blocks.COPPER_LANTERN.waxed().oxidized()) return ModBlocks.FROZEN_WAXED_OXIDIZED_COPPER_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_COPPER_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_EXPOSED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_EXPOSED_COPPER_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_WEATHERED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_WEATHERED_COPPER_LANTERN;
		if (placed == CorrodedBlocks.CORRODED_OXIDIZED_COPPER_LANTERN) return CorrodedBlocks.CORRODED_FROZEN_OXIDIZED_COPPER_LANTERN;
		return null;
	}

	private static boolean isFloorTorch(Block block) {
		return block == Blocks.TORCH
			|| block == Blocks.SOUL_TORCH
			|| block == Blocks.COPPER_TORCH
			|| block == Blocks.REDSTONE_TORCH;
	}

	private static boolean isWallTorch(Block block) {
		return block == Blocks.WALL_TORCH
			|| block == Blocks.SOUL_WALL_TORCH
			|| block == Blocks.COPPER_WALL_TORCH
			|| block == Blocks.REDSTONE_WALL_TORCH;
	}

	private static BlockPos supportPos(BlockPos pos, BlockState state, boolean wall) {
		if (wall && state.hasProperty(HorizontalDirectionalBlock.FACING)) {
			Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
			return pos.relative(facing.getOpposite());
		}
		return pos.below();
	}

	private static boolean isCopperSupport(BlockState state) {
		Block block = state.getBlock();
		if (block instanceof FastWeatheringCopperBlock
			|| block instanceof FastWeatheringCopperStairsBlock
			|| block instanceof FastWeatheringCopperSlabBlock) {
			return true;
		}
		return contains(Blocks.COPPER_BLOCK, block)
			|| contains(Blocks.CUT_COPPER, block)
			|| contains(Blocks.CHISELED_COPPER, block)
			|| contains(Blocks.CUT_COPPER_STAIRS, block)
			|| contains(Blocks.CUT_COPPER_SLAB, block)
			|| contains(Blocks.COPPER_GRATE, block)
			|| contains(Blocks.COPPER_DOOR, block)
			|| contains(Blocks.COPPER_TRAPDOOR, block)
			|| contains(Blocks.COPPER_BARS, block)
			|| contains(Blocks.COPPER_CHAIN, block)
			|| contains(Blocks.COPPER_BULB, block)
			|| contains(Blocks.COPPER_CHEST, block)
			|| contains(Blocks.COPPER_LANTERN, block)
			|| contains(Blocks.COPPER_GOLEM_STATUE, block);
	}

	private static boolean contains(WeatheringCopperCollection<Block> collection, Block block) {
		for (Block candidate : collection.asList()) {
			if (candidate == block) {
				return true;
			}
		}
		return false;
	}

	private static void consumeFireSource(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos pos) {
		if (stack.is(Items.FLINT_AND_STEEL)) {
			level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
			stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
		} else {
			RandomSource random = level.getRandom();
			level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
			stack.consume(1, player);
		}
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}
}
