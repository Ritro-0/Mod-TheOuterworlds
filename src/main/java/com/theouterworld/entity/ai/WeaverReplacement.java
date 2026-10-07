package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.world.WeaverAbsence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * A colony that has lost a Weaver picks one living adult at random to replace them.
 * The pick stays until that courtship finishes or the adult is gone.
 */
public final class WeaverReplacement {
	private static final Map<Long, UUID> CHOSEN = new HashMap<>();

	private WeaverReplacement() {
	}

	public static boolean shouldLead(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		if (colony == 0L || !WeaverAbsence.get(level).hasAny(colony)) {
			CHOSEN.remove(colony);
			return false;
		}
		UUID chosenId = CHOSEN.get(colony);
		WeaverEntity chosen = chosenId == null ? null : find(level, chosenId);
		if (chosen == null || chosen.colonyId() != colony || !chosen.isAlive() || chosen.isBaby()) {
			List<WeaverEntity> adults = adults(level, colony);
			if (adults.isEmpty()) {
				CHOSEN.remove(colony);
				return false;
			}
			chosen = adults.get(level.getRandom().nextInt(adults.size()));
			CHOSEN.put(colony, chosen.getUUID());
		}
		return chosen == weaver;
	}

	public static void clear(long colony) {
		CHOSEN.remove(colony);
	}

	private static WeaverEntity find(ServerLevel level, UUID id) {
		if (level.getEntity(id) instanceof WeaverEntity weaver) {
			return weaver;
		}
		return null;
	}

	private static List<WeaverEntity> adults(ServerLevel level, long colony) {
		List<WeaverEntity> adults = new ArrayList<>();
		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof WeaverEntity weaver) || weaver.colonyId() != colony) {
				continue;
			}
			if (!weaver.isAlive() || weaver.isBaby() || weaver.isLeashed() || weaver.isRemoved()) {
				continue;
			}
			adults.add(weaver);
		}
		return adults;
	}
}
