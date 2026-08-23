package com.theouterworld.screen;

import com.theouterworld.block.ProcessorBlockEntity;
import com.theouterworld.registry.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ProcessorScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData propertyDelegate;
    private final BlockPos pos;

    // Client constructor
    public ProcessorScreenHandler(int syncId, Inventory playerInventory) {
        // On client, we can't get the BlockPos directly, so we'll get it from the player's interaction
        // For now, use ORIGIN and we'll get it from the screen context
        this(syncId, playerInventory, new SimpleContainer(ProcessorBlockEntity.INVENTORY_SIZE), new SimpleContainerData(5), BlockPos.ZERO);
    }

    // Server constructor
    public ProcessorScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        // On server, if inventory is the BlockEntity, get pos from it
        this(syncId, playerInventory, inventory, propertyDelegate, 
            inventory instanceof ProcessorBlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO);
    }
    
    // Server constructor with BlockPos
    public ProcessorScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate, BlockPos pos) {
        super(ModScreenHandlers.PROCESSOR_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, ProcessorBlockEntity.INVENTORY_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.pos = pos;
        inventory.startOpen(playerInventory.player);

        // Add processor slots (positioned like brewing stand)
        // Primary input slot (ingredient slot - top center area)
        this.addSlot(new Slot(inventory, ProcessorBlockEntity.PRIMARY_INPUT_SLOT, 79, 17));
        
        // Secondary input slot (fuel/catalyst slot - left side)
        this.addSlot(new Slot(inventory, ProcessorBlockEntity.SECONDARY_INPUT_SLOT, 17, 17));
        
        // Output slot (bottom center)
        this.addSlot(new OutputSlot(inventory, ProcessorBlockEntity.OUTPUT_SLOT, 79, 58));

        // Add player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(propertyDelegate);
    }

    public int getProgress() {
        return propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return propertyDelegate.get(1);
    }

    public int getHeat() {
        return propertyDelegate.get(2);
    }

    public int getMaxHeat() {
        return propertyDelegate.get(3);
    }

    public boolean isHeatMode() {
        return propertyDelegate.get(4) == 1;
    }

    public float getProgressScaled() {
        int progress = getProgress();
        int maxProgress = getMaxProgress();
        return maxProgress > 0 ? (float) progress / maxProgress : 0;
    }

    public float getHeatScaled() {
        int heat = getHeat();
        int maxHeat = getMaxHeat();
        return maxHeat > 0 ? (float) heat / maxHeat : 0;
    }
    
    public BlockPos getBlockPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            
            // If clicking on processor slots (0-2)
            if (slotIndex < ProcessorBlockEntity.INVENTORY_SIZE) {
                // Move to player inventory
                if (!this.moveItemStackTo(originalStack, ProcessorBlockEntity.INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to processor
                // Try primary input first, then secondary
                if (!this.moveItemStackTo(originalStack, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    // Output slot that doesn't accept items
    private static class OutputSlot extends Slot {
        public OutputSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}

