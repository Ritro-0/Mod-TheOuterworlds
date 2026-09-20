package com.theouterworld.block;

import com.theouterworld.world.ColdDimensionLights;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FrozenLanternBlock extends LanternBlock {

	public FrozenLanternBlock(Properties properties) {
		super(properties);
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
		if (!ColdDimensionLights.isFireSource(stack)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		return ColdDimensionLights.relightLantern(stack, state, level, pos, player, hand);
	}
}
