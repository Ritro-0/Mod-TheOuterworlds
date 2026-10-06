package com.theouterworld.entity.ai;

import com.theouterworld.block.WeaverPadBlock;
import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * A new Weaver leaps onto the home plate of the empty pad it was born for,
 * then either sleeps in that pad or touches it, whichever the time of day allows.
 */
public class WeaverBabyHomeGoal extends Goal {
	private static final double WALK_SPEED = 1.05;
	private static final double BED_SQR = 2.2 * 2.2;

	private final WeaverEntity weaver;
	private boolean onDeck;
	private boolean leftGround;

	public WeaverBabyHomeGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return this.weaver.isBaby()
			&& !this.weaver.hasHome()
			&& this.weaver.getIntendedBed() != null
			&& !this.weaver.isAggressive()
			&& !this.weaver.isOfferingGift();
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public void start() {
		this.onDeck = false;
		this.leftGround = !this.weaver.onGround();
	}

	@Override
	public void stop() {
		this.onDeck = false;
		this.weaver.releaseDeck();
		this.weaver.setFlatApproach(false);
		this.weaver.getNavigation().stop();
		if (this.weaver.isHomePlateLeap()) {
			this.weaver.endHomeLeap();
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	/**
	 * Leap to the plate, then walk the deck to the pad. Walking off the plate is
	 * the point, so it never starts another leap. Only dropping below the plate does.
	 */
	@Override
	public void tick() {
		BlockPos bed = this.weaver.getIntendedBed();
		if (bed == null) {
			return;
		}
		BlockPos plate = this.weaver.getIntendedPlate();
		if (plate == null) {
			plate = WeaverHomes.findPlate(this.weaver.level(), bed);
			this.weaver.setIntendedPlate(plate);
		}
		this.weaver.getLookControl().setLookAt(bed.getX() + 0.5, bed.getY() + 0.3, bed.getZ() + 0.5);
		if (plate == null) {
			walkToBed(bed);
			return;
		}
		if (this.weaver.isHomeLeaping() && !this.weaver.isHomePlateLeap()) {
			this.weaver.endHomeLeap();
		}
		if (this.onDeck) {
			if (this.weaver.getY() < plate.getY()) {
				this.onDeck = false;
				this.leftGround = false;
				this.weaver.releaseDeck();
				this.weaver.beginHomeLeap(plate);
				return;
			}
			walkTheDeck(bed);
			return;
		}
		if (this.weaver.isHomePlateLeap()) {
			this.weaver.steerHomeLeap(plate);
			if (!this.weaver.onGround()) {
				this.leftGround = true;
				return;
			}
			if (!this.leftGround) {
				return;
			}
			this.weaver.endHomeLeap();
			this.leftGround = false;
			if (atLanding(plate)) {
				this.onDeck = true;
				walkTheDeck(bed);
			}
			return;
		}
		if (atLanding(plate)) {
			this.onDeck = true;
			walkTheDeck(bed);
			return;
		}
		if (this.weaver.onGround()) {
			this.leftGround = false;
			this.weaver.beginHomeLeap(plate);
		}
	}

	private void walkTheDeck(BlockPos bed) {
		this.weaver.bindToDeck(bed);
		this.weaver.getNavigation().stop();
		if (closeToBed(bed)) {
			claim(bed);
		}
	}

	private boolean atLanding(BlockPos plate) {
		return this.weaver.onGround()
			&& horizontal(plate) <= 16.0
			&& Math.abs(this.weaver.getY() - (plate.getY() + 1.0)) <= 3.0;
	}

	private boolean closeToBed(BlockPos bed) {
		double dx = bed.getX() + 0.5 - this.weaver.getX();
		double dz = bed.getZ() + 0.5 - this.weaver.getZ();
		return dx * dx + dz * dz <= BED_SQR && Math.abs(this.weaver.getY() - bed.getY()) <= 1.6;
	}

	private void walkToBed(BlockPos bed) {
		if (closeToBed(bed)) {
			claim(bed);
			return;
		}
		if (this.weaver.getNavigation().isDone() || this.weaver.getNavigation().isStuck()) {
			this.weaver.getNavigation().moveTo(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5, 1, WALK_SPEED);
		}
	}

	private void claim(BlockPos bed) {
		Level level = this.weaver.level();
		BlockState state = level.getBlockState(bed);
		if (!(state.getBlock() instanceof WeaverPadBlock) || !WeaverPadBlock.availableTo(level, bed, this.weaver.getUUID())) {
			this.weaver.clearIntendedHome();
			return;
		}
		BlockPos plate = this.weaver.getIntendedPlate();
		if (WeaverSchedule.isBedtime(level)) {
			if (this.weaver.startSleeping(bed)) {
				this.weaver.claimBed(bed, plate);
			} else if (!this.weaver.claimBed(bed, plate)) {
				return;
			}
		} else if (!this.weaver.claimBed(bed, plate)) {
			return;
		}
		this.weaver.clearIntendedHome();
		this.weaver.getNavigation().stop();
	}

	private double horizontal(BlockPos pos) {
		double dx = pos.getX() + 0.5 - this.weaver.getX();
		double dz = pos.getZ() + 0.5 - this.weaver.getZ();
		return dx * dx + dz * dz;
	}
}
