package com.theouterworld.entity.ai;

import com.theouterworld.entity.FrostworldJellyEntity;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.phys.AABB;

/**
 * Now and then a swimmer commits to a jelly. A driftmite bites it once, twice, or three times,
 * then wanders off. A feeder does not bite; it swims in, and the jelly eats it on contact.
 */
public class JellyHuntGoal extends Goal {
	private static final double RANGE = 20.0;
	private static final double BITE_RANGE = 2.4;
	private static final float BITE_DAMAGE = 4.0F;
	private static final int BITE_GAP = 14;
	private static final int CHECK_INTERVAL = 100;

	private final WaterAnimal mob;
	private final boolean bites;
	private FrostworldJellyEntity target;
	private int bitesLeft;
	private int biteCooldown;
	private int giveUp;
	private int nextCheck;
	private int restTicks;

	public JellyHuntGoal(WaterAnimal mob, boolean bites) {
		this.mob = mob;
		this.bites = bites;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.restTicks > 0) {
			this.restTicks--;
			return false;
		}
		if (--this.nextCheck > 0) {
			return false;
		}
		this.nextCheck = CHECK_INTERVAL;
		if (this.mob.getRandom().nextFloat() > 0.45F) {
			return false;
		}
		this.target = this.findJelly();
		if (this.target == null) {
			return false;
		}
		this.bitesLeft = 1 + this.mob.getRandom().nextInt(3);
		this.biteCooldown = 0;
		this.giveUp = 280;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.bitesLeft > 0
			&& this.target != null
			&& this.target.isAlive()
			&& this.giveUp > 0
			&& this.mob.distanceToSqr(this.target) <= RANGE * RANGE * 2.0;
	}

	@Override
	public void stop() {
		this.target = null;
		this.mob.getNavigation().stop();
		if (this.bitesLeft <= 0) {
			this.restTicks = 600 + this.mob.getRandom().nextInt(601);
		} else if (this.restTicks <= 0) {
			this.restTicks = 200 + this.mob.getRandom().nextInt(200);
		}
		this.bitesLeft = 0;
	}

	@Override
	public void tick() {
		if (this.target == null || !(this.mob.level() instanceof ServerLevel server)) {
			return;
		}
		this.giveUp--;
		this.mob.getNavigation().moveTo(
			this.target.getX(),
			this.target.getY() + this.target.getBbHeight() * 0.45,
			this.target.getZ(),
			1.25
		);
		if (!this.bites) {
			return;
		}
		if (this.biteCooldown > 0) {
			this.biteCooldown--;
			return;
		}
		if (this.mob.distanceTo(this.target) > BITE_RANGE) {
			return;
		}
		this.target.hurtServer(server, this.mob.damageSources().mobAttack(this.mob), BITE_DAMAGE);
		this.mob.playSound(SoundEvents.GENERIC_EAT.value(), 0.8F, 0.9F + this.mob.getRandom().nextFloat() * 0.2F);
		this.bitesLeft--;
		this.biteCooldown = BITE_GAP;
	}

	private FrostworldJellyEntity findJelly() {
		AABB box = this.mob.getBoundingBox().inflate(RANGE);
		FrostworldJellyEntity nearest = null;
		double nearestDistance = RANGE * RANGE;
		for (FrostworldJellyEntity candidate : this.mob.level().getEntitiesOfClass(FrostworldJellyEntity.class, box, LivingEntity::isAlive)) {
			double distance = this.mob.distanceToSqr(candidate);
			if (distance < nearestDistance) {
				nearest = candidate;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
}
