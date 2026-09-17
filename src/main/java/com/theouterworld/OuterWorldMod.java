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
import com.theouterworld.network.RiftPadVisitPacket;
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
		com.theouterworld.registry.ModDensityFunctions.register();
		LOGGER.info("Outerworld gravity={}, Moon gravity={}, Innerworld gravity={}, Nearworld gravity={}, Highworld gravity={}",
			OuterworldConfig.GRAVITY_MULTIPLIER,
			OuterworldConfig.MOON_GRAVITY_MULTIPLIER,
			OuterworldConfig.INNERWORLD_GRAVITY_MULTIPLIER,
			OuterworldConfig.NEARWORLD_GRAVITY_MULTIPLIER,
			OuterworldConfig.HIGHWORLD_GRAVITY_MULTIPLIER);
		com.theouterworld.registry.ModFluids.registerModFluids();
		ModBlocks.registerModBlocks();
		CorrodedBlocks.register();
		ModItems.registerModItems();
		com.theouterworld.item.EmergencyReturnPodRitual.register();
		ModDecoratedPotPatterns.register();
		ModItemGroups.registerItemGroups();
		ModFeatures.registerModFeatures();
		ModDimensions.registerModDimensions();
		com.theouterworld.registry.ModBlockEntities.registerModBlockEntities();
		ModScreenHandlers.registerModScreenHandlers();
		com.theouterworld.registry.ModSounds.registerModSounds();
		com.theouterworld.registry.ModEntities.registerModEntities();
		VanillaIronReplacementListener.register();
		com.theouterworld.world.PlantFreeze.register();
		com.theouterworld.world.ColdDimensionLights.register();
		GlassHelmetUtil.register();
		com.theouterworld.world.OuterworldWorldType.register();
		com.theouterworld.world.BeaconConcentratorPortals.register();

		PayloadTypeRegistry.clientboundPlay().register(DustStormSyncPacket.ID, DustStormSyncPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(
			com.theouterworld.network.InteriorShelterSyncPacket.ID,
			com.theouterworld.network.InteriorShelterSyncPacket.CODEC
		);
		PayloadTypeRegistry.clientboundPlay().register(
			com.theouterworld.network.SunDeathSyncPacket.ID,
			com.theouterworld.network.SunDeathSyncPacket.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(ProcessorModeTogglePacket.ID, ProcessorModeTogglePacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RiftPadVisitPacket.ID, RiftPadVisitPacket.CODEC);

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

		ServerPlayNetworking.registerGlobalReceiver(RiftPadVisitPacket.ID, (packet, context) -> {
			context.server().execute(() -> {
				var player = context.player();
				if (!(player.containerMenu instanceof com.theouterworld.screen.RiftPadMenu menu)) {
					return;
				}
				var destId = net.minecraft.resources.Identifier.tryParse(packet.destination());
				if (destId == null) {
					return;
				}
				var dest = net.minecraft.resources.ResourceKey.create(
					net.minecraft.core.registries.Registries.DIMENSION,
					destId
				);

				if (menu.isQuantumPod()) {
					boolean holdingPod = false;
					for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
						if (player.getInventory().getItem(i).is(com.theouterworld.item.ModItems.QUANTUM_POD)) {
							holdingPod = true;
							break;
						}
					}
					if (!holdingPod) {
						return;
					}
					player.closeContainer();
					com.theouterworld.world.QuantumPodTravel.begin(player, dest);
					return;
				}

				var world = (ServerLevel) player.level();
				var pos = menu.getPadPos();
				if (!(world.getBlockEntity(pos) instanceof com.theouterworld.block.RiftPadBlockEntity pad)
					|| player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64) {
					return;
				}
				if (!com.theouterworld.block.RiftPadBlock.isRiftPadDestination(dest)) {
					return;
				}
				player.closeContainer();
				pad.beginVisit(player, dest);
			});
		});

		DustStormTicker.register();
		com.theouterworld.weather.NearworldStormTicker.register();
		com.theouterworld.weather.HighworldStormTicker.register();
		com.theouterworld.weather.DeepworldStormTicker.register();
		com.theouterworld.weather.FarworldStormTicker.register();
		com.theouterworld.weather.EdgeworldStormTicker.register();
		com.theouterworld.weather.InteriorShelterTracker.register();
		DustStormEffects.register();
		com.theouterworld.world.VacuumSuffocation.register();
		com.theouterworld.world.SpongeworldVoidFall.register();
		com.theouterworld.world.PotatoworldsVoidFall.register();
		com.theouterworld.world.BeyondlandsAltitudeCrossing.register();
		com.theouterworld.world.SolarIrradiation.register();
		com.theouterworld.world.SunDeathSequence.register();
		com.theouterworld.world.SunExistenceGuard.register();
		com.theouterworld.world.ExtremePressure.register();
		com.theouterworld.world.HighworldCrush.register();
		com.theouterworld.world.MercuryFluidInteractions.register();
		com.theouterworld.world.LiquidHydrogenFluidInteractions.register();
		com.theouterworld.world.LiquidHeliumFluidInteractions.register();
		com.theouterworld.world.LiquidAmmoniaFluidInteractions.register();
		com.theouterworld.world.LiquidMethaneFluidInteractions.register();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> {
				ServerLevel world = (ServerLevel) handler.player.level();
				if (com.theouterworld.util.ReplayCompat.isReplayServer(server)) {
					// A replay viewer's state comes from the recording, not from this fake server.
					return;
				}
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
		// TEMP VIDEO FEATURE — delete com.theouterworld.video.patrick + this line to remove
		com.theouterworld.video.patrick.PatrickVideo.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
