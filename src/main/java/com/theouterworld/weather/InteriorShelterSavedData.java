package com.theouterworld.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.OuterWorldMod;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Persisted cache of sealed interior air cells in the Outerworld.
 */
public class InteriorShelterSavedData extends SavedData {
	public static final Codec<InteriorShelterSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Region.CODEC.listOf().fieldOf("regions").forGetter(data -> data.regions)
	).apply(instance, InteriorShelterSavedData::new));

	public static final SavedDataType<InteriorShelterSavedData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "interior_shelter"),
		InteriorShelterSavedData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final List<Region> regions;
	private final LongOpenHashSet index = new LongOpenHashSet();

	public InteriorShelterSavedData() {
		this(List.of());
	}

	public InteriorShelterSavedData(List<Region> regions) {
		this.regions = new ArrayList<>(regions);
		rebuildIndex();
	}

	public static InteriorShelterSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	public boolean isInterior(BlockPos pos) {
		return index.contains(pos.asLong());
	}

	public boolean isInterior(long packedPos) {
		return index.contains(packedPos);
	}

	public int cellCount() {
		return index.size();
	}

	public int regionCount() {
		return regions.size();
	}

	public LongSet allCells() {
		return index;
	}

	public boolean addRegion(Set<BlockPos> cells, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (cells.isEmpty()) {
			return false;
		}

		List<Long> packed = new ArrayList<>(cells.size());
		boolean allPresent = true;
		for (BlockPos pos : cells) {
			long key = pos.asLong();
			packed.add(key);
			if (allPresent && !index.contains(key)) {
				allPresent = false;
			}
		}

		if (allPresent && packed.size() <= index.size()) {
			// Already fully covered by existing cache.
			return false;
		}

		// Drop overlapping regions so we don't keep stale larger/smaller copies.
		regions.removeIf(region -> overlaps(region, minX, minY, minZ, maxX, maxY, maxZ));
		regions.add(new Region(packed, minX, minY, minZ, maxX, maxY, maxZ));
		rebuildIndex();
		setDirty();
		return true;
	}

	public boolean invalidateAround(BlockPos pos) {
		return !removeAround(pos).isEmpty();
	}

	/**
	 * Removes any cached interior regions overlapping this block (and a 1-block margin).
	 * Returns the removed regions so callers can re-probe if they are still sealed.
	 */
	public List<Region> removeAround(BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		List<Region> removed = new ArrayList<>();
		regions.removeIf(region -> {
			boolean matches = x >= region.minX() - 1 && x <= region.maxX() + 1
				&& y >= region.minY() - 1 && y <= region.maxY() + 1
				&& z >= region.minZ() - 1 && z <= region.maxZ() + 1;
			if (matches) {
				removed.add(region);
			}
			return matches;
		});
		if (!removed.isEmpty()) {
			rebuildIndex();
			setDirty();
		}
		return removed;
	}

	public void clearAll() {
		if (regions.isEmpty()) {
			return;
		}
		regions.clear();
		index.clear();
		setDirty();
	}

	private void rebuildIndex() {
		index.clear();
		for (Region region : regions) {
			index.addAll(region.cells());
		}
	}

	private static boolean overlaps(Region region, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		return region.minX() <= maxX && region.maxX() >= minX
			&& region.minY() <= maxY && region.maxY() >= minY
			&& region.minZ() <= maxZ && region.maxZ() >= minZ;
	}

	public record Region(
		List<Long> cells,
		int minX,
		int minY,
		int minZ,
		int maxX,
		int maxY,
		int maxZ
	) {
		public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.listOf().fieldOf("cells").forGetter(Region::cells),
			Codec.INT.fieldOf("minX").forGetter(Region::minX),
			Codec.INT.fieldOf("minY").forGetter(Region::minY),
			Codec.INT.fieldOf("minZ").forGetter(Region::minZ),
			Codec.INT.fieldOf("maxX").forGetter(Region::maxX),
			Codec.INT.fieldOf("maxY").forGetter(Region::maxY),
			Codec.INT.fieldOf("maxZ").forGetter(Region::maxZ)
		).apply(instance, Region::new));

		public Region(Collection<Long> cells, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
			this(List.copyOf(cells), minX, minY, minZ, maxX, maxY, maxZ);
		}
	}
}
