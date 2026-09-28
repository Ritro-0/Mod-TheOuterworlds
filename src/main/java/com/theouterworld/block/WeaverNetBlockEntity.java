package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** Holds whatever the Weavers have tucked into a net. */
public class WeaverNetBlockEntity extends BlockEntity implements Container {
	public static final int SIZE = 5;

	private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

	public WeaverNetBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WEAVER_NET, pos, state);
	}

	/**
	 * Tucks a stack into the weave, merging with a matching stack first.
	 * Returns whatever would not fit.
	 */
	public ItemStack stow(ItemStack stack) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack remainder = stack.copy();
		for (int slot = 0; slot < SIZE && !remainder.isEmpty(); slot++) {
			ItemStack held = this.items.get(slot);
			if (held.isEmpty()) {
				this.items.set(slot, remainder.split(remainder.getCount()));
			} else if (ItemStack.isSameItemSameComponents(held, remainder)) {
				int room = Math.min(held.getMaxStackSize(), this.getMaxStackSize()) - held.getCount();
				int moved = Math.min(room, remainder.getCount());
				if (moved > 0) {
					held.grow(moved);
					remainder.shrink(moved);
				}
			}
		}
		if (remainder.getCount() != stack.getCount()) {
			this.markUpdated();
		}
		return remainder;
	}

	public boolean hasRoomFor(ItemStack stack) {
		for (ItemStack held : this.items) {
			if (held.isEmpty()) {
				return true;
			}
			if (ItemStack.isSameItemSameComponents(held, stack)
				&& held.getCount() < Math.min(held.getMaxStackSize(), this.getMaxStackSize())) {
				return true;
			}
		}
		return false;
	}

	private void markUpdated() {
		this.setChanged();
		if (this.level != null && !this.level.isClientSide()) {
			BlockState state = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
		}
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot >= 0 && slot < SIZE ? this.items.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack removed = ContainerHelper.removeItem(this.items, slot, amount);
		if (!removed.isEmpty()) {
			this.markUpdated();
		}
		return removed;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack removed = ContainerHelper.takeItem(this.items, slot);
		if (!removed.isEmpty()) {
			this.markUpdated();
		}
		return removed;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot < 0 || slot >= SIZE) {
			return;
		}
		this.items.set(slot, stack);
		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}
		this.markUpdated();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.markUpdated();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items, false);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveCustomOnly(registries);
	}
}
