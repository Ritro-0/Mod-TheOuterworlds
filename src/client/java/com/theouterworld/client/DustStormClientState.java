package com.theouterworld.client;

import java.util.NavigableMap;
import java.util.TreeMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Client-side view of whether a dust storm is blowing.
 * <p>
 * During a Flashback replay the server logic that drives storms is not running, so the state has to
 * be reconstructed from the recorded sync packets. Each packet seen in a replay is remembered
 * against the replay tick it arrived on, and the storm is evaluated against the current position on
 * the timeline. That way scrubbing backwards past the start of a storm correctly shows clear skies,
 * instead of leaving the storm switched on forever.
 */
public final class DustStormClientState {
	private static final NavigableMap<Integer, Boolean> REPLAY_TRANSITIONS = new TreeMap<>();

	private static boolean active = false;

	private DustStormClientState() {
	}

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	public static void reset() {
		active = false;
		REPLAY_TRANSITIONS.clear();
	}

	public static void accept(boolean value) {
		active = value;
		Integer replayTick = FlashbackTimeline.replayTick();
		if (replayTick != null) {
			REPLAY_TRANSITIONS.put(replayTick, value);
		}
	}

	public static boolean isActive() {
		Integer replayTick = FlashbackTimeline.replayTick();
		if (replayTick == null) {
			return active;
		}
		var lastChange = REPLAY_TRANSITIONS.floorEntry(replayTick);
		return lastChange != null && lastChange.getValue();
	}
}
