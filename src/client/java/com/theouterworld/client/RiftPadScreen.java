package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.network.RiftPadVisitPacket;
import com.theouterworld.screen.RiftPadMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

/**
 * Fullscreen rift-pad / quantum-pod visit UI: same solar layout as the astral telescope,
 * but selectable worlds open a side panel with a Visit button (or an upgrade notice).
 * The Sun requires a multi-step "are you sure?" confirmation before travel.
 * Mouse wheel zooms the solar system (background stays fixed).
 */
public class RiftPadScreen extends Screen implements MenuAccess<RiftPadMenu> {
	private static final Identifier SPACE = OuterWorldMod.id("textures/telescope/space_background.png");
	private static final Identifier SUN = OuterWorldMod.id("textures/telescope/sun.png");
	private static final Identifier MERCURY = OuterWorldMod.id("textures/telescope/innerworld.png");
	private static final Identifier VENUS = OuterWorldMod.id("textures/telescope/nearworld.png");
	private static final Identifier OVERWORLD = OuterWorldMod.id("textures/telescope/overworld.png");
	private static final Identifier OUTERWORLD = OuterWorldMod.id("textures/telescope/outerworld.png");
	private static final Identifier WANDERLANDS = OuterWorldMod.id("textures/telescope/wanderlands.png");
	private static final Identifier HIGHWORLD = OuterWorldMod.id("textures/telescope/highworld.png");
	private static final Identifier DEEPWORLD = OuterWorldMod.id("textures/telescope/deepworld.png");
	private static final Identifier FARWORLD = OuterWorldMod.id("textures/telescope/farworld.png");
	private static final Identifier EDGEWORLD = OuterWorldMod.id("textures/telescope/edgeworld.png");
	private static final Identifier BEYONDLANDS = OuterWorldMod.id("textures/telescope/beyondlands.png");
	private static final Identifier BEYONDLANDS_II = OuterWorldMod.id("textures/telescope/beyondlands2.png");
	private static final Identifier SPINLANDS = OuterWorldMod.id("textures/telescope/spinlands.png");
	private static final Identifier SCARLETLANDS = OuterWorldMod.id("textures/telescope/scarletlands.png");
	private static final Identifier LONELANDS = OuterWorldMod.id("textures/telescope/lonelands.png");
	private static final Identifier EMBERWORLD = OuterWorldMod.id("textures/telescope/emberworld_io.png");
	private static final Identifier FROSTWORLD = OuterWorldMod.id("textures/telescope/frostworld_europa.png");
	private static final Identifier AMBERWORLD = OuterWorldMod.id("textures/telescope/amberworld_titan.png");
	private static final Identifier SPONGEWORLD = OuterWorldMod.id("textures/telescope/spongeworld_hyperion.png");
	private static final Identifier POTATOWORLDS = OuterWorldMod.id("textures/telescope/potatoworlds_phobos_deimos.png");
	private static final Identifier MOON = OuterWorldMod.id("textures/telescope/moon.png");

	private static final int HIGHLIGHT = 0xAAFFFFFF;

	private static final float MIN_ZOOM = 0.30F;
	private static final float MAX_ZOOM = 3.6F;
	private static final float ZOOM_STEP = 0.08F;

	private static final int PANEL_X = 16;
	private static final int PANEL_Y = 16;
	private static final int PANEL_PADDING = 10;
	private static final int PANEL_MIN_WIDTH = 120;
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 4;

	/** 0 = normal visit panel; 1–3 = Sun confirmation steps. */
	private static final int SUN_CONFIRM_NONE = 0;
	private static final int SUN_CONFIRM_SURE = 1;
	private static final int SUN_CONFIRM_REALLY = 2;
	private static final int SUN_CONFIRM_CHANCES = 3;

	private final RiftPadMenu menu;
	private final boolean quantumPod;
	private long openMillis;
	private float zoom = 1.0F;
	private float panX = 0.0F;
	private float panY = 0.0F;
	@Nullable
	private Body selected;
	private int sunConfirmStep = SUN_CONFIRM_NONE;
	@Nullable
	private Button visitButton;
	@Nullable
	private Button yesButton;
	@Nullable
	private Button noButton;
	private int panelW;
	private int panelH;

	public RiftPadScreen(RiftPadMenu menu, Inventory inventory, Component title) {
		super(title);
		this.menu = menu;
		this.quantumPod = menu.isQuantumPod();
	}

	@Override
	public RiftPadMenu getMenu() {
		return this.menu;
	}

	@Override
	protected void init() {
		super.init();
		this.openMillis = System.currentTimeMillis();
		this.zoom = 1.0F;
		this.panX = 0.0F;
		this.panY = 0.0F;
		this.visitButton = this.addRenderableWidget(
			Button.builder(Component.translatable("gui.theouterworlds.rift_pad.visit"), button -> onPrimaryAction())
				.bounds(PANEL_X + PANEL_PADDING, PANEL_Y + PANEL_PADDING + 16, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build()
		);
		this.yesButton = this.addRenderableWidget(
			Button.builder(Component.translatable("gui.theouterworlds.rift_pad.yes"), button -> onYes())
				.bounds(PANEL_X + PANEL_PADDING, PANEL_Y + PANEL_PADDING + 16, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build()
		);
		this.noButton = this.addRenderableWidget(
			Button.builder(Component.translatable("gui.theouterworlds.rift_pad.no"), button -> closeDossierPanels())
				.bounds(PANEL_X + PANEL_PADDING, PANEL_Y + PANEL_PADDING + 40, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build()
		);
		updatePanelButtons();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.closeContainer();
		}
		super.onClose();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SPACE, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		SolarSystemLayout.Snapshot orbit = currentOrbit();

		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.mercuryOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.venusOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.overworldOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.outerworldOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.wanderlandsOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.highworldOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.deepworldOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.farworldOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.edgeworldOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.beyondlandsOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.spinlandsOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.scarletlandsOrbit());
		drawOrbitRing(graphics, orbit.cx(), orbit.cy(), orbit.lonelandsOrbit());
		drawOrbitRing(graphics, orbit.overworld().x(), orbit.overworld().y(), orbit.moonOrbit());
		drawOrbitRing(graphics, orbit.outerworld().x(), orbit.outerworld().y(), orbit.potatoworldsOrbit());
		drawOrbitRing(graphics, orbit.highworld().x(), orbit.highworld().y(), orbit.emberworldOrbit());
		drawOrbitRing(graphics, orbit.deepworld().x(), orbit.deepworld().y(), orbit.amberworldOrbit());
		drawOrbitRing(graphics, orbit.beyondlands().x(), orbit.beyondlands().y(), orbit.beyondlandsIiOrbit());

		SolarSystemLayout.drawBody(graphics, SUN, orbit.cx(), orbit.cy(), orbit.sunSize(), this.selected == Body.SUN ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, MERCURY, orbit.mercury().x(), orbit.mercury().y(), orbit.mercurySize(), this.selected == Body.MERCURY ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, VENUS, orbit.venus().x(), orbit.venus().y(), orbit.venusSize(), this.selected == Body.VENUS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, OVERWORLD, orbit.overworld().x(), orbit.overworld().y(), orbit.overworldSize(), this.selected == Body.OVERWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, OUTERWORLD, orbit.outerworld().x(), orbit.outerworld().y(), orbit.outerworldSize(), this.selected == Body.OUTERWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, WANDERLANDS, orbit.wanderlands().x(), orbit.wanderlands().y(), orbit.wanderlandsSize(), this.selected == Body.WANDERLANDS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, HIGHWORLD, orbit.highworld().x(), orbit.highworld().y(), orbit.highworldSize(), this.selected == Body.HIGHWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, DEEPWORLD, orbit.deepworld().x(), orbit.deepworld().y(), orbit.deepworldSize(), this.selected == Body.DEEPWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, FARWORLD, orbit.farworld().x(), orbit.farworld().y(), orbit.farworldSize(), this.selected == Body.FARWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, EDGEWORLD, orbit.edgeworld().x(), orbit.edgeworld().y(), orbit.edgeworldSize(), this.selected == Body.EDGEWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, BEYONDLANDS, orbit.beyondlands().x(), orbit.beyondlands().y(), orbit.beyondlandsSize(), this.selected == Body.BEYONDLANDS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, BEYONDLANDS_II, orbit.beyondlandsIi().x(), orbit.beyondlandsIi().y(), orbit.beyondlandsIiSize(), this.selected == Body.BEYONDLANDS_II ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, SPINLANDS, orbit.spinlands().x(), orbit.spinlands().y(), orbit.spinlandsWidth(), orbit.spinlandsHeight(), this.selected == Body.SPINLANDS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, SCARLETLANDS, orbit.scarletlands().x(), orbit.scarletlands().y(), orbit.scarletlandsSize(), this.selected == Body.SCARLETLANDS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, LONELANDS, orbit.lonelands().x(), orbit.lonelands().y(), orbit.lonelandsSize(), this.selected == Body.LONELANDS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, EMBERWORLD, orbit.emberworld().x(), orbit.emberworld().y(), orbit.emberworldSize(), this.selected == Body.EMBERWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, FROSTWORLD, orbit.frostworld().x(), orbit.frostworld().y(), orbit.frostworldSize(), this.selected == Body.FROSTWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, AMBERWORLD, orbit.amberworld().x(), orbit.amberworld().y(), orbit.amberworldSize(), this.selected == Body.AMBERWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, SPONGEWORLD, orbit.spongeworld().x(), orbit.spongeworld().y(), orbit.spongeworldSize(), this.selected == Body.SPONGEWORLD ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, POTATOWORLDS, orbit.potatoworlds().x(), orbit.potatoworlds().y(), orbit.potatoworldsSize(), this.selected == Body.POTATOWORLDS ? HIGHLIGHT : 0);
		SolarSystemLayout.drawBody(graphics, MOON, orbit.moon().x(), orbit.moon().y(), orbit.moonSize(), this.selected == Body.MOON ? HIGHLIGHT : 0);

		if (this.selected != null) {
			drawVisitPanel(graphics, this.selected);
		}
		// Widgets (Visit / Yes / No) must render after the panel fill or they are covered.
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0.0) {
			float next = this.zoom + (float) scrollY * ZOOM_STEP * this.zoom;
			this.zoom = Mth.clamp(next, MIN_ZOOM, MAX_ZOOM);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (event.button() == 2 || event.button() == 3) {
			this.panX += (float) dragX;
			this.panY += (float) dragY;
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 1) {
			return super.mouseClicked(event, doubleClick);
		}

		int mx = (int) event.x();
		int my = (int) event.y();
		if (this.selected != null && isInsidePanel(mx, my)) {
			return super.mouseClicked(event, doubleClick);
		}

		Body hit = findHovered(mx, my, currentOrbit());
		if (hit != null) {
			if (this.selected == hit) {
				closeDossierPanels();
			} else {
				this.selected = hit;
				this.sunConfirmStep = SUN_CONFIRM_NONE;
				updatePanelButtons();
			}
			return true;
		}
		if (this.selected != null) {
			closeDossierPanels();
			return true;
		}
		return false;
	}

	private boolean canVisit(Body body) {
		if (body.destinationId == null) {
			return false;
		}
		if (this.quantumPod) {
			return true;
		}
		return body.visitableOnPad;
	}

	private boolean requiresUpgrade(Body body) {
		return !this.quantumPod && body.requiresUpgrade;
	}

	private boolean inSunConfirm() {
		return this.selected == Body.SUN && this.sunConfirmStep > SUN_CONFIRM_NONE;
	}

	private void drawVisitPanel(GuiGraphicsExtractor graphics, Body body) {
		Component title = panelTitle(body);
		Component upgrade = (!inSunConfirm() && requiresUpgrade(body))
			? Component.translatable("gui.theouterworlds.rift_pad.requires_upgrade")
			: null;
		int titleW = this.font.width(title);
		int upgradeW = upgrade != null ? this.font.width(upgrade) : 0;
		boolean dualButtons = inSunConfirm();
		boolean showVisit = !dualButtons && canVisit(body);
		int buttonBlock = 0;
		if (dualButtons) {
			buttonBlock = BUTTON_HEIGHT * 2 + BUTTON_GAP + 4;
		} else if (showVisit) {
			buttonBlock = BUTTON_HEIGHT + 4;
		}
		this.panelW = Math.max(PANEL_MIN_WIDTH, Math.max(titleW, Math.max(upgradeW, BUTTON_WIDTH)) + PANEL_PADDING * 2);
		this.panelH = PANEL_PADDING + 12 + 8
			+ (upgrade != null ? 12 + 6 : 0)
			+ buttonBlock
			+ PANEL_PADDING;

		int x = PANEL_X;
		int y = PANEL_Y;
		graphics.fill(x - 2, y - 2, x + this.panelW + 2, y + this.panelH + 2, 0xAA000000);
		graphics.fill(x, y, x + this.panelW, y + this.panelH, 0xCC101820);
		graphics.text(this.font, title, x + PANEL_PADDING, y + PANEL_PADDING, 0xFFFFFFFF, false);
		if (upgrade != null) {
			graphics.text(this.font, upgrade, x + PANEL_PADDING, y + PANEL_PADDING + 12 + 6, 0xFFFFAA55, false);
		}
		updatePanelButtons();
	}

	private Component panelTitle(Body body) {
		return switch (this.sunConfirmStep) {
			case SUN_CONFIRM_SURE -> Component.translatable("gui.theouterworlds.rift_pad.sun_sure");
			case SUN_CONFIRM_REALLY -> Component.translatable("gui.theouterworlds.rift_pad.sun_really_sure");
			case SUN_CONFIRM_CHANCES -> Component.translatable("gui.theouterworlds.rift_pad.sun_chances");
			default -> body.tooltip;
		};
	}

	private void updatePanelButtons() {
		if (this.visitButton == null || this.yesButton == null || this.noButton == null) {
			return;
		}
		boolean dual = inSunConfirm();
		boolean showVisit = this.selected != null && canVisit(this.selected) && !dual;
		this.visitButton.visible = showVisit;
		this.visitButton.active = showVisit;
		this.yesButton.visible = dual;
		this.yesButton.active = dual;
		this.noButton.visible = dual;
		this.noButton.active = dual;

		int buttonX = PANEL_X + PANEL_PADDING;
		int buttonY = PANEL_Y + PANEL_PADDING + 12 + 8;
		if (this.selected != null && !dual && requiresUpgrade(this.selected)) {
			buttonY += 12 + 6;
		}
		if (showVisit) {
			this.visitButton.setMessage(Component.translatable("gui.theouterworlds.rift_pad.visit"));
			this.visitButton.setRectangle(BUTTON_WIDTH, BUTTON_HEIGHT, buttonX, buttonY);
		}
		if (dual) {
			boolean finalStep = this.sunConfirmStep == SUN_CONFIRM_CHANCES;
			this.yesButton.setMessage(finalStep
				? Component.translatable("gui.theouterworlds.rift_pad.visit")
				: Component.translatable("gui.theouterworlds.rift_pad.yes"));
			this.noButton.setMessage(finalStep
				? Component.translatable("gui.theouterworlds.rift_pad.cancel")
				: Component.translatable("gui.theouterworlds.rift_pad.no"));
			this.yesButton.setRectangle(BUTTON_WIDTH, BUTTON_HEIGHT, buttonX, buttonY);
			this.noButton.setRectangle(BUTTON_WIDTH, BUTTON_HEIGHT, buttonX, buttonY + BUTTON_HEIGHT + BUTTON_GAP);
		}
	}

	private void onPrimaryAction() {
		if (this.selected == null || !canVisit(this.selected) || this.selected.destinationId == null) {
			return;
		}
		if (this.selected == Body.SUN) {
			this.sunConfirmStep = SUN_CONFIRM_SURE;
			updatePanelButtons();
			return;
		}
		requestVisit(this.selected.destinationId);
	}

	private void onYes() {
		if (this.selected != Body.SUN) {
			return;
		}
		if (this.sunConfirmStep == SUN_CONFIRM_SURE) {
			this.sunConfirmStep = SUN_CONFIRM_REALLY;
			updatePanelButtons();
			return;
		}
		if (this.sunConfirmStep == SUN_CONFIRM_REALLY) {
			this.sunConfirmStep = SUN_CONFIRM_CHANCES;
			updatePanelButtons();
			return;
		}
		if (this.sunConfirmStep == SUN_CONFIRM_CHANCES && this.selected.destinationId != null) {
			requestVisit(this.selected.destinationId);
		}
	}

	private void closeDossierPanels() {
		this.selected = null;
		this.sunConfirmStep = SUN_CONFIRM_NONE;
		updatePanelButtons();
	}

	private boolean isInsidePanel(int mouseX, int mouseY) {
		return mouseX >= PANEL_X - 2
			&& mouseX <= PANEL_X + this.panelW + 2
			&& mouseY >= PANEL_Y - 2
			&& mouseY <= PANEL_Y + this.panelH + 2;
	}

	private void requestVisit(String destinationId) {
		ClientPlayNetworking.send(new RiftPadVisitPacket(destinationId));
		this.onClose();
	}

	private SolarSystemLayout.Snapshot currentOrbit() {
		return SolarSystemLayout.compute(this.width, this.height, this.zoom, this.openMillis, this.panX, this.panY);
	}

	@Nullable
	private Body findHovered(int mouseX, int mouseY, SolarSystemLayout.Snapshot orbit) {
		if (hit(mouseX, mouseY, orbit.lonelands().x(), orbit.lonelands().y(), orbit.hitRadius(orbit.lonelandsSize()))) {
			return Body.LONELANDS;
		}
		if (hit(mouseX, mouseY, orbit.scarletlands().x(), orbit.scarletlands().y(), orbit.hitRadius(orbit.scarletlandsSize()))) {
			return Body.SCARLETLANDS;
		}
		if (hit(mouseX, mouseY, orbit.spinlands().x(), orbit.spinlands().y(), orbit.hitRadius(orbit.spinlandsWidth(), orbit.spinlandsHeight()))) {
			return Body.SPINLANDS;
		}
		if (hit(mouseX, mouseY, orbit.beyondlandsIi().x(), orbit.beyondlandsIi().y(), orbit.hitRadius(orbit.beyondlandsIiSize()))) {
			return Body.BEYONDLANDS_II;
		}
		if (hit(mouseX, mouseY, orbit.spongeworld().x(), orbit.spongeworld().y(), orbit.hitRadius(orbit.spongeworldSize()))) {
			return Body.SPONGEWORLD;
		}
		if (hit(mouseX, mouseY, orbit.amberworld().x(), orbit.amberworld().y(), orbit.hitRadius(orbit.amberworldSize()))) {
			return Body.AMBERWORLD;
		}
		if (hit(mouseX, mouseY, orbit.frostworld().x(), orbit.frostworld().y(), orbit.hitRadius(orbit.frostworldSize()))) {
			return Body.FROSTWORLD;
		}
		if (hit(mouseX, mouseY, orbit.emberworld().x(), orbit.emberworld().y(), orbit.hitRadius(orbit.emberworldSize()))) {
			return Body.EMBERWORLD;
		}
		if (hit(mouseX, mouseY, orbit.potatoworlds().x(), orbit.potatoworlds().y(), orbit.hitRadius(orbit.potatoworldsSize()))) {
			return Body.POTATOWORLDS;
		}
		if (hit(mouseX, mouseY, orbit.moon().x(), orbit.moon().y(), orbit.hitRadius(orbit.moonSize()))) {
			return Body.MOON;
		}
		if (hit(mouseX, mouseY, orbit.beyondlands().x(), orbit.beyondlands().y(), orbit.hitRadius(orbit.beyondlandsSize()))) {
			return Body.BEYONDLANDS;
		}
		if (hit(mouseX, mouseY, orbit.edgeworld().x(), orbit.edgeworld().y(), orbit.hitRadius(orbit.edgeworldSize()))) {
			return Body.EDGEWORLD;
		}
		if (hit(mouseX, mouseY, orbit.farworld().x(), orbit.farworld().y(), orbit.hitRadius(orbit.farworldSize()))) {
			return Body.FARWORLD;
		}
		if (hit(mouseX, mouseY, orbit.deepworld().x(), orbit.deepworld().y(), orbit.hitRadius(orbit.deepworldSize()))) {
			return Body.DEEPWORLD;
		}
		if (hit(mouseX, mouseY, orbit.highworld().x(), orbit.highworld().y(), orbit.hitRadius(orbit.highworldSize()))) {
			return Body.HIGHWORLD;
		}
		if (hit(mouseX, mouseY, orbit.wanderlands().x(), orbit.wanderlands().y(), orbit.hitRadius(orbit.wanderlandsSize()))) {
			return Body.WANDERLANDS;
		}
		if (hit(mouseX, mouseY, orbit.outerworld().x(), orbit.outerworld().y(), orbit.hitRadius(orbit.outerworldSize()))) {
			return Body.OUTERWORLD;
		}
		if (hit(mouseX, mouseY, orbit.overworld().x(), orbit.overworld().y(), orbit.hitRadius(orbit.overworldSize()))) {
			return Body.OVERWORLD;
		}
		if (hit(mouseX, mouseY, orbit.venus().x(), orbit.venus().y(), orbit.hitRadius(orbit.venusSize()))) {
			return Body.VENUS;
		}
		if (hit(mouseX, mouseY, orbit.mercury().x(), orbit.mercury().y(), orbit.hitRadius(orbit.mercurySize()))) {
			return Body.MERCURY;
		}
		if (hit(mouseX, mouseY, orbit.cx(), orbit.cy(), orbit.hitRadius(orbit.sunSize()))) {
			return Body.SUN;
		}
		return null;
	}

	private static boolean hit(int mouseX, int mouseY, int x, int y, int radius) {
		int dx = mouseX - x;
		int dy = mouseY - y;
		return dx * dx + dy * dy <= radius * radius;
	}

	private static void drawOrbitRing(GuiGraphicsExtractor graphics, int cx, int cy, float radius) {
		int steps = 64;
		int color = 0x44FFFFFF;
		for (int i = 0; i < steps; i++) {
			float a0 = (float) (Math.PI * 2.0 * i / steps);
			int x0 = cx + Math.round(Mth.cos(a0) * radius);
			int y0 = cy + Math.round(Mth.sin(a0) * radius);
			graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
		}
	}

	private enum Body {
		SUN(Component.translatable("gui.theouterworlds.astral_telescope.sun"), false, true, "theouterworlds:sun"),
		MERCURY(Component.translatable("gui.theouterworlds.astral_telescope.mercury"), true, false, "theouterworlds:innerworld"),
		VENUS(Component.translatable("gui.theouterworlds.astral_telescope.venus"), true, false, "theouterworlds:nearworld"),
		OVERWORLD(Component.translatable("gui.theouterworlds.astral_telescope.overworld"), true, false, "minecraft:overworld"),
		OUTERWORLD(Component.translatable("gui.theouterworlds.astral_telescope.outerworld"), true, false, "theouterworlds:outerworld"),
		WANDERLANDS(Component.translatable("gui.theouterworlds.astral_telescope.wanderlands"), false, true, "theouterworlds:wanderlands"),
		HIGHWORLD(Component.translatable("gui.theouterworlds.astral_telescope.highworld"), true, false, "theouterworlds:highworld"),
		DEEPWORLD(Component.translatable("gui.theouterworlds.astral_telescope.deepworld"), true, false, "theouterworlds:deepworld"),
		FARWORLD(Component.translatable("gui.theouterworlds.astral_telescope.farworld"), true, false, "theouterworlds:farworld"),
		EDGEWORLD(Component.translatable("gui.theouterworlds.astral_telescope.edgeworld"), true, false, "theouterworlds:edgeworld"),
		BEYONDLANDS(Component.translatable("gui.theouterworlds.astral_telescope.beyondlands"), false, true, "theouterworlds:beyondlands"),
		BEYONDLANDS_II(Component.translatable("gui.theouterworlds.astral_telescope.beyondlands_ii"), false, true, "theouterworlds:beyondlands_ii"),
		SPINLANDS(Component.translatable("gui.theouterworlds.astral_telescope.spinlands"), false, true, "theouterworlds:spinlands"),
		SCARLETLANDS(Component.translatable("gui.theouterworlds.astral_telescope.scarletlands"), false, true, "theouterworlds:scarletlands"),
		LONELANDS(Component.translatable("gui.theouterworlds.astral_telescope.lonelands"), false, true, "theouterworlds:lonelands"),
		MOON(Component.translatable("gui.theouterworlds.rift_pad.moon"), true, false, "theouterworlds:moon"),
		EMBERWORLD(Component.translatable("gui.theouterworlds.astral_telescope.emberworld"), false, true, "theouterworlds:emberworld"),
		FROSTWORLD(Component.translatable("gui.theouterworlds.astral_telescope.frostworld"), false, true, "theouterworlds:frostworld"),
		AMBERWORLD(Component.translatable("gui.theouterworlds.astral_telescope.amberworld"), false, true, "theouterworlds:amberworld"),
		SPONGEWORLD(Component.translatable("gui.theouterworlds.astral_telescope.spongeworld"), false, true, "theouterworlds:spongeworld"),
		POTATOWORLDS(Component.translatable("gui.theouterworlds.astral_telescope.potatoworlds"), false, true, "theouterworlds:potatoworlds");

		final Component tooltip;
		/** Visit-able from a bare rift pad. */
		final boolean visitableOnPad;
		/** Needs Quantum Pod when using a rift pad. */
		final boolean requiresUpgrade;
		@Nullable
		final String destinationId;

		Body(Component tooltip, boolean visitableOnPad, boolean requiresUpgrade, @Nullable String destinationId) {
			this.tooltip = tooltip;
			this.visitableOnPad = visitableOnPad;
			this.requiresUpgrade = requiresUpgrade;
			this.destinationId = destinationId;
		}
	}
}
