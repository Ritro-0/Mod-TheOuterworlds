package com.theouterworld.mixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
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
	private static final int CELESTIALS_INNERWORLD = 2;

	/** Celestial angle 0 is noon / zenith. */
	private static final float INNERWORLD_MOON_ANGLE = 0.0F;
	private static final int INNERWORLD_NIGHT_SKY_COLOR = 0x00000A;
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
	private GpuBuffer theouterworlds$moonBuffer;

	@Unique
	private GpuBuffer theouterworlds$innerworldMoonBuffer;

	@Unique
	private int theouterworlds$celestialMode;

	@Unique
	private float theouterworlds$sunAngle;

	@Unique
	private float theouterworlds$moonAngle;

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
		this.theouterworlds$sunBuffer = theouterworlds$buildCelestialQuad(
			"Outerworlds sun",
			this.celestialsAtlas.getSprite(OuterWorldMod.id("sun"))
		);
		this.theouterworlds$moonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, null);
		this.theouterworlds$innerworldMoonBuffer = theouterworlds$buildMoonBuffer(OuterWorldMod.MOD_ID, "innerworld");
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
		if (world.dimension().equals(ModDimensions.INNERWORLD_WORLD_KEY)) {
			this.theouterworlds$celestialMode = CELESTIALS_INNERWORLD;
			state.moonAngle = INNERWORLD_MOON_ANGLE;
			state.starBrightness = Math.max(state.starBrightness, INNERWORLD_STAR_BRIGHTNESS);
			state.skyColor = INNERWORLD_NIGHT_SKY_COLOR;
			state.sunriseAndSunsetColor = 0;
			this.theouterworlds$sunAngle = state.sunAngle;
			this.theouterworlds$moonAngle = state.moonAngle;
		} else if (world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			this.theouterworlds$celestialMode = CELESTIALS_OUTERWORLD;
		} else {
			this.theouterworlds$celestialMode = CELESTIALS_VANILLA;
		}
	}

	@Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$hideSunBehindMoon(float alpha, PoseStack poseStack, CallbackInfo ci) {
		if (this.theouterworlds$celestialMode != CELESTIALS_INNERWORLD) {
			return;
		}
		float delta = Math.abs(this.theouterworlds$sunAngle - this.theouterworlds$moonAngle);
		delta = Math.min(delta, 1.0F - delta);
		// Hide the sun while it is behind the noon moon so additive blending cannot shine through.
		if (delta < 0.05F) {
			ci.cancel();
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
		return this.theouterworlds$celestialMode == CELESTIALS_OUTERWORLD ? this.theouterworlds$sunBuffer : this.sunBuffer;
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
		return switch (this.theouterworlds$celestialMode) {
			case CELESTIALS_INNERWORLD -> this.theouterworlds$innerworldMoonBuffer;
			case CELESTIALS_OUTERWORLD -> this.theouterworlds$moonBuffer;
			default -> this.moonBuffer;
		};
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void theouterworlds$closeCustomCelestials(CallbackInfo ci) {
		if (this.theouterworlds$sunBuffer != null) {
			this.theouterworlds$sunBuffer.close();
		}
		if (this.theouterworlds$moonBuffer != null) {
			this.theouterworlds$moonBuffer.close();
		}
		if (this.theouterworlds$innerworldMoonBuffer != null) {
			this.theouterworlds$innerworldMoonBuffer.close();
		}
	}
}
