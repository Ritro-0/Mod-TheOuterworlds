package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;

/**
 * Steps handle a one-block rise. A jump happens only when the whole body
 * column above and beside the weaver is empty for that rise. A lintel, or any
 * block near head height, is walked under.
 */
public class WeaverMoveControl extends MoveControl<WeaverEntity> {
	public WeaverMoveControl(WeaverEntity weaver) {
		super(weaver);
	}

	@Override
	public void tick() {
		if (this.mob.isHomeLeaping()) {
			this.operation = Operation.WAIT;
			this.mob.setZza(0.0F);
			return;
		}
		if (this.mob.isDeckBound()) {
			this.operation = Operation.WAIT;
			this.mob.setJumping(false);
			BlockPos bed = this.mob.getDeckTarget();
			if (bed == null) {
				this.mob.setZza(0.0F);
				return;
			}
			double xd = bed.getX() + 0.5 - this.mob.getX();
			double zd = bed.getZ() + 0.5 - this.mob.getZ();
			if (xd * xd + zd * zd < 0.04) {
				this.mob.setZza(0.0F);
				return;
			}
			float yRotD = (float) (Mth.atan2(zd, xd) * 180.0F / (float) Math.PI) - 90.0F;
			this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRotD, 90.0F));
			this.mob.setSpeed(0.18F);
			this.mob.setZza(1.0F);
			return;
		}
		if (this.operation != Operation.MOVE_TO) {
			super.tick();
			return;
		}

		this.operation = Operation.WAIT;
		double xd = this.wantedX - this.mob.getX();
		double zd = this.wantedZ - this.mob.getZ();
		double yd = this.wantedY - this.mob.getY();
		if (xd * xd + yd * yd + zd * zd < 2.5000003E-7F) {
			this.mob.setZza(0.0F);
			return;
		}

		float yRotD = (float) (Mth.atan2(zd, xd) * 180.0F / (float) Math.PI) - 90.0F;
		this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRotD, 90.0F));
		this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
		// The plate-to-bed deck is flat. A rise in the path is a lintel, not a ledge.
		if (this.mob.isFlatApproach()) {
			return;
		}
		if (yd > this.mob.maxUpStep() + 0.5 && xd * xd + zd * zd < 1.0 && this.columnClear(yd)) {
			this.mob.getJumpControl().jump();
			this.operation = Operation.JUMPING;
		}
	}

	/** Air for the body and the leap, including the blocks beside a wide hitbox. */
	private boolean columnClear(double rise) {
		Level level = this.mob.level();
		BlockPos feet = this.mob.blockPosition();
		int top = feet.getY() + Mth.ceil(this.mob.getBbHeight() + rise);
		for (int y = feet.getY() + 2; y <= top; y++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos pos = new BlockPos(feet.getX() + dx, y, feet.getZ() + dz);
					if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
