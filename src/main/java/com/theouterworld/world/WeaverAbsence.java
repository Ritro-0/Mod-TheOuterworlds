package com.theouterworld.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.OuterWorldMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Weavers who died, left the dimension, or walked out of home range.
 * The morning head count reads this. A missed leap is not recorded here.
 */
public class WeaverAbsence extends SavedData {
	public static final Codec<WeaverAbsence> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Entry.CODEC.listOf().optionalFieldOf("colonies", List.of()).forGetter(WeaverAbsence::entries)
	).apply(instance, WeaverAbsence::new));

	public static final SavedDataType<WeaverAbsence> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "weaver_absence"),
		WeaverAbsence::new,
		CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final Map<Long, Set<UUID>> absent = new HashMap<>();

	public WeaverAbsence() {
		this(List.of());
	}

	public WeaverAbsence(List<Entry> entries) {
		for (Entry entry : entries) {
			absent.computeIfAbsent(entry.colony, id -> new HashSet<>()).addAll(entry.weavers);
		}
	}

	public static WeaverAbsence get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	private List<Entry> entries() {
		List<Entry> list = new ArrayList<>(absent.size());
		absent.forEach((colony, weavers) -> {
			if (!weavers.isEmpty()) {
				list.add(new Entry(colony, new ArrayList<>(weavers)));
			}
		});
		return list;
	}

	public boolean mark(long colony, UUID weaver) {
		if (colony == 0L || weaver == null) {
			return false;
		}
		if (absent.computeIfAbsent(colony, id -> new HashSet<>()).add(weaver)) {
			setDirty();
			return true;
		}
		return false;
	}

	public void clear(long colony, UUID weaver) {
		Set<UUID> ids = absent.get(colony);
		if (ids != null && ids.remove(weaver)) {
			setDirty();
		}
	}

	public boolean isAbsent(long colony, UUID weaver) {
		Set<UUID> ids = absent.get(colony);
		return ids != null && weaver != null && ids.contains(weaver);
	}

	public record Entry(long colony, List<UUID> weavers) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("colony").forGetter(Entry::colony),
			UUIDUtil.CODEC.listOf().fieldOf("weavers").forGetter(Entry::weavers)
		).apply(instance, Entry::new));
	}
}
