package com.theouterworld.mixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.client.AmberworldAtmosphere;
import com.theouterworld.client.DeepworldAtmosphere;
import com.theouterworld.client.EdgeworldAtmosphere;
import com.theouterworld.client.EmberworldAtmosphere;
import com.theouterworld.client.FarworldAtmosphere;
import com.theouterworld.client.FrostworldAtmosphere;
import com.theouterworld.client.HighworldAtmosphere;
import com.theouterworld.client.NearworldAtmosphere;
import com.theouterworld.client.SkyCelestialReloader;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.EmberworldDayCycle;
import com.theouterworld.world.InnerworldDayCycle;
import com.theouterworld.world.SpinlandsDayCycle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRenderingMixin {
	private static final int CELESTIALS_VANILLA = 0;
	private static final int CELESTIALS_OUTERWORLD = 1;
	private static final int CELESTIALS_MOON = 2;
	private static final int CELESTIALS_INNERWORLD = 3;
	private static final int CELESTIALS_HIGHWORLD = 4;
	private static final int CELESTIALS_DEEPWORLD = 5;
	private static final int CELESTIALS_FARWORLD = 6;
	private static final int CELESTIALS_EDGEWORLD = 7;
	private static final int CELESTIALS_EMBERWORLD = 8;
	private static final int CELESTIALS_FROSTWORLD = 9;
	private static final int CELESTIALS_AMBERWORLD = 10;
	private static final int CELESTIALS_SPONGEWORLD = 11;
	private static final int CELESTIALS_POTATOWORLDS = 12;
	private static final int CELESTIALS_WANDERLANDS = 13;
	private static final int CELESTIALS_BEYONDLANDS = 14;
	private static final int CELESTIALS_BEYONDLANDS_II = 15;
	private static final int CELESTIALS_SPINLANDS = 16;
	private static final int CELESTIALS_SCARLETLANDS = 17;
	private static final int CELESTIALS_LONELANDS = 18;

	/** Celestial angle 0 is noon / zenith (radians). */
	private static final float MOON_SURFACE_MOON_ANGLE = 0.0F;
	private static final float CELESTIAL_CIRCLE = (float) (Math.PI * 2.0);
	private static final float MOON_SUN_ECLIPSE_THRESHOLD = 0.05F;
	/** Earth's moon as seen from Outerworld — direct moon/ textures at 1/8 size. */
	private static final float OUTERWORLD_MOON_SCALE = 0.125F;
	private static final int MOON_NIGHT_SKY_COLOR = 0x00000A;
	private static final float MOON_STAR_BRIGHTNESS = 0.55F;
	/** Same near-black as the Moon — no daytime orange wash. */
	private static final int INNERWORLD_SKY_COLOR = 0x00000A;
	private static final float INNERWORLD_STAR_BRIGHTNESS = 0.55F;

	@Shadow
	@Final
	private TextureAtlas celestialsAtlas;

	@Shadow
	@Final
	private GpuBuffer sunBuffer;

	@Shadow
	@Final
	private GpuBuffer moonBuffer;

	@Unique
	private GpuBuffer theouterworlds$sunBuffer;

	@Unique
	private GpuBuffer theouterworlds$innerworldSunBuffer;

	@Unique
	private GpuBuffer theouterworlds$highworldBeyondSunBuffer;

	@Unique
	private GpuBuffer theouterworlds$dwarfPlanetSunBuffer;

	@Unique
	private GpuBuffer theouterworlds$moonBuffer;

	@Unique
	private GpuBuffer theouterworlds$lunarMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$ioMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$titanMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$phobosDeimosMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$plutoMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$charonMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$haumeaMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$makemakeMoonBuffer;

	@Unique
	private GpuBuffer theouterworlds$erisMoonBuffer;

	@Unique
	private float theouterworlds$sunAngle;

	@Unique
	private boolean theouterworlds$scaledCustomMoon;

	@Unique
	private static String theouterworlds$moonSpriteNamespace;

	@Unique
	private static String theouterworlds$moonSpritePathPrefix;

	@Invoker("buildCelestialQuad")
	private static GpuBuffer theouterworlds$buildCelestialQuad(String name, TextureAtlasSprite sprite) {
		throw new AssertionError();
	}

	@Invoker("buildMoonPhases")
	private static GpuBuffer theouterworlds$buildMoonPhases(TextureAtlas atlas) {
		throw new AssertionError();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void theouterworlds$buildCustomCelestials(
		TextureManager textureManager,
		AtlasManager atlasManager,
		RenderTarget renderTarget,
		CallbackInfo ci
	) {
		theouterworlds$rebuildCustomCelestials();
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void theouterworlds$ensureCustomCelestials(
		ClientLevel world,
		float tickDelta,
		Camera camera,
		SkyRenderState state,
		CallbackInfo ci
	) {
		if (SkyCelestialReloader.consumeStale()) {
			theouterworlds$closeCustomBuffers();
		}
		theouterworlds$rebuildCustomCelestials();
	}

	@Unique
	private void theouterworlds$rebuildCustomCelestials() {
		if (this.theouterworlds$sunBuffer != null
			&& this.theouterworlds$innerworldSunBuffer != null
			&& this.theouterworlds$highworldBeyondSunBuffer != null
			&& this.theouterworlds$dwarfPlanetSunBuffer != null
			&& this.theouterworlds$moonBuffer != null
			&& this.theouterworlds$lunarMoonBuffer != null
			&& this.theouterworlds$ioMoonBuffer != null
			&& this.theouterworlds$titanMoonBuffer != null
			&& this.theouterworlds$phobosDeimosMoonBuffer != null
			&& this.theouterworlds$plutoMoonBuffer != null
			&& this.theouterworlds$charonMoonBuffer != null
			&& this.theouterworlds$haumeaMoonBuffer != null
			&& this.theouterworlds$makemakeMoonBuffer != null
			&& this.theouterworlds$erisMoonBuffer != null) {
			return;
		}
		theouterworlds$closeCustomBuffers();
		this.theouterworlds$sunBuffer = theouterworlds$buildCelestialQuad(
			"Outerworlds sun",
			this.celestialsAtlas.getSprite(OuterWorldMod.id("sun"))
		);
		this.theouterworlds$innerworldSunBuffer = theouterworlds$buildCelestialQuad(
			"Innerworld sun",
			this.celestialsAtlas.getSprite(OuterWorldMod.id("sun_innerworld"))
		);
		this.theouterworlds$highworldBeyondSunBuffer = theouterworlds$buildCelestialQuad(
			"Highworld-beyond sun",
			this.celestialsAtlas.getSprite(OuterWorldMod.id("sun_highworld_beyond"))
		);
		this.theouterworlds$dwarfPlanetSunBuffer = theouterworlds$buildCelestialQuad(
			"Dwarf-planet sun",
			this.celestialsAtlas.getSprite(OuterWorldMod.id("sun_dwarf_planets"))
		);
		this.theouterworlds$moonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, null);
		this.theouterworlds$lunarMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "lunar");
		this.theouterworlds$ioMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "io");
		this.theouterworlds$titanMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "titan");
		this.theouterworlds$phobosDeimosMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "phobos_deimos");
		this.theouterworlds$plutoMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "beyondlands_pluto");
		this.theouterworlds$charonMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "beyondlands_charon");
		this.theouterworlds$haumeaMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "spinlands_haumea");
		this.theouterworlds$makemakeMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "scarletlands_makemake");
		this.theouterworlds$erisMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "lonelands_eris");
	}

	@Unique
	private GpuBuffer theouterworlds$buildMoonBuffer(String namespace, String pathPrefix) {
		theouterworlds$moonSpriteNamespace = namespace;
		theouterworlds$moonSpritePathPrefix = pathPrefix;
		try {
			return theouterworlds$buildMoonPhases(this.celestialsAtlas);
		} finally {
			theouterworlds$moonSpriteNamespace = null;
			theouterworlds$moonSpritePathPrefix = null;
		}
	}

	@Redirect(
		method = "buildMoonPhases",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/resources/Identifier;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
		)
	)
	private static Identifier theouterworlds$moonSpriteId(String path) {
		if (theouterworlds$moonSpriteNamespace != null) {
			return Identifier.fromNamespaceAndPath(theouterworlds$moonSpriteNamespace, theouterworlds$rewriteMoonPath(path));
		}
		return Identifier.withDefaultNamespace(path);
	}

	@Unique
	private static String theouterworlds$rewriteMoonPath(String path) {
		if (theouterworlds$moonSpritePathPrefix == null || theouterworlds$moonSpritePathPrefix.isEmpty()) {
			return path;
		}
		int slash = path.lastIndexOf('/');
		if (slash >= 0) {
			return path.substring(0, slash + 1) + theouterworlds$moonSpritePathPrefix + "/" + path.substring(slash + 1);
		}
		return theouterworlds$moonSpritePathPrefix + "/" + path;
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void theouterworlds$updateSky(
		ClientLevel world,
		float tickDelta,
		Camera camera,
		SkyRenderState state,
		CallbackInfo ci
	) {
		if (world == null) {
			return;
		}
		if (world.dimension().equals(ModDimensions.MOON_WORLD_KEY)) {
			state.moonAngle = MOON_SURFACE_MOON_ANGLE;
			state.starBrightness = Math.max(state.starBrightness, MOON_STAR_BRIGHTNESS);
			state.skyColor = MOON_NIGHT_SKY_COLOR;
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = state.sunAngle;
		} else if (world.dimension().equals(ModDimensions.INNERWORLD_WORLD_KEY)) {
			float sunAngle = InnerworldDayCycle.sunAngleRadians(world, tickDelta);
			state.sunAngle = sunAngle;
			// Permanent night sky: keep stars up even while the sun disc is visible.
			state.starBrightness = Math.max(state.starBrightness, INNERWORLD_STAR_BRIGHTNESS);
			state.skyColor = INNERWORLD_SKY_COLOR;
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = sunAngle;
		} else if (world.dimension().equals(ModDimensions.NEARWORLD_WORLD_KEY)) {
			state.skyColor = NearworldAtmosphere.HAZE_COLOR;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
		} else if (ModDimensions.isAmberworld(world.dimension())) {
			// Nearworld-style haze, but muted; sun stays on the day arc, Saturn offset 90°.
			state.skyColor = AmberworldAtmosphere.HAZE_COLOR;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
			float sun = theouterworlds$forceAboveHorizon(state.sunAngle);
			state.sunAngle = sun;
			state.moonAngle = theouterworlds$amberworldSaturnAngle(sun);
			this.theouterworlds$sunAngle = sun;
		} else if (ModDimensions.isHighworld(world.dimension())) {
			state.skyColor = HighworldAtmosphere.HAZE_COLOR;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
		} else if (ModDimensions.isDeepworld(world.dimension())) {
			state.skyColor = DeepworldAtmosphere.HAZE_COLOR;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
		} else if (ModDimensions.isFarworld(world.dimension())) {
			state.skyColor = FarworldAtmosphere.HAZE_COLOR;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
		} else if (ModDimensions.isEdgeworld(world.dimension())) {
			state.skyColor = EdgeworldAtmosphere.HAZE_COLOR;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
		} else if (ModDimensions.isEmberworld(world.dimension())) {
			state.moonAngle = EmberworldDayCycle.moonAngleRadians(world, tickDelta);
			state.starBrightness = Math.max(state.starBrightness, EmberworldAtmosphere.STAR_BRIGHTNESS);
			state.skyColor = EmberworldAtmosphere.skyColor(EmberworldDayCycle.isSunUp(world));
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = state.sunAngle;
		} else if (ModDimensions.isFrostworld(world.dimension())) {
			state.moonAngle = EmberworldDayCycle.moonAngleRadians(world, tickDelta);
			state.starBrightness = Math.max(state.starBrightness, FrostworldAtmosphere.STAR_BRIGHTNESS);
			state.skyColor = FrostworldAtmosphere.skyColor(EmberworldDayCycle.isSunUp(world));
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = state.sunAngle;
		} else if (ModDimensions.isSpongeworld(world.dimension())) {
			state.moonAngle = theouterworlds$amberworldSaturnAngle(
				theouterworlds$forceAboveHorizon(state.sunAngle)
			);
			state.starBrightness = Math.max(state.starBrightness, FrostworldAtmosphere.STAR_BRIGHTNESS);
			state.skyColor = FrostworldAtmosphere.skyColor(EmberworldDayCycle.isSunUp(world));
			state.sunriseAndSunsetColor = 0;
			state.sunAngle = theouterworlds$forceAboveHorizon(state.sunAngle);
			this.theouterworlds$sunAngle = state.sunAngle;
		} else if (ModDimensions.isPotatoworlds(world.dimension())) {
			state.starBrightness = Math.max(state.starBrightness, FrostworldAtmosphere.STAR_BRIGHTNESS);
			state.skyColor = FrostworldAtmosphere.skyColor(EmberworldDayCycle.isSunUp(world));
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = state.sunAngle;
		} else if (ModDimensions.isWanderlands(world.dimension())
			|| ModDimensions.isBeyondlands(world.dimension())
			|| ModDimensions.isBeyondlandsIi(world.dimension())
			|| ModDimensions.isScarletlands(world.dimension())
			|| ModDimensions.isLonelands(world.dimension())) {
			state.starBrightness = Math.max(state.starBrightness, MOON_STAR_BRIGHTNESS);
			state.skyColor = MOON_NIGHT_SKY_COLOR;
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = state.sunAngle;
		} else if (ModDimensions.isSpinlands(world.dimension())) {
			float sunAngle = SpinlandsDayCycle.sunAngleRadians(world, tickDelta);
			state.sunAngle = sunAngle;
			state.moonAngle = SpinlandsDayCycle.moonAngleRadians(world, tickDelta);
			state.starBrightness = Math.max(state.starBrightness, MOON_STAR_BRIGHTNESS);
			state.skyColor = MOON_NIGHT_SKY_COLOR;
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = sunAngle;
		}
	}

	@Unique
	private static float theouterworlds$forceAboveHorizon(float angle) {
		return (float) Math.cos(angle) > 0.05F ? angle : angle + (float) Math.PI;
	}

	/**
	 * Place Saturn a quarter-turn from the sun. Vanilla moon is opposite the sun, so
	 * forcing both above the horizon stacked them on the same sky slot.
	 */
	@Unique
	private static float theouterworlds$amberworldSaturnAngle(float sunAngle) {
		float offset = AmberworldAtmosphere.SATURN_SUN_OFFSET;
		float saturn = sunAngle - offset;
		if ((float) Math.cos(saturn) > 0.05F) {
			return saturn;
		}
		return sunAngle + offset;
	}

	@Unique
	private static int theouterworlds$celestialMode() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return CELESTIALS_VANILLA;
		}
		ResourceKey<Level> dimension = level.dimension();
		if (ModDimensions.MOON_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_MOON;
		}
		if (ModDimensions.INNERWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_INNERWORLD;
		}
		if (ModDimensions.OUTERWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_OUTERWORLD;
		}
		if (ModDimensions.HIGHWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_HIGHWORLD;
		}
		if (ModDimensions.DEEPWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_DEEPWORLD;
		}
		if (ModDimensions.FARWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_FARWORLD;
		}
		if (ModDimensions.EDGEWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_EDGEWORLD;
		}
		if (ModDimensions.EMBERWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_EMBERWORLD;
		}
		if (ModDimensions.FROSTWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_FROSTWORLD;
		}
		if (ModDimensions.AMBERWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_AMBERWORLD;
		}
		if (ModDimensions.SPONGEWORLD_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_SPONGEWORLD;
		}
		if (ModDimensions.POTATOWORLDS_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_POTATOWORLDS;
		}
		if (ModDimensions.WANDERLANDS_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_WANDERLANDS;
		}
		if (ModDimensions.BEYONDLANDS_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_BEYONDLANDS;
		}
		if (ModDimensions.BEYONDLANDS_II_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_BEYONDLANDS_II;
		}
		if (ModDimensions.SPINLANDS_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_SPINLANDS;
		}
		if (ModDimensions.SCARLETLANDS_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_SCARLETLANDS;
		}
		if (ModDimensions.LONELANDS_WORLD_KEY.equals(dimension)) {
			return CELESTIALS_LONELANDS;
		}
		return CELESTIALS_VANILLA;
	}

	@Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$hideSunBehindMoon(float alpha, PoseStack poseStack, CallbackInfo ci) {
		int mode = theouterworlds$celestialMode();
		if (mode == CELESTIALS_HIGHWORLD || mode == CELESTIALS_DEEPWORLD || mode == CELESTIALS_FARWORLD || mode == CELESTIALS_EDGEWORLD) {
			ci.cancel();
			return;
		}
		if (mode != CELESTIALS_MOON) {
			return;
		}
		float delta = Math.abs(this.theouterworlds$sunAngle - MOON_SURFACE_MOON_ANGLE);
		delta = Math.min(delta, CELESTIAL_CIRCLE - delta);
		// Hide the sun while it is behind the noon moon so additive blending cannot shine through.
		if (delta < MOON_SUN_ECLIPSE_THRESHOLD) {
			ci.cancel();
		}
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$hideOrScaleMoon(MoonPhase moonPhase, float alpha, PoseStack poseStack, CallbackInfo ci) {
		this.theouterworlds$scaledCustomMoon = false;
		int mode = theouterworlds$celestialMode();
		if (mode == CELESTIALS_INNERWORLD || mode == CELESTIALS_HIGHWORLD || mode == CELESTIALS_DEEPWORLD || mode == CELESTIALS_FARWORLD || mode == CELESTIALS_EDGEWORLD || mode == CELESTIALS_WANDERLANDS) {
			ci.cancel();
			return;
		}
		if (mode == CELESTIALS_BEYONDLANDS || mode == CELESTIALS_BEYONDLANDS_II) {
			float scale = mode == CELESTIALS_BEYONDLANDS ? 0.55F : 0.85F;
			poseStack.pushPose();
			poseStack.scale(scale, scale, scale);
			this.theouterworlds$scaledCustomMoon = true;
			return;
		}
		if (mode == CELESTIALS_EMBERWORLD || mode == CELESTIALS_FROSTWORLD) {
			poseStack.pushPose();
			poseStack.scale(2.0F, 2.0F, 2.0F);
			this.theouterworlds$scaledCustomMoon = true;
		} else if (mode == CELESTIALS_AMBERWORLD || mode == CELESTIALS_SPONGEWORLD) {
			float scale = AmberworldAtmosphere.SATURN_MOON_SCALE;
			poseStack.pushPose();
			poseStack.scale(scale, scale, scale);
			this.theouterworlds$scaledCustomMoon = true;
		} else if (mode == CELESTIALS_OUTERWORLD) {
			poseStack.pushPose();
			poseStack.scale(OUTERWORLD_MOON_SCALE, OUTERWORLD_MOON_SCALE, OUTERWORLD_MOON_SCALE);
			this.theouterworlds$scaledCustomMoon = true;
		}
	}

	@Inject(method = "renderMoon", at = @At("RETURN"))
	private void theouterworlds$unscaleEmberMoon(MoonPhase moonPhase, float alpha, PoseStack poseStack, CallbackInfo ci) {
		if (this.theouterworlds$scaledCustomMoon) {
			poseStack.popPose();
			this.theouterworlds$scaledCustomMoon = false;
		}
	}

	@Redirect(
		method = "renderSun",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/renderer/SkyRenderer;sunBuffer:Lcom/mojang/blaze3d/buffers/GpuBuffer;"
		)
	)
	private GpuBuffer theouterworlds$selectSunBuffer(SkyRenderer instance) {
		return switch (theouterworlds$celestialMode()) {
			case CELESTIALS_INNERWORLD -> this.theouterworlds$innerworldSunBuffer != null
				? this.theouterworlds$innerworldSunBuffer
				: this.sunBuffer;
			// Distant sun for Highworld-orbit moons (Emberworld / Frostworld) and any later Highworld+ skies that show the sun.
			case CELESTIALS_EMBERWORLD, CELESTIALS_FROSTWORLD, CELESTIALS_SPONGEWORLD, CELESTIALS_POTATOWORLDS -> this.theouterworlds$highworldBeyondSunBuffer != null
				? this.theouterworlds$highworldBeyondSunBuffer
				: this.sunBuffer;
			// Outerworld / Wanderlands keep the Outerworld custom sun; dwarf planets share sun_dwarf_planets.
			case CELESTIALS_OUTERWORLD, CELESTIALS_WANDERLANDS -> this.theouterworlds$sunBuffer != null
				? this.theouterworlds$sunBuffer
				: this.sunBuffer;
			case CELESTIALS_BEYONDLANDS, CELESTIALS_BEYONDLANDS_II, CELESTIALS_SPINLANDS, CELESTIALS_SCARLETLANDS, CELESTIALS_LONELANDS -> this.theouterworlds$dwarfPlanetSunBuffer != null
				? this.theouterworlds$dwarfPlanetSunBuffer
				: this.sunBuffer;
			default -> this.sunBuffer;
		};
	}

	@Redirect(
		method = "renderMoon",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/renderer/SkyRenderer;moonBuffer:Lcom/mojang/blaze3d/buffers/GpuBuffer;"
		)
	)
	private GpuBuffer theouterworlds$selectMoonBuffer(SkyRenderer instance) {
		return switch (theouterworlds$celestialMode()) {
			case CELESTIALS_MOON -> this.theouterworlds$lunarMoonBuffer != null
				? this.theouterworlds$lunarMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_EMBERWORLD, CELESTIALS_FROSTWORLD -> this.theouterworlds$ioMoonBuffer != null
				? this.theouterworlds$ioMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_AMBERWORLD, CELESTIALS_SPONGEWORLD -> this.theouterworlds$titanMoonBuffer != null
				? this.theouterworlds$titanMoonBuffer
				: this.moonBuffer;
			// Direct celestial/moon/* textures (not phobos_deimos / lunar / io / titan subfolders).
			case CELESTIALS_OUTERWORLD -> this.theouterworlds$moonBuffer != null
				? this.theouterworlds$moonBuffer
				: this.moonBuffer;
			case CELESTIALS_POTATOWORLDS -> this.theouterworlds$phobosDeimosMoonBuffer != null
				? this.theouterworlds$phobosDeimosMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_BEYONDLANDS -> this.theouterworlds$plutoMoonBuffer != null
				? this.theouterworlds$plutoMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_BEYONDLANDS_II -> this.theouterworlds$charonMoonBuffer != null
				? this.theouterworlds$charonMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_SPINLANDS -> this.theouterworlds$haumeaMoonBuffer != null
				? this.theouterworlds$haumeaMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_SCARLETLANDS -> this.theouterworlds$makemakeMoonBuffer != null
				? this.theouterworlds$makemakeMoonBuffer
				: this.moonBuffer;
			case CELESTIALS_LONELANDS -> this.theouterworlds$erisMoonBuffer != null
				? this.theouterworlds$erisMoonBuffer
				: this.moonBuffer;
			default -> this.moonBuffer;
		};
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void theouterworlds$closeCustomCelestials(CallbackInfo ci) {
		theouterworlds$closeCustomBuffers();
	}

	@Unique
	private void theouterworlds$closeCustomBuffers() {
		if (this.theouterworlds$sunBuffer != null) {
			this.theouterworlds$sunBuffer.close();
			this.theouterworlds$sunBuffer = null;
		}
		if (this.theouterworlds$innerworldSunBuffer != null) {
			this.theouterworlds$innerworldSunBuffer.close();
			this.theouterworlds$innerworldSunBuffer = null;
		}
		if (this.theouterworlds$highworldBeyondSunBuffer != null) {
			this.theouterworlds$highworldBeyondSunBuffer.close();
			this.theouterworlds$highworldBeyondSunBuffer = null;
		}
		if (this.theouterworlds$dwarfPlanetSunBuffer != null) {
			this.theouterworlds$dwarfPlanetSunBuffer.close();
			this.theouterworlds$dwarfPlanetSunBuffer = null;
		}
		if (this.theouterworlds$moonBuffer != null) {
			this.theouterworlds$moonBuffer.close();
			this.theouterworlds$moonBuffer = null;
		}
		if (this.theouterworlds$lunarMoonBuffer != null) {
			this.theouterworlds$lunarMoonBuffer.close();
			this.theouterworlds$lunarMoonBuffer = null;
		}
		if (this.theouterworlds$ioMoonBuffer != null) {
			this.theouterworlds$ioMoonBuffer.close();
			this.theouterworlds$ioMoonBuffer = null;
		}
		if (this.theouterworlds$titanMoonBuffer != null) {
			this.theouterworlds$titanMoonBuffer.close();
			this.theouterworlds$titanMoonBuffer = null;
		}
		if (this.theouterworlds$phobosDeimosMoonBuffer != null) {
			this.theouterworlds$phobosDeimosMoonBuffer.close();
			this.theouterworlds$phobosDeimosMoonBuffer = null;
		}
		if (this.theouterworlds$plutoMoonBuffer != null) {
			this.theouterworlds$plutoMoonBuffer.close();
			this.theouterworlds$plutoMoonBuffer = null;
		}
		if (this.theouterworlds$charonMoonBuffer != null) {
			this.theouterworlds$charonMoonBuffer.close();
			this.theouterworlds$charonMoonBuffer = null;
		}
		if (this.theouterworlds$haumeaMoonBuffer != null) {
			this.theouterworlds$haumeaMoonBuffer.close();
			this.theouterworlds$haumeaMoonBuffer = null;
		}
		if (this.theouterworlds$makemakeMoonBuffer != null) {
			this.theouterworlds$makemakeMoonBuffer.close();
			this.theouterworlds$makemakeMoonBuffer = null;
		}
		if (this.theouterworlds$erisMoonBuffer != null) {
			this.theouterworlds$erisMoonBuffer.close();
			this.theouterworlds$erisMoonBuffer = null;
		}
	}
}
