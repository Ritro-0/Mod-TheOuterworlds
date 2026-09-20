package com.theouterworld.block;

import net.minecraft.tags.ItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Waxed Iron Golem Statue block - cannot oxidize further.
 * Can be scraped with an axe to remove wax and allow further oxidation/de-oxidation.
 */
public class WaxedIronGolemStatueBlock extends AbstractIronGolemStatueBlock {

	private final Block unwaxedVersion;

	public WaxedIronGolemStatueBlock(Block unwaxedVersion, Properties settings) {
		super(settings);
		this.unwaxedVersion = unwaxedVersion;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(player.getUsedItemHand());

		if (stack.is(ItemTags.AXES)) {
			if (!world.isClientSide()) {
				replaceBothHalves(world, pos, state, unwaxedVersion);
				world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
				world.playSound(null, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0f, 1.0f);

				if (world instanceof ServerLevel serverWorld) {
					BlockPos lower = lowerPos(pos, state);
					for (int i = 0; i < 10; i++) {
						serverWorld.sendParticles(ParticleTypes.WAX_OFF,
							lower.getX() + 0.5 + world.getRandom().nextGaussian() * 0.3,
							lower.getY() + 1.35 + world.getRandom().nextGaussian() * 0.5,
							lower.getZ() + 0.5 + world.getRandom().nextGaussian() * 0.3,
							1, 0, 0, 0, 0);
					}
				}

				if (!player.isCreative()) {
					stack.hurtAndBreak(1, player, player.getUsedItemHand());
				}
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	public Block getUnwaxedVersion() {
		return unwaxedVersion;
	}
}
