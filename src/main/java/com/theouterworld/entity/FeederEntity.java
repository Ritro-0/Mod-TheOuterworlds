package com.theouterworld.entity;

import com.theouterworld.entity.ai.FeedOnVentGoal;
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
 * Worm-like swimmer. The glowing segment is rendered fullbright, like a glow squid's body,
 * and the rest of the worm stays normally lit. An erupting vent is preferred food. Otherwise a
 * Frostworld Jelly within 10 blocks is eaten on contact.
 */
public class FeederEntity extends OceanFishEntity {
	public static final double JELLY_RANGE = 10.0;

	public FeederEntity(EntityType<? extends FeederEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 6.0)
			.add(Attributes.MOVEMENT_SPEED, 0.8)
			.add(Attributes.FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.3));
		this.goalSelector.addGoal(1, new FeedOnVentGoal(this));
		this.goalSelector.addGoal(2, new SwimToPreyGoal(this, FrostworldJellyEntity.class, JELLY_RANGE, 1.35));
		this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 40));
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
