package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** One leap to the morning meeting, then stand there until the meeting is over. */
public class WeaverRollCallGoal extends Goal {
	private static final double ARRIVED_SQR = 6.0 * 6.0;

	private final WeaverEntity weaver;
	private boolean leftGround;
	private int launchTicks;

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
		return this.weaver.level() instanceof ServerLevel
			&& WeaverRollCall.isGathering(this.weaver.colonyId());
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
		this.launchTicks = 0;
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
		if (!(this.weaver.level() instanceof ServerLevel level)) {
			return;
		}
		BlockPos spot = WeaverRollCall.meetingFor(level, this.weaver);
		if (this.weaver.isHomeLeaping() && this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER) {
			if (spot != null) {
				this.weaver.steerHomeLeap(spot);
			}
			if (!this.weaver.onGround()) {
				this.leftGround = true;
			}
			if (this.leftGround && this.weaver.onGround()) {
				this.weaver.endGatherLeap();
				this.leftGround = false;
				this.launchTicks = 0;
				return;
			}
			int limit = this.leftGround ? 400 : 20;
			if (++this.launchTicks > limit) {
				this.weaver.endGatherLeap();
				this.leftGround = false;
				this.launchTicks = 0;
			}
			return;
		}
		if (!WeaverRollCall.hasLeaped(this.weaver)) {
			if (spot != null && !arrived(spot)) {
				this.leftGround = false;
				this.launchTicks = 0;
				this.weaver.beginGatherLeap(spot);
			}
			WeaverRollCall.markLeaped(this.weaver);
			if (this.weaver.isHomeLeaping()) {
				return;
			}
		}
		this.weaver.getNavigation().stop();
		this.weaver.setZza(0.0F);
		BlockPos center = WeaverRollCall.center(this.weaver.colonyId());
		if (center != null) {
			this.weaver.getLookControl().setLookAt(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 30.0F, 30.0F);
		}
	}

	private boolean arrived(BlockPos spot) {
		double dx = spot.getX() + 0.5 - this.weaver.getX();
		double dz = spot.getZ() + 0.5 - this.weaver.getZ();
		double dy = spot.getY() + 1.0 - this.weaver.getY();
		return this.weaver.onGround() && dx * dx + dz * dz <= ARRIVED_SQR && Math.abs(dy) <= 4.0;
	}
}
