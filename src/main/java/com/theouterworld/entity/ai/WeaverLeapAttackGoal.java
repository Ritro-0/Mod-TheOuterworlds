package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Closes on the attacker in leaps, lands one hit, then hands off to retreat.
 */
public class WeaverLeapAttackGoal extends Goal {
	private static final double STRIKE_RANGE_SQR = 6.25;

	private final WeaverEntity weaver;
	private int leapCooldown;
	private boolean hasStruck;

	public WeaverLeapAttackGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return !weaver.isVengeanceLeaping()
			&& weaver.isAggressive()
			&& weaver.getTarget() != null
			&& weaver.getTarget().isAlive();
	}

	@Override
	public boolean canContinueToUse() {
		return canUse() && !hasStruck && !weaver.isRetreating();
	}

	@Override
	public void start() {
		leapCooldown = 0;
		hasStruck = false;
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = weaver.getTarget();
		if (target == null) {
			return;
		}
		weaver.getLookControl().setLookAt(target, 40.0F, 40.0F);
		double distSq = weaver.distanceToSqr(target);
		if (weaver.holdsBloodFeud(target) && distSq > 96.0 * 96.0) {
			weaver.dropFeudChase();
			return;
		}
		leapCooldown--;

		if (distSq <= STRIKE_RANGE_SQR) {
			if (weaver.level() instanceof ServerLevel serverLevel && weaver.doHurtTarget(serverLevel, target)) {
				if (weaver.holdsBloodFeud(target)) {
					leapCooldown = 8;
					return;
				}
				hasStruck = true;
				weaver.beginRetreat(target);
			}
			return;
		}

		Vec3 toTarget = target.position().subtract(weaver.position());
		double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
		double rise = target.getY() - weaver.getY();
		if (leapCooldown <= 0 && weaver.onGround() && horizontal > 1.0E-4 && rise > weaver.maxUpStep() + 0.35) {
			leapCooldown = weaver.hopToward(toTarget.x / horizontal, toTarget.z / horizontal, horizontal, rise);
			if (leapCooldown > 0) {
				return;
			}
		}
		weaver.getNavigation().moveTo(target, 1.15);
	}
}
