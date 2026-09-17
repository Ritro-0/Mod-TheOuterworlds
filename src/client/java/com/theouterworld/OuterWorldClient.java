package com.theouterworld;

import com.theouterworld.client.DustStormClientState;
import com.theouterworld.client.DustStormHandler;
import com.theouterworld.client.DustStormVisualEffects;
import com.theouterworld.client.InteriorShelterClient;
import com.theouterworld.client.SkyCelestialReloader;
import com.theouterworld.client.AstralTelescopeScreen;
import com.theouterworld.client.ForgePlateScreen;
import com.theouterworld.client.KnappingTableScreen;
import com.theouterworld.client.OxidizableIronGolemEntityRenderer;
import com.theouterworld.client.ProcessorScreen;
import com.theouterworld.client.RiftPadScreen;
import com.theouterworld.network.DustStormSyncPacket;
import com.theouterworld.network.InteriorShelterSyncPacket;
import com.theouterworld.registry.ModEntities;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ARGB;

public class OuterWorldClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		OuterWorldMod.LOGGER.info("Initializing The Outerworlds client");

		DustStormClientState.register();
		ClientPlayNetworking.registerGlobalReceiver(DustStormSyncPacket.ID, (payload, context) -> {
			DustStormClientState.accept(payload.active());
		});
		ClientPlayNetworking.registerGlobalReceiver(InteriorShelterSyncPacket.ID, (payload, context) -> {
			InteriorShelterClient.replaceAll(payload.cells());
		});

		DustStormHandler.register();
		DustStormVisualEffects.register();
		com.theouterworld.client.SolarHeatVisualEffects.register();
		com.theouterworld.client.SunDeathVisualEffects.register();
		com.theouterworld.client.GlassHelmetOverlay.register();
		com.theouterworld.client.HydrogenSubmersionEffects.register();
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return OuterWorldMod.id("sky_celestials");
			}

			@Override
			public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
				SkyCelestialReloader.markStale();
			}
		});
		EntityRendererRegistry.register(ModEntities.OXIDIZABLE_IRON_GOLEM, OxidizableIronGolemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.PRIMED_PERCHLORATE_CHARGE, net.minecraft.client.renderer.entity.TntRenderer::new);
		EntityRendererRegistry.register(ModEntities.OPALINE_NICKEL_FLAIL, com.theouterworld.client.OpalineNickelFlailRenderer::new);
		EntityRendererRegistry.register(ModEntities.KHARAX, com.theouterworld.client.KharaxRenderer::new);
		net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
			com.theouterworld.registry.ModBlockEntities.RIFT,
			com.theouterworld.client.RiftBlockEntityRenderer::new
		);
		net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
			com.theouterworld.registry.ModBlockEntities.BEACON_CONCENTRATOR,
			com.theouterworld.client.BeaconConcentratorRenderer::new
		);
		net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
			com.theouterworld.registry.ModBlockEntities.ASTRAL_TELESCOPE,
			com.theouterworld.client.AstralTelescopeRenderer::new
		);
		MenuScreens.register(ModScreenHandlers.PROCESSOR_SCREEN_HANDLER, ProcessorScreen::new);
		MenuScreens.register(ModScreenHandlers.KNAPPING_TABLE_SCREEN_HANDLER, KnappingTableScreen::new);
		MenuScreens.register(ModScreenHandlers.FORGE_PLATE_SCREEN_HANDLER, ForgePlateScreen::new);
		MenuScreens.register(ModScreenHandlers.ASTRAL_TELESCOPE_SCREEN_HANDLER, AstralTelescopeScreen::new);
		MenuScreens.register(ModScreenHandlers.RIFT_PAD_SCREEN_HANDLER, RiftPadScreen::new);
		MenuScreens.register(ModScreenHandlers.QUANTUM_POD_SCREEN_HANDLER, RiftPadScreen::new);

		FluidRenderingRegistry.register(
			ModFluids.MERCURY,
			ModFluids.FLOWING_MERCURY,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/mercury_still"), true),
				new Material(OuterWorldMod.id("block/mercury_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0xC5C8CE))
			)
		);
		FluidRenderingRegistry.register(
			ModFluids.LIQUID_HYDROGEN,
			ModFluids.FLOWING_LIQUID_HYDROGEN,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/hydrogen_still"), true),
				new Material(OuterWorldMod.id("block/hydrogen_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0x7EC8C0))
			)
		);
		FluidRenderingRegistry.register(
			ModFluids.LIQUID_HELIUM,
			ModFluids.FLOWING_LIQUID_HELIUM,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/helium_still"), true),
				new Material(OuterWorldMod.id("block/helium_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0xB8D4FF))
			)
		);
		FluidRenderingRegistry.register(
			ModFluids.METALLIC_HYDROGEN,
			ModFluids.FLOWING_METALLIC_HYDROGEN,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/metallic_hydrogen_still"), true),
				new Material(OuterWorldMod.id("block/metallic_hydrogen_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0xB8E8FF))
			)
		);
		FluidRenderingRegistry.register(
			ModFluids.LIQUID_AMMONIA,
			ModFluids.FLOWING_LIQUID_AMMONIA,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/ammonia_still"), true),
				new Material(OuterWorldMod.id("block/ammonia_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0x90D4A8))
			)
		);
		FluidRenderingRegistry.register(
			ModFluids.LIQUID_METHANE,
			ModFluids.FLOWING_LIQUID_METHANE,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/methane_still"), true),
				new Material(OuterWorldMod.id("block/methane_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0x6EC8E8))
			)
		);
		FluidRenderingRegistry.register(
			ModFluids.SOLAR_PLASMA,
			ModFluids.FLOWING_SOLAR_PLASMA,
			new FluidModel.Unbaked(
				new Material(OuterWorldMod.id("block/solar_plasma_still"), true),
				new Material(OuterWorldMod.id("block/solar_plasma_flow"), true),
				new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				BlockTintSources.constant(ARGB.opaque(0xFFAA22))
			)
		);
		com.theouterworld.client.render.MetallicHydrogenGlowRenderer.register();
		com.theouterworld.client.render.MetallicHeliumGlowRenderer.register();
		com.theouterworld.client.render.IonicAmmoniaGlowRenderer.register();
		com.theouterworld.client.render.IonicMethaneGlowRenderer.register();
	}
}
