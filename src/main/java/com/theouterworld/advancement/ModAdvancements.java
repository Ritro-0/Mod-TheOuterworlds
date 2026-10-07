package com.theouterworld.advancement;

import com.theouterworld.item.AerostatBalloonItem;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GlassHelmetUtil;
import com.theouterworld.util.ReplayCompat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class ModAdvancements {
	private static final double BALLOON_ASCENT_BLOCKS = 200.0;
	private static final Map<UUID, Ascent> BALLOON_ASCENT = new HashMap<>();

	private ModAdvancements() {
	}

	public static void register() {
		ModCriteria.register();
		PlayerProgress.register();

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
			ResourceKey<Level> from = origin.dimension();
			ResourceKey<Level> to = destination.dimension();
			if (ModDimensions.isOuterworld(from)) {
				PlayerProgress.clearStartedInOuterworld(player);
			}
			if (ModDimensions.isOuterworld(to) && !PlayerProgress.startedInOuterworld(player)) {
				ModCriteria.ENTER_OUTERWORLD.trigger(player);
			}
			if (ModDimensions.isChartedBody(to)) {
				ModCriteria.VISIT_BODY.trigger(player, to);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> {
			if (ReplayCompat.isReplayServer(server)) {
				return;
			}
			ServerPlayer player = handler.player;
			ResourceKey<Level> dimension = player.level().dimension();
			if (ModDimensions.isChartedBody(dimension)) {
				ModCriteria.VISIT_BODY.trigger(player, dimension);
			}
		}));

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
			BALLOON_ASCENT.remove(handler.player.getUUID())
		);
	}

	/**
	 * Outerworld-preset players are already home. Red Horizon waits until they leave and come back.
	 * Call this before the starter teleport so that teleport is not counted as a first arrival.
	 */
	public static void markStartedInOuterworld(ServerPlayer player) {
		PlayerProgress.markStartedInOuterworld(player);
	}

	public static void onProcessorStarted(ServerPlayer player) {
		ModCriteria.START_PROCESSOR.trigger(player);
	}

	public static void onProcessorIronTaken(ServerPlayer player, int count) {
		PlayerProgress.addProcessorIron(player, count);
	}

	public static void onSuspiciousRegolithBrushed(ServerPlayer player) {
		ModCriteria.BRUSH_SUSPICIOUS_REGOLITH.trigger(player);
	}

	public static void onEmergencyReturn(ServerPlayer player) {
		ModCriteria.EMERGENCY_RETURN.trigger(player);
	}

	public static void onTelescopeUsed(ServerPlayer player) {
		if (ModDimensions.isMoon(player.level().dimension())) {
			ModCriteria.USE_ASTRAL_TELESCOPE.trigger(player);
		}
	}

	public static void onRiftChargeActivated(ServerLevel world, BlockPos pos) {
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 0.5;
		double cz = pos.getZ() + 0.5;
		double rangeSqr = 15.0 * 15.0;
		for (ServerPlayer player : world.players()) {
			if (player.isSpectator() || !player.isAlive()) {
				continue;
			}
			if (player.distanceToSqr(cx, cy, cz) <= rangeSqr) {
				ModCriteria.WITNESS_RIFT_CHARGE.trigger(player);
			}
		}
	}

	public static void onBeaconConcentratorPowered(ServerLevel world, BlockPos pos, UUID placedBy) {
		if (placedBy != null) {
			ServerPlayer placer = world.getServer().getPlayerList().getPlayer(placedBy);
			if (placer != null) {
				ModCriteria.POWER_BEACON_CONCENTRATOR.trigger(placer);
				return;
			}
		}
		double rangeSqr = 32.0 * 32.0;
		for (ServerPlayer player : world.players()) {
			if (player.isSpectator()) {
				continue;
			}
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= rangeSqr) {
				ModCriteria.POWER_BEACON_CONCENTRATOR.trigger(player);
			}
		}
	}

	public static void onDustStorm(ServerPlayer player) {
		ModCriteria.DUST_STORM_EXPOSURE.trigger(player);
		if (GlassHelmetUtil.isWearingDustStormProtection(player)) {
			ModCriteria.DUST_STORM_NEGATED.trigger(player);
		}
	}

	public static void tickBalloon(ServerPlayer player) {
		boolean climbing = AerostatBalloonItem.isActive(player)
			&& !player.isShiftKeyDown()
			&& !player.getAbilities().flying
			&& ModDimensions.isGasGiant(player.level().dimension());
		if (!climbing) {
			BALLOON_ASCENT.remove(player.getUUID());
			return;
		}
		double y = player.getY();
		Ascent previous = BALLOON_ASCENT.get(player.getUUID());
		double risen = previous == null ? 0.0 : previous.risen + Math.max(0.0, y - previous.lastY);
		if (risen >= BALLOON_ASCENT_BLOCKS) {
			ModCriteria.AEROSTAT_ASCENT.trigger(player);
			BALLOON_ASCENT.remove(player.getUUID());
		} else {
			BALLOON_ASCENT.put(player.getUUID(), new Ascent(y, risen));
		}
	}

	public static void onQuantumTravel(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
		if (!from.equals(to)) {
			ModCriteria.QUANTUM_TRAVEL.trigger(player);
		}
	}

	public static void onSunVisitCancelled(ServerPlayer player) {
		ModCriteria.CANCEL_SUN_VISIT.trigger(player);
	}

	public static void onWeaverGift(ServerPlayer player) {
		ModCriteria.WEAVER_GIFT.trigger(player);
	}

	public static void onKharaxAdopted(ServerPlayer player) {
		ModCriteria.KHARAX_ADOPTED.trigger(player);
	}

	public static void onMoonVoidFall(ServerPlayer player) {
		ModCriteria.MOON_VOID_FALL.trigger(player);
	}

	private record Ascent(double lastY, double risen) {
	}
}
