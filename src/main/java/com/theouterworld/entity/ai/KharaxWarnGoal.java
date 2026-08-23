package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Stand still and perform the fly-rub / head-shake warning for ~8 seconds when a
 * survival player is within 15 blocks. Becomes aggressive if they do not leave.
 */
public class KharaxWarnGoal extends Goal {
	private final KharaxEntity kharax;
	private Player target;
	private int warnTicks;

	public KharaxWarnGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (kharax.isAggressive() || kharax.isRetreating() || kharax.isSpooking()) {
			return false;
		}
		Player player = kharax.findThreateningPlayer(KharaxEntity.WARN_RANGE);
		if (player == null) {
			return false;
		}
		this.target = player;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (target == null || !target.isAlive() || target.isCreative() || target.isSpectator()) {
			return false;
		}
		if (kharax.distanceToSqr(target) > KharaxEntity.WARN_RANGE * KharaxEntity.WARN_RANGE) {
			return false;
		}
		return warnTicks < KharaxEntity.WARN_DURATION_TICKS && !kharax.isAggressive();
	}

	@Override
	public void start() {
		warnTicks = 0;
		kharax.getNavigation().stop();
		kharax.setWarning(true);
		kharax.setTarget(null);
		kharax.playWarningSound();
	}

	@Override
	public void stop() {
		boolean stillClose = target != null
			&& target.isAlive()
			&& !target.isCreative()
			&& !target.isSpectator()
			&& kharax.distanceToSqr(target) <= KharaxEntity.WARN_RANGE * KharaxEntity.WARN_RANGE;
		kharax.setWarning(false);
		if (stillClose && warnTicks >= KharaxEntity.WARN_DURATION_TICKS) {
			kharax.beginAggression(target);
		}
		target = null;
		warnTicks = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		warnTicks++;
		if (target != null) {
			kharax.getLookControl().setLookAt(target, 40.0F, 40.0F);
			kharax.getNavigation().stop();
			kharax.setDeltaMovement(Vec3.ZERO);
		}
	}
}
