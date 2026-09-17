package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.AstralTelescopeBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.screen.AstralTelescopeMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Fullscreen telescope view: space backdrop with the sun and orbiting worlds.
 * Full observation only works on the Moon with sky access; other dimensions show blocked signals.
 * Mouse wheel zooms the solar system (background stays fixed). Escape closes via the normal screen keybind.
 */
public class AstralTelescopeScreen extends Screen implements MenuAccess<AstralTelescopeMenu> {
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

	/** Nearworld sulfuric yellow ({@link NearworldAtmosphere#HAZE_COLOR}). */
	private static final int TINT_NEARWORLD = 0xE0FFE01F;
	/** Innerworld — sun-like yellow-white. */
	private static final int TINT_INNERWORLD = 0xE0FFF8DC;
	/** Outerworld — dusty orange-reddish sky. */
	private static final int TINT_OUTERWORLD = 0xE0C45A28;
	/** Highworld — amber Jovian haze. */
	private static final int TINT_HIGHWORLD = 0xE0C9A227;
	/** Deepworld — pale Saturn haze. */
	private static final int TINT_DEEPWORLD = 0xE0D4C48A;
	/** Farworld — light teal ice-giant haze. */
	private static final int TINT_FARWORLD = 0xE07EC8D0;
	/** Edgeworld — deep soul-blue haze. */
	private static final int TINT_EDGEWORLD = 0xE01A2A6E;
	/** Overworld daytime — earthy blue. */
	private static final int TINT_OVERWORLD_DAY = 0xE04A6FA5;
	/** Obstructed sky. */
	private static final int TINT_BLOCKED = 0xE0181820;

	private static final float MIN_ZOOM = 0.30F;
	private static final float MAX_ZOOM = 3.6F;
	private static final float ZOOM_STEP = 0.08F;

	private final AstralTelescopeMenu menu;
	private long openMillis;
	private float zoom = 1.0F;
	private float panX = 0.0F;
	private float panY = 0.0F;
	private ViewMode viewMode = ViewMode.FULL;
	@Nullable
	private Body selected;

	public AstralTelescopeScreen(AstralTelescopeMenu menu, Inventory inventory, Component title) {
		super(title);
		this.menu = menu;
	}

	@Override
	public AstralTelescopeMenu getMenu() {
		return this.menu;
	}

	@Override
	protected void init() {
		super.init();
		this.openMillis = System.currentTimeMillis();
		this.viewMode = resolveViewMode();
		this.selected = null;
		this.zoom = 1.0F;
		this.panX = 0.0F;
		this.panY = 0.0F;
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
		if (this.viewMode == ViewMode.FULL || this.viewMode == ViewMode.OVERWORLD_NIGHT) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, SPACE, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
		} else {
			graphics.fill(0, 0, this.width, this.height, this.viewMode.tint);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		if (this.viewMode == ViewMode.FULL) {
			renderFullSystem(graphics, mouseX, mouseY);
			return;
		}
		if (this.viewMode == ViewMode.OVERWORLD_NIGHT) {
			renderOverworldNight(graphics, mouseX, mouseY);
			return;
		}
		drawDetailPanel(graphics, this.viewMode.failureMessage);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.viewMode == ViewMode.FULL && scrollY != 0.0) {
			float oldZoom = this.zoom;
			this.zoom = Mth.clamp(this.zoom + (float) scrollY * ZOOM_STEP, MIN_ZOOM, MAX_ZOOM);
			this.panX = SolarSystemLayout.panAfterZoom(this.panX, mouseX, this.width / 2, oldZoom, this.zoom);
			this.panY = SolarSystemLayout.panAfterZoom(this.panY, mouseY, this.height / 2, oldZoom, this.zoom);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void renderFullSystem(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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

		SolarSystemLayout.drawBody(graphics, SUN, orbit.cx(), orbit.cy(), orbit.sunSize());
		SolarSystemLayout.drawBody(graphics, MERCURY, orbit.mercury().x(), orbit.mercury().y(), orbit.mercurySize());
		SolarSystemLayout.drawBody(graphics, VENUS, orbit.venus().x(), orbit.venus().y(), orbit.venusSize());
		SolarSystemLayout.drawBody(graphics, OVERWORLD, orbit.overworld().x(), orbit.overworld().y(), orbit.overworldSize());
		SolarSystemLayout.drawBody(graphics, OUTERWORLD, orbit.outerworld().x(), orbit.outerworld().y(), orbit.outerworldSize());
		SolarSystemLayout.drawBody(graphics, WANDERLANDS, orbit.wanderlands().x(), orbit.wanderlands().y(), orbit.wanderlandsSize());
		SolarSystemLayout.drawBody(graphics, HIGHWORLD, orbit.highworld().x(), orbit.highworld().y(), orbit.highworldSize());
		SolarSystemLayout.drawBody(graphics, DEEPWORLD, orbit.deepworld().x(), orbit.deepworld().y(), orbit.deepworldSize());
		SolarSystemLayout.drawBody(graphics, FARWORLD, orbit.farworld().x(), orbit.farworld().y(), orbit.farworldSize());
		SolarSystemLayout.drawBody(graphics, EDGEWORLD, orbit.edgeworld().x(), orbit.edgeworld().y(), orbit.edgeworldSize());
		SolarSystemLayout.drawBody(graphics, BEYONDLANDS, orbit.beyondlands().x(), orbit.beyondlands().y(), orbit.beyondlandsSize());
		SolarSystemLayout.drawBody(graphics, MOON, orbit.moon().x(), orbit.moon().y(), orbit.moonSize());
		SolarSystemLayout.drawBody(graphics, POTATOWORLDS, orbit.potatoworlds().x(), orbit.potatoworlds().y(), orbit.potatoworldsSize());
		SolarSystemLayout.drawBody(graphics, EMBERWORLD, orbit.emberworld().x(), orbit.emberworld().y(), orbit.emberworldSize());
		SolarSystemLayout.drawBody(graphics, FROSTWORLD, orbit.frostworld().x(), orbit.frostworld().y(), orbit.frostworldSize());
		SolarSystemLayout.drawBody(graphics, AMBERWORLD, orbit.amberworld().x(), orbit.amberworld().y(), orbit.amberworldSize());
		SolarSystemLayout.drawBody(graphics, SPONGEWORLD, orbit.spongeworld().x(), orbit.spongeworld().y(), orbit.spongeworldSize());
		SolarSystemLayout.drawBody(graphics, BEYONDLANDS_II, orbit.beyondlandsIi().x(), orbit.beyondlandsIi().y(), orbit.beyondlandsIiSize());
		SolarSystemLayout.drawBody(
			graphics,
			SPINLANDS,
			orbit.spinlands().x(),
			orbit.spinlands().y(),
			orbit.spinlandsWidth(),
			orbit.spinlandsHeight(),
			0
		);
		SolarSystemLayout.drawBody(graphics, SCARLETLANDS, orbit.scarletlands().x(), orbit.scarletlands().y(), orbit.scarletlandsSize());
		SolarSystemLayout.drawBody(graphics, LONELANDS, orbit.lonelands().x(), orbit.lonelands().y(), orbit.lonelandsSize());

		if (this.selected != null) {
			drawDetailPanel(graphics, this.selected.dossier);
		} else {
			Body hovered = findHovered(mouseX, mouseY, orbit);
			if (hovered != null) {
				graphics.setTooltipForNextFrame(this.font, hovered.tooltip, mouseX, mouseY);
			}
		}
	}

	private void renderOverworldNight(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int cx = this.width / 2;
		int cy = this.height / 2;
		SolarSystemLayout.drawBody(graphics, MOON, cx, cy, 96);

		boolean hoveringMoon = hit(mouseX, mouseY, cx, cy, 48);
		if (this.selected == Body.MOON_OVERWORLD_NIGHT) {
			drawDetailPanel(graphics, Body.MOON_OVERWORLD_NIGHT.dossier);
		} else if (hoveringMoon) {
			graphics.setTooltipForNextFrame(this.font, Body.MOON_OVERWORLD_NIGHT.tooltip, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubled);
		}
		if (this.viewMode == ViewMode.FULL) {
			Body hit = findHovered((int) event.x(), (int) event.y(), currentOrbit());
			if (hit != null && hit.hasDossier()) {
				this.selected = this.selected == hit ? null : hit;
				return true;
			}
			if (this.selected != null) {
				this.selected = null;
				return true;
			}
		} else if (this.viewMode == ViewMode.OVERWORLD_NIGHT) {
			int cx = this.width / 2;
			int cy = this.height / 2;
			if (hit((int) event.x(), (int) event.y(), cx, cy, 48)) {
				this.selected = this.selected == Body.MOON_OVERWORLD_NIGHT ? null : Body.MOON_OVERWORLD_NIGHT;
				return true;
			}
			if (this.selected != null) {
				this.selected = null;
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private ViewMode resolveViewMode() {
		if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) {
			return ViewMode.BLOCKED_SKY;
		}
		Level level = this.minecraft.level;
		BlockPos telescopePos = findTelescopePos(level, this.minecraft.player.blockPosition());
		if (telescopePos == null || !AstralTelescopeBlock.hasSkyAccess(level, telescopePos)) {
			return ViewMode.BLOCKED_SKY;
		}

		ResourceKey<Level> dimension = level.dimension();
		if (ModDimensions.isMoon(dimension)
			|| ModDimensions.isEmberworld(dimension)
			|| ModDimensions.isFrostworld(dimension)
			|| ModDimensions.isAmberworld(dimension)
			|| ModDimensions.isSpongeworld(dimension)
			|| ModDimensions.isPotatoworlds(dimension)
			|| ModDimensions.isWanderlands(dimension)
			|| ModDimensions.isBeyondlands(dimension)
			|| ModDimensions.isBeyondlandsIi(dimension)
			|| ModDimensions.isSpinlands(dimension)
			|| ModDimensions.isScarletlands(dimension)
			|| ModDimensions.isLonelands(dimension)) {
			return ViewMode.FULL;
		}
		if (ModDimensions.isNearworld(dimension)) {
			return ViewMode.NEARWORLD;
		}
		if (ModDimensions.isInnerworld(dimension)) {
			return ViewMode.INNERWORLD;
		}
		if (ModDimensions.isOuterworld(dimension)) {
			return ViewMode.OUTERWORLD;
		}
		if (ModDimensions.isHighworld(dimension)) {
			return ViewMode.HIGHWORLD;
		}
		if (ModDimensions.isDeepworld(dimension)) {
			return ViewMode.DEEPWORLD;
		}
		if (ModDimensions.isFarworld(dimension)) {
			return ViewMode.FARWORLD;
		}
		if (ModDimensions.isEdgeworld(dimension)) {
			return ViewMode.EDGEWORLD;
		}
		if (dimension.equals(Level.OVERWORLD)) {
			long dayTime = Math.floorMod(level.getDefaultClockTime(), 24000L);
			// Vanilla: night begins around 13000.
			boolean daytime = dayTime < 13000L;
			return daytime ? ViewMode.OVERWORLD_DAY : ViewMode.OVERWORLD_NIGHT;
		}
		return ViewMode.BLOCKED_SKY;
	}

	@Nullable
	private BlockPos findTelescopePos(Level level, BlockPos near) {
		BlockPos menuPos = this.menu.getTelescopePos();
		if (menuPos != null && !menuPos.equals(BlockPos.ZERO) && level.getBlockState(menuPos).is(ModBlocks.ASTRAL_TELESCOPE)) {
			return menuPos;
		}
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dy = -2; dy <= 2; dy++) {
			for (int dx = -4; dx <= 4; dx++) {
				for (int dz = -4; dz <= 4; dz++) {
					cursor.set(near.getX() + dx, near.getY() + dy, near.getZ() + dz);
					if (level.getBlockState(cursor).is(ModBlocks.ASTRAL_TELESCOPE)) {
						return cursor.immutable();
					}
				}
			}
		}
		return null;
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

	private void drawDetailPanel(GuiGraphicsExtractor graphics, List<Component> lines) {
		int lineHeight = 12;
		int padding = 10;
		int maxWidth = 0;
		for (Component line : lines) {
			maxWidth = Math.max(maxWidth, this.font.width(line));
		}
		int panelW = maxWidth + padding * 2;
		int panelH = lines.size() * lineHeight + padding * 2;
		int x = 16;
		int y = 16;

		graphics.fill(x - 2, y - 2, x + panelW + 2, y + panelH + 2, 0xAA000000);
		graphics.fill(x, y, x + panelW, y + panelH, 0xCC101820);

		int textY = y + padding;
		for (Component line : lines) {
			graphics.text(this.font, line, x + padding, textY, 0xFFFFFFFF, false);
			textY += lineHeight;
		}
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

	private static MutableComponent bold(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withBold(true));
	}

	private static MutableComponent italic(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withItalic(true));
	}

	private static MutableComponent plain(String text) {
		return Component.literal(text);
	}

	private static MutableComponent labeled(String label, Component value) {
		return Component.literal(label).append(value);
	}

	private enum ViewMode {
		FULL(0, List.of()),
		OVERWORLD_NIGHT(0, List.of()),
		NEARWORLD(TINT_NEARWORLD, List.of(plain("Dense atmosphere too thick for observation!"))),
		INNERWORLD(TINT_INNERWORLD, List.of(plain("Sunlight too bright for observation!"))),
		OUTERWORLD(TINT_OUTERWORLD, List.of(plain("Dust too dense for observation!"))),
		HIGHWORLD(TINT_HIGHWORLD, List.of(plain("Atmosphere too turbulent for observation!"))),
		DEEPWORLD(TINT_DEEPWORLD, List.of(plain("Atmosphere too turbulent for observation!"))),
		FARWORLD(TINT_FARWORLD, List.of(plain("Atmosphere too turbulent for observation!"))),
		EDGEWORLD(TINT_EDGEWORLD, List.of(plain("Atmosphere too turbulent for observation!"))),
		OVERWORLD_DAY(TINT_OVERWORLD_DAY, List.of(plain("Sunlight scattered too dense for observation!"))),
		BLOCKED_SKY(TINT_BLOCKED, List.of(plain("No clear view of the sky!")));

		final int tint;
		final List<Component> failureMessage;

		ViewMode(int tint, List<Component> failureMessage) {
			this.tint = tint;
			this.failureMessage = failureMessage;
		}
	}

	private enum Body {
		SUN(
			Component.translatable("gui.theouterworlds.astral_telescope.sun"),
			List.of(
				bold("SUN"),
				labeled("Classification: ", italic("Star")),
				plain("Atmosphere: Dense plasma"),
				plain("Gravity: 27.9g"),
				plain("Surface Conditions: Incinerating"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": None."))
			)
		),
		MERCURY(
			Component.translatable("gui.theouterworlds.astral_telescope.mercury"),
			List.of(
				bold("INNERWORLD"),
				labeled("Classification: ", italic("Terrestrial")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.37g"),
				plain("Surface Conditions: Extreme heat"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Redsteel or Graphite trim resists heat. Glass helmet required."))
			)
		),
		VENUS(
			Component.translatable("gui.theouterworlds.astral_telescope.venus"),
			List.of(
				bold("NEARWORLD"),
				labeled("Classification: ", italic("Terrestrial")),
				plain("Atmosphere: Toxic / crushing"),
				plain("Gravity: 0.90g"),
				plain("Surface Conditions: Extreme pressure and heat"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet + 3 Redsteel pieces (or 1 Graphite trim) required."))
			)
		),
		OVERWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.overworld"),
			List.of(
				bold("OVERWORLD"),
				labeled("Classification: ", italic("Terrestrial")),
				plain("Atmosphere: Breathable"),
				plain("Gravity: 1.00g"),
				plain("Surface: Habitable"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": No special equipment required."))
			)
		),
		OUTERWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.outerworld"),
			List.of(
				bold("OUTERWORLD"),
				labeled("Classification: ", italic("Terrestrial")),
				plain("Atmosphere: Unbreathable"),
				plain("Gravity: 0.38g"),
				plain("Surface Conditions: Hostile"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		WANDERLANDS(
			Component.translatable("gui.theouterworlds.astral_telescope.wanderlands"),
			List.of(
				bold("WANDERLANDS"),
				labeled("Classification: ", italic("Dwarf planet")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.03g"),
				plain("Surface Conditions: Vacuum — anorthosite crust, salt crater floors"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		HIGHWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.highworld"),
			List.of(
				bold("HIGHWORLD"),
				labeled("Classification: ", italic("Gas giant")),
				plain("Atmosphere: Toxic / Crushing"),
				plain("Gravity: 2.5g"),
				plain("Surface Conditions: Dense liquid hydrogen"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet + 3 Iridium armor pieces required."))
			)
		),
		DEEPWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.deepworld"),
			List.of(
				bold("DEEPWORLD"),
				labeled("Classification: ", italic("Gas giant")),
				plain("Atmosphere: Toxic / Crushing"),
				plain("Gravity: 1.1g"),
				plain("Surface Conditions: Liquid helium beneath liquid hydrogen"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet + 3 Iridium armor pieces required."))
			)
		),
		FARWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.farworld"),
			List.of(
				bold("FARWORLD"),
				labeled("Classification: ", italic("Ice giant")),
				plain("Atmosphere: Toxic / Crushing"),
				plain("Gravity: 0.89g"),
				plain("Surface Conditions: Liquid ammonia ocean, diamond rain"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet + 3 Iridium armor pieces required."))
			)
		),
		EDGEWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.edgeworld"),
			List.of(
				bold("EDGEWORLD"),
				labeled("Classification: ", italic("Ice giant")),
				plain("Atmosphere: Toxic / Crushing"),
				plain("Gravity: 1.14g"),
				plain("Surface Conditions: Liquid methane ocean"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet + 3 Iridium armor pieces required."))
			)
		),
		BEYONDLANDS(
			Component.translatable("gui.theouterworlds.astral_telescope.beyondlands"),
			List.of(
				bold("BEYONDLANDS"),
				labeled("Classification: ", italic("Dwarf planet")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.06g"),
				plain("Surface Conditions: Nitrogen ice crust under tholin"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		BEYONDLANDS_II(
			Component.translatable("gui.theouterworlds.astral_telescope.beyondlands_ii"),
			List.of(
				bold("Beyondlands II"),
				labeled("Classification: ", italic("Natural satellite")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.03g"),
				plain("Surface Conditions: Packed-ice body under tholin"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		SPINLANDS(
			Component.translatable("gui.theouterworlds.astral_telescope.spinlands"),
			List.of(
				bold("SPINLANDS"),
				labeled("Classification: ", italic("Dwarf planet")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.04g"),
				plain("Surface Conditions: Rapidly spinning ice ellipsoid under tholin"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		SCARLETLANDS(
			Component.translatable("gui.theouterworlds.astral_telescope.scarletlands"),
			List.of(
				bold("SCARLETLANDS"),
				labeled("Classification: ", italic("Dwarf planet")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.04g"),
				plain("Surface Conditions: Methane ice crust with tholin patches, impact basins"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		LONELANDS(
			Component.translatable("gui.theouterworlds.astral_telescope.lonelands"),
			List.of(
				bold("LONELANDS"),
				labeled("Classification: ", italic("Dwarf planet")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.08g"),
				plain("Surface Conditions: Dry ice over a thick methane ice shell"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		MOON(
			Component.translatable("gui.theouterworlds.rift_pad.moon"),
			List.of(
				bold("MOON"),
				labeled("Classification: ", italic("Natural satellite")),
				plain("Atmosphere: None"),
				plain("Gravity: Low"),
				plain("Surface Conditions: Vacuum"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		EMBERWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.emberworld"),
			List.of(
				bold("EMBERWORLD"),
				labeled("Classification: ", italic("Natural Jovian satellite")),
				plain("Atmosphere: Thin sulfur haze"),
				plain("Gravity: 0.18g"),
				plain("Surface Conditions: Sulfur, lava oceans"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet + 3 Graphite-trimmed Redsteel pieces required."))
			)
		),
		FROSTWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.frostworld"),
			List.of(
				bold("FROSTWORLD"),
				labeled("Classification: ", italic("Natural Jovian satellite")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.13g"),
				plain("Surface Conditions: Ice crust over ocean"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		AMBERWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.amberworld"),
			List.of(
				bold("AMBERWORLD"),
				labeled("Classification: ", italic("Natural Saturnian satellite")),
				plain("Atmosphere: Thick haze (breathable)"),
				plain("Gravity: 0.14g"),
				plain("Surface Conditions: Tholinic regolith, methane seas"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": No special gear required."))
			)
		),
		SPONGEWORLD(
			Component.translatable("gui.theouterworlds.astral_telescope.spongeworld"),
			List.of(
				bold("SPONGEWORLD"),
				labeled("Classification: ", italic("Natural Saturnian satellite")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.05g"),
				plain("Surface Conditions: Porous ice sponge, open void"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		POTATOWORLDS(
			Component.translatable("gui.theouterworlds.astral_telescope.potatoworlds"),
			List.of(
				bold("POTATOWORLDS"),
				labeled("Classification: ", italic("Natural Martian satellites")),
				plain("Atmosphere: None"),
				plain("Gravity: 0.04g"),
				plain("Surface Conditions: Small anorthositic asteroids"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required."))
			)
		),
		MOON_OVERWORLD_NIGHT(
			Component.translatable("gui.theouterworlds.rift_pad.moon"),
			List.of(
				bold("MOON"),
				labeled("Classification: ", italic("Natural satellite")),
				plain("Atmosphere: None"),
				plain("Gravity: Low"),
				plain("Surface Conditions: Vacuum"),
				plain(""),
				Component.empty().append(bold("SURVIVAL")).append(plain(": Glass helmet required.")),
				plain(""),
				plain("Optimal Observational Hub for Telescope!")
			)
		);

		final Component tooltip;
		final List<Component> dossier;

		Body(Component tooltip, List<Component> dossier) {
			this.tooltip = tooltip;
			this.dossier = dossier;
		}

		boolean hasDossier() {
			return !this.dossier.isEmpty();
		}
	}
}
