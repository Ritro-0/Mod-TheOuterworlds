package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.WeaverNetBlockEntity;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Anchor housekeeping. A Weaver that finds a block it did not put there walks over, gives it a
 * long look, and then either tucks it into a net or pitches it off the side.
 */
public class WeaverHousekeepingGoal extends Goal {
	private static final int SCAN_INTERVAL = 80;
	private static final int SCAN_RADIUS = 6;
	private static final int SCAN_HEIGHT = 4;
	private static final int MAX_CANDIDATES = 12;
	private static final int NET_REACH_XZ = 40;
	private static final int NET_REACH_Y = 32;
	private static final double REACH_SQR = 6.25;
	private static final int INSPECT_TICKS = 40;
	private static final int TRAVEL_TIMEOUT = 240;
	private static final int DISCARD_WINDUP = 8;
	private static final double WALK_SPEED = 1.0;

	private enum Phase {
		APPROACH,
		INSPECT,
		DELIVER,
		DISCARD
	}

	private final WeaverEntity weaver;

	private Phase phase = Phase.APPROACH;
	private @Nullable BlockPos litter;
	private @Nullable BlockPos net;
	private int scanCooldown;
	private int phaseTicks;
	private boolean finished;

	public WeaverHousekeepingGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (weaver.isAggressive() || weaver.isRetreating() || weaver.isSleeping() || !(weaver.level() instanceof ServerLevel)) {
			return false;
		}
		// A weaver that died mid-errand, or was reloaded holding something, finishes the job.
		if (!weaver.getCarriedItem().isEmpty()) {
			return true;
		}
		if (scanCooldown > 0) {
			scanCooldown--;
			return false;
		}
		scanCooldown = SCAN_INTERVAL;
		litter = findLitter();
		return litter != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (finished || weaver.isAggressive() || weaver.isRetreating() || weaver.isSleeping()) {
			return false;
		}
		return switch (phase) {
			case APPROACH -> litter != null && phaseTicks < TRAVEL_TIMEOUT;
			case INSPECT -> litter != null;
			case DELIVER -> net != null && phaseTicks < TRAVEL_TIMEOUT && !weaver.getCarriedItem().isEmpty();
			case DISCARD -> !weaver.getCarriedItem().isEmpty();
		};
	}

	@Override
	public void start() {
		finished = false;
		phaseTicks = 0;
		net = null;
		phase = weaver.getCarriedItem().isEmpty() ? Phase.APPROACH : Phase.DISCARD;
		if (phase == Phase.DISCARD) {
			// Resuming with something in hand: try a net first, fall back to throwing it away.
			net = findNet();
			if (net != null) {
				phase = Phase.DELIVER;
			}
		}
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
		weaver.setInspecting(false);
		litter = null;
		net = null;
		phase = Phase.APPROACH;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		phaseTicks++;
		switch (phase) {
			case APPROACH -> tickApproach();
			case INSPECT -> tickInspect();
			case DELIVER -> tickDeliver();
			case DISCARD -> tickDiscard();
		}
	}

	private void tickApproach() {
		BlockPos pos = litter;
		if (pos == null || !WeaverAnchors.isLitter(weaver.level(), pos)) {
			finished = true;
			return;
		}
		weaver.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		if (weaver.distanceToSqr(Vec3.atCenterOf(pos)) <= REACH_SQR) {
			weaver.getNavigation().stop();
			weaver.setInspecting(true);
			phase = Phase.INSPECT;
			phaseTicks = 0;
		} else if (weaver.getNavigation().isDone()) {
			weaver.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 2, WALK_SPEED);
		}
	}

	private void tickInspect() {
		BlockPos pos = litter;
		Level level = weaver.level();
		if (pos == null || !WeaverAnchors.isLitter(level, pos)) {
			weaver.setInspecting(false);
			finished = true;
			return;
		}
		weaver.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		if (phaseTicks < INSPECT_TICKS) {
			return;
		}

		weaver.setInspecting(false);
		BlockState state = level.getBlockState(pos);
		boolean keep = WeaverAnchors.isCuriosity(state);
		ItemStack carried = new ItemStack(state.getBlock());
		level.destroyBlock(pos, false);
		weaver.setCarriedItem(carried);
		litter = null;
		phaseTicks = 0;

		net = keep ? findNet() : null;
		phase = net != null ? Phase.DELIVER : Phase.DISCARD;
	}

	private void tickDeliver() {
		BlockPos pos = net;
		if (pos == null || !weaver.level().getBlockState(pos).is(ModBlocks.WEAVER_NET)) {
			net = findNet();
			if (net == null) {
				phase = Phase.DISCARD;
				phaseTicks = 0;
			}
			return;
		}
		weaver.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		if (weaver.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQR) {
			if (weaver.getNavigation().isDone()) {
				Vec3 stand = standBeside(pos);
				weaver.getNavigation().moveTo(stand.x, stand.y, stand.z, 2, WALK_SPEED);
			}
			return;
		}

		weaver.getNavigation().stop();
		ItemStack leftover = stashInNet(pos, weaver.getCarriedItem());
		weaver.setCarriedItem(leftover);
		if (leftover.isEmpty()) {
			finished = true;
			return;
		}
		net = findNet();
		if (net == null) {
			phase = Phase.DISCARD;
			phaseTicks = 0;
		}
	}

	private void tickDiscard() {
		if (phaseTicks < DISCARD_WINDUP) {
			return;
		}
		ItemStack carried = weaver.getCarriedItem();
		if (!carried.isEmpty() && weaver.level() instanceof ServerLevel level) {
			Vec3 look = weaver.getLookAngle();
			double spreadX = (weaver.getRandom().nextDouble() - 0.5) * 0.12;
			double spreadZ = (weaver.getRandom().nextDouble() - 0.5) * 0.12;
			ItemEntity thrown = new ItemEntity(
				level,
				weaver.getX() + look.x * 0.6,
				weaver.getEyeY() - 0.3,
				weaver.getZ() + look.z * 0.6,
				carried.copy(),
				look.x * 0.32 + spreadX,
				0.18,
				look.z * 0.32 + spreadZ
			);
			level.addFreshEntity(thrown);
		}
		weaver.setCarriedItem(ItemStack.EMPTY);
		finished = true;
	}

	private @Nullable BlockPos findLitter() {
		Level level = weaver.level();
		BlockPos origin = weaver.blockPosition();
		if (!WeaverAnchors.isInsideAnchor(level, origin)) {
			return null;
		}

		List<BlockPos> candidates = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
			for (int dy = -SCAN_HEIGHT; dy <= SCAN_HEIGHT; dy++) {
				for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (WeaverAnchors.isLitter(level, cursor)) {
						candidates.add(cursor.immutable());
						if (candidates.size() >= MAX_CANDIDATES) {
							dx = SCAN_RADIUS;
							dy = SCAN_HEIGHT;
							break;
						}
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			return null;
		}

		candidates.sort(Comparator.comparingDouble(pos -> weaver.distanceToSqr(Vec3.atCenterOf(pos))));
		for (BlockPos pos : candidates) {
			if (WeaverAnchors.isInsideAnchor(level, pos)) {
				return pos;
			}
		}
		return null;
	}

	private ItemStack stashInNet(BlockPos netPos, ItemStack stack) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack leftover = tryPlaceInNet(netPos, stack);
		if (leftover.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (weaver.level().getBlockEntity(netPos) instanceof WeaverNetBlockEntity netEntity) {
			return netEntity.stow(leftover);
		}
		return leftover;
	}

	private ItemStack tryPlaceInNet(BlockPos netPos, ItemStack stack) {
		BlockState placed = Block.byItem(stack.getItem()).defaultBlockState();
		if (placed.isAir() || placed.is(ModBlocks.WEAVER_NET)) {
			return stack;
		}
		Level level = weaver.level();
		BlockPos slot = findPhysicalSlot(level, netPos);
		if (slot == null) {
			return stack;
		}
		if (!level.setBlock(slot, placed, 3)) {
			return stack;
		}
		ItemStack leftover = stack.copy();
		leftover.shrink(1);
		return leftover.isEmpty() ? ItemStack.EMPTY : leftover;
	}

	private static @Nullable BlockPos findPhysicalSlot(Level level, BlockPos netPos) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;
		for (int dy = -2; dy <= 1; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) {
						continue;
					}
					cursor.set(netPos.getX() + dx, netPos.getY() + dy, netPos.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!state.isAir() && !state.canBeReplaced()) {
						continue;
					}
					if (state.is(ModBlocks.WEAVER_NET) || state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
						continue;
					}
					if (!WeaverAnchors.isStashedInNet(level, cursor)) {
						continue;
					}
					int score = Math.abs(dx) + Math.abs(dz) + Math.abs(dy + 1) * 3;
					if (score < bestScore) {
						bestScore = score;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	/** Floor next to the net, with room for the body. The weave itself is not a place to stand. */
	private Vec3 standBeside(BlockPos net) {
		Level level = weaver.level();
		BlockPos.MutableBlockPos floor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos beside = net.relative(dir);
			for (int dy = -3; dy <= 1; dy++) {
				floor.set(beside.getX(), net.getY() + dy, beside.getZ());
				if (!openStand(level, floor)) {
					continue;
				}
				double dist = weaver.distanceToSqr(floor.getX() + 0.5, floor.getY() + 1.0, floor.getZ() + 0.5);
				if (dist < bestDist) {
					bestDist = dist;
					best = floor.immutable();
				}
			}
		}
		if (best == null) {
			return Vec3.atCenterOf(net);
		}
		return new Vec3(best.getX() + 0.5, best.getY() + 1.0, best.getZ() + 0.5);
	}

	private static boolean openStand(Level level, BlockPos floor) {
		if (level.getBlockState(floor).getCollisionShape(level, floor).isEmpty()) {
			return false;
		}
		for (int up = 1; up <= 3; up++) {
			BlockPos above = floor.above(up);
			BlockState state = level.getBlockState(above);
			if (state.is(ModBlocks.WEAVER_NET) || !state.getCollisionShape(level, above).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private @Nullable BlockPos findNet() {
		Level level = weaver.level();
		ItemStack carried = weaver.getCarriedItem();
		BlockPos display = null;
		for (BlockPos pos : BlockPos.withinClippedManhattan(weaver.blockPosition(), NET_REACH_XZ, NET_REACH_Y, NET_REACH_XZ)) {
			if (!level.getBlockState(pos).is(ModBlocks.WEAVER_NET)) {
				continue;
			}
			if (findPhysicalSlot(level, pos) != null) {
				return pos.immutable();
			}
			if (display == null
				&& level.getBlockEntity(pos) instanceof WeaverNetBlockEntity netEntity
				&& netEntity.hasRoomFor(carried)) {
				display = pos.immutable();
			}
		}
		return display;
	}
}
