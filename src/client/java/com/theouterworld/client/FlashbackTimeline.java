package com.theouterworld.client;

import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Reads the current replay position from Flashback, if it is installed.
 * <p>
 * Reflection keeps Flashback an optional dependency: without it every call simply reports that we
 * are not in a replay.
 */
public final class FlashbackTimeline {
	private static boolean usable = FabricLoader.getInstance().isModLoaded("flashback");
	private static boolean resolved = false;
	private static Method isInReplay;
	private static Method getReplayServer;
	private static Method getReplayTick;

	private FlashbackTimeline() {
	}

	public static boolean isInReplay() {
		return replayTick() != null;
	}

	/** Current replay tick, or {@code null} when not playing back a replay. */
	public static Integer replayTick() {
		if (!usable) {
			return null;
		}
		try {
			resolve();
			if (!(boolean) isInReplay.invoke(null)) {
				return null;
			}
			Object replayServer = getReplayServer.invoke(null);
			if (replayServer == null) {
				return null;
			}
			return (Integer) getReplayTick.invoke(replayServer);
		} catch (Throwable failure) {
			// Flashback changed shape or misbehaved; degrade to normal (non-replay) behaviour.
			usable = false;
			return null;
		}
	}

	private static void resolve() throws ReflectiveOperationException {
		if (resolved) {
			return;
		}
		Class<?> flashback = Class.forName("com.moulberry.flashback.Flashback");
		isInReplay = flashback.getMethod("isInReplay");
		getReplayServer = flashback.getMethod("getReplayServer");
		getReplayTick = Class.forName("com.moulberry.flashback.playback.ReplayServer")
			.getMethod("getReplayTick");
		resolved = true;
	}
}
