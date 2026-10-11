package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import com.theouterworld.registry.ModScreenHandlers;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Chest-equivalent storage for the Oxidized Basalt Vault: 27 slots, loot tables, openers counter and lid animation. */
public class OxidizedBasaltVaultBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
	public static final int ROWS = 3;
	private static final int EVENT_SET_OPEN_COUNT = 1;
	private static final Component DEFAULT_NAME = Component.translatable("block.theouterworlds.oxidized_basalt_vault");

	private NonNullList<ItemStack> items = NonNullList.withSize(ROWS * 9, ItemStack.EMPTY);
	private final ChestLidController lidController = new ChestLidController();
	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			playSound(level, pos, OxidizedBasaltVaultBlock.OPEN_SOUND);
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			playSound(level, pos, OxidizedBasaltVaultBlock.CLOSE_SOUND);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
			Block block = state.getBlock();
			level.blockEvent(pos, block, EVENT_SET_OPEN_COUNT, current);
		}

		@Override
		public boolean isOwnContainer(Player player) {
			if (!(player.containerMenu instanceof ChestMenu menu)) {
				return false;
			}
			Container container = menu.getContainer();
			return container == OxidizedBasaltVaultBlockEntity.this;
		}
	};

	public OxidizedBasaltVaultBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.OXIDIZED_BASALT_VAULT, pos, state);
	}

	@Override
	public int getContainerSize() {
		return ROWS * 9;
	}

	@Override
	protected Component getDefaultName() {
		return DEFAULT_NAME;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(input)) {
			ContainerHelper.loadAllItems(input, this.items);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.trySaveLootTable(output)) {
			ContainerHelper.saveAllItems(output, this.items);
		}
	}

	public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, OxidizedBasaltVaultBlockEntity entity) {
		entity.lidController.tickLid();
	}

	private static void playSound(Level level, BlockPos pos, SoundEvent event) {
		level.playSound(
			null,
			pos.getX() + 0.5,
			pos.getY() + 0.5,
			pos.getZ() + 0.5,
			event,
			SoundSource.BLOCKS,
			0.5F,
			level.getRandom().nextFloat() * 0.1F + 0.9F
		);
	}

	@Override
	public boolean triggerEvent(int type, int data) {
		if (type == EVENT_SET_OPEN_COUNT) {
			this.lidController.shouldBeOpen(data > 0);
			return true;
		}
		return super.triggerEvent(type, data);
	}

	@Override
	public void startOpen(ContainerUser user) {
		if (!this.remove && !user.getLivingEntity().isSpectator()) {
			this.openersCounter.incrementOpeners(
				user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(), user.getContainerInteractionRange()
			);
		}
	}

	@Override
	public void stopOpen(ContainerUser user) {
		if (!this.remove && !user.getLivingEntity().isSpectator()) {
			this.openersCounter.decrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	public List<ContainerUser> getEntitiesWithContainerOpen() {
		return this.openersCounter.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public float getOpenNess(float partialTick) {
		return this.lidController.getOpenness(partialTick);
	}

	public void recheckOpen() {
		if (!this.remove) {
			this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new ChestMenu(ModScreenHandlers.OXIDIZED_BASALT_VAULT_MENU, containerId, inventory, this, ROWS);
	}
}
