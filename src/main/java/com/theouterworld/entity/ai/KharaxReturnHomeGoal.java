package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Prefer staying near the den; stroll home when too far outside a spook trip.
 */
public class KharaxReturnHomeGoal extends Goal {
	private final KharaxEntity kharax;
	private final double speed;
	private final float maxDistance;

	public KharaxReturnHomeGoal(KharaxEntity kharax, double speed, float maxDistance) {
		this.kharax = kharax;
		this.speed = speed;
		this.maxDistance = maxDistance;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (kharax.isSpooking() || kharax.isWarning() || kharax.isAggressive() || kharax.isRetreating()) {
			return false;
		}
		BlockPos home = kharax.getHomePos();
		if (home == null) {
			return false;
		}
		return kharax.distanceToSqr(Vec3.atCenterOf(home)) > maxDistance * maxDistance;
	}

	@Override
	public boolean canContinueToUse() {
		BlockPos home = kharax.getHomePos();
		return home != null
			&& !kharax.isSpooking()
			&& !kharax.isWarning()
			&& !kharax.isAggressive()
			&& !kharax.isRetreating()
			&& kharax.distanceToSqr(Vec3.atCenterOf(home)) > 16.0;
	}

	@Override
	public void start() {
		BlockPos home = kharax.getHomePos();
		if (home == null) {
			return;
		}
		Vec3 toward = DefaultRandomPos.getPosTowards(
			kharax,
			16,
			7,
			Vec3.atBottomCenterOf(home),
			(float) Math.PI / 2.0F
		);
		if (toward != null) {
			kharax.getNavigation().moveTo(toward.x, toward.y, toward.z, speed);
		} else {
			kharax.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, speed);
		}
	}
}
