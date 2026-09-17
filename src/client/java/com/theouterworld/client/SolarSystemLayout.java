package com.theouterworld.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Pixel-clearance solar layout for telescope / rift-pad GUIs.
 * Sprite sizes are screen-relative and scale with zoom. Orbit radii are derived from
 * those sizes so bodies never overlap, including when zoomed in.
 * The system is not shrunk to fit: zoom out to see the outer planets.
 */
public final class SolarSystemLayout {
	private static final float MERCURY_SPEED = 0.045F;
	private static final float VENUS_SPEED = 0.032F;
	private static final float OVERWORLD_SPEED = 0.022F;
	private static final float OUTERWORLD_SPEED = 0.014F;
	private static final float WANDERLANDS_SPEED = 0.010F;
	private static final float HIGHWORLD_SPEED = 0.007F;
	private static final float DEEPWORLD_SPEED = 0.005F;
	private static final float FARWORLD_SPEED = 0.0035F;
	private static final float EDGEWORLD_SPEED = 0.0025F;
	private static final float BEYONDLANDS_SPEED = 0.0018F;
	private static final float SPINLANDS_SPEED = 0.0014F;
	private static final float SCARLETLANDS_SPEED = 0.00115F;
	private static final float LONELANDS_SPEED = 0.00095F;
	private static final float MOON_SPEED = 0.09F;
	private static final float EMBERWORLD_SPEED = 0.11F;
	private static final float AMBERWORLD_SPEED = 0.095F;
	private static final float POTATOWORLDS_SPEED = 0.12F;
	private static final float BEYONDLANDS_II_SPEED = 0.08F;

	private static final float UNIT = 0.50F;
	private static final float GAP = 0.018F;
	private static final float DEEPWORLD_RING_SCALE = 32.0F / 20.0F;

	private SolarSystemLayout() {
	}

	public static void drawBody(
		GuiGraphicsExtractor graphics,
		Identifier texture,
		int cx,
		int cy,
		int size
	) {
		drawBody(graphics, texture, cx, cy, size, 0);
	}

	public static void drawBody(
		GuiGraphicsExtractor graphics,
		Identifier texture,
		int cx,
		int cy,
		int size,
		int outlineColor
	) {
		drawBody(graphics, texture, cx, cy, size, size, outlineColor);
	}

	public static void drawBody(
		GuiGraphicsExtractor graphics,
		Identifier texture,
		int cx,
		int cy,
		int width,
		int height,
		int outlineColor
	) {
		int spriteX = cx - width / 2;
		int spriteY = cy - height / 2;
		if (outlineColor != 0) {
			int outlineW = width + 6;
			int outlineH = height + 6;
			int outlineX = cx - outlineW / 2;
			int outlineY = cy - outlineH / 2;
			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				texture,
				outlineX,
				outlineY,
				0.0F,
				0.0F,
				outlineW,
				outlineH,
				outlineW,
				outlineH,
				outlineColor
			);
		}
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, spriteX, spriteY, 0.0F, 0.0F, width, height, width, height);
	}

	public static Snapshot compute(int width, int height, float zoom, long openMillis) {
		return compute(width, height, zoom, openMillis, 0.0F, 0.0F);
	}

	/** Keep the point under the cursor fixed when zoom changes. */
	public static float panAfterZoom(float pan, double mouse, int origin, float oldZoom, float newZoom) {
		float scale = newZoom / Math.max(0.05F, oldZoom);
		return (float) (mouse - origin - scale * (mouse - origin - pan));
	}

	public static Snapshot compute(int width, int height, float zoom, long openMillis, float panX, float panY) {
		float z = Math.max(0.05F, zoom);
		float unit = Math.min(width, height) * UNIT * z;
		float gap = Math.max(4.0F * z, unit * GAP);

		float sunSize = unit * 0.22F;
		float mercSize = unit * 0.09F;
		float venusSize = unit * 0.12F;
		float overSize = unit * 0.135F;
		float outerSize = unit * 0.14F;
		float lonelySize = unit * 0.07F;
		float highSize = unit * 0.17F;
		float deepSize = highSize * DEEPWORLD_RING_SCALE;
		float farSize = unit * 0.20F;
		float edgeSize = unit * 0.20F;
		float beyondSize = unit * 0.075F;
		float moonSize = unit * 0.065F;
		float emberSize = unit * 0.055F;
		float frostSize = unit * 0.055F;
		float amberSize = unit * 0.06F;
		float spongeSize = unit * 0.045F;
		float potatoSize = unit * 0.05F;
		float beyondIiSize = unit * 0.045F;
		float spinSize = unit * 0.07F;
		float scarletSize = unit * 0.07F;
		float lonelandsSize = unit * 0.075F;

		float sunR = sunSize * 0.5F;
		float mercR = mercSize * 0.5F;
		float venusR = venusSize * 0.5F;
		float overR = overSize * 0.5F;
		float outerR = outerSize * 0.5F;
		float lonelyR = lonelySize * 0.5F;
		float highR = highSize * 0.5F;
		float deepR = deepSize * 0.5F;
		float farR = farSize * 0.5F;
		float edgeR = edgeSize * 0.5F;
		float beyondR = beyondSize * 0.5F;
		float moonR = moonSize * 0.5F;
		float emberR = emberSize * 0.5F;
		float frostR = frostSize * 0.5F;
		float amberR = amberSize * 0.5F;
		float spongeR = spongeSize * 0.5F;
		float potatoR = potatoSize * 0.5F;
		float beyondIiR = beyondIiSize * 0.5F;
		float spinR = spinSize * 0.5F;
		float scarletR = scarletSize * 0.5F;
		float lonelandsR = lonelandsSize * 0.5F;

		float moonOrbit = overR + moonR + gap;
		float martianMoonOrbit = outerR + potatoR + gap;
		float jovianMoonOrbit = highR + Math.max(emberR, frostR) + gap;
		float saturnianMoonOrbit = deepR + Math.max(amberR, spongeR) + gap;
		float plutoMoonOrbit = beyondR + beyondIiR + gap;
		float rMerc = sunR + mercR + gap;
		float rVenus = rMerc + mercR + venusR + gap;
		float rOver = rVenus + venusR + moonOrbit + moonR + gap;
		float rOuter = rOver + moonOrbit + moonR + outerR + gap;
		float rLonely = rOuter + outerR + martianMoonOrbit + potatoR + lonelyR + gap;
		float rHigh = rLonely + lonelyR + highR + gap;
		float rDeep = rHigh + highR + jovianMoonOrbit + Math.max(emberR, frostR) + deepR + gap;
		float rFar = rDeep + deepR + saturnianMoonOrbit + Math.max(amberR, spongeR) + farR + gap;
		float rEdge = rFar + farR + edgeR + gap;
		float rBeyond = rEdge + edgeR + plutoMoonOrbit + beyondIiR + beyondR + gap;
		float rSpin = rBeyond + beyondR + plutoMoonOrbit + beyondIiR + spinR + gap;
		float rScarlet = rSpin + spinR + scarletR + gap;
		float rLonelands = rScarlet + scarletR + lonelandsR + gap;

		int cx = width / 2 + Math.round(panX);
		int cy = height / 2 + Math.round(panY);
		float t = (System.currentTimeMillis() - openMillis) / 1000.0F;

		BodyPos overworld = pos(cx, cy, rOver, t * OVERWORLD_SPEED + 2.4F);
		BodyPos outerworld = pos(cx, cy, rOuter, t * OUTERWORLD_SPEED + 3.6F);
		BodyPos wanderlands = pos(cx, cy, rLonely, t * WANDERLANDS_SPEED + 4.4F);
		BodyPos highworld = pos(cx, cy, rHigh, t * HIGHWORLD_SPEED + 5.1F);
		BodyPos deepworld = pos(cx, cy, rDeep, t * DEEPWORLD_SPEED + 6.3F);
		BodyPos beyondlands = pos(cx, cy, rBeyond, t * BEYONDLANDS_SPEED + 9.5F);
		BodyPos spinlands = pos(cx, cy, rSpin, t * SPINLANDS_SPEED + 10.6F);
		BodyPos scarletlands = pos(cx, cy, rScarlet, t * SCARLETLANDS_SPEED + 11.4F);
		BodyPos lonelands = pos(cx, cy, rLonelands, t * LONELANDS_SPEED + 12.2F);
		float emberAngle = t * EMBERWORLD_SPEED + 1.7F;
		float amberAngle = t * AMBERWORLD_SPEED + 2.4F;
		float potatoAngle = t * POTATOWORLDS_SPEED + 0.4F;
		float beyondIiAngle = t * BEYONDLANDS_II_SPEED + 1.1F;
		return new Snapshot(
			cx,
			cy,
			rMerc,
			rVenus,
			rOver,
			rOuter,
			rLonely,
			rHigh,
			rDeep,
			rFar,
			rEdge,
			rBeyond,
			moonOrbit,
			martianMoonOrbit,
			jovianMoonOrbit,
			saturnianMoonOrbit,
			plutoMoonOrbit,
			pos(cx, cy, rMerc, t * MERCURY_SPEED),
			pos(cx, cy, rVenus, t * VENUS_SPEED + 1.2F),
			overworld,
			outerworld,
			wanderlands,
			highworld,
			deepworld,
			pos(cx, cy, rFar, t * FARWORLD_SPEED + 7.5F),
			pos(cx, cy, rEdge, t * EDGEWORLD_SPEED + 8.7F),
			beyondlands,
			pos(overworld.x, overworld.y, moonOrbit, t * MOON_SPEED + 0.8F),
			pos(outerworld.x, outerworld.y, martianMoonOrbit, potatoAngle),
			pos(highworld.x, highworld.y, jovianMoonOrbit, emberAngle),
			pos(highworld.x, highworld.y, jovianMoonOrbit, emberAngle + (float) Math.PI),
			pos(deepworld.x, deepworld.y, saturnianMoonOrbit, amberAngle),
			pos(deepworld.x, deepworld.y, saturnianMoonOrbit, amberAngle + (float) Math.PI),
			pos(beyondlands.x, beyondlands.y, plutoMoonOrbit, beyondIiAngle),
			px(sunSize),
			px(mercSize),
			px(venusSize),
			px(overSize),
			px(outerSize),
			px(lonelySize),
			px(highSize),
			px(deepSize),
			px(farSize),
			px(edgeSize),
			px(beyondSize),
			px(moonSize),
			px(potatoSize),
			px(emberSize),
			px(frostSize),
			px(amberSize),
			px(spongeSize),
			px(beyondIiSize),
			rSpin,
			spinlands,
			px(spinSize),
			Math.max(4, Math.round(spinSize * 0.72F)),
			Math.max(4, Math.round(spinSize * 1.38F)),
			rScarlet,
			scarletlands,
			px(scarletSize),
			rLonelands,
			lonelands,
			px(lonelandsSize)
		);
	}

	private static BodyPos pos(int cx, int cy, float radius, float angle) {
		return new BodyPos(
			cx + Math.round(Mth.cos(angle) * radius),
			cy + Math.round(Mth.sin(angle) * radius)
		);
	}

	private static int px(float value) {
		return Math.max(4, Math.round(value));
	}

	public record BodyPos(int x, int y) {
	}

	public record Snapshot(
		int cx,
		int cy,
		float mercuryOrbit,
		float venusOrbit,
		float overworldOrbit,
		float outerworldOrbit,
		float wanderlandsOrbit,
		float highworldOrbit,
		float deepworldOrbit,
		float farworldOrbit,
		float edgeworldOrbit,
		float beyondlandsOrbit,
		float moonOrbit,
		float potatoworldsOrbit,
		float emberworldOrbit,
		float amberworldOrbit,
		float beyondlandsIiOrbit,
		BodyPos mercury,
		BodyPos venus,
		BodyPos overworld,
		BodyPos outerworld,
		BodyPos wanderlands,
		BodyPos highworld,
		BodyPos deepworld,
		BodyPos farworld,
		BodyPos edgeworld,
		BodyPos beyondlands,
		BodyPos moon,
		BodyPos potatoworlds,
		BodyPos emberworld,
		BodyPos frostworld,
		BodyPos amberworld,
		BodyPos spongeworld,
		BodyPos beyondlandsIi,
		int sunSize,
		int mercurySize,
		int venusSize,
		int overworldSize,
		int outerworldSize,
		int wanderlandsSize,
		int highworldSize,
		int deepworldSize,
		int farworldSize,
		int edgeworldSize,
		int beyondlandsSize,
		int moonSize,
		int potatoworldsSize,
		int emberworldSize,
		int frostworldSize,
		int amberworldSize,
		int spongeworldSize,
		int beyondlandsIiSize,
		float spinlandsOrbit,
		BodyPos spinlands,
		int spinlandsSize,
		int spinlandsWidth,
		int spinlandsHeight,
		float scarletlandsOrbit,
		BodyPos scarletlands,
		int scarletlandsSize,
		float lonelandsOrbit,
		BodyPos lonelands,
		int lonelandsSize
	) {
		/** Shared Highworld-centric orbit used by Emberworld and Frostworld. */
		public float frostworldOrbit() {
			return emberworldOrbit;
		}

		/** Shared Deepworld-centric orbit used by Amberworld and Spongeworld. */
		public float spongeworldOrbit() {
			return amberworldOrbit;
		}

		public int hitRadius(int size) {
			return Math.max(6, size / 2);
		}

		public int hitRadius(int width, int height) {
			return Math.max(6, Math.max(width, height) / 2);
		}
	}
}
