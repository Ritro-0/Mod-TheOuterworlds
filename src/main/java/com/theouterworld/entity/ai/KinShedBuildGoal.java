package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.entity.KinKharaxEntity;
import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * After adoption, linger where the hearts played, then walk to an open patch
 * near the Anchor and raise the shed.
 */
public class KinShedBuildGoal extends Goal {
	private static final int PLACE_INTERVAL = 3;

	private final KinKharaxEntity kin;
	private int placeTicks;
	private int blocked;
	private int walkTicks;
	private boolean leftGround;

	public KinShedBuildGoal(KinKharaxEntity kin) {
		this.kin = kin;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return this.kin.wantsDen() && !this.kin.isDenning() && !this.kin.isAggressive() && !this.kin.isRetreating();
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
		this.placeTicks = 0;
		this.blocked = 0;
		this.walkTicks = 0;
		this.leftGround = false;
	}

	@Override
	public void stop() {
		this.kin.getNavigation().stop();
		this.kin.setCarriedItem(ItemStack.EMPTY);
		if (this.kin.leapKind() == WeaverEntity.LeapKind.GATHER) {
			this.kin.endGatherLeap();
		}
	}

	@Override
	public void tick() {
		if (!(this.kin.level() instanceof ServerLevel level)) {
			return;
		}
		this.kin.chooseDenSite(level);
		if (this.kin.denPauseLeft() > 0) {
			hold();
			float yaw = (this.kin.tickCount * 6) % 360;
			this.kin.setYRot(yaw);
			this.kin.setYBodyRot(yaw);
			this.kin.setYHeadRot(yaw);
			return;
		}
		if (this.kin.isLeashed()) {
			this.kin.dropLeash();
		}
		if (!this.kin.hasDenSite()) {
			return;
		}
		BlockPos origin = this.kin.denOrigin();
		if (origin == null) {
			return;
		}
		if (this.kin.distanceToSqr(Vec3.atCenterOf(origin)) > 16.0 * 16.0) {
			this.kin.setCarriedItem(ItemStack.EMPTY);
			approach(level, origin);
			return;
		}
		if (this.kin.leapKind() == WeaverEntity.LeapKind.GATHER) {
			this.kin.endGatherLeap();
		}
		this.kin.getNavigation().stop();
		KinDen.Placement next = this.kin.peekDenBlock(level);
		if (next == null) {
			this.kin.setCarriedItem(ItemStack.EMPTY);
			this.kin.completeDen();
			return;
		}
		if (!KinDen.canPlace(level, next)) {
			this.kin.advanceDen();
			this.walkTicks = 0;
			this.placeTicks = 0;
			return;
		}
		BlockPos block = next.pos();
		ItemStack carried = new ItemStack(next.spore() ? ModBlocks.KHARAX_SPORE : ModBlocks.KHARAX_SHED);
		if (!ItemStack.isSameItem(this.kin.getCarriedItem(), carried)) {
			this.kin.setCarriedItem(carried);
		}
		this.kin.getLookControl().setLookAt(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, 45.0F, 45.0F);
		if (next.stage() == KinDen.Stage.SHELL) {
			lock(origin);
		} else if (!closeEnough(block)) {
			BlockPos spot = KinDen.workSpot(origin, this.kin.denDoor(), block, next.stage());
			this.kin.setPos(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
			this.kin.setDeltaMovement(Vec3.ZERO);
			this.kin.resetFallDistance();
			this.kin.setJumping(false);
			this.placeTicks = 0;
			return;
		}
		if (++this.placeTicks < (next.stage() == KinDen.Stage.SHELL ? 1 : PLACE_INTERVAL)) {
			return;
		}
		this.placeTicks = 0;
		this.walkTicks = 0;
		if (KinDen.tryPlace(level, next, this.kin)) {
			this.blocked = 0;
			this.kin.advanceDen();
			return;
		}
		if (++this.blocked >= 8) {
			this.blocked = 0;
			this.kin.advanceDen();
		}
	}

	/** Feet on the center of the floor. Nothing else moves them until the shell is done. */
	private void lock(BlockPos origin) {
		this.kin.getNavigation().stop();
		this.kin.setJumping(false);
		this.kin.setSpeed(0.0F);
		if (this.kin.leapKind() == WeaverEntity.LeapKind.GATHER) {
			this.kin.endGatherLeap();
		}
		this.kin.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
		this.kin.setDeltaMovement(Vec3.ZERO);
		this.kin.resetFallDistance();
	}

	private boolean closeEnough(BlockPos block) {
		return horizontal(block) <= 2.1 && withinVertical(block);
	}

	/** Blocks up to eight above the top of the head, and a short reach downward for the floor. */
	private boolean withinVertical(BlockPos block) {
		double head = this.kin.getY() + this.kin.getBbHeight();
		double aboveHead = (block.getY() + 1.0) - head;
		double belowEye = block.getY() + 0.5 - this.kin.getEyeY();
		return aboveHead <= 8.0 && belowEye > -2.0;
	}

	private double horizontal(BlockPos block) {
		double dx = block.getX() + 0.5 - this.kin.getX();
		double dz = block.getZ() + 0.5 - this.kin.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	private void approach(ServerLevel level, BlockPos origin) {
		BlockPos stand = KinDen.stand(origin, this.kin.denDoor());
		BlockPos ground = stand.below();
		if (this.kin.isHomePlateLeap()) {
			this.kin.steerCurrentLeap();
			return;
		}
		boolean gathering = this.kin.isHomeLeaping() && this.kin.leapKind() == WeaverEntity.LeapKind.GATHER;
		if (gathering) {
			if (!this.kin.onGround()) {
				this.leftGround = true;
				this.kin.steerHomeLeap(ground);
				return;
			}
			if (!this.leftGround) {
				return;
			}
			this.kin.endGatherLeap();
			this.leftGround = false;
		} else if (this.walkTicks == 0 && this.kin.onGround() && WeaverLeapSpot.isDry(level, ground)) {
			this.walkTicks = 1;
			if (this.kin.beginGatherLeap(ground)) {
				this.leftGround = false;
				return;
			}
		}
		this.walkTicks++;
		if (this.kin.getNavigation().isDone() || this.walkTicks % 30 == 0) {
			this.kin.getNavigation().moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 2, 1.05);
		}
		this.kin.getLookControl().setLookAt(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 0.5);
	}

	private void hold() {
		this.kin.getNavigation().stop();
		this.kin.setJumping(false);
		this.kin.setSpeed(0.0F);
		Vec3 motion = this.kin.getDeltaMovement();
		this.kin.setDeltaMovement(0.0, motion.y, 0.0);
		this.kin.setYRot(Mth.wrapDegrees(this.kin.getYRot()));
	}
}
