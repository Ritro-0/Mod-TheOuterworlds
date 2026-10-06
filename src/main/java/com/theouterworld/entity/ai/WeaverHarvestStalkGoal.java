package com.theouterworld.entity.ai;

import com.theouterworld.block.TholinStalkBlock;
import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

/**
 * Cuts a ripe tholin tip and keeps the stalks. Usually only the tip.
 * Sometimes the block under it as well. The base of the plant is never taken.
 */
public class WeaverHarvestStalkGoal extends Goal {
	private static final int RADIUS = 10;
	private static final double REACH_SQR = 2.4 * 2.4;
	private static final float SECOND_CHANCE = 0.25F;
	private static final double WALK_SPEED = 1.0;

	private final WeaverEntity weaver;
	private @Nullable BlockPos tip;
	private int pause;

	public WeaverHarvestStalkGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.pause > 0) {
			this.pause--;
			return false;
		}
		if (!idle() || WeaverRollCall.isGathering(this.weaver.colonyId())) {
			return false;
		}
		if (this.weaver.level() instanceof ServerLevel level && WeaverHomes.findVacancy(level, this.weaver) != null) {
			this.pause = 40;
			return false;
		}
		this.tip = TholinStalkBlock.findMatureTip(this.weaver.level(), this.weaver.blockPosition(), RADIUS);
		if (this.tip == null || wildReservedForCourtship()) {
			this.tip = null;
			this.pause = 40;
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return idle() && this.tip != null && stillRipe(this.tip);
	}

	@Override
	public void start() {
		if (this.tip != null) {
			this.weaver.getNavigation().moveTo(this.tip.getX() + 0.5, this.tip.getY(), this.tip.getZ() + 0.5, 1, WALK_SPEED);
		}
	}

	@Override
	public void stop() {
		this.weaver.getNavigation().stop();
		this.tip = null;
		this.pause = 15;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		BlockPos at = this.tip;
		if (at == null || !(this.weaver.level() instanceof ServerLevel level)) {
			return;
		}
		this.weaver.getLookControl().setLookAt(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5);
		double dx = at.getX() + 0.5 - this.weaver.getX();
		double dz = at.getZ() + 0.5 - this.weaver.getZ();
		if (dx * dx + dz * dz <= REACH_SQR && Math.abs(this.weaver.getY() - at.getY()) <= 2.0) {
			boolean second = this.weaver.getRandom().nextFloat() < SECOND_CHANCE;
			int plucked = TholinStalkBlock.pluck(level, at, second);
			if (plucked > 0) {
				this.weaver.addStalks(level, plucked);
			}
			this.tip = null;
			return;
		}
		if (this.weaver.getNavigation().isDone() || this.weaver.getNavigation().isStuck()) {
			this.weaver.getNavigation().moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 1, WALK_SPEED);
		}
	}

	private boolean idle() {
		return !this.weaver.isBaby()
			&& !this.weaver.isBreeding()
			&& !this.weaver.isAggressive()
			&& !this.weaver.isRetreating()
			&& !this.weaver.isSleeping()
			&& !this.weaver.isOfferingGift()
			&& this.weaver.getCarriedItem().isEmpty()
			&& this.weaver.getStalkCount() < WeaverEntity.STALK_CAP;
	}

	/** A colony is already walking to a wild tip. Leave those plants for that Weaver. */
	private boolean wildReservedForCourtship() {
		BlockPos at = this.tip;
		if (at == null || !(this.weaver.level() instanceof ServerLevel level)) {
			return false;
		}
		var state = level.getBlockState(at);
		if (!state.is(com.theouterworld.block.ModBlocks.THOLIN_STALK) || !state.getValue(TholinStalkBlock.WILD)) {
			return false;
		}
		long colony = this.weaver.colonyId();
		for (Entity entity : level.getAllEntities()) {
			if (entity instanceof WeaverEntity other
				&& other != this.weaver
				&& other.isAlive()
				&& other.colonyId() == colony
				&& other.isBreedLeader()
				&& other.isBreeding()) {
				return true;
			}
		}
		return false;
	}

	private boolean stillRipe(BlockPos pos) {
		var state = this.weaver.level().getBlockState(pos);
		return state.is(com.theouterworld.block.ModBlocks.THOLIN_STALK)
			&& state.getValue(TholinStalkBlock.MATURE)
			&& this.weaver.level().getBlockState(pos.above()).isAir();
	}
}
