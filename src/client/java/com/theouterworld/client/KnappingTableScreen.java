package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.screen.KnappingTableScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class KnappingTableScreen extends AbstractContainerScreen<KnappingTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "textures/gui/container/knapping_table.png");

    public KnappingTableScreen(KnappingTableScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        context.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            x, y,
            0.0f, 0.0f,
            this.imageWidth, this.imageHeight,
            256, 256
        );
    }
}
