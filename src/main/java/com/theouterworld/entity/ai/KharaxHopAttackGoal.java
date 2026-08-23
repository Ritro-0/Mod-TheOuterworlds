package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Closes on the target in leaps, pouncing outright once within range, then hands off to retreat.
 */
public class KharaxHopAttackGoal extends Goal {
	private static final double POUNCE_RANGE_SQR = 36.0;
	private static final double STRIKE_RANGE_SQR = 4.0;

	private final KharaxEntity kharax;
	private int hopCooldown;
	private boolean hasStruck;

	public KharaxHopAttackGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return kharax.isAggressive() && kharax.getTarget() != null && kharax.getTarget().isAlive();
	}

	@Override
	public boolean canContinueToUse() {
		return canUse() && !hasStruck && !kharax.isRetreating();
	}

	@Override
	public void start() {
		hopCooldown = 0;
		hasStruck = false;
		kharax.setWarning(false);
	}

	@Override
	public void stop() {
		kharax.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = kharax.getTarget();
		if (target == null) {
			return;
		}
		kharax.getLookControl().setLookAt(target, 40.0F, 40.0F);
		double distSq = kharax.distanceToSqr(target);
		hopCooldown--;

		if (distSq <= STRIKE_RANGE_SQR) {
			if (kharax.level() instanceof ServerLevel serverLevel && kharax.doHurtTarget(serverLevel, target)) {
				hasStruck = true;
				kharax.beginRetreat(target);
			}
			return;
		}

		if (hopCooldown <= 0 && kharax.onGround() && distSq <= POUNCE_RANGE_SQR) {
			Vec3 toTarget = target.position().subtract(kharax.position());
			double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
			if (horizontal > 1.0E-4) {
				hopCooldown = kharax.launchLunge(toTarget.x / horizontal, toTarget.z / horizontal, horizontal);
				return;
			}
		}
		kharax.getNavigation().moveTo(target, 1.55);
	}
}
