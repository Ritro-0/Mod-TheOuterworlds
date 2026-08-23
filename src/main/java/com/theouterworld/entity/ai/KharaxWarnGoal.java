package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Stand still and perform the fly-rub / head-shake warning when a survival player is within
 * 15 blocks. Crossing into 5 blocks cuts the display short and commits to the attack. Once the
 * kharax has been struck the display becomes a brief show of power rather than a real deterrent.
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
		if (kharax.isAggressive()) {
			return false;
		}
		// A provoked kharax finishes the display and attacks even if the player backs off.
		if (!kharax.isProvoked() && kharax.distanceToSqr(target) > sqr(KharaxEntity.WARN_RANGE)) {
			return false;
		}
		return warnTicks < warnDuration();
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
		kharax.setWarning(false);
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
		if (target == null) {
			return;
		}
		kharax.getLookControl().setLookAt(target, 40.0F, 40.0F);
		kharax.getNavigation().stop();
		// Plant it in place without cancelling gravity, in case the display began mid-hop.
		Vec3 motion = kharax.getDeltaMovement();
		kharax.setDeltaMovement(0.0, motion.y, 0.0);

		boolean withinPouncingRange = kharax.distanceToSqr(target) <= sqr(KharaxEntity.PROXIMITY_AGGRO_RANGE);
		if (withinPouncingRange || warnTicks >= warnDuration()) {
			kharax.beginAggression(target);
		}
	}

	private int warnDuration() {
		return kharax.isProvoked() ? KharaxEntity.PROVOKED_WARN_TICKS : KharaxEntity.WARN_DURATION_TICKS;
	}

	private static double sqr(float value) {
		return (double) value * value;
	}
}
