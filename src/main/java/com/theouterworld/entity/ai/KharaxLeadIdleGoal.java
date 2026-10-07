package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** Holds the leash still. Pathing home or wandering would fight the lead. */
public class KharaxLeadIdleGoal extends Goal {
	private final KharaxEntity kharax;

	public KharaxLeadIdleGoal(KharaxEntity kharax) {
		this.kharax = kharax;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return this.kharax.isLeashCalm();
	}

	@Override
	public boolean canContinueToUse() {
		return this.kharax.isLeashCalm();
	}

	@Override
	public void tick() {
		this.kharax.getNavigation().stop();
	}
}
