package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Sporadically leave the den to wander/jump around (the "spook"), then return home.
 */
public class KharaxSpookGoal extends Goal {
	private final KharaxEntity kharax;
	private Phase phase = Phase.IDLE;
	private int phaseTicks;
	private int cooldownTicks;
	private Vec3 wanderTarget;

	public KharaxSpookGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (kharax.isWarning() || kharax.isAggressive() || kharax.isRetreating()) {
			return false;
		}
		if (kharax.getHomePos() == null) {
			return false;
		}
		if (cooldownTicks > 0) {
			cooldownTicks--;
			return false;
		}
		return kharax.getRandom().nextInt(600) == 0;
	}

	@Override
	public boolean canContinueToUse() {
		if (kharax.isWarning() || kharax.isAggressive() || kharax.isRetreating()) {
			return false;
		}
		return phase != Phase.IDLE && phaseTicks < 400;
	}

	@Override
	public void start() {
		phase = Phase.OUTBOUND;
		phaseTicks = 0;
		kharax.setSpooking(true);
		wanderTarget = pickAwayFromHome(28 + kharax.getRandom().nextInt(24));
		if (wanderTarget != null) {
			kharax.getNavigation().moveTo(wanderTarget.x, wanderTarget.y, wanderTarget.z, 1.1);
		}
	}

	@Override
	public void stop() {
		kharax.setSpooking(false);
		kharax.getNavigation().stop();
		phase = Phase.IDLE;
		cooldownTicks = 400 + kharax.getRandom().nextInt(800);
		wanderTarget = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		phaseTicks++;
		if (phase == Phase.OUTBOUND) {
			if (wanderTarget == null || kharax.distanceToSqr(wanderTarget) < 9.0 || phaseTicks > 120) {
				phase = Phase.WANDER;
				phaseTicks = 0;
			} else if (kharax.getNavigation().isDone()) {
				kharax.getNavigation().moveTo(wanderTarget.x, wanderTarget.y, wanderTarget.z, 1.1);
			}
		} else if (phase == Phase.WANDER) {
			if (kharax.getNavigation().isDone() || kharax.getRandom().nextInt(40) == 0) {
				Vec3 next = DefaultRandomPos.getPos(kharax, 12, 5);
				if (next != null) {
					kharax.getNavigation().moveTo(next.x, next.y, next.z, 1.05);
				}
			}
			if (phaseTicks > 100 + kharax.getRandom().nextInt(80)) {
				phase = Phase.RETURN;
				phaseTicks = 0;
				BlockPos home = kharax.getHomePos();
				if (home != null) {
					kharax.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.2);
				}
			}
		} else if (phase == Phase.RETURN) {
			BlockPos home = kharax.getHomePos();
			if (home == null || kharax.distanceToSqr(Vec3.atCenterOf(home)) < 9.0 || phaseTicks > 160) {
				phase = Phase.IDLE;
				return;
			}
			if (kharax.getNavigation().isDone()) {
				kharax.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.2);
			}
		}
	}

	private Vec3 pickAwayFromHome(int distance) {
		BlockPos home = kharax.getHomePos();
		if (home == null) {
			return null;
		}
		Vec3 away = DefaultRandomPos.getPosAway(kharax, distance, 8, Vec3.atBottomCenterOf(home));
		return away != null ? away : DefaultRandomPos.getPos(kharax, distance, 7);
	}

	private enum Phase {
		IDLE,
		OUTBOUND,
		WANDER,
		RETURN
	}
}
