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
 * Falling off Spongeworld (Hyperion) drops you into Deepworld instead of void death.
 *
 * TODO(return-path): Add a reliable way to get back to the Overworld from Deepworld
 * (e.g. rift-pad upgrade, emergency return ritual support on gas giants, or a
 * dedicated Deepworld→Overworld escape). Right now falling off Hyperion strands
 * you in Saturn's atmosphere unless you already have another exit.
 */
public final class SpongeworldVoidFall {
	private SpongeworldVoidFall() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerLevel world : server.getAllLevels()) {
				tickWorld(world);
			}
		});
	}

	private static void tickWorld(ServerLevel world) {
		if (!ModDimensions.isSpongeworld(world.dimension())) {
			return;
		}
		double voidY = world.getMinY() - 16.0;
		// Snapshot first: teleport() removes the player from world.players() and CME's the iterator.
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
			ejectToDeepworld(player);
		}
	}

	private static void ejectToDeepworld(ServerPlayer player) {
		ServerLevel deepworld = player.level().getServer().getLevel(ModDimensions.DEEPWORLD_WORLD_KEY);
		if (deepworld == null) {
			return;
		}

		double minY = deepworld.getMinY() + 1.0;
		double maxY = deepworld.getMaxY() - 1.0;
		double y = Mth.clamp(maxY, minY, maxY);
		double fallY = Math.min(player.getDeltaMovement().y, -0.8);

		var teleported = player.teleport(new TeleportTransition(
			deepworld,
			new Vec3(player.getX(), y, player.getZ()),
			new Vec3(0.0, fallY, 0.0),
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		if (teleported != null) {
			// Drop the Spongeworld void's accumulated fall, then keep falling in Deepworld.
			teleported.resetFallDistance();
			teleported.setDeltaMovement(0.0, fallY, 0.0);
		}
	}
}
