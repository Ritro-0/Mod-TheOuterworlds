package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Daytime wandering, kept close to the claimed bed / Anchor so Weavers do not
 * roam the whole of Amberworld.
 */
public class WeaverWanderGoal extends WaterAvoidingRandomStrollGoal {
	private final WeaverEntity weaver;

	public WeaverWanderGoal(WeaverEntity weaver) {
		super(weaver, 0.85);
		this.weaver = weaver;
	}

	@Override
	public boolean canUse() {
		if (weaver.isSleeping() || weaver.isAggressive() || weaver.isRetreating() || weaver.isInspecting()) {
			return false;
		}
		return super.canUse();
	}

	@Override
	protected @Nullable Vec3 getPosition() {
		if (this.mob.isInWater()) {
			Vec3 pos = LandRandomPos.getPos(this.mob, 12, 10);
			return pos == null ? super.getPosition() : pos;
		}
		return LandRandomPos.getPos(this.mob, 12, 16);
	}
}
