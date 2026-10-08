package com.theouterworld.world;

import com.theouterworld.registry.ModDamageTypes;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GlassHelmetUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

	public static boolean shouldSuffocate(LivingEntity entity) {
		if (entity == null || !entity.isAlive()) {
			return false;
		}

		Level level = entity.level();
		if (level == null || !ModDimensions.isVacuum(level.dimension())) {
			return false;
		}

		if (entity instanceof Player player) {
			GameType mode = player.gameMode();
			if (mode == null || !mode.isSurvival() || player.getAbilities().invulnerable) {
				return false;
			}
		} else if (!(entity instanceof Mob) || !PlanetaryMobRules.isVanillaMob(entity) || entity.isInvulnerable()) {
			return false;
		}

		if (GlassHelmetUtil.isWearingAirProtection(entity) || PlanetaryMobRules.ignoresVacuum(entity)) {
			return false;
		}
		if (entity.isEyeInFluid(FluidTags.WATER) || PlanetaryMobRules.breathesFrostworldOcean(entity)) {
			return false;
		}
		return true;
	}

	static void tick(LivingEntity entity) {
		if (!shouldSuffocate(entity)) {
			return;
		}
		if (!(entity.level() instanceof ServerLevel level)) {
			return;
		}
		if (entity.tickCount % AIR_DRAIN_INTERVAL != 0) {
			return;
		}

		entity.setAirSupply(entity.getAirSupply() - 1);
		if (entity.getAirSupply() > -20) {
			return;
		}

		entity.setAirSupply(0);
		level.broadcastEntityEvent(entity, DROWN_PARTICLE_EVENT);
		entity.hurtServer(level, entity.damageSources().source(ModDamageTypes.VACUUM), DROWN_DAMAGE);
	}
}
