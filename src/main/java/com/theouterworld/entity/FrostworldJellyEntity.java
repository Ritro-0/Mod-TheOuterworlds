package com.theouterworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Gelatinous mushroom that stays on its block. Feeders within 10 blocks swim to it
 * and are eaten, with an eating sound, once they touch it.
 */
public class FrostworldJellyEntity extends PlantedOceanEntity {
	public FrostworldJellyEntity(EntityType<? extends FrostworldJellyEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data) {
		data = super.finalizeSpawn(level, difficulty, reason, data);
		settleOnFloor(level);
		return data;
	}

	/** Appear already sitting on the seafloor instead of sinking down through the water. */
	private void settleOnFloor(ServerLevelAccessor level) {
		BlockPos.MutableBlockPos cursor = BlockPos.containing(this.getX(), this.getY(), this.getZ()).mutable();
		int min = level.getMinY();
		while (cursor.getY() > min && (level.getFluidState(cursor).is(FluidTags.WATER) || level.getBlockState(cursor).isAir())) {
			cursor.move(Direction.DOWN);
		}
		BlockPos water = cursor.above();
		if (level.getFluidState(water).is(FluidTags.WATER)) {
			this.setPos(water.getX() + 0.5, water.getY(), water.getZ() + 0.5);
		}
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
