package com.theouterworld.entity.ai;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * Cod-style swim steering, plus a pitch so feeders and driftmites point along their path.
 */
public class OceanSwimControl extends MoveControl<Mob> {
	public OceanSwimControl(Mob mob) {
		super(mob);
	}

	@Override
	public void tick() {
		if (this.mob.isEyeInFluid(FluidTags.WATER)) {
			this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
		}

		if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
			float targetSpeed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
			this.mob.setSpeed(Mth.lerp(0.125F, this.mob.getSpeed(), targetSpeed));
			double xd = this.wantedX - this.mob.getX();
			double yd = this.wantedY - this.mob.getY();
			double zd = this.wantedZ - this.mob.getZ();
			double horizontal = Math.sqrt(xd * xd + zd * zd);
			if (yd != 0.0) {
				double distance = Math.sqrt(horizontal * horizontal + yd * yd);
				this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, this.mob.getSpeed() * (yd / distance) * 0.1, 0.0));
			}
			if (xd != 0.0 || zd != 0.0) {
				float yRot = (float) (Mth.atan2(zd, xd) * 180.0F / (float) Math.PI) - 90.0F;
				this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRot, 90.0F));
				this.mob.yBodyRot = this.mob.getYRot();
			}
			if (horizontal > 1.0E-5 || Math.abs(yd) > 1.0E-5) {
				float xRot = (float) (-(Mth.atan2(yd, Math.max(horizontal, 1.0E-5)) * 180.0F / (float) Math.PI));
				this.mob.setXRot(this.rotlerp(this.mob.getXRot(), xRot, 20.0F));
			}
		} else {
			this.mob.setSpeed(0.0F);
		}
	}
}
