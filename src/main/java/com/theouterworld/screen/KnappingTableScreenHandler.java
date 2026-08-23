package com.theouterworld.screen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModScreenHandlers;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class KnappingTableScreenHandler extends AbstractContainerMenu {
    private final TransientCraftingContainer input;
    private final ResultContainer result;
    private final ContainerLevelAccess context;
    private final Player player;

    public KnappingTableScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public KnappingTableScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModScreenHandlers.KNAPPING_TABLE_SCREEN_HANDLER, syncId);
        this.context = context;
        this.player = playerInventory.player;
        this.input = new TransientCraftingContainer(this, 3, 3);
        this.result = new ResultContainer();

        // Result slot
        this.addSlot(new ResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));

        // 3x3 crafting grid
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.input, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container inventory) {
        this.context.execute((world, pos) -> {
            updateResult(this, world, this.player, this.input, this.result);
        });
    }

    private static void updateResult(AbstractContainerMenu handler, Level world, Player player, 
                                     TransientCraftingContainer craftingInventory, ResultContainer resultInventory) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ItemStack result = ItemStack.EMPTY;
            
            // Create recipe input from the crafting inventory
            CraftingInput.Positioned positioned = craftingInventory.asPositionedCraftInput();
            CraftingInput recipeInput = positioned.input();
            
            // Find matching recipe - use proper generic type
            var optional = world.getServer()
                .getRecipeManager()
                .<CraftingInput, net.minecraft.world.item.crafting.CraftingRecipe>getRecipeFor(
                    RecipeType.CRAFTING, recipeInput, world);
            
            if (optional.isPresent()) {
                RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> recipeEntry = optional.get();
                if (resultInventory.setRecipeUsed(serverPlayer, recipeEntry)) {
                    ItemStack craftResult = recipeEntry.value().assemble(recipeInput);
                    if (craftResult.isItemEnabled(world.enabledFeatures())) {
                        result = craftResult;
                    }
                }
            }
            
            resultInventory.setItem(0, result);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), 0, result));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, pos) -> {
            this.clearContainer(player, this.input);
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.KNAPPING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        
        if (slot2 != null && slot2.hasItem()) {
            ItemStack slotStack = slot2.getItem();
            itemStack = slotStack.copy();
            
            if (slot == 0) {
                // Result slot - move to player inventory
                this.context.execute((world, pos) -> slotStack.getItem().onCraftedBy(slotStack, player));
                if (!this.moveItemStackTo(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot2.onQuickCraft(slotStack, itemStack);
            } else if (slot >= 10 && slot < 46) {
                // Player inventory - move to crafting grid
                if (!this.moveItemStackTo(slotStack, 1, 10, false)) {
                    if (slot < 37) {
                        if (!this.moveItemStackTo(slotStack, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, 10, 46, false)) {
                // Crafting grid - move to player inventory
                return ItemStack.EMPTY;
            }
            
            if (slotStack.isEmpty()) {
                slot2.setByPlayer(ItemStack.EMPTY);
            } else {
                slot2.setChanged();
            }
            
            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot2.onTake(player, slotStack);
            if (slot == 0) {
                player.drop(slotStack, false);
            }
        }
        
        return itemStack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.result && super.canTakeItemForPickAll(stack, slot);
    }
}
