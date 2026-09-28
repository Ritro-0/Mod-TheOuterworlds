package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Pulls a Weaver back toward its claimed bed when daytime wandering strays too far. */
public class WeaverReturnHomeGoal extends Goal {
	private static final double WALK_SPEED = 1.0;

	private final WeaverEntity weaver;

	public WeaverReturnHomeGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (weaver.isSleeping() || weaver.isAggressive() || weaver.isRetreating() || weaver.isInspecting()) {
			return false;
		}
		if (!weaver.getCarriedItem().isEmpty()) {
			return false;
		}
		return weaver.isHomeReachable() && !weaver.isWithinHome();
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public void start() {
		pathHome();
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (weaver.getNavigation().isDone() || weaver.getNavigation().isStuck()) {
			pathHome();
		}
	}

	private void pathHome() {
		BlockPos home = weaver.getHomePosition();
		weaver.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 2, WALK_SPEED);
	}
}
