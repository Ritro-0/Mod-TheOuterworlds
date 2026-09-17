package com.theouterworld.world;

import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Crossing Y=350 in Beyondlands / Beyondlands II swaps you into the twin world's atmosphere
 * at the same X/Z, clamped to Y=350.
 */
public final class BeyondlandsAltitudeCrossing {
	public static final double CROSS_Y = 350.0;

	private BeyondlandsAltitudeCrossing() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerLevel world : server.getAllLevels()) {
				tickWorld(world);
			}
		});
	}

	private static void tickWorld(ServerLevel world) {
		if (!ModDimensions.isBeyondlandsPair(world.dimension())) {
			return;
		}
		List<ServerPlayer> crossing = null;
		for (ServerPlayer player : world.players()) {
			if (!player.isAlive() || player.isSpectator()) {
				continue;
			}
			if (player.getY() <= CROSS_Y) {
				continue;
			}
			if (crossing == null) {
				crossing = new ArrayList<>(1);
			}
			crossing.add(player);
		}
		if (crossing == null) {
			return;
		}
		for (ServerPlayer player : crossing) {
			cross(player);
		}
	}

	private static void cross(ServerPlayer player) {
		ResourceKey<Level> destKey = ModDimensions.isBeyondlands(player.level().dimension())
			? ModDimensions.BEYONDLANDS_II_WORLD_KEY
			: ModDimensions.BEYONDLANDS_WORLD_KEY;
		ServerLevel dest = player.level().getServer().getLevel(destKey);
		if (dest == null) {
			return;
		}

		Vec3 velocity = player.getDeltaMovement();
		var teleported = player.teleport(new TeleportTransition(
			dest,
			new Vec3(player.getX(), CROSS_Y, player.getZ()),
			new Vec3(velocity.x, Math.min(velocity.y, -0.15), velocity.z),
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.DO_NOTHING
		));
		if (teleported != null) {
			teleported.resetFallDistance();
			teleported.setDeltaMovement(velocity.x, Math.min(velocity.y, -0.15), velocity.z);
		}
	}
}
