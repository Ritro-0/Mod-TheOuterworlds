package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * A leashed kharax stays manageable: one short hop at the holder about every half minute,
 * then it settles again instead of committing to the warning-and-chase loop.
 */
public class KharaxLeadNipGoal extends Goal {
	private static final double BITE_RANGE_SQR = 4.0;

	private final KharaxEntity kharax;
	private int cooldown = 600;
	private int ticks;
	private boolean launched;
	private boolean leftGround;
	private boolean finished;

	public KharaxLeadNipGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (!kharax.isLeashCalm() || !(kharax.getLeashHolder() instanceof LivingEntity holder) || !holder.isAlive()) {
			return false;
		}
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.finished
			&& this.ticks < 50
			&& kharax.isLeashCalm()
			&& kharax.getLeashHolder() instanceof LivingEntity holder
			&& holder.isAlive();
	}

	@Override
	public void start() {
		this.ticks = 0;
		this.launched = false;
		this.leftGround = false;
		this.finished = false;
	}

	@Override
	public void stop() {
		this.cooldown = 520 + this.kharax.getRandom().nextInt(160);
		this.finished = false;
		this.leftGround = false;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.ticks++;
		if (!(this.kharax.getLeashHolder() instanceof LivingEntity holder)) {
			this.finished = true;
			return;
		}
		this.kharax.getLookControl().setLookAt(holder, 40.0F, 40.0F);
		if (!this.launched) {
			this.launched = true;
			if (this.kharax.distanceToSqr(holder) <= BITE_RANGE_SQR) {
				this.bite(holder);
				return;
			}
			Vec3 to = holder.position().subtract(this.kharax.position());
			double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
			if (horizontal > 1.0E-4 && this.kharax.onGround()) {
				this.kharax.launchHop(to.x / horizontal, to.z / horizontal, Math.min(horizontal, 4.0), 0.0, 0.85);
			} else {
				this.finished = true;
			}
			return;
		}
		if (!this.kharax.onGround()) {
			this.leftGround = true;
		}
		if ((this.leftGround && this.kharax.onGround()) || this.ticks > 40) {
			if (this.kharax.distanceToSqr(holder) <= BITE_RANGE_SQR) {
				this.bite(holder);
			} else {
				this.finished = true;
			}
		}
	}

	private void bite(LivingEntity holder) {
		if (this.kharax.level() instanceof ServerLevel server) {
			this.kharax.doHurtTarget(server, holder);
		}
		this.finished = true;
	}
}
