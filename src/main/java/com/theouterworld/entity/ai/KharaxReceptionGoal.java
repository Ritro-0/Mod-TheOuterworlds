package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Planted in the circle while the colony looks it over. The head still tracks the Weavers. */
public class KharaxReceptionGoal extends Goal {
	private static final double STRIKE_RANGE_SQR = 4.0;

	private final KharaxEntity kharax;
	private int lungeCooldown;

	public KharaxReceptionGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return this.kharax.isReceiving();
	}

	@Override
	public boolean canContinueToUse() {
		return this.kharax.isReceiving();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (KharaxReception.isLunging(this.kharax)) {
			this.tickLunge();
			return;
		}
		this.lungeCooldown = 0;
		this.kharax.getNavigation().stop();
		this.kharax.setJumping(false);
		this.kharax.setSpeed(0.0F);
		Vec3 motion = this.kharax.getDeltaMovement();
		this.kharax.setDeltaMovement(0.0, motion.y, 0.0);
		LivingEntity look = KharaxReception.lookTarget(this.kharax);
		if (look != null) {
			this.kharax.getLookControl().setLookAt(look, 60.0F, 60.0F);
			double dx = look.getX() - this.kharax.getX();
			double dz = look.getZ() - this.kharax.getZ();
			float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
			this.kharax.setYRot(yaw);
			this.kharax.setYBodyRot(yaw);
			this.kharax.setYHeadRot(yaw);
		}
	}

	private void tickLunge() {
		LivingEntity target = KharaxReception.lungeTarget(this.kharax);
		if (target == null) {
			return;
		}
		this.kharax.getLookControl().setLookAt(target, 50.0F, 50.0F);
		double dist = this.kharax.distanceToSqr(target);
		if (dist <= STRIKE_RANGE_SQR && this.kharax.level() instanceof ServerLevel level && this.kharax.doHurtTarget(level, target)) {
			KharaxReception.landedStrike(level, this.kharax);
			return;
		}
		this.lungeCooldown--;
		if (this.lungeCooldown <= 0 && this.kharax.onGround()) {
			Vec3 to = target.position().subtract(this.kharax.position());
			double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
			if (horizontal > 1.0E-4 && horizontal <= 7.0) {
				this.lungeCooldown = this.kharax.launchLunge(to.x / horizontal, to.z / horizontal, horizontal);
				return;
			}
		}
		if (this.kharax.getNavigation().isDone()) {
			this.kharax.getNavigation().moveTo(target, 1.2);
		}
	}
}
