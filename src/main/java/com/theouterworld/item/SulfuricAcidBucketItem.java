package com.theouterworld.item;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Not a fluid. Poured onto a sulfuric cloud it becomes a deposit and leaves an empty bucket.
 * Poured anywhere else, it boils off: empty bucket, smoke, no block.
 */
public class SulfuricAcidBucketItem extends Item {
	public SulfuricAcidBucketItem(Properties properties) {
		super(properties);
	}

	public static ItemStack exchange(Player player, ItemStack held, ItemStack replacement) {
		return ItemUtils.createFilledResult(held, player, replacement);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (state.is(ModBlocks.SULFURIC_CLOUD)) {
			return pourIntoCloud(level, pos, player, context.getHand());
		}
		BlockPos placePos = state.canBeReplaced() ? pos : pos.relative(context.getClickedFace());
		BlockState placeState = level.getBlockState(placePos);
		if (placeState.is(ModBlocks.SULFURIC_CLOUD)) {
			return pourIntoCloud(level, placePos, player, context.getHand());
		}
		if (state.is(ModBlocks.SULFURIC_CLOUD_DEPOSIT) || placeState.is(ModBlocks.SULFURIC_CLOUD_DEPOSIT)) {
			return InteractionResult.CONSUME;
		}
		return boilOff(level, placePos, player, context.getHand());
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		return boilOff(level, player.blockPosition(), player, hand);
	}

	private static InteractionResult pourIntoCloud(Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		level.setBlock(pos, ModBlocks.SULFURIC_CLOUD_DEPOSIT.defaultBlockState(), Block.UPDATE_ALL);
		level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		player.setItemInHand(hand, exchange(player, player.getItemInHand(hand), new ItemStack(Items.BUCKET)));
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult boilOff(Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (level instanceof ServerLevel server) {
			server.sendParticles(
				ParticleTypes.SMOKE,
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				8,
				0.25,
				0.25,
				0.25,
				0.02
			);
			server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.6F);
		}
		player.setItemInHand(hand, exchange(player, player.getItemInHand(hand), new ItemStack(Items.BUCKET)));
		return InteractionResult.SUCCESS;
	}
}
