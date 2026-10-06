package com.theouterworld.entity.ai;

import com.theouterworld.entity.OceanVentEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.phys.Vec3;

/**
 * Feeders drop whatever else they were doing and mill around inside an erupting vent.
 */
public class FeedOnVentGoal extends Goal {
	private static final double SEEK_RANGE = 32.0;

	private final WaterAnimal mob;
	private OceanVentEntity vent;
	private int redirect;

	public FeedOnVentGoal(WaterAnimal mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		this.vent = OceanVentEntity.nearestActive(this.mob, SEEK_RANGE);
		this.redirect = 0;
		return this.vent != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.vent != null
			&& this.vent.isAlive()
			&& this.vent.isErupting()
			&& this.mob.distanceToSqr(this.vent) <= (SEEK_RANGE + 8.0) * (SEEK_RANGE + 8.0);
	}

	@Override
	public void stop() {
		this.vent = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.vent == null) {
			return;
		}
		if (--this.redirect <= 0) {
			this.redirect = 12 + this.mob.getRandom().nextInt(24);
			Vec3 point = this.vent.randomPointInside(this.mob.getRandom());
			this.mob.getNavigation().moveTo(point.x, point.y, point.z, 1.15);
		}
	}
}
