package com.theouterworld.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.OuterWorldMod;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Marks worlds created with the Outerworld world preset and tracks which players
 * have already received the one-time tinted-glass starter helmet.
 */
public class OuterworldWorldTypeSavedData extends SavedData {
	public static final Codec<OuterworldWorldTypeSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.fieldOf("active").forGetter(data -> data.active),
		Codec.BOOL.fieldOf("spawn_established").forGetter(data -> data.spawnEstablished),
		UUIDUtil.STRING_CODEC.listOf().fieldOf("starter_glass_players").forGetter(OuterworldWorldTypeSavedData::starterGlassList)
	).apply(instance, OuterworldWorldTypeSavedData::new));

	public static final SavedDataType<OuterworldWorldTypeSavedData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "outerworld_world_type"),
		OuterworldWorldTypeSavedData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private boolean active;
	private boolean spawnEstablished;
	private final Set<UUID> starterGlassPlayers = new HashSet<>();

	public OuterworldWorldTypeSavedData() {
		this(false, false, List.of());
	}

	public OuterworldWorldTypeSavedData(boolean active, boolean spawnEstablished, List<UUID> starterGlassPlayers) {
		this.active = active;
		this.spawnEstablished = spawnEstablished;
		this.starterGlassPlayers.addAll(starterGlassPlayers);
	}

	private List<UUID> starterGlassList() {
		return List.copyOf(starterGlassPlayers);
	}

	@Nullable
	public static OuterworldWorldTypeSavedData get(MinecraftServer server) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return null;
		}
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		if (this.active != active) {
			this.active = active;
			setDirty();
		}
	}

	public boolean isSpawnEstablished() {
		return spawnEstablished;
	}

	public void setSpawnEstablished(boolean spawnEstablished) {
		if (this.spawnEstablished != spawnEstablished) {
			this.spawnEstablished = spawnEstablished;
			setDirty();
		}
	}

	public boolean hasReceivedStarterGlass(UUID playerId) {
		return starterGlassPlayers.contains(playerId);
	}

	public void markStarterGlassReceived(UUID playerId) {
		if (starterGlassPlayers.add(playerId)) {
			setDirty();
		}
	}
}
