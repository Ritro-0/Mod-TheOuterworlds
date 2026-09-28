package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * After a single retaliatory hit, path away and drop hostility.
 */
public class WeaverRetreatGoal extends Goal {
	private final WeaverEntity weaver;
	private Vec3 retreatPos;
	private int giveUpTicks;

	public WeaverRetreatGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return weaver.isRetreating();
	}

	@Override
	public boolean canContinueToUse() {
		return weaver.isRetreating() && giveUpTicks < 100 && retreatPos != null;
	}

	@Override
	public void start() {
		giveUpTicks = 0;
		retreatPos = pickRetreatPos();
		if (retreatPos != null) {
			weaver.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.5);
		} else {
			weaver.finishRetreat();
		}
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
		retreatPos = null;
		if (weaver.isRetreating()) {
			weaver.finishRetreat();
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		giveUpTicks++;
		if (retreatPos == null) {
			weaver.finishRetreat();
			return;
		}
		if (weaver.distanceToSqr(retreatPos) < 4.0 || giveUpTicks > 80) {
			weaver.finishRetreat();
			return;
		}
		if (weaver.getNavigation().isDone()) {
			weaver.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.5);
		}
	}

	private Vec3 pickRetreatPos() {
		LivingEntity threat = weaver.getLastThreat();
		Vec3 awayFrom = threat != null ? threat.position() : weaver.position();
		int distance = WeaverEntity.RETREAT_MIN
			+ weaver.getRandom().nextInt(WeaverEntity.RETREAT_MAX - WeaverEntity.RETREAT_MIN + 1);
		Vec3 away = weaver.position().subtract(awayFrom);
		if (away.lengthSqr() < 1.0E-3) {
			float yaw = weaver.getYRot() * Mth.DEG_TO_RAD;
			away = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
		}
		away = away.normalize().scale(distance);
		Vec3 target = weaver.position().add(away);
		Vec3 random = DefaultRandomPos.getPosTowards(weaver, distance, 7, target, (float) Math.PI / 2.0F);
		return random != null ? random : target;
	}
}
