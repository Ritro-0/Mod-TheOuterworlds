package com.theouterworld.world;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModWorldPresets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outerworld world-preset behaviour: persistent Outerworld world spawn, plus a
 * one-time tinted-glass helmet for each player the first time they join.
 */
public final class OuterworldWorldType {
	private static final Logger LOGGER = LoggerFactory.getLogger(OuterworldWorldType.class);

	private OuterworldWorldType() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(OuterworldWorldType::onServerStarted);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> onPlayerJoin(handler.player));
		});
	}

	public static void writePresetMarker(Path worldRoot) {
		try {
			Files.createDirectories(worldRoot);
			Path marker = worldRoot.resolve(ModWorldPresets.PRESET_MARKER_FILE);
			Files.writeString(marker, ModWorldPresets.OUTERWORLD.identifier().toString());
			LOGGER.info("Wrote Outerworld world-preset marker at {}", marker);
		} catch (IOException e) {
			LOGGER.warn("Failed to write Outerworld world-preset marker at {}", worldRoot, e);
		}
	}

	private static void onServerStarted(MinecraftServer server) {
		ensureConfigured(server);
	}

	/**
	 * Detect the Outerworld preset, set world spawn in Outerworld, and apply the
	 * one-time starter kit. Safe to call from both server-start and player-join.
	 */
	private static OuterworldWorldTypeSavedData ensureConfigured(MinecraftServer server) {
		OuterworldWorldTypeSavedData data = OuterworldWorldTypeSavedData.get(server);
		if (data == null) {
			return null;
		}

		if (!data.isActive() && detectOuterworldPreset(server)) {
			data.setActive(true);
			LOGGER.info("Detected Outerworld world preset; enabling Outerworld spawn behaviour");
		}

		if (data.isActive() && !data.isSpawnEstablished()) {
			if (establishOuterworldSpawn(server)) {
				data.setSpawnEstablished(true);
			}
		} else if (data.isActive() && !isSpawnInOuterworld(server)) {
			// Spawn was moved away somehow; restore Outerworld as world spawn.
			if (establishOuterworldSpawn(server)) {
				data.setSpawnEstablished(true);
			}
		}

		return data;
	}

	private static boolean detectOuterworldPreset(MinecraftServer server) {
		Path root = server.getWorldPath(LevelResource.ROOT).normalize();
		Path marker = root.resolve(ModWorldPresets.PRESET_MARKER_FILE);
		if (Files.isRegularFile(marker)) {
			return true;
		}

		if (server instanceof DedicatedServer dedicated) {
			return isDedicatedOuterworldLevelType(dedicated);
		}

		return false;
	}

	private static boolean isDedicatedOuterworldLevelType(DedicatedServer dedicated) {
		try {
			Path propertiesPath = dedicated.getFile("server.properties");
			if (!Files.isRegularFile(propertiesPath)) {
				return false;
			}
			Properties properties = new Properties();
			try (var reader = Files.newBufferedReader(propertiesPath)) {
				properties.load(reader);
			}
			String levelType = properties.getProperty("level-type", "minecraft:normal");
			return ModWorldPresets.OUTERWORLD.identifier().toString().equals(levelType)
				|| ModWorldPresets.OUTERWORLD.identifier().getPath().equals(levelType);
		} catch (IOException e) {
			LOGGER.warn("Failed to read server.properties for Outerworld world-preset detection", e);
			return false;
		}
	}

	private static boolean isSpawnInOuterworld(MinecraftServer server) {
		return ModDimensions.OUTERWORLD_WORLD_KEY.equals(server.getRespawnData().dimension());
	}

	private static boolean establishOuterworldSpawn(MinecraftServer server) {
		ServerLevel outerworld = server.getLevel(ModDimensions.OUTERWORLD_WORLD_KEY);
		if (outerworld == null) {
			LOGGER.warn("Outerworld dimension missing; cannot establish Outerworld world spawn");
			return false;
		}

		var chunkSource = outerworld.getChunkSource();
		BlockPos suggested = chunkSource.randomState().sampler().findSpawnPosition();
		ChunkPos chunkPos = ChunkPos.containing(suggested);
		outerworld.getChunk(chunkPos.x(), chunkPos.z());

		BlockPos spawnPos = PlayerSpawnFinder.getSpawnPosInChunk(outerworld, chunkPos);
		if (spawnPos == null) {
			int spawnHeight = chunkSource.getGenerator().getSpawnHeight(outerworld);
			spawnPos = new BlockPos(suggested.getX(), Math.max(spawnHeight, outerworld.getMinY() + 1), suggested.getZ());
		}

		LevelData.RespawnData respawnData = LevelData.RespawnData.of(
			ModDimensions.OUTERWORLD_WORLD_KEY,
			spawnPos,
			0.0F,
			0.0F
		);
		server.setRespawnData(respawnData);
		LOGGER.info("Established Outerworld world spawn at {}", spawnPos);
		return true;
	}

	private static void onPlayerJoin(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		if (server == null) {
			return;
		}
		if (com.theouterworld.util.ReplayCompat.isReplayServer(server)) {
			return;
		}

		OuterworldWorldTypeSavedData data = ensureConfigured(server);
		if (data == null || !data.isActive()) {
			return;
		}

		if (data.hasReceivedStarterGlass(player.getUUID())) {
			return;
		}

		if (!ModDimensions.OUTERWORLD_WORLD_KEY.equals(player.level().dimension())) {
			teleportToOuterworldSpawn(player, server);
		}

		ItemStack existingHelmet = player.getItemBySlot(EquipmentSlot.HEAD);
		ItemStack glass = new ItemStack(Items.TINTED_GLASS);
		if (!existingHelmet.isEmpty() && !existingHelmet.is(Items.TINTED_GLASS)) {
			if (!player.getInventory().add(existingHelmet.copy())) {
				player.drop(existingHelmet.copy(), false);
			}
		}
		player.setItemSlot(EquipmentSlot.HEAD, glass);

		data.markStarterGlassReceived(player.getUUID());
		LOGGER.info("Granted Outerworld starter tinted glass to {}", player.getScoreboardName());
	}

	private static void teleportToOuterworldSpawn(ServerPlayer player, MinecraftServer server) {
		ServerLevel outerworld = server.getLevel(ModDimensions.OUTERWORLD_WORLD_KEY);
		if (outerworld == null) {
			LOGGER.warn("Outerworld dimension missing; cannot teleport {}", player.getScoreboardName());
			return;
		}

		LevelData.RespawnData spawn = server.getRespawnData();
		if (!ModDimensions.OUTERWORLD_WORLD_KEY.equals(spawn.dimension())) {
			if (!establishOuterworldSpawn(server)) {
				return;
			}
			spawn = server.getRespawnData();
		}

		BlockPos spawnPos = spawn.pos();
		outerworld.getChunk(spawnPos);
		BlockPos safe = player.adjustSpawnLocation(outerworld, spawnPos);
		Vec3 destination = Vec3.atBottomCenterOf(safe);

		player.teleport(new TeleportTransition(
			outerworld,
			destination,
			Vec3.ZERO,
			spawn.yaw(),
			spawn.pitch(),
			TeleportTransition.DO_NOTHING
		));
		LOGGER.info("Teleported {} to Outerworld spawn {}", player.getScoreboardName(), safe);
	}
}
