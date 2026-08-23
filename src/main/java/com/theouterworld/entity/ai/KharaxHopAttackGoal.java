package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Swift hop toward the current target, then hand off to retreat.
 */
public class KharaxHopAttackGoal extends Goal {
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

		if (distSq <= 4.0) {
			if (kharax.level() instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& kharax.doHurtTarget(serverLevel, target)) {
				hasStruck = true;
				kharax.beginRetreat(target);
			}
			return;
		}

		if (hopCooldown <= 0 && kharax.onGround()) {
			Vec3 toTarget = target.position().subtract(kharax.position());
			Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);
			if (horizontal.lengthSqr() > 1.0E-4) {
				horizontal = horizontal.normalize().scale(1.15);
			}
			kharax.setDeltaMovement(horizontal.x, 0.55, horizontal.z);
			kharax.hurtMarked = true;
			hopCooldown = 12;
		} else {
			kharax.getNavigation().moveTo(target, 1.55);
		}
	}
}
