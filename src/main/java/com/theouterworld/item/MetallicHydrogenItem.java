package com.theouterworld.item;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

/**
 * Places metallic hydrogen in Highworld / Deepworld / Farworld / Edgeworld,
 * otherwise crystallizes into {@link ModBlocks#HYDROGEN_CRYSTAL}.
 */
public class MetallicHydrogenItem extends Item {
	public MetallicHydrogenItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		BlockPos clicked = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockState clickedState = level.getBlockState(clicked);

		BlockPos placePos = clickedState.canBeReplaced() ? clicked : clicked.relative(face);
		if (!level.getWorldBorder().isWithinBounds(placePos)) {
			return InteractionResult.FAIL;
		}

		BlockState existing = level.getBlockState(placePos);
		FluidState existingFluid = existing.getFluidState();
		boolean replaceable = existing.canBeReplaced()
			|| existing.isAir()
			|| existingFluid.is(ModFluids.LIQUID_HYDROGEN)
			|| existingFluid.is(ModFluids.FLOWING_LIQUID_HYDROGEN)
			|| existing.is(ModBlocks.METALLIC_HYDROGEN);
		if (!replaceable) {
			return InteractionResult.FAIL;
		}
		if (player != null && !level.mayInteract(player, placePos)) {
			return InteractionResult.FAIL;
		}

		boolean keepLiquid = ModDimensions.keepsMetallicHydrogen(level.dimension());
		BlockState toPlace = keepLiquid
			? ModBlocks.METALLIC_HYDROGEN.defaultBlockState()
			: ModBlocks.HYDROGEN_CRYSTAL.defaultBlockState();

		if (!level.setBlock(placePos, toPlace, Block.UPDATE_ALL)) {
			return InteractionResult.FAIL;
		}

		level.playSound(
			player,
			placePos,
			keepLiquid ? SoundEvents.BUCKET_EMPTY : SoundEvents.GLASS_PLACE,
			SoundSource.BLOCKS,
			1.0F,
			keepLiquid ? 1.2F : 1.0F
		);
		level.gameEvent(GameEvent.BLOCK_PLACE, placePos, GameEvent.Context.of(player, toPlace));
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
