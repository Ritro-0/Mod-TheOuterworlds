package com.theouterworld.item;

import com.theouterworld.screen.RiftPadMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Handheld rift travel — opens the solar visit UI from anywhere. */
public class QuantumPodItem extends Item {
	private static final Component TITLE = Component.translatable("container.theouterworlds.quantum_pod");

	public QuantumPodItem(ResourceKey<Item> registryKey, Item.Properties properties) {
		super(properties.setId(registryKey).stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			player.openMenu(new SimpleMenuProvider(
				(containerId, inventory, opener) -> RiftPadMenu.quantumPod(containerId, inventory),
				TITLE
			));
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}
}
