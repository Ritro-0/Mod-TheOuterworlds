package com.theouterworld.screen;

import com.theouterworld.registry.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Empty container so the client can open the fullscreen solar visit view. */
public class RiftPadMenu extends AbstractContainerMenu {
	private final BlockPos padPos;
	private final boolean quantumPod;

	public RiftPadMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, BlockPos.ZERO, false);
	}

	public RiftPadMenu(int containerId, Inventory inventory, BlockPos padPos) {
		this(containerId, inventory, padPos, false);
	}

	public RiftPadMenu(int containerId, Inventory inventory, BlockPos padPos, boolean quantumPod) {
		super(
			quantumPod ? ModScreenHandlers.QUANTUM_POD_SCREEN_HANDLER : ModScreenHandlers.RIFT_PAD_SCREEN_HANDLER,
			containerId
		);
		this.padPos = padPos;
		this.quantumPod = quantumPod;
	}

	public static RiftPadMenu quantumPod(int containerId, Inventory inventory) {
		return new RiftPadMenu(containerId, inventory, BlockPos.ZERO, true);
	}

	public BlockPos getPadPos() {
		return this.padPos;
	}

	public boolean isQuantumPod() {
		return this.quantumPod;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}
