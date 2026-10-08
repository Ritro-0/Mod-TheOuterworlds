package com.theouterworld.world;

import com.theouterworld.entity.ai.WeaverColonies;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/** Per-player chat log for colony score and grudge changes. Forgotten on restart. */
public final class WeaverReputationDebug {
	private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

	private WeaverReputationDebug() {
	}

	public static boolean toggle(UUID player) {
		if (!ENABLED.add(player)) {
			ENABLED.remove(player);
			return false;
		}
		return true;
	}

	public static boolean enabled(UUID player) {
		return ENABLED.contains(player);
	}

	public static void report(
		ServerLevel level,
		@Nullable UUID player,
		long colonyId,
		BlockPos hint,
		String message
	) {
		if (colonyId == 0L) {
			return;
		}
		Component line = Component.literal(label(level, colonyId, hint) + ": " + message);
		if (player != null) {
			send(level, player, line);
			return;
		}
		for (ServerPlayer online : level.getServer().getPlayerList().getPlayers()) {
			if (online.level() == level && ENABLED.contains(online.getUUID())) {
				online.sendSystemMessage(line);
			}
		}
	}

	private static void send(ServerLevel level, UUID player, Component line) {
		if (!ENABLED.contains(player)) {
			return;
		}
		ServerPlayer online = level.getServer().getPlayerList().getPlayer(player);
		if (online != null) {
			online.sendSystemMessage(line);
		}
	}

	public static String label(ServerLevel level, long colonyId, BlockPos hint) {
		BlockPos center = WeaverColonies.centerOf(level, colonyId, hint);
		String id = Long.toHexString(colonyId);
		if (center == null) {
			return "Colony " + id;
		}
		return "Colony " + id + " near " + center.getX() + ", " + center.getZ();
	}
}
