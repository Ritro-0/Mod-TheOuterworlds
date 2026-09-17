package com.theouterworld.world;

import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Falling off the Potatoworlds (Phobos / Deimos) drops you into Outerworld's sky.
 */
public final class PotatoworldsVoidFall {
	private PotatoworldsVoidFall() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerLevel world : server.getAllLevels()) {
				tickWorld(world);
			}
		});
	}

	private static void tickWorld(ServerLevel world) {
		if (!ModDimensions.isPotatoworlds(world.dimension())) {
			return;
		}
		double voidY = world.getMinY() - 16.0;
		List<ServerPlayer> falling = null;
		for (ServerPlayer player : world.players()) {
			if (!player.isAlive() || player.isSpectator()) {
				continue;
			}
			if (player.getY() > voidY) {
				continue;
			}
			if (falling == null) {
				falling = new ArrayList<>(1);
			}
			falling.add(player);
		}
		if (falling == null) {
			return;
		}
		for (ServerPlayer player : falling) {
			ejectToOuterworld(player);
		}
	}

	private static void ejectToOuterworld(ServerPlayer player) {
		ServerLevel outerworld = player.level().getServer().getLevel(ModDimensions.OUTERWORLD_WORLD_KEY);
		if (outerworld == null) {
			return;
		}

		double minY = outerworld.getMinY() + 1.0;
		double maxY = outerworld.getMaxY() - 1.0;
		double y = Mth.clamp(maxY, minY, maxY);
		double fallY = Math.min(player.getDeltaMovement().y, -0.8);

		var teleported = player.teleport(new TeleportTransition(
			outerworld,
			new Vec3(player.getX(), y, player.getZ()),
			new Vec3(0.0, fallY, 0.0),
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		if (teleported != null) {
			teleported.resetFallDistance();
			teleported.setDeltaMovement(0.0, fallY, 0.0);
		}
	}
}
