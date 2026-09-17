package com.theouterworld.block;

import com.theouterworld.item.ModItems;
import com.theouterworld.registry.ModBlockEntities;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.screen.ProcessorScreenHandler;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessorBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final int PRIMARY_INPUT_SLOT = 0;
    public static final int SECONDARY_INPUT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int INVENTORY_SIZE = 3;

    public static final int MODE_PROCESS = 0;
    public static final int MODE_HEAT = 1;
    public static final int MODE_PRESSURIZE = 2;

    public static final int MAX_PROGRESS = 400;
    public static final int MAX_HEAT = 200;
    /** Pressurize prep phase is 2× as fast as heat (100 ticks). */
    public static final int MAX_PRESSURE = 100;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int progress = 0;
    private int heat = 0;
    private int mode = MODE_PROCESS;

    private final Random random = new Random();

    protected final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> MAX_PROGRESS;
                case 2 -> heat;
                case 3 -> isPressurizeMode() ? MAX_PRESSURE : MAX_HEAT;
                case 4 -> mode;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> heat = value;
                case 4 -> mode = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public ProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROCESSOR, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.theouterworlds.processor");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ProcessorScreenHandler(syncId, playerInventory, this, this.propertyDelegate, this.worldPosition);
    }

    protected void writeNbt(CompoundTag nbt) {
        nbt.putInt("Progress", progress);
        nbt.putInt("Heat", heat);
        nbt.putInt("Mode", mode);
        // Legacy key for older saves
        nbt.putBoolean("HeatMode", mode == MODE_HEAT);

        ListTag items = new ListTag();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemNbt = new CompoundTag();
                itemNbt.putInt("Slot", i);
                itemNbt.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                itemNbt.putInt("Count", stack.getCount());
                items.add(itemNbt);
            }
        }
        nbt.put("Items", items);
    }

    protected void readNbt(CompoundTag nbt) {
        nbt.getInt("Progress").ifPresent(val -> this.progress = val);
        nbt.getInt("Heat").ifPresent(val -> this.heat = val);
        if (nbt.contains("Mode")) {
            nbt.getInt("Mode").ifPresent(val -> this.mode = val);
        } else {
            nbt.getBoolean("HeatMode").ifPresent(val -> this.mode = val ? MODE_HEAT : MODE_PROCESS);
        }

        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
        if (nbt.contains("Items")) {
            ListTag items = nbt.getListOrEmpty("Items");
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemNbt = items.getCompoundOrEmpty(i);
                int slot = itemNbt.getInt("Slot").orElse(-1);
                if (slot >= 0 && slot < inventory.size()) {
                    itemNbt.getString("id").ifPresent(idStr -> {
                        var itemId = net.minecraft.resources.Identifier.tryParse(idStr);
                        if (itemId != null) {
                            var item = BuiltInRegistries.ITEM.getValue(itemId);
                            int count = itemNbt.getInt("Count").orElse(1);
                            inventory.set(slot, new ItemStack(item, count));
                        }
                    });
                }
            }
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState state, ProcessorBlockEntity entity) {
        if (world.isClientSide()) return;

        // Pressurize is Nearworld-only; snap back if the block somehow left the dimension.
        if (entity.mode == MODE_PRESSURIZE && !ModDimensions.isNearworld(world.dimension())) {
            entity.mode = MODE_PROCESS;
            entity.progress = 0;
            entity.heat = 0;
            entity.setChanged();
            return;
        }

        boolean dirty = switch (entity.mode) {
            case MODE_HEAT -> tickHeatMode(world, entity);
            case MODE_PRESSURIZE -> tickPressurizeMode(entity);
            default -> tickProcessingMode(entity);
        };

        if (dirty) {
            entity.setChanged();
        }
    }

    private static boolean tickProcessingMode(ProcessorBlockEntity entity) {
        if (!hasProcessingRecipe(entity)) {
            if (entity.progress > 0) {
                entity.progress = 0;
                return true;
            }
            return false;
        }

        entity.progress++;
        if (entity.progress >= MAX_PROGRESS) {
            entity.progress = 0;
            processRecipe(entity);
            return true;
        }

        return true;
    }

    private static boolean tickHeatMode(Level world, ProcessorBlockEntity entity) {
        boolean hasRecipe = hasHeatRecipe(world, entity);

        if (!hasRecipe) {
            if (entity.progress > 0 || entity.heat > 0) {
                entity.progress = 0;
                entity.heat = 0;
                return true;
            }
            return false;
        }

        if (entity.heat < MAX_HEAT) {
            entity.heat++;
            return true;
        } else {
            entity.progress++;
            if (entity.progress >= MAX_PROGRESS) {
                entity.progress = 0;
                entity.heat = 0;
                processHeatRecipe(world, entity);
                return true;
            }
            return true;
        }
    }

    private static boolean tickPressurizeMode(ProcessorBlockEntity entity) {
        boolean hasRecipe = hasPressurizeRecipe(entity);

        if (!hasRecipe) {
            if (entity.progress > 0 || entity.heat > 0) {
                entity.progress = 0;
                entity.heat = 0;
                return true;
            }
            return false;
        }

        if (entity.heat < MAX_PRESSURE) {
            entity.heat++;
            return true;
        } else {
            entity.progress++;
            if (entity.progress >= MAX_PROGRESS) {
                entity.progress = 0;
                entity.heat = 0;
                processPressurizeRecipe(entity);
                return true;
            }
            return true;
        }
    }

    private static boolean hasProcessingRecipe(ProcessorBlockEntity entity) {
        ItemStack primary = entity.getItem(PRIMARY_INPUT_SLOT);
        if (primary.isEmpty()) {
            return false;
        }
        if (primary.is(ModBlocks.REGOLITH.asItem())) {
            return true;
        }
        return primary.is(ModBlocks.RAW_OSMIUM.asItem())
            && canAcceptOutput(entity, new ItemStack(ModItems.OSMIUM_FLAKE));
    }

    private static boolean hasHeatRecipe(Level world, ProcessorBlockEntity entity) {
        ItemStack primary = entity.getItem(PRIMARY_INPUT_SLOT);
        ItemStack secondary = entity.getItem(SECONDARY_INPUT_SLOT);

        boolean primaryValid = !primary.isEmpty() && primary.is(ModBlocks.OXIDIZED_BASALT.asItem());
        boolean secondaryValid = !secondary.isEmpty() && secondary.is(ModBlocks.REGOLITH.asItem());
        if (primaryValid && secondaryValid) {
            return canAcceptOutput(entity, new ItemStack(ModItems.RUST_SPLINT));
        }

        ItemStack smelted = getSmeltingResult(world, primary);
        return !smelted.isEmpty() && canAcceptOutput(entity, smelted);
    }

    private static boolean hasPressurizeRecipe(ProcessorBlockEntity entity) {
        ItemStack primary = entity.getItem(PRIMARY_INPUT_SLOT);
        ItemStack secondary = entity.getItem(SECONDARY_INPUT_SLOT);
        boolean primaryValid = !primary.isEmpty() && primary.is(ModItems.OPALINE_NICKEL);
        boolean secondaryValid = !secondary.isEmpty() && secondary.is(ModItems.OSMIUM_FLAKE);
        return primaryValid && secondaryValid
            && canAcceptOutput(entity, new ItemStack(ModItems.IRIDIUM_INGOT));
    }

    private static void processRecipe(ProcessorBlockEntity entity) {
        ItemStack primary = entity.getItem(PRIMARY_INPUT_SLOT);

        if (primary.is(ModBlocks.RAW_OSMIUM.asItem())) {
            primary.shrink(1);
            if (entity.random.nextFloat() < 0.6f) {
                insertOutput(entity, new ItemStack(ModItems.OSMIUM_FLAKE));
            }
            return;
        }

        // Regolith -> 40% iron nugget, 60% nothing
        if (primary.is(ModBlocks.REGOLITH.asItem())) {
            primary.shrink(1);

            if (entity.random.nextFloat() < 0.4f) {
                ItemStack output = entity.getItem(OUTPUT_SLOT);
                if (output.isEmpty()) {
                    entity.setItem(OUTPUT_SLOT, new ItemStack(Items.IRON_NUGGET));
                } else if (output.is(Items.IRON_NUGGET) && output.getCount() < output.getMaxStackSize()) {
                    output.grow(1);
                }
            }
        }
    }

    private static void processHeatRecipe(Level world, ProcessorBlockEntity entity) {
        ItemStack primary = entity.getItem(PRIMARY_INPUT_SLOT);
        ItemStack secondary = entity.getItem(SECONDARY_INPUT_SLOT);

        if (primary.is(ModBlocks.OXIDIZED_BASALT.asItem())
            && secondary.is(ModBlocks.REGOLITH.asItem())) {
            primary.shrink(1);
            secondary.shrink(1);

            if (entity.random.nextFloat() < 0.2f) {
                insertOutput(entity, new ItemStack(ModItems.RUST_SPLINT));
            }
            return;
        }

        ItemStack smelted = getSmeltingResult(world, primary);
        if (smelted.isEmpty()) {
            return;
        }
        primary.shrink(1);
        if (entity.random.nextFloat() < 0.4f) {
            insertOutput(entity, smelted);
        }
    }

    private static void processPressurizeRecipe(ProcessorBlockEntity entity) {
        ItemStack primary = entity.getItem(PRIMARY_INPUT_SLOT);
        ItemStack secondary = entity.getItem(SECONDARY_INPUT_SLOT);
        if (!primary.is(ModItems.OPALINE_NICKEL) || !secondary.is(ModItems.OSMIUM_FLAKE)) {
            return;
        }
        primary.shrink(1);
        secondary.shrink(1);
        insertOutput(entity, new ItemStack(ModItems.IRIDIUM_INGOT));
    }

    private static ItemStack getSmeltingResult(Level world, ItemStack input) {
        if (input.isEmpty() || world.getServer() == null) {
            return ItemStack.EMPTY;
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return world.getServer()
            .getRecipeManager()
            .<SingleRecipeInput, SmeltingRecipe>getRecipeFor(RecipeType.SMELTING, recipeInput, world)
            .map(RecipeHolder::value)
            .map(recipe -> recipe.assemble(recipeInput))
            .orElse(ItemStack.EMPTY);
    }

    private static boolean canAcceptOutput(ProcessorBlockEntity entity, ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = entity.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
            && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private static void insertOutput(ProcessorBlockEntity entity, ItemStack result) {
        ItemStack output = entity.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            entity.setItem(OUTPUT_SLOT, result.copy());
        } else if (ItemStack.isSameItemSameComponents(output, result)
            && output.getCount() < output.getMaxStackSize()) {
            output.grow(Math.min(result.getCount(), output.getMaxStackSize() - output.getCount()));
        }
    }

    /** Cycles Process → Heat → (Pressurize if Nearworld) → Process. */
    public void toggleMode() {
        boolean nearworld = this.level != null && ModDimensions.isNearworld(this.level.dimension());
        this.mode = switch (this.mode) {
            case MODE_PROCESS -> MODE_HEAT;
            case MODE_HEAT -> nearworld ? MODE_PRESSURIZE : MODE_PROCESS;
            default -> MODE_PROCESS;
        };
        this.progress = 0;
        this.heat = 0;
        this.setChanged();
    }

    public int getMode() {
        return mode;
    }

    public boolean isHeatMode() {
        return mode == MODE_HEAT;
    }

    public boolean isPressurizeMode() {
        return mode == MODE_PRESSURIZE;
    }

    public boolean isProcessMode() {
        return mode == MODE_PROCESS;
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = inventory.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result;
        if (stack.getCount() <= amount) {
            result = stack;
            inventory.set(slot, ItemStack.EMPTY);
        } else {
            result = stack.split(amount);
        }
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.get(slot);
        inventory.set(slot, ItemStack.EMPTY);
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        inventory.clear();
        setChanged();
    }
}
