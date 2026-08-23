package com.theouterworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DryIceBlock extends Block {
	public DryIceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		if (!hasSilkTouch(level, tool)) {
			sublimate(level, pos);
		}
	}

	private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
		if (tool.isEmpty()) {
			return false;
		}
		Holder<Enchantment> silk = level.registryAccess()
			.lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(Enchantments.SILK_TOUCH);
		return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
	}

	private static void sublimate(ServerLevel level, BlockPos pos) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;
		level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 18, 0.25, 0.35, 0.25, 0.01);
		level.sendParticles(ParticleTypes.SMOKE, x, y, z, 24, 0.3, 0.4, 0.3, 0.02);
		level.sendParticles(ParticleTypes.CLOUD, x, y, z, 8, 0.2, 0.2, 0.2, 0.01);
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.4F);
	}
}
