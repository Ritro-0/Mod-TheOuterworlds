package com.theouterworld.world;

import com.theouterworld.network.SunDeathSyncPacket;
import com.theouterworld.registry.ModDamageTypes;
import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Visit The Sun: 3s grace → burning overlay → whiteout → punchline → dissolution.
 * Creative / spectator are immune.
 */
public final class SunDeathSequence {
	public static final int GRACE_TICKS = 60;
	public static final int BURN_TICKS = 40;
	public static final int WHITE_TICKS = 30;
	public static final int TEXT_TICKS = 40;
	public static final int KILL_AT = GRACE_TICKS + BURN_TICKS + WHITE_TICKS + TEXT_TICKS;

	private static final Map<UUID, Integer> TICKS_ON_SUN = new HashMap<>();

	private SunDeathSequence() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
			Iterator<Map.Entry<UUID, Integer>> it = TICKS_ON_SUN.entrySet().iterator();
			while (it.hasNext()) {
				UUID id = it.next().getKey();
				if (server.getPlayerList().getPlayer(id) == null) {
					it.remove();
				}
			}
		});
	}

	public static void markArrived(ServerPlayer player) {
		GameType mode = player.gameMode();
		if (mode == GameType.CREATIVE || mode == GameType.SPECTATOR || player.getAbilities().invulnerable) {
			return;
		}
		TICKS_ON_SUN.put(player.getUUID(), 0);
		ServerPlayNetworking.send(player, new SunDeathSyncPacket(0));
	}

	public static int getTicks(ServerPlayer player) {
		return TICKS_ON_SUN.getOrDefault(player.getUUID(), -1);
	}

	private static void tickPlayer(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		if (!ModDimensions.isSun(level.dimension())) {
			if (TICKS_ON_SUN.remove(player.getUUID()) != null) {
				ServerPlayNetworking.send(player, new SunDeathSyncPacket(-1));
			}
			return;
		}

		GameType mode = player.gameMode();
		if (mode == GameType.CREATIVE || mode == GameType.SPECTATOR
			|| player.getAbilities().invulnerable || !player.isAlive()) {
			if (TICKS_ON_SUN.remove(player.getUUID()) != null) {
				ServerPlayNetworking.send(player, new SunDeathSyncPacket(-1));
			}
			return;
		}

		int ticks = TICKS_ON_SUN.getOrDefault(player.getUUID(), 0) + 1;
		TICKS_ON_SUN.put(player.getUUID(), ticks);

		if (ticks == 1 || ticks == GRACE_TICKS || ticks == GRACE_TICKS + BURN_TICKS
			|| ticks == GRACE_TICKS + BURN_TICKS + WHITE_TICKS
			|| ticks == KILL_AT) {
			ServerPlayNetworking.send(player, new SunDeathSyncPacket(ticks));
		}

		if (ticks >= KILL_AT) {
			TICKS_ON_SUN.remove(player.getUUID());
			player.hurtServer(level, player.damageSources().source(ModDamageTypes.SOLAR_DISSOLUTION), Float.MAX_VALUE);
			if (player.isAlive()) {
				player.kill(level);
			}
		}
	}
}
