package com.theouterworld.screen;

import com.theouterworld.registry.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Empty container so the client can open the fullscreen telescope view. */
public class AstralTelescopeMenu extends AbstractContainerMenu {
	private final BlockPos telescopePos;

	public AstralTelescopeMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, BlockPos.ZERO);
	}

	public AstralTelescopeMenu(int containerId, Inventory inventory, BlockPos telescopePos) {
		super(ModScreenHandlers.ASTRAL_TELESCOPE_SCREEN_HANDLER, containerId);
		this.telescopePos = telescopePos;
	}

	public BlockPos getTelescopePos() {
		return this.telescopePos;
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
