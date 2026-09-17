package com.theouterworld.item;

import com.theouterworld.registry.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EmergencyReturnPodItem extends Item {
	public EmergencyReturnPodItem(ResourceKey<Item> registryKey, Item.Properties properties) {
		super(properties.setId(registryKey).stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!ModDimensions.isOuterworld(level.dimension())) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.sendOverlayMessage(
					Component.translatable("item.theouterworlds.emergency_return_pod.wrong_dimension")
				);
				level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					SoundEvents.FIRE_EXTINGUISH,
					SoundSource.PLAYERS,
					0.5F,
					1.2F
				);
			}
			return InteractionResult.FAIL;
		}

		if (level instanceof ServerLevel serverLevel) {
			if (EmergencyReturnPodRitual.isActive(player.getUUID())) {
				return InteractionResult.FAIL;
			}

			Vec3 center = player.position();
			EmergencyReturnPodRitual.start(serverLevel, player.getUUID(), hand, center);
			serverLevel.playSound(
				null,
				center.x,
				center.y,
				center.z,
				SoundEvents.BEACON_ACTIVATE,
				SoundSource.PLAYERS,
				0.8F,
				0.75F
			);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}
}
