package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/** Picks up dropped tholin stalks. Two stalks in hand start a courtship without a harvest. */
public class WeaverStalkPickupGoal extends Goal {
	public static final double SEEK = 32.0;
	private static final double REACH_SQR = 2.0 * 2.0;
	private static final double WALK_SPEED = 1.05;

	private final WeaverEntity weaver;
	private @Nullable ItemEntity target;

	public WeaverStalkPickupGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	public static boolean nearby(WeaverEntity weaver, double range) {
		return nearest(weaver, range) != null;
	}

	@Override
	public boolean canUse() {
		if (this.weaver.isBaby()
			|| this.weaver.isBreeding()
			|| this.weaver.isAggressive()
			|| this.weaver.isHomeLeaping()
			|| this.weaver.getStalkCount() >= WeaverEntity.STALK_CAP
			|| WeaverRollCall.isGathering(this.weaver.colonyId())) {
			return false;
		}
		this.target = nearest(this.weaver, SEEK);
		return this.target != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.target.isAlive() && !this.target.getItem().isEmpty() && canUse();
	}

	@Override
	public void stop() {
		this.weaver.getNavigation().stop();
		this.target = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		ItemEntity item = this.target;
		if (item == null || !(this.weaver.level() instanceof ServerLevel level)) {
			return;
		}
		this.weaver.getLookControl().setLookAt(item, 30.0F, 30.0F);
		if (this.weaver.distanceToSqr(item) > REACH_SQR) {
			this.weaver.getNavigation().moveTo(item, WALK_SPEED);
			return;
		}
		int count = item.getItem().getCount();
		item.discard();
		this.weaver.addStalks(level, count);
		level.playSound(null, this.weaver.getX(), this.weaver.getY(), this.weaver.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.5F, 1.1F);
		this.target = null;
	}

	private static @Nullable ItemEntity nearest(WeaverEntity weaver, double range) {
		if (!(weaver.level() instanceof ServerLevel level)) {
			return null;
		}
		AABB box = weaver.getBoundingBox().inflate(range, 8.0, range);
		ItemEntity best = null;
		double bestDist = range * range;
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, WeaverStalkPickupGoal::isStalk)) {
			double dist = weaver.distanceToSqr(item);
			if (dist < bestDist) {
				bestDist = dist;
				best = item;
			}
		}
		return best;
	}

	private static boolean isStalk(ItemEntity item) {
		ItemStack stack = item.getItem();
		return item.isAlive() && !stack.isEmpty() && stack.is(ModBlocks.THOLIN_STALK.asItem());
	}
}
