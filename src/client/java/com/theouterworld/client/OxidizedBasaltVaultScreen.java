package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/** The vanilla 9x3 chest screen layout, drawn with the vault's own background (a 256x256 generic_54 sheet). */
public class OxidizedBasaltVaultScreen extends AbstractContainerScreen<ChestMenu> implements MenuAccess<ChestMenu> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "textures/gui/container/oxidized_basalt_vault.png");
	private final int containerRows;

	public OxidizedBasaltVaultScreen(ChestMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 114 + menu.getRowCount() * 18);
		this.containerRows = menu.getRowCount();
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.containerRows * 18 + 17, 256, 256);
		graphics.blit(
			RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + this.containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256
		);
	}
}
