package com.theouterworld.block;

import com.theouterworld.item.ModItems;
import com.theouterworld.item.SulfuricAcidBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Sulfuric cloud that holds a scoop of acid. Same drift as the cloud around it.
 * An empty bucket lifts that acid out and leaves an ordinary sulfuric cloud.
 */
public class SulfuricCloudDepositBlock extends AerogelCloudBlock {
	public SulfuricCloudDepositBlock(
		Properties properties,
		float horizontalDrag,
		double sinkSpeed,
		double climbSpeed,
		int nauseaAmplifier
	) {
		super(properties, horizontalDrag, sinkSpeed, climbSpeed, nauseaAmplifier);
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack stack,
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		InteractionHand hand,
		BlockHitResult hit
	) {
		if (!stack.is(Items.BUCKET)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		level.setBlock(pos, ModBlocks.SULFURIC_CLOUD.defaultBlockState(), Block.UPDATE_ALL);
		level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		player.setItemInHand(hand, SulfuricAcidBucketItem.exchange(player, stack, new ItemStack(ModItems.SULFURIC_ACID_BUCKET)));
		return InteractionResult.SUCCESS;
	}
}
