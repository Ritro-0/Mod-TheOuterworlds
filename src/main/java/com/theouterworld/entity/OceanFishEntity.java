package com.theouterworld.entity;

import com.theouterworld.entity.ai.OceanSwimControl;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Swimming Frostworld fish. Shares the vanilla fish flop and the water pathing used by cod.
 */
public abstract class OceanFishEntity extends WaterAnimal {
	protected OceanFishEntity(EntityType<? extends OceanFishEntity> type, Level level) {
		super(type, level);
		this.moveControl = new OceanSwimControl(this);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	public void aiStep() {
		if (!this.isInWater() && this.onGround() && this.verticalCollision) {
			this.setDeltaMovement(
				this.getDeltaMovement().add(
					(this.random.nextFloat() * 2.0F - 1.0F) * 0.05F,
					0.4F,
					(this.random.nextFloat() * 2.0F - 1.0F) * 0.05F
				)
			);
			this.setOnGround(false);
			this.needsSync = true;
			this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
		}
		super.aiStep();
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		this.moveRelative(0.02F, input);
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
		if (this.getTarget() == null) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.003, 0.0));
		}
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}

	protected abstract SoundEvent getFlopSound();
}
