package com.theouterworld.entity.ai;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.phys.AABB;

/**
 * Steers a swimmer toward the nearest living prey inside {@code range}.
 */
public class SwimToPreyGoal extends Goal {
	private final WaterAnimal mob;
	private final Class<? extends LivingEntity> preyType;
	private final double range;
	private final double speed;
	private final Predicate<LivingEntity> allowed;
	private LivingEntity prey;

	public SwimToPreyGoal(WaterAnimal mob, Class<? extends LivingEntity> preyType, double range, double speed) {
		this(mob, preyType, range, speed, prey -> true);
	}

	public SwimToPreyGoal(
		WaterAnimal mob,
		Class<? extends LivingEntity> preyType,
		double range,
		double speed,
		Predicate<LivingEntity> allowed
	) {
		this.mob = mob;
		this.preyType = preyType;
		this.range = range;
		this.speed = speed;
		this.allowed = allowed;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		this.prey = this.findPrey();
		return this.prey != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.prey != null
			&& this.prey.isAlive()
			&& this.allowed.test(this.prey)
			&& this.mob.distanceToSqr(this.prey) <= this.range * this.range;
	}

	@Override
	public void stop() {
		this.prey = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.prey == null) {
			return;
		}
		this.mob.getNavigation().moveTo(
			this.prey.getX(),
			this.prey.getY() + this.prey.getBbHeight() * 0.5,
			this.prey.getZ(),
			this.speed
		);
	}

	private LivingEntity findPrey() {
		AABB box = this.mob.getBoundingBox().inflate(this.range);
		LivingEntity nearest = null;
		double nearestDistance = this.range * this.range;
		for (LivingEntity candidate : this.mob.level().getEntitiesOfClass(this.preyType, box, LivingEntity::isAlive)) {
			if (!this.allowed.test(candidate)) {
				continue;
			}
			double distance = this.mob.distanceToSqr(candidate);
			if (distance < nearestDistance) {
				nearest = candidate;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
}
