package com.theouterworld.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.OuterWorldMod;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent overworld registry of beacon concentrator portals and the Moon
 * return beams they own. Moon chunks reconcile against this on load so a
 * broken concentrator still removes its beam the next time that chunk is loaded.
 */
public class BeaconConcentratorSavedData extends SavedData {
	public static final Codec<BeaconConcentratorSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.LONG.listOf().fieldOf("concentrators").forGetter(BeaconConcentratorSavedData::concentratorList),
		Codec.LONG.listOf().fieldOf("inner_beams").forGetter(BeaconConcentratorSavedData::innerBeamList)
	).apply(instance, BeaconConcentratorSavedData::new));

	public static final SavedDataType<BeaconConcentratorSavedData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "beacon_concentrator_portals"),
		BeaconConcentratorSavedData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final LongOpenHashSet concentrators = new LongOpenHashSet();
	private final LongOpenHashSet innerBeams = new LongOpenHashSet();

	public BeaconConcentratorSavedData() {
		this(List.of(), List.of());
	}

	public BeaconConcentratorSavedData(List<Long> concentrators, List<Long> innerBeams) {
		this.concentrators.addAll(concentrators);
		this.innerBeams.addAll(innerBeams);
	}

	private List<Long> concentratorList() {
		return toList(concentrators);
	}

	private List<Long> innerBeamList() {
		return toList(innerBeams);
	}

	private static List<Long> toList(LongOpenHashSet set) {
		List<Long> list = new ArrayList<>(set.size());
		set.forEach((long value) -> list.add(value));
		return list;
	}

	@Nullable
	public static BeaconConcentratorSavedData get(MinecraftServer server) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return null;
		}
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	public boolean addConcentrator(BlockPos pos) {
		if (concentrators.add(pos.asLong())) {
			setDirty();
			return true;
		}
		return false;
	}

	public boolean removeConcentrator(BlockPos pos) {
		if (concentrators.remove(pos.asLong())) {
			setDirty();
			return true;
		}
		return false;
	}

	public boolean hasConcentrator(BlockPos pos) {
		return concentrators.contains(pos.asLong());
	}

	public boolean hasConcentratorAt(int x, int z) {
		return findConcentratorAt(x, z) != null;
	}

	@Nullable
	public BlockPos findConcentratorAt(int x, int z) {
		for (long packed : concentrators) {
			BlockPos pos = BlockPos.of(packed);
			if (pos.getX() == x && pos.getZ() == z) {
				return pos;
			}
		}
		return null;
	}

	public List<BlockPos> concentratorsInChunk(int chunkX, int chunkZ) {
		List<BlockPos> result = new ArrayList<>();
		for (long packed : concentrators) {
			BlockPos pos = BlockPos.of(packed);
			if ((pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ) {
				result.add(pos);
			}
		}
		return result;
	}

	public void rememberInnerBeam(BlockPos pos) {
		if (innerBeams.add(pos.asLong())) {
			setDirty();
		}
	}

	public void forgetInnerBeam(BlockPos pos) {
		if (innerBeams.remove(pos.asLong())) {
			setDirty();
		}
	}

	public List<BlockPos> innerBeamsInChunk(int chunkX, int chunkZ) {
		List<BlockPos> result = new ArrayList<>();
		for (long packed : innerBeams) {
			BlockPos pos = BlockPos.of(packed);
			if ((pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ) {
				result.add(pos);
			}
		}
		return result;
	}

	public List<BlockPos> innerBeamsAt(int x, int z) {
		List<BlockPos> result = new ArrayList<>();
		for (long packed : innerBeams) {
			BlockPos pos = BlockPos.of(packed);
			if (pos.getX() == x && pos.getZ() == z) {
				result.add(pos);
			}
		}
		return result;
	}
}
