package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ProcessorBlockEntity;
import com.theouterworld.network.ProcessorModeTogglePacket;
import com.theouterworld.screen.ProcessorScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ProcessorScreen extends AbstractContainerScreen<ProcessorScreenHandler> implements MenuAccess<ProcessorScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "textures/gui/container/processor.png");

    private static final int PROGRESS_BAR_X = 97;
    private static final int PROGRESS_BAR_Y = 16;
    private static final int PROGRESS_BAR_WIDTH = 9;
    private static final int PROGRESS_BAR_HEIGHT = 28;

    private static final int HEAT_BAR_X = 17;
    private static final int HEAT_BAR_Y = 34;
    private static final int HEAT_BAR_WIDTH = 16;

    private static final int HEAT_COLOR = 0xFFFF6600;
    private static final int PRESSURE_COLOR = 0xFFB8E000;

    private Button modeButton;

    public ProcessorScreen(ProcessorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;

        int buttonX = this.leftPos + 7;
        int buttonY = this.topPos + 56;
        modeButton = Button.builder(getModeButtonText(), button -> {
            net.minecraft.core.BlockPos pos = menu.getBlockPos();
            if (pos.equals(net.minecraft.core.BlockPos.ZERO) && this.minecraft != null && this.minecraft.player != null) {
                var hitResult = this.minecraft.hitResult;
                if (hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                    pos = blockHit.getBlockPos();
                } else {
                    var playerPos = this.minecraft.player.blockPosition();
                    var world = this.minecraft.player.level();
                    outer:
                    for (int x = -5; x <= 5; x++) {
                        for (int y = -5; y <= 5; y++) {
                            for (int z = -5; z <= 5; z++) {
                                var checkPos = playerPos.offset(x, y, z);
                                if (world.getBlockEntity(checkPos) instanceof ProcessorBlockEntity) {
                                    pos = checkPos;
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
            if (!pos.equals(net.minecraft.core.BlockPos.ZERO)) {
                ClientPlayNetworking.send(new ProcessorModeTogglePacket(pos));
            }
        }).bounds(buttonX, buttonY, 58, 16).build();
        this.addRenderableWidget(modeButton);
    }

    private Component getModeButtonText() {
        int mode = menu.getMode();
        return switch (mode) {
            case ProcessorBlockEntity.MODE_HEAT -> Component.literal("Heat");
            case ProcessorBlockEntity.MODE_PRESSURIZE -> Component.literal("Pressurize");
            default -> Component.literal("Process");
        };
    }

    private String getModeLabel() {
        return switch (menu.getMode()) {
            case ProcessorBlockEntity.MODE_HEAT -> "Heat Mode";
            case ProcessorBlockEntity.MODE_PRESSURIZE -> "Pressurize Mode";
            default -> "Process Mode";
        };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        context.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            x, y,
            0.0f, 0.0f,
            imageWidth, imageHeight,
            256, 256
        );

        float progress = menu.getProgressScaled();
        if (progress > 0) {
            int progressHeight = (int) (PROGRESS_BAR_HEIGHT * progress);
            int barX = x + PROGRESS_BAR_X;
            int barY = y + PROGRESS_BAR_Y + (PROGRESS_BAR_HEIGHT - progressHeight);
            context.fill(barX, barY, barX + PROGRESS_BAR_WIDTH, barY + progressHeight, 0xFF00AA00);
        }

        if (menu.isHeatMode() || menu.isPressurizeMode()) {
            float heat = menu.getHeatScaled();
            if (heat > 0) {
                int heatHeight = (int) (20 * heat);
                int barX = x + HEAT_BAR_X;
                int barY = y + HEAT_BAR_Y + (20 - heatHeight);
                int color = menu.isPressurizeMode() ? PRESSURE_COLOR : HEAT_COLOR;
                context.fill(barX, barY, barX + HEAT_BAR_WIDTH, barY + heatHeight, color);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        // If client left Nearworld while GUI open and mode is Pressurize, still show label from sync.
        context.text(this.font, getModeLabel(), 7, 64, 4210752, false);
        if (modeButton != null) {
            modeButton.setMessage(getModeButtonText());
            // Pressurize only appears via cycling in Nearworld; button always present for Process/Heat.
            modeButton.visible = true;
        }
    }
}
