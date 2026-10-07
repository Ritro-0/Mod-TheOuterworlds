package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Drop the current chore, leap to a spot a few blocks off the Kharax, and keep
 * the inspect pose on it. A rejected adoption is one punch, a short retreat, then back.
 */
public class WeaverKharaxReceptionGoal extends Goal {
	private final WeaverEntity weaver;
	private int retreatTicks;
	private boolean leftGround;
	private boolean leapStarted;
	private @Nullable Vec3 retreatPos;

	public WeaverKharaxReceptionGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.weaver.hasKharaxReception();
	}

	@Override
	public boolean canContinueToUse() {
		return this.weaver.hasKharaxReception();
	}

	@Override
	public void start() {
		this.retreatTicks = 0;
		this.leftGround = false;
		this.leapStarted = this.weaver.isHomeLeaping() && this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER;
		this.retreatPos = null;
	}

	@Override
	public void stop() {
		this.weaver.getNavigation().stop();
		this.weaver.setInspecting(false);
		if (this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER) {
			this.weaver.endGatherLeap();
		}
		this.retreatPos = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.weaver.isReceptionRecoiling()) {
			this.tickRecoil();
			return;
		}
		KharaxEntity kharax = this.kharax();
		BlockPos stand = this.weaver.getReceptionStand();
		if (kharax == null || stand == null) {
			this.weaver.clearKharaxReception();
			return;
		}
		this.weaver.getLookControl().setLookAt(kharax, 50.0F, 50.0F);
		double dist = this.weaver.distanceToSqr(Vec3.atBottomCenterOf(stand));
		if (dist > 2.56) {
			this.weaver.setInspecting(false);
			if (this.weaver.isHomePlateLeap()) {
				this.weaver.steerCurrentLeap();
				return;
			}
			BlockPos floor = stand.below();
			if (this.weaver.isHomeLeaping() && this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER) {
				if (!this.weaver.onGround()) {
					this.leftGround = true;
					this.weaver.steerHomeLeap(floor);
					return;
				}
				if (!this.leftGround) {
					return;
				}
				this.weaver.endGatherLeap();
				this.leftGround = false;
			} else if (!this.leapStarted && this.weaver.onGround()) {
				this.leapStarted = true;
				if (this.weaver.beginGatherLeap(floor)) {
					this.leftGround = false;
					return;
				}
			}
			if (this.weaver.getNavigation().isDone()) {
				this.weaver.getNavigation().moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 2, 1.2);
			}
			return;
		}
		this.weaver.getNavigation().stop();
		if (this.weaver.leapKind() == WeaverEntity.LeapKind.GATHER) {
			this.weaver.endGatherLeap();
		}
		this.weaver.setInspecting(true);
	}

	private void tickRecoil() {
		this.weaver.setInspecting(false);
		KharaxEntity kharax = this.kharax();
		if (!this.weaver.hasReceptionPunched()) {
			if (kharax == null) {
				this.weaver.markReceptionPunched();
				return;
			}
			this.weaver.getLookControl().setLookAt(kharax, 50.0F, 50.0F);
			this.retreatTicks++;
			if (this.retreatTicks <= 10) {
				this.weaver.getNavigation().stop();
				return;
			}
			boolean close = this.weaver.distanceToSqr(kharax) < 9.0;
			if (!this.weaver.wasReceptionStruck() && !close && this.retreatTicks < 80) {
				if (this.weaver.getNavigation().isDone()) {
					this.weaver.getNavigation().moveTo(kharax, 1.25);
				}
				return;
			}
			if (this.weaver.level() instanceof ServerLevel server) {
				this.weaver.doHurtTarget(server, kharax);
			}
			this.weaver.markReceptionPunched();
			this.retreatPos = this.pickRetreat(kharax);
			this.retreatTicks = 0;
			return;
		}
		this.retreatTicks++;
		if (this.retreatPos != null && this.retreatTicks < 45 && this.weaver.distanceToSqr(this.retreatPos) > 3.0) {
			if (this.weaver.getNavigation().isDone()) {
				this.weaver.getNavigation().moveTo(this.retreatPos.x, this.retreatPos.y, this.retreatPos.z, 1.45);
			}
			return;
		}
		BlockPos stand = this.weaver.getReceptionStand();
		if (stand == null || this.weaver.distanceToSqr(Vec3.atBottomCenterOf(stand)) <= 2.56 || this.retreatTicks > 160) {
			this.weaver.resumeReceptionInspect();
			return;
		}
		if (this.weaver.getNavigation().isDone()) {
			this.weaver.getNavigation().moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 1.2);
		}
	}

	private @Nullable Vec3 pickRetreat(LivingEntity threat) {
		Vec3 away = this.weaver.position().subtract(threat.position());
		if (away.lengthSqr() < 1.0E-3) {
			float yaw = this.weaver.getYRot() * Mth.DEG_TO_RAD;
			away = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
		}
		away = away.normalize().scale(8.0);
		Vec3 target = this.weaver.position().add(away);
		Vec3 random = DefaultRandomPos.getPosTowards(this.weaver, 8, 4, target, (float) Math.PI / 2.0F);
		return random != null ? random : target;
	}

	private @Nullable KharaxEntity kharax() {
		if (!(this.weaver.level() instanceof ServerLevel server)) {
			return null;
		}
		var id = this.weaver.getReceptionKharaxId();
		if (id == null) {
			return null;
		}
		return server.getEntity(id) instanceof KharaxEntity kharax && kharax.isAlive() ? kharax : null;
	}
}
