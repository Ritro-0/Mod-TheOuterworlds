package com.theouterworld;

import com.theouterworld.client.DustStormHandler;
import com.theouterworld.client.DustStormVisualEffects;
import com.theouterworld.client.InteriorShelterClient;
import com.theouterworld.client.KnappingTableScreen;
import com.theouterworld.client.OxidizableIronGolemEntityRenderer;
import com.theouterworld.client.ProcessorScreen;
import com.theouterworld.network.DustStormSyncPacket;
import com.theouterworld.network.InteriorShelterSyncPacket;
import com.theouterworld.registry.ModEntities;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class OuterWorldClient implements ClientModInitializer {
	public static boolean isDustStormActive = false;

	@Override
	public void onInitializeClient() {
		OuterWorldMod.LOGGER.info("Initializing The Outerworlds client");

		ClientPlayNetworking.registerGlobalReceiver(DustStormSyncPacket.ID, (payload, context) -> {
			isDustStormActive = payload.active();
		});
		ClientPlayNetworking.registerGlobalReceiver(InteriorShelterSyncPacket.ID, (payload, context) -> {
			InteriorShelterClient.replaceAll(payload.cells());
		});

		DustStormHandler.register();
		DustStormVisualEffects.register();
		com.theouterworld.client.GlassHelmetOverlay.register();
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
		MenuScreens.register(ModScreenHandlers.PROCESSOR_SCREEN_HANDLER, ProcessorScreen::new);
		MenuScreens.register(ModScreenHandlers.KNAPPING_TABLE_SCREEN_HANDLER, KnappingTableScreen::new);

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
	}
}
