package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * Kharaxes never walk. Every step of a path is spent as a single leap, so pathfinding
 * output is translated into launch impulses instead of ground velocity.
 */
public class KharaxHopMoveControl extends MoveControl<KharaxEntity> {
	private int hopDelay;

	public KharaxHopMoveControl(KharaxEntity kharax) {
		super(kharax);
	}

	@Override
	public void tick() {
		if (this.operation != Operation.MOVE_TO) {
			super.tick();
			return;
		}
		this.operation = Operation.WAIT;
		this.mob.setZza(0.0F);

		double dx = this.wantedX - this.mob.getX();
		double dz = this.wantedZ - this.mob.getZ();
		double horizontalSq = dx * dx + dz * dz;
		if (horizontalSq < MIN_SPEED_SQR) {
			return;
		}

		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, MAX_TURN));

		if (this.hopDelay > 0) {
			this.hopDelay--;
			return;
		}
		if (!this.mob.onGround()) {
			return;
		}

		double distance = Math.sqrt(horizontalSq);
		double rise = this.wantedY - this.mob.getY();
		this.hopDelay = this.mob.launchHop(dx / distance, dz / distance, distance, rise, this.speedModifier);
	}
}
