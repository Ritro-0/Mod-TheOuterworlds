package com.theouterworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Ocean creature that rests on the block under it. External motion still moves it,
 * so a Strand Hydra can reel one in, but it does not swim on its own.
 */
public abstract class PlantedOceanEntity extends WaterAnimal {
	protected PlantedOceanEntity(EntityType<? extends PlantedOceanEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void push(double xa, double ya, double za) {
	}

	protected boolean supported() {
		BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 0.05, this.getZ());
		return !this.level().getBlockState(below).getCollisionShape(this.level(), below).isEmpty();
	}

	@Override
	public void travel(Vec3 input) {
		if (this.supported()) {
			Vec3 motion = this.getDeltaMovement();
			if (motion.lengthSqr() > 0.004) {
				this.move(MoverType.SELF, motion);
				this.setDeltaMovement(motion.scale(0.4));
			} else {
				this.setDeltaMovement(Vec3.ZERO);
			}
			return;
		}
		if (this.isInWater()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
			return;
		}
		super.travel(input);
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}
}
