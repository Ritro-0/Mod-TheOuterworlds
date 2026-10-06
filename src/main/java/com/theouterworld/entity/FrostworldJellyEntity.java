package com.theouterworld.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Gelatinous mushroom that stays on its block. Feeders within 10 blocks swim to it
 * and are eaten, with an eating sound, once they touch it.
 */
public class FrostworldJellyEntity extends PlantedOceanEntity {
	public FrostworldJellyEntity(EntityType<? extends FrostworldJellyEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 12.0)
			.add(Attributes.MOVEMENT_SPEED, 0.0);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() || !this.isAlive()) {
			return;
		}
		for (FeederEntity feeder : this.level().getEntitiesOfClass(
			FeederEntity.class,
			this.getBoundingBox().inflate(0.85),
			feeder -> feeder.isAlive() && !feeder.isRemoved()
		)) {
			OceanPredation.consume(this, feeder);
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SLIME_HURT_SMALL;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SLIME_DEATH_SMALL;
	}
}
