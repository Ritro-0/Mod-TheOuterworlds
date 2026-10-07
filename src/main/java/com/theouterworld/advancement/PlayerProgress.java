package com.theouterworld.advancement;

import com.mojang.serialization.Codec;
import com.theouterworld.OuterWorldMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/** Persistent counters and flags that advancements need across relogs and death. */
public final class PlayerProgress {
	public static final int PROCESSOR_IRON_GOAL = 27;

	/**
	 * True only for players who began on an Outerworld world preset and have not left yet.
	 * While this is set, arriving in the Outerworld does not grant Red Horizon.
	 */
	public static final AttachmentType<Boolean> STARTED_IN_OUTERWORLD = AttachmentRegistry.create(
		OuterWorldMod.id("started_in_outerworld"),
		builder -> builder.persistent(Codec.BOOL).copyOnDeath()
	);

	public static final AttachmentType<Integer> PROCESSOR_IRON_TAKEN = AttachmentRegistry.create(
		OuterWorldMod.id("processor_iron_taken"),
		builder -> builder.persistent(Codec.INT).copyOnDeath().initializer(() -> 0)
	);

	private PlayerProgress() {
	}

	public static void register() {
	}

	public static boolean startedInOuterworld(ServerPlayer player) {
		return Boolean.TRUE.equals(player.getAttached(STARTED_IN_OUTERWORLD));
	}

	public static void markStartedInOuterworld(ServerPlayer player) {
		player.setAttached(STARTED_IN_OUTERWORLD, true);
	}

	public static void clearStartedInOuterworld(ServerPlayer player) {
		if (startedInOuterworld(player)) {
			player.setAttached(STARTED_IN_OUTERWORLD, false);
		}
	}

	public static void addProcessorIron(ServerPlayer player, int count) {
		if (count <= 0) {
			return;
		}
		int taken = player.getAttachedOrElse(PROCESSOR_IRON_TAKEN, 0);
		if (taken >= PROCESSOR_IRON_GOAL) {
			ModCriteria.EXTRACT_PROCESSOR_IRON.trigger(player);
			return;
		}
		taken += count;
		player.setAttached(PROCESSOR_IRON_TAKEN, taken);
		if (taken >= PROCESSOR_IRON_GOAL) {
			ModCriteria.EXTRACT_PROCESSOR_IRON.trigger(player);
		}
	}
}
