package com.theouterworld.entity.ai;

import com.theouterworld.block.KharaxShellBlock;
import com.theouterworld.entity.KinKharaxEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Night return into the shed. The leap falls through the roof and the Kharax
 * waits on the floor until morning. There is no pad.
 */
public class KinShedSleepGoal extends Goal {
	private final KinKharaxEntity kin;
	private boolean leftGround;

	public KinShedSleepGoal(KinKharaxEntity kin) {
		this.kin = kin;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (!this.kin.denBuilt() || this.kin.isAggressive() || this.kin.isRetreating()) {
			return false;
		}
		return WeaverSchedule.isBedtime(this.kin.level()) && this.kin.denFloor() != null;
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		this.leftGround = false;
	}

	@Override
	public void stop() {
		this.kin.setDenning(false);
		this.kin.getNavigation().stop();
		if (this.kin.isHomePlateLeap()) {
			this.kin.endHomeLeap();
		}
	}

	@Override
	public void tick() {
		BlockPos floor = this.kin.denFloor();
		BlockPos room = this.kin.denOrigin();
		if (floor == null || room == null) {
			return;
		}
		if (inside(room)) {
			settle(room);
			return;
		}
		this.kin.setDenning(false);
		if (this.kin.isHomeLeaping() && !this.kin.isHomePlateLeap()) {
			this.kin.endHomeLeap();
		}
		if (this.kin.isHomePlateLeap()) {
			this.kin.steerHomeLeap(floor);
			if (!this.kin.onGround()) {
				this.leftGround = true;
				return;
			}
			if (!this.leftGround) {
				return;
			}
			if (stillInShell(floor)) {
				this.kin.setDeltaMovement(0.0, Math.min(this.kin.getDeltaMovement().y, -0.12), 0.0);
				return;
			}
			this.kin.endHomeLeap();
			this.leftGround = false;
			if (inside(room) || planted(room)) {
				settle(room);
			}
			return;
		}
		if (planted(room)) {
			settle(room);
			return;
		}
		this.leftGround = false;
		this.kin.getNavigation().stop();
		this.kin.beginHomeLeap(floor);
	}

	private void settle(BlockPos room) {
		this.kin.endHomeLeap();
		this.leftGround = false;
		this.kin.setDenning(true);
		hold(room);
	}

	private void hold(BlockPos room) {
		this.kin.getNavigation().stop();
		this.kin.setJumping(false);
		this.kin.setSpeed(0.0F);
		Vec3 motion = this.kin.getDeltaMovement();
		this.kin.setDeltaMovement(0.0, motion.y, 0.0);
		BlockPos door = room.relative(this.kin.denDoor(), 3);
		this.kin.getLookControl().setLookAt(door.getX() + 0.5, this.kin.getEyeY(), door.getZ() + 0.5, 20.0F, 20.0F);
	}

	private boolean inside(BlockPos room) {
		return this.kin.onGround() && planted(room);
	}

	/** Standing on the floor under the roof, even if the feet sit a little below the shell. */
	private boolean planted(BlockPos room) {
		double dx = this.kin.getX() - (room.getX() + 0.5);
		double dz = this.kin.getZ() - (room.getZ() + 0.5);
		return dx * dx + dz * dz <= 2.6 * 2.6
			&& this.kin.getY() >= room.getY() - 1.4
			&& this.kin.getY() <= room.getY() + 2.6;
	}

	/** Roof still under the body. Stay in the leap so the shell stays passable. */
	private boolean stillInShell(BlockPos floor) {
		if (this.kin.getY() <= floor.getY() + 1.6) {
			return false;
		}
		BlockPos under = BlockPos.containing(this.kin.getX(), this.kin.getY() - 0.2, this.kin.getZ());
		return KharaxShellBlock.isShell(this.kin.level().getBlockState(under))
			|| KharaxShellBlock.isShell(this.kin.level().getBlockState(under.below()));
	}
}
