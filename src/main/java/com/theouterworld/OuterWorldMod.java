package com.theouterworld;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.CorrodedBlocks;
import com.theouterworld.block.ProcessorBlockEntity;
import com.theouterworld.command.DustStormCommand;
import com.theouterworld.config.OuterworldConfig;
import com.theouterworld.event.VanillaIronReplacementListener;
import com.theouterworld.item.ModItemGroups;
import com.theouterworld.item.ModItems;
import com.theouterworld.network.DustStormSyncPacket;
import com.theouterworld.network.ProcessorModeTogglePacket;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModDecoratedPotPatterns;
import com.theouterworld.registry.ModFeatures;
import com.theouterworld.registry.ModScreenHandlers;
import com.theouterworld.util.GlassHelmetUtil;
import com.theouterworld.weather.DustStormEffects;
import com.theouterworld.weather.DustStormTicker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OuterWorldMod implements ModInitializer {
	public static final String MOD_ID = "theouterworlds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Outerworlds");
		OuterworldConfig.register();
		LOGGER.info("Outerworld gravity={}, Innerworld gravity={}",
			OuterworldConfig.GRAVITY_MULTIPLIER,
			OuterworldConfig.INNERWORLD_GRAVITY_MULTIPLIER);
		com.theouterworld.registry.ModFluids.registerModFluids();
		ModBlocks.registerModBlocks();
		CorrodedBlocks.register();
		ModItems.registerModItems();
		ModDecoratedPotPatterns.register();
		ModItemGroups.registerItemGroups();
		ModFeatures.registerModFeatures();
		ModDimensions.registerModDimensions();
		com.theouterworld.registry.ModBlockEntities.registerModBlockEntities();
		ModScreenHandlers.registerModScreenHandlers();
		com.theouterworld.registry.ModSounds.registerModSounds();
		com.theouterworld.registry.ModEntities.registerModEntities();
		VanillaIronReplacementListener.register();
		GlassHelmetUtil.register();
		com.theouterworld.world.BeaconConcentratorPortals.register();

		PayloadTypeRegistry.clientboundPlay().register(DustStormSyncPacket.ID, DustStormSyncPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(
			com.theouterworld.network.InteriorShelterSyncPacket.ID,
			com.theouterworld.network.InteriorShelterSyncPacket.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(ProcessorModeTogglePacket.ID, ProcessorModeTogglePacket.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ProcessorModeTogglePacket.ID, (packet, context) -> {
			context.server().execute(() -> {
				var player = context.player();
				var world = (ServerLevel) player.level();
				var pos = packet.pos();
				if (world.getBlockEntity(pos) instanceof ProcessorBlockEntity processor
					&& player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64) {
					processor.toggleMode();
				}
			});
		});

		DustStormTicker.register();
		com.theouterworld.weather.InteriorShelterTracker.register();
		DustStormEffects.register();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> {
				ServerLevel world = (ServerLevel) handler.player.level();
				if (world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
					var manager = DustStormTicker.getManager(world);
					if (manager != null) {
						ServerPlayNetworking.send(handler.player, new DustStormSyncPacket(manager.isStormActive()));
					}
					com.theouterworld.weather.InteriorShelterTracker.syncToPlayer(handler.player);
				}
			});
		});

		CommandRegistrationCallback.EVENT.register(DustStormCommand::register);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
