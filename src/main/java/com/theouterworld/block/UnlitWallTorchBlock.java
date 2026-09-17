package com.theouterworld.block;

import com.theouterworld.world.ColdDimensionLights;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class UnlitWallTorchBlock extends WallTorchBlock {
	public UnlitWallTorchBlock(Properties properties) {
		super(ParticleTypes.FLAME, properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
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
		return ColdDimensionLights.relightTorch(stack, state, level, pos, player, hand);
	}
}
