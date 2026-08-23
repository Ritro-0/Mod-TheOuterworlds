package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * After a successful hop-attack, path 15–30 blocks away and clear aggression.
 */
public class KharaxRetreatGoal extends Goal {
	private final KharaxEntity kharax;
	private Vec3 retreatPos;
	private int giveUpTicks;

	public KharaxRetreatGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return kharax.isRetreating();
	}

	@Override
	public boolean canContinueToUse() {
		return kharax.isRetreating() && giveUpTicks < 100 && retreatPos != null;
	}

	@Override
	public void start() {
		giveUpTicks = 0;
		retreatPos = pickRetreatPos();
		if (retreatPos != null) {
			kharax.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.6);
		} else {
			kharax.finishRetreat();
		}
	}

	@Override
	public void stop() {
		kharax.getNavigation().stop();
		retreatPos = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		giveUpTicks++;
		if (retreatPos == null) {
			kharax.finishRetreat();
			return;
		}
		if (kharax.distanceToSqr(retreatPos) < 4.0 || giveUpTicks > 80) {
			kharax.finishRetreat();
			return;
		}
		if (kharax.getNavigation().isDone()) {
			kharax.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.6);
		}
	}

	private Vec3 pickRetreatPos() {
		LivingEntity threat = kharax.getLastThreat();
		BlockPos home = kharax.getHomePos();
		Vec3 awayFrom = threat != null ? threat.position() : (home != null ? Vec3.atCenterOf(home) : kharax.position());
		int distance = KharaxEntity.RETREAT_MIN + kharax.getRandom().nextInt(KharaxEntity.RETREAT_MAX - KharaxEntity.RETREAT_MIN + 1);
		Vec3 away = kharax.position().subtract(awayFrom);
		if (away.lengthSqr() < 1.0E-3) {
			float yaw = kharax.getYRot() * Mth.DEG_TO_RAD;
			away = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
		}
		away = away.normalize().scale(distance);
		Vec3 target = kharax.position().add(away);
		Vec3 random = DefaultRandomPos.getPosTowards(kharax, distance, 7, target, (float) Math.PI / 2.0F);
		return random != null ? random : target;
	}
}
