package com.theouterworld.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Keeps the "casted" flail model honest. A flail stack is only supposed to look casted while its
 * owner has a flail entity in flight; anything else (dropped, stored, owner relogged, flight
 * cancelled) puts it back to the normal model.
 */
public final class OpalineNickelFlailTracker {
	private static final int SWEEP_INTERVAL_TICKS = 10;

	/** Owner UUID -> number of flail entities currently flying. Server thread only. */
	private static final Map<UUID, Integer> ACTIVE_FLIGHTS = new HashMap<>();

	private OpalineNickelFlailTracker() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> ACTIVE_FLIGHTS.clear());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE_FLIGHTS.clear());

		// A casted flail never stays casted once it is lying on the ground.
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof ItemEntity itemEntity && isCastFlail(itemEntity.getItem())) {
				ItemStack reset = itemEntity.getItem().copy();
				OpalineNickelFlailItem.setCast(reset, false);
				itemEntity.setItem(reset);
			}
		});

		// Catches everything the entity could not clean up itself (relogging, owner in another
		// dimension, crashes): no flight in progress means no flail should look casted.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % SWEEP_INTERVAL_TICKS != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!hasActiveFlight(player.getUUID())) {
					uncastAll(player);
				}
			}
		});
	}

	public static void flightStarted(UUID owner) {
		ACTIVE_FLIGHTS.merge(owner, 1, Integer::sum);
	}

	public static void flightEnded(UUID owner) {
		ACTIVE_FLIGHTS.computeIfPresent(owner, (id, count) -> count > 1 ? count - 1 : null);
	}

	public static boolean hasActiveFlight(UUID owner) {
		return ACTIVE_FLIGHTS.containsKey(owner);
	}

	public static boolean isCastFlail(ItemStack stack) {
		return !stack.isEmpty() && stack.is(ModItems.OPALINE_NICKEL_FLAIL) && OpalineNickelFlailItem.isCast(stack);
	}

	/** Whether the player still carries (inventory, offhand or cursor) a flail that is marked as casted. */
	public static boolean hasCastFlail(Player player) {
		if (isCastFlail(player.containerMenu.getCarried())) {
			return true;
		}
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (isCastFlail(player.getInventory().getItem(i))) {
				return true;
			}
		}
		return false;
	}

	public static void uncastAll(Player player) {
		uncast(player.containerMenu.getCarried());
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			uncast(player.getInventory().getItem(i));
		}
	}

	private static void uncast(ItemStack stack) {
		if (isCastFlail(stack)) {
			OpalineNickelFlailItem.setCast(stack, false);
		}
	}
}
