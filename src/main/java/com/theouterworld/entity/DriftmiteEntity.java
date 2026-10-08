package com.theouterworld.entity;

import com.theouterworld.entity.ai.JellyHuntGoal;
import com.theouterworld.entity.ai.SwimToPreyGoal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.level.Level;

/**
 * Passive swimmer. The lure cube is fullbright. Now and then it swims up to a jelly and bites
 * it once, twice, or three times, then wanders off. A feeder that comes within a few blocks
 * is chased and eaten, unless that feeder is inside an erupting vent.
 */
public class DriftmiteEntity extends OceanFishEntity {
	public static final double FEEDER_RANGE = 5.0;

	public DriftmiteEntity(EntityType<? extends DriftmiteEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 8.0)
			.add(Attributes.MOVEMENT_SPEED, 0.7)
			.add(Attributes.FOLLOW_RANGE, 16.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(1, new JellyHuntGoal(this, true));
		this.goalSelector.addGoal(2, new SwimToPreyGoal(
			this,
			FeederEntity.class,
			FEEDER_RANGE,
			1.3,
			prey -> !OceanVentEntity.cloudContains(prey.level(), prey.position())
		));
		this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 40));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() || !this.isAlive()) {
			return;
		}
		for (FeederEntity feeder : this.level().getEntitiesOfClass(
			FeederEntity.class,
			this.getBoundingBox().inflate(0.75),
			feeder -> feeder.isAlive()
				&& !feeder.isRemoved()
				&& !OceanVentEntity.cloudContains(feeder.level(), feeder.position())
		)) {
			OceanPredation.consume(this, feeder);
		}
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.COD_FLOP;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.COD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.COD_DEATH;
	}
}
