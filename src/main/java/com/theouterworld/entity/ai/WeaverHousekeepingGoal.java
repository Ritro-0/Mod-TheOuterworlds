package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.WeaverNetBlockEntity;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.world.WeaverColonyGifts;
import com.theouterworld.world.WeaverColonySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Anchor housekeeping. A Weaver that finds a block it did not put there walks over, gives it a
 * look, and then tucks it into a net, sets a screen-block down in its own pod, or pitches it
 * off the side. The colony remembers the verdict, so the next Weaver only glances.
 */
public class WeaverHousekeepingGoal extends Goal {
	private static final int SCAN_INTERVAL = 80;
	private static final int SCAN_RADIUS = 6;
	private static final int SCAN_HEIGHT = 4;
	private static final int MAX_CANDIDATES = 12;
	private static final double REACH_SQR = 6.25;
	private static final int INSPECT_TICKS = 40;
	private static final int GLANCE_TICKS = 8;
	private static final int TRAVEL_TIMEOUT = 240;
	private static final int DISCARD_WINDUP = 8;
	private static final double WALK_SPEED = 1.0;
	private static final int PATH_TESTS = 4;

	private enum Phase {
		APPROACH,
		INSPECT,
		DELIVER,
		FURNISH,
		DISCARD
	}

	private final WeaverEntity weaver;

	private Phase phase = Phase.APPROACH;
	private @Nullable BlockPos litter;
	private @Nullable BlockPos net;
	private @Nullable BlockPos furnishAt;
	private int scanCooldown;
	private int phaseTicks;
	private int inspectTicks = INSPECT_TICKS;
	private boolean finished;
	private boolean keep;
	private boolean remembered;
	private boolean triedExpand;
	private @Nullable String inspectKey;
	private @Nullable SpecimenScan specimenScan;

	public WeaverHousekeepingGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (weaver.isOfferingGift() || weaver.isAggressive() || weaver.isRetreating() || weaver.isSleeping()) {
			return false;
		}
		if (!(weaver.level() instanceof ServerLevel)) {
			return false;
		}
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
		if (finished || weaver.isOfferingGift() || weaver.isAggressive() || weaver.isRetreating() || weaver.isSleeping()) {
			return false;
		}
		return switch (phase) {
			case APPROACH -> litter != null && phaseTicks < TRAVEL_TIMEOUT;
			case INSPECT -> litter != null;
			case DELIVER -> net != null && phaseTicks < TRAVEL_TIMEOUT && !weaver.getCarriedItem().isEmpty();
			case FURNISH -> furnishAt != null && phaseTicks < TRAVEL_TIMEOUT && !weaver.getCarriedItem().isEmpty();
			case DISCARD -> !weaver.getCarriedItem().isEmpty();
		};
	}

	@Override
	public void start() {
		finished = false;
		phaseTicks = 0;
		net = null;
		triedExpand = false;
		remembered = false;
		if (weaver.getCarriedItem().isEmpty()) {
			phase = Phase.APPROACH;
			return;
		}
		if (weaver.level() instanceof ServerLevel level
			&& WeaverAnchors.carriedHasScreen(weaver.getCarriedItem(), level, weaver.blockPosition())) {
			keep = false;
			furnishAt = findFurnishSpot(level);
			phase = furnishAt != null ? Phase.FURNISH : Phase.DISCARD;
			return;
		}
		ItemStack carried = weaver.getCarriedItem();
		Block carriedBlock = Block.byItem(carried.getItem());
		boolean specimen = carriedBlock != Blocks.AIR
			&& WeaverAnchors.isSpecimen(carriedBlock.defaultBlockState())
			&& !WeaverAnchors.isCuriosity(carried);
		keep = WeaverAnchors.isCuriosity(carried) || specimen;
		net = keep ? findNet() : null;
		phase = net != null ? Phase.DELIVER : Phase.DISCARD;
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
		weaver.setInspecting(false);
		litter = null;
		net = null;
		furnishAt = null;
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
			case FURNISH -> tickFurnish();
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
			beginInspect(pos);
		} else if (weaver.getNavigation().isDone()) {
			weaver.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 2, WALK_SPEED);
		}
	}

	private void beginInspect(BlockPos pos) {
		weaver.setInspecting(true);
		phase = Phase.INSPECT;
		phaseTicks = 0;
		remembered = false;
		inspectKey = null;
		inspectTicks = INSPECT_TICKS;
		if (!(weaver.level() instanceof ServerLevel level)) {
			return;
		}
		BlockState state = level.getBlockState(pos);
		inspectKey = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		Boolean known = WeaverColonySavedData.get(level).recall(weaver.colonyId(), inspectKey);
		if (known != null) {
			remembered = true;
			keep = known;
			inspectTicks = GLANCE_TICKS;
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
		if (phaseTicks < inspectTicks) {
			return;
		}

		weaver.setInspecting(false);
		BlockState state = level.getBlockState(pos);
		String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		boolean screen = WeaverAnchors.hasScreen(state, level, pos);
		boolean specimen = WeaverAnchors.isSpecimen(state) && !WeaverAnchors.isCuriosity(state);
		boolean shunned = false;
		WeaverColonySavedData data = null;
		if (level instanceof ServerLevel server) {
			data = WeaverColonySavedData.get(server);
			UUID placer = data.placementAt(weaver.colonyId(), pos);
			shunned = placer != null
				&& data.isUntrusted(weaver.colonyId(), placer)
				&& !weaver.personallyTrusts(placer);
		}
		boolean alreadyFurnished = false;
		if (shunned) {
			keep = false;
			screen = false;
			specimen = false;
		} else if (screen) {
			alreadyFurnished = weaver.hasFurnished(key);
			keep = false;
		} else if (specimen) {
			if (data != null && specimenStillHeld((ServerLevel) level, key)) {
				finished = true;
				return;
			}
			keep = true;
		} else if (!remembered || !key.equals(inspectKey)) {
			keep = WeaverAnchors.isCuriosity(state);
			if (data != null) {
				data.remember(weaver.colonyId(), key, keep);
			}
		}
		if (screen) {
			furnishAt = !alreadyFurnished && level instanceof ServerLevel server ? findFurnishSpot(server) : null;
		}

		if (data != null) {
			data.takePlacement(weaver.colonyId(), pos);
		}
		ItemStack carried = new ItemStack(state.getBlock());
		level.destroyBlock(pos, false);
		weaver.setCarriedItem(carried);
		litter = null;
		phaseTicks = 0;
		triedExpand = false;

		if (screen) {
			phase = furnishAt != null ? Phase.FURNISH : Phase.DISCARD;
			return;
		}
		net = keep ? findNet() : null;
		phase = net != null ? Phase.DELIVER : Phase.DISCARD;
	}

	private void tickDeliver() {
		BlockPos pos = net;
		if (!(weaver.level() instanceof ServerLevel level) || pos == null || !level.getBlockState(pos).is(ModBlocks.WEAVER_NET)) {
			net = findNet();
			if (net == null) {
				phase = Phase.DISCARD;
				phaseTicks = 0;
			}
			return;
		}
		weaver.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		Vec3 stand = WeaverColonies.standBeside(level, pos, weaver.getX(), weaver.getY(), weaver.getZ());
		Vec3 reachFrom = stand != null ? stand : Vec3.atCenterOf(pos);
		if (weaver.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQR && weaver.distanceToSqr(reachFrom) > REACH_SQR) {
			if (weaver.getNavigation().isDone()) {
				weaver.getNavigation().moveTo(reachFrom.x, reachFrom.y, reachFrom.z, 2, WALK_SPEED);
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
		if (net != null) {
			phaseTicks = 0;
			return;
		}
		if (!triedExpand) {
			triedExpand = true;
			BlockPos grown = tryExpand(level);
			if (grown != null) {
				net = grown;
				phaseTicks = 0;
				return;
			}
		}
		phase = Phase.DISCARD;
		phaseTicks = 0;
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

	private void tickFurnish() {
		if (!(weaver.level() instanceof ServerLevel level) || furnishAt == null) {
			phase = Phase.DISCARD;
			phaseTicks = 0;
			return;
		}
		BlockPos spot = furnishAt;
		BlockState there = level.getBlockState(spot);
		if (!there.isAir() && !there.canBeReplaced()) {
			furnishAt = findFurnishSpot(level);
			if (furnishAt == null) {
				phase = Phase.DISCARD;
				phaseTicks = 0;
			}
			return;
		}
		weaver.getLookControl().setLookAt(spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5);
		Vec3 stand = WeaverColonies.standBeside(level, spot, weaver.getX(), weaver.getY(), weaver.getZ());
		Vec3 reachFrom = stand != null ? stand : Vec3.atCenterOf(spot);
		if (weaver.distanceToSqr(Vec3.atCenterOf(spot)) > REACH_SQR && weaver.distanceToSqr(reachFrom) > REACH_SQR) {
			if (weaver.getNavigation().isDone()) {
				weaver.getNavigation().moveTo(reachFrom.x, reachFrom.y, reachFrom.z, 2, WALK_SPEED);
			}
			return;
		}

		weaver.getNavigation().stop();
		ItemStack carried = weaver.getCarriedItem();
		Block block = Block.byItem(carried.getItem());
		if (block == Blocks.AIR || !level.setBlock(spot, block.defaultBlockState(), 3)) {
			phase = Phase.DISCARD;
			phaseTicks = 0;
			return;
		}
		String key = BuiltInRegistries.BLOCK.getKey(block).toString();
		WeaverColonySavedData.get(level).noteFurniture(weaver.colonyId(), key, spot);
		weaver.markFurnished(key);
		weaver.setCarriedItem(ItemStack.EMPTY);
		finished = true;
	}

	private boolean shouldLeave(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel server)) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		return WeaverAnchors.isSpecimen(state)
			&& !WeaverAnchors.isCuriosity(state)
			&& specimenStillHeld(server, key);
	}

	private boolean specimenStillHeld(ServerLevel level, String key) {
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		if (!data.holdsSpecimen(weaver.colonyId(), key)) {
			return false;
		}
		SpecimenScan scan = specimensInReach(level);
		if (!scan.sawNet || scan.held.contains(key)) {
			return true;
		}
		data.forgetSpecimen(weaver.colonyId(), key);
		return false;
	}

	private SpecimenScan specimensInReach(ServerLevel level) {
		if (specimenScan != null) {
			return specimenScan;
		}
		SpecimenScan scan = new SpecimenScan();
		for (BlockPos pos : WeaverColonies.findNets(level, weaver.blockPosition(), weaver.colonyId())) {
			scan.sawNet = true;
			if (!(level.getBlockEntity(pos) instanceof WeaverNetBlockEntity net)) {
				continue;
			}
			for (int slot = 0; slot < WeaverNetBlockEntity.SIZE; slot++) {
				ItemStack stack = net.getItem(slot);
				Block block = Block.byItem(stack.getItem());
				if (block == Blocks.AIR || !WeaverAnchors.isSpecimen(block.defaultBlockState())) {
					continue;
				}
				if (WeaverAnchors.isCuriosity(block.defaultBlockState())) {
					continue;
				}
				scan.held.add(BuiltInRegistries.BLOCK.getKey(block).toString());
			}
		}
		specimenScan = scan;
		return scan;
	}

	/** A floor cell in this Weaver's pod, behind the bed rather than in the doorway. */
	private @Nullable BlockPos findFurnishSpot(ServerLevel level) {
		if (!weaver.hasHome()) {
			return null;
		}
		BlockPos bed = weaver.getHomePosition();
		BlockPos plate = weaver.getHomePlate();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -5; dx <= 5; dx++) {
				for (int dz = -5; dz <= 5; dz++) {
					BlockPos spot = bed.offset(dx, dy, dz);
					if (!canFurnish(level, spot, bed, plate)) {
						continue;
					}
					double dist = spot.distSqr(bed);
					if (dist < bestDist) {
						bestDist = dist;
						best = spot.immutable();
					}
				}
			}
		}
		return best;
	}

	private boolean canFurnish(Level level, BlockPos spot, BlockPos bed, @Nullable BlockPos plate) {
		BlockState state = level.getBlockState(spot);
		if (state.liquid() || (!state.isAir() && !state.canBeReplaced())) {
			return false;
		}
		BlockPos belowPos = spot.below();
		BlockState below = level.getBlockState(belowPos);
		if (below.getCollisionShape(level, belowPos).isEmpty()) {
			return false;
		}
		if (below.is(ModBlocks.WEAVER_PAD) || below.is(ModBlocks.WEAVER_NET) || below.is(ModBlocks.THOLIN_FIBER_HOME_PLATE)) {
			return false;
		}
		if (spot.equals(bed)) {
			return false;
		}
		if (plate != null && (spot.equals(plate) || belowPos.equals(plate))) {
			return false;
		}
		return WeaverAnchors.isInsideAnchor(level, spot) || below.is(ModBlocks.THOLIN_FIBER);
	}

	private @Nullable BlockPos findLitter() {
		Level level = weaver.level();
		BlockPos origin = weaver.blockPosition();
		if (!WeaverAnchors.isInsideAnchor(level, origin)) {
			return null;
		}

		specimenScan = null;
		List<BlockPos> candidates = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
			for (int dy = -SCAN_HEIGHT; dy <= SCAN_HEIGHT; dy++) {
				for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (WeaverAnchors.isLitter(level, cursor) && !shouldLeave(level, cursor)) {
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
		if (stack.isEmpty() || !(weaver.level() instanceof ServerLevel level)) {
			return stack;
		}
		if (!(level.getBlockEntity(netPos) instanceof WeaverNetBlockEntity netEntity)) {
			return stack;
		}
		int before = stack.getCount();
		ItemStack leftover = netEntity.stow(stack);
		int stored = before - leftover.getCount();
		if (stored > 0 && keep && WeaverAnchors.isCuriosity(stack)) {
			ItemStack scored = stack.copy();
			scored.setCount(stored);
			WeaverColonySavedData.get(level).addContribution(level, weaver.colonyId(), WeaverColonyGifts.valueOf(scored));
		} else if (stored > 0 && keep) {
			Block block = Block.byItem(stack.getItem());
			if (block != Blocks.AIR && WeaverAnchors.isSpecimen(block.defaultBlockState())) {
				String key = BuiltInRegistries.BLOCK.getKey(block).toString();
				WeaverColonySavedData.get(level).noteSpecimen(weaver.colonyId(), key);
			}
		}
		return leftover;
	}

	private @Nullable BlockPos findNet() {
		if (!(weaver.level() instanceof ServerLevel level)) {
			return null;
		}
		ItemStack carried = weaver.getCarriedItem();
		List<BlockPos> nets = WeaverColonies.findNets(level, weaver.blockPosition(), weaver.colonyId());
		nets.sort(Comparator.comparingDouble(pos -> weaver.distanceToSqr(Vec3.atCenterOf(pos))));
		int tested = 0;
		for (BlockPos pos : nets) {
			if (!(level.getBlockEntity(pos) instanceof WeaverNetBlockEntity netEntity) || !netEntity.hasRoomFor(carried)) {
				continue;
			}
			Vec3 stand = WeaverColonies.standBeside(level, pos, weaver.getX(), weaver.getY(), weaver.getZ());
			if (stand == null) {
				continue;
			}
			if (weaver.distanceToSqr(stand) <= 36.0 || (tested < PATH_TESTS && canReach(BlockPos.containing(stand)))) {
				return pos;
			}
			tested++;
		}
		return null;
	}

	private boolean canReach(BlockPos feet) {
		Path path = weaver.getNavigation().createPath(feet, 1);
		return path != null && path.canReach();
	}

	/**
	 * Spins one new net onto an existing colony net, beside a walkway the Weaver can stand on.
	 * One success per colony per cooldown, and never past the colony cap.
	 */
	private @Nullable BlockPos tryExpand(ServerLevel level) {
		long id = weaver.colonyId();
		List<BlockPos> nets = WeaverColonies.findNets(level, weaver.blockPosition(), id);
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		if (!data.canExpand(id, level.getGameTime(), nets.size())) {
			return null;
		}
		int checks = 0;
		int paths = 0;
		for (BlockPos origin : nets) {
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				BlockPos[] candidates = {origin.relative(dir), origin.relative(dir).below(), origin.below()};
				for (BlockPos candidate : candidates) {
					if (++checks > 48) {
						return null;
					}
					if (!canHang(level, candidate)) {
						continue;
					}
					Vec3 stand = WeaverColonies.standBeside(level, candidate, weaver.getX(), weaver.getY(), weaver.getZ());
					if (stand == null) {
						continue;
					}
					if (paths >= PATH_TESTS) {
						return null;
					}
					paths++;
					if (!canReach(BlockPos.containing(stand))) {
						continue;
					}
					if (!level.setBlock(candidate, ModBlocks.WEAVER_NET.defaultBlockState(), 3)) {
						continue;
					}
					if (!(level.getBlockEntity(candidate) instanceof WeaverNetBlockEntity placed)) {
						level.removeBlock(candidate, false);
						continue;
					}
					placed.setColonyId(id);
					data.markExpanded(id, level.getGameTime());
					return candidate.immutable();
				}
			}
		}
		return null;
	}

	private boolean canHang(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.liquid() || state.hasBlockEntity() || state.is(ModBlocks.WEAVER_NET)) {
			return false;
		}
		if (!state.isAir() && !state.canBeReplaced()) {
			return false;
		}
		boolean attached = false;
		for (Direction dir : Direction.values()) {
			if (level.getBlockState(pos.relative(dir)).is(ModBlocks.WEAVER_NET)) {
				attached = true;
				break;
			}
		}
		if (!attached || !WeaverColonies.nearWalkway(level, pos, WeaverColonies.WALKWAY_RANGE)) {
			return false;
		}
		return !WeaverColonies.blocksWalkway(level, pos) && !WeaverColonies.inLeapShaft(level, pos);
	}

	private static final class SpecimenScan {
		private boolean sawNet;
		private final Set<String> held = new HashSet<>();
	}
}
