package com.theouterworld.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * Detects when the "server" we are running inside is actually a replay being played back.
 * <p>
 * Flashback drives playback through its own {@code MinecraftServer} implementation, and our
 * server-side logic ticks inside it just like on a real server. Anything that pushes authoritative
 * state to clients has to stand down there, otherwise it fights the recorded packets: a fresh
 * dust storm manager on the replay server has no storm running and would happily announce that once
 * a second, cancelling the storm the recording is trying to show.
 * <p>
 * Matched by class name so Flashback stays an optional, compile-free dependency.
 */
public final class ReplayCompat {
	private static final String FLASHBACK_PACKAGE = "com.moulberry.flashback.";

	private ReplayCompat() {
	}

	public static boolean isReplayServer(MinecraftServer server) {
		return server != null && server.getClass().getName().startsWith(FLASHBACK_PACKAGE);
	}

	public static boolean isReplay(ServerLevel level) {
		return level != null && isReplayServer(level.getServer());
	}
}
