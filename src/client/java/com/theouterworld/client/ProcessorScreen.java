package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
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
    
    // Progress bar dimensions (similar to brewing stand bubbles/arrow)
    private static final int PROGRESS_BAR_X = 97;
    private static final int PROGRESS_BAR_Y = 16;
    private static final int PROGRESS_BAR_WIDTH = 9;
    private static final int PROGRESS_BAR_HEIGHT = 28;
    
    // Heat bar dimensions (similar to blaze powder meter)
    private static final int HEAT_BAR_X = 17;
    private static final int HEAT_BAR_Y = 34;
    private static final int HEAT_BAR_WIDTH = 16;
    private static final int HEAT_BAR_HEIGHT = 4;
    
    // Mode toggle button
    private Button modeButton;

    public ProcessorScreen(ProcessorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleLabelX = (imageWidth - font.width(title)) / 2;
        
        // Add mode toggle button (below secondary input slot)
        int buttonX = this.leftPos + 7;
        int buttonY = this.topPos + 59;
        modeButton = Button.builder(getModeButtonText(), button -> {
            // Get BlockPos from handler (works on server) or from player's target block
            net.minecraft.core.BlockPos pos = menu.getBlockPos();
            OuterWorldMod.LOGGER.info("[Processor Client] Button clicked, handler BlockPos: {}", pos);
            // If handler doesn't have the pos (client side), try to get it from player's target
            if (pos.equals(net.minecraft.core.BlockPos.ZERO) && this.minecraft != null && this.minecraft.player != null) {
                // Try crosshair target first
                var hitResult = this.minecraft.hitResult;
                if (hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                    pos = blockHit.getBlockPos();
                    OuterWorldMod.LOGGER.info("[Processor Client] Got BlockPos from crosshair: {}", pos);
                } else {
                    // Fallback: look for processor block near player (within 5 blocks)
                    var playerPos = this.minecraft.player.blockPosition();
                    var world = this.minecraft.player.level();
                    for (int x = -5; x <= 5; x++) {
                        for (int y = -5; y <= 5; y++) {
                            for (int z = -5; z <= 5; z++) {
                                var checkPos = playerPos.offset(x, y, z);
                                if (world.getBlockEntity(checkPos) instanceof com.theouterworld.block.ProcessorBlockEntity) {
                                    pos = checkPos;
                                    OuterWorldMod.LOGGER.info("[Processor Client] Found BlockPos near player: {}", pos);
                                    break;
                                }
                            }
                            if (!pos.equals(net.minecraft.core.BlockPos.ZERO)) break;
                        }
                        if (!pos.equals(net.minecraft.core.BlockPos.ZERO)) break;
                    }
                }
            }
            // Send packet to toggle mode on server
            if (!pos.equals(net.minecraft.core.BlockPos.ZERO)) {
                OuterWorldMod.LOGGER.info("[Processor Client] Sending toggle packet with BlockPos: {}", pos);
                ClientPlayNetworking.send(new ProcessorModeTogglePacket(pos));
            } else {
                OuterWorldMod.LOGGER.warn("[Processor Client] Could not determine BlockPos, not sending packet");
            }
        }).bounds(buttonX, buttonY, 50, 16).build();
        this.addRenderableWidget(modeButton);
    }

    private Component getModeButtonText() {
        return menu.isHeatMode() ? Component.literal("Heat") : Component.literal("Process");
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

        // Progress bar (green, fills downward)
        float progress = menu.getProgressScaled();
        if (progress > 0) {
            int progressHeight = (int) (PROGRESS_BAR_HEIGHT * progress);
            int barX = x + PROGRESS_BAR_X;
            int barY = y + PROGRESS_BAR_Y + (PROGRESS_BAR_HEIGHT - progressHeight);
            context.fill(barX, barY, barX + PROGRESS_BAR_WIDTH, barY + progressHeight, 0xFF00AA00);
        }

        // Heat bar (orange, fills upward) — heat mode only
        if (menu.isHeatMode()) {
            float heat = menu.getHeatScaled();
            if (heat > 0) {
                int heatHeight = (int) (20 * heat);
                int barX = x + HEAT_BAR_X;
                int barY = y + HEAT_BAR_Y + (20 - heatHeight);
                context.fill(barX, barY, barX + HEAT_BAR_WIDTH, barY + heatHeight, 0xFFFF6600);
            }
        }
    }

    @Override
    protected void extractLabels(net.minecraft.client.gui.GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        String modeText = menu.isHeatMode() ? "Heat Mode" : "Process Mode";
        context.text(this.font, modeText, 7, 64, 4210752, false);
        if (modeButton != null) {
            modeButton.setMessage(getModeButtonText());
        }
    }
}

