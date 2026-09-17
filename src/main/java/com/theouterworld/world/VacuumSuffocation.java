package com.theouterworld.world;

import com.theouterworld.registry.ModDamageTypes;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GlassHelmetUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

/**
 * Survival/adventure players in vacuum dimensions drown in the air
 * unless they wear glass or an Opal Lens. Air drains at half of vanilla drowning speed.
 */
public final class VacuumSuffocation {
	/** Vanilla drowning removes 1 air per tick; vacuum is half that rate. */
	private static final int AIR_DRAIN_INTERVAL = 2;
	private static final float DROWN_DAMAGE = 2.0F;
	private static final byte DROWN_PARTICLE_EVENT = 67;

	private VacuumSuffocation() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});
	}

	public static boolean shouldSuffocate(LivingEntity entity) {
		if (!(entity instanceof Player player) || !player.isAlive()) {
			return false;
		}

		Level level = player.level();
		if (level == null) {
			return false;
		}

		ResourceKey<Level> dimension = level.dimension();
		if (!ModDimensions.isVacuum(dimension)) {
			return false;
		}

		GameType mode = player.gameMode();
		if (mode == null || !mode.isSurvival()) {
			return false;
		}

		if (player.getAbilities().invulnerable) {
			return false;
		}

		if (GlassHelmetUtil.isWearingAirProtection(player)) {
			return false;
		}

		return !player.isEyeInFluid(FluidTags.WATER);
	}

	private static void tickPlayer(ServerPlayer player) {
		if (!shouldSuffocate(player)) {
			return;
		}
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		if (player.tickCount % AIR_DRAIN_INTERVAL != 0) {
			return;
		}

		player.setAirSupply(player.getAirSupply() - 1);
		if (player.getAirSupply() > -20) {
			return;
		}

		player.setAirSupply(0);
		level.broadcastEntityEvent(player, DROWN_PARTICLE_EVENT);
		player.hurtServer(level, player.damageSources().source(ModDamageTypes.VACUUM), DROWN_DAMAGE);
	}
}
