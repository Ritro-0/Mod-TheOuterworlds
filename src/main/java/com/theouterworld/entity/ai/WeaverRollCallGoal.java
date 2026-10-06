package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** Home-plate leap to the morning meeting. Landing short does not cost the Weaver its pad. */
public class WeaverRollCallGoal extends Goal {
	private static final double ARRIVED_SQR = 6.0 * 6.0;

	private final WeaverEntity weaver;
	private boolean leftGround;
	private int leaps;

	public WeaverRollCallGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (WeaverSchedule.isBedtime(this.weaver.level())) {
			return false;
		}
		if (this.weaver.isBaby()
			|| this.weaver.isBreeding()
			|| this.weaver.isAggressive()
			|| this.weaver.isRetreating()
			|| this.weaver.isVengeanceLeaping()
			|| this.weaver.isSleeping()) {
			return false;
		}
		return this.weaver.level() instanceof ServerLevel level
			&& WeaverRollCall.isGathering(this.weaver.colonyId())
			&& WeaverRollCall.meetingFor(level, this.weaver) != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (WeaverSchedule.isBedtime(this.weaver.level())) {
			return false;
		}
		if (this.weaver.isBreeding() || this.weaver.isAggressive() || this.weaver.isVengeanceLeaping()) {
			return false;
		}
		if (this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER && this.weaver.isHomeLeaping()) {
			return true;
		}
		return canUse();
	}

	@Override
	public void start() {
		this.leftGround = false;
		this.leaps = 0;
	}

	@Override
	public void stop() {
		if (this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER || this.weaver.getGatherLanding() != null) {
			this.weaver.endGatherLeap();
		}
		this.weaver.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		BlockPos spot = this.weaver.level() instanceof ServerLevel level
			? WeaverRollCall.meetingFor(level, this.weaver)
			: this.weaver.getGatherLanding();
		if (spot == null) {
			if (this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER) {
				this.weaver.endGatherLeap();
			}
			return;
		}
		if (!WeaverLeapSpot.isDry(this.weaver.level(), spot)) {
			this.weaver.endGatherLeap();
			return;
		}
		if (this.weaver.isHomeLeaping() && this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER) {
			this.weaver.steerHomeLeap(spot);
			if (!this.weaver.onGround()) {
				this.leftGround = true;
			}
			if (this.leftGround && this.weaver.onGround()) {
				this.weaver.endGatherLeap();
				this.leftGround = false;
			}
			return;
		}
		double dx = spot.getX() + 0.5 - this.weaver.getX();
		double dz = spot.getZ() + 0.5 - this.weaver.getZ();
		double dy = spot.getY() + 1.0 - this.weaver.getY();
		if (this.weaver.onGround() && dx * dx + dz * dz <= ARRIVED_SQR && Math.abs(dy) <= 4.0) {
			this.weaver.getNavigation().stop();
			return;
		}
		if (this.leaps < 2) {
			this.leaps++;
			this.leftGround = false;
			this.weaver.beginGatherLeap(spot);
		}
	}
}
