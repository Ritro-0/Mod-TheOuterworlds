package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.WeaverPadBlock;
import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.StrawBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * At night a Weaver finds open sky near its home plate, leaps well above it,
 * and falls onto the plate before walking in to the claimed bed.
 */
public class WeaverSleepGoal extends Goal {
	private static final int SEARCH_XZ = 16;
	private static final int SEARCH_Y = 10;
	private static final int PLATE_SEARCH = 16;
	private static final int SKY_RADIUS = 12;
	private static final double REACH_SQR = 6.25;
	private static final double APPROACH_SQR = (double) WeaverHomeLeap.APPROACH_XZ * WeaverHomeLeap.APPROACH_XZ;
	private static final double UNDER_PLATE_SQR = 6.25;
	private static final int TRAVEL_TIMEOUT = 1200;
	private static final double WALK_SPEED = 1.05;

	private final WeaverEntity weaver;
	private @Nullable BlockPos bed;
	private @Nullable BlockPos sky;
	private int ticks;
	private int leapCooldown;
	private boolean onDeck;

	public WeaverSleepGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (weaver.isAggressive() || weaver.isRetreating() || !weaver.getCarriedItem().isEmpty()) {
			return false;
		}
		if (weaver.isSleeping()) {
			return true;
		}
		if (!weaver.level().isDarkOutside()) {
			return false;
		}
		bed = resolveBed();
		return bed != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (weaver.isAggressive() || weaver.isRetreating()) {
			return false;
		}
		if (weaver.isSleeping()) {
			return weaver.level().isDarkOutside();
		}
		return weaver.level().isDarkOutside() && (weaver.isHomeLeaping() || ticks < TRAVEL_TIMEOUT);
	}

	@Override
	public void start() {
		ticks = 0;
		leapCooldown = 0;
		onDeck = false;
		weaver.setFlatApproach(false);
		if (bed == null) {
			bed = resolveBed();
		}
	}

	@Override
	public void stop() {
		onDeck = false;
		weaver.releaseDeck();
		weaver.setFlatApproach(false);
		weaver.getNavigation().stop();
		if (weaver.isHomeLeaping()) {
			weaver.endHomeLeap();
		}
		if (weaver.isSleeping() && weaver.level().isBrightOutside()) {
			weaver.stopSleeping();
		}
		bed = null;
		sky = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		ticks++;
		if (weaver.isSleeping()) {
			if (weaver.level().isBrightOutside()) {
				weaver.stopSleeping();
			}
			return;
		}

		BlockPos bunk = bed != null ? bed : resolveBed();
		if (bunk == null) {
			return;
		}
		bed = bunk;
		weaver.getLookControl().setLookAt(bunk.getX() + 0.5, bunk.getY() + 0.2, bunk.getZ() + 0.5);

		BlockPos plate = resolvePlate(bunk);
		if (plate == null) {
			walkIntoBed(bunk);
			return;
		}

		if (weaver.isHomeLeaping()) {
			if (!weaver.onGround()) {
				weaver.steerHomeLeap(plate);
				return;
			}
			weaver.endHomeLeap();
			leapCooldown = 10;
			weaver.bindToDeck(bunk);
			onDeck = true;
		}

		if (weaver.isDeckBound()) {
			walkTheDeck(bunk);
			return;
		}

		if (standingOn(plate) || closeEnoughToSleep(bunk)) {
			walkIntoBed(bunk);
			return;
		}
		if (leapCooldown > 0) {
			leapCooldown--;
			return;
		}

		double dx = plate.getX() + 0.5 - weaver.getX();
		double dz = plate.getZ() + 0.5 - weaver.getZ();
		if (dx * dx + dz * dz > APPROACH_SQR) {
			sky = null;
			double ox = bunk.getX() - plate.getX();
			double oz = bunk.getZ() - plate.getZ();
			double len = Math.sqrt(ox * ox + oz * oz);
			if (len < 0.5) {
				ox = 1.0;
				oz = 0.0;
				len = 1.0;
			}
			walk(plate.getX() + 0.5 + ox / len * 8.0, weaver.getY(), plate.getZ() + 0.5 + oz / len * 8.0);
			return;
		}

		if (sky == null || ticks % 30 == 0 || !hasSkylight(sky)) {
			BlockPos found = findSkylight(plate);
			if (found != null) {
				sky = found;
			}
		}
		if (sky != null && atPad(sky, plate)) {
			weaver.beginHomeLeap(plate);
			sky = null;
			return;
		}
		if (sky != null) {
			walk(sky.getX() + 0.5, sky.getY(), sky.getZ() + 0.5);
			return;
		}
		stepOutFromUnder(plate);
	}

	private void walkIntoBed(BlockPos bunk) {
		if (closeEnoughToSleep(bunk)) {
			settleInto(bunk);
			return;
		}
		walk(bunk.getX() + 0.5, weaver.getY(), bunk.getZ() + 0.5);
	}

	/** Pathfinding stays off. MoveControl steps them toward the bed without a jump. */
	private void walkTheDeck(BlockPos bunk) {
		weaver.bindToDeck(bunk);
		weaver.getNavigation().stop();
		if (!closeEnoughToSleep(bunk)) {
			return;
		}
		settleInto(bunk);
	}

	/** Lie down only in a bunk this Weaver owns, and never on top of someone already there. */
	private void settleInto(BlockPos bunk) {
		Level level = weaver.level();
		BlockState state = level.getBlockState(bunk);
		boolean pad = state.getBlock() instanceof WeaverPadBlock;
		if (!(state.getBlock() instanceof StrawBedBlock)
			|| !WeaverPadBlock.availableTo(level, bunk, weaver.getUUID())
			|| (!pad && WeaverEntity.isBedClaimed(level, bunk, weaver))) {
			weaver.releaseDeck();
			onDeck = false;
			weaver.releaseHome();
			bed = null;
			return;
		}
		if (state.getValue(AbstractBedBlock.OCCUPIED)) {
			if (WeaverPadBlock.heldByOther(level, bunk, weaver)) {
				return;
			}
			level.setBlockAndUpdate(bunk, state.setValue(AbstractBedBlock.OCCUPIED, false));
		}
		if (weaver.startSleeping(bunk)) {
			weaver.claimBed(bunk, resolvePlate(bunk));
			weaver.releaseDeck();
			onDeck = false;
			weaver.getNavigation().stop();
		}
	}

	private boolean closeEnoughToSleep(BlockPos bunk) {
		return weaver.distanceToSqr(bunk.getX() + 0.5, bunk.getY(), bunk.getZ() + 0.5) <= REACH_SQR;
	}

	private boolean standingOn(BlockPos plate) {
		double dx = plate.getX() + 0.5 - weaver.getX();
		double dz = plate.getZ() + 0.5 - weaver.getZ();
		return dx * dx + dz * dz <= 1.7 && Math.abs(weaver.getY() - (plate.getY() + 1.0)) <= 1.35;
	}

	private boolean atPad(BlockPos pad, BlockPos plate) {
		return weaver.onGround()
			&& hasSkylight(weaver.blockPosition())
			&& !underPlate(weaver.blockPosition(), plate)
			&& Math.abs(weaver.getY() - pad.getY()) <= 1.25
			&& weaver.distanceToSqr(pad.getX() + 0.5, weaver.getY(), pad.getZ() + 0.5) <= 2.0;
	}

	private void stepOutFromUnder(BlockPos plate) {
		double dx = weaver.getX() - (plate.getX() + 0.5);
		double dz = weaver.getZ() - (plate.getZ() + 0.5);
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len < 0.5) {
			dx = 1.0;
			dz = 0.0;
			len = 1.0;
		}
		walk(weaver.getX() + dx / len * 6.0, weaver.getY(), weaver.getZ() + dz / len * 6.0);
	}

	/** Open ground near the plate, never the column directly beneath it. */
	private @Nullable BlockPos findSkylight(BlockPos plate) {
		BlockPos here = weaver.blockPosition();
		if (hasSkylight(here) && !underPlate(here, plate)) {
			return here;
		}
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int dx = -SKY_RADIUS; dx <= SKY_RADIUS; dx += 2) {
			for (int dz = -SKY_RADIUS; dz <= SKY_RADIUS; dz += 2) {
				if (dx * dx + dz * dz > SKY_RADIUS * SKY_RADIUS) {
					continue;
				}
				int x = here.getX() + dx;
				int z = here.getZ() + dz;
				if (underPlate(x, z, plate)) {
					continue;
				}
				double fromPlate = (x + 0.5 - plate.getX() - 0.5) * (x + 0.5 - plate.getX() - 0.5)
					+ (z + 0.5 - plate.getZ() - 0.5) * (z + 0.5 - plate.getZ() - 0.5);
				if (fromPlate > APPROACH_SQR) {
					continue;
				}
				int feetY = weaver.getBlockY();
				for (int y = feetY + 6; y >= feetY - 12; y--) {
					cursor.set(x, y, z);
					if (!hasSkylight(cursor)) {
						continue;
					}
					double score = weaver.distanceToSqr(x + 0.5, y, z + 0.5) + Math.abs(y - feetY) * 4.0;
					if (score < bestScore) {
						bestScore = score;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	private boolean hasSkylight(BlockPos feet) {
		Level level = weaver.level();
		if (!level.canSeeSky(feet)) {
			return false;
		}
		BlockPos ground = feet.below();
		if (level.getBlockState(ground).getCollisionShape(level, ground).isEmpty()) {
			return false;
		}
		for (int i = 0; i < 3; i++) {
			BlockPos air = feet.above(i);
			if (!level.getBlockState(air).getCollisionShape(level, air).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean underPlate(BlockPos feet, BlockPos plate) {
		return underPlate(feet.getX(), feet.getZ(), plate);
	}

	private boolean underPlate(int x, int z, BlockPos plate) {
		double dx = x + 0.5 - plate.getX() - 0.5;
		double dz = z + 0.5 - plate.getZ() - 0.5;
		return dx * dx + dz * dz < UNDER_PLATE_SQR;
	}

	private void walk(double x, double y, double z) {
		if (weaver.getNavigation().isDone() || weaver.getNavigation().isStuck()) {
			weaver.getNavigation().moveTo(x, y, z, 2, WALK_SPEED);
		}
	}

	private @Nullable BlockPos resolveBed() {
		if (weaver.isHomeReachable()) {
			BlockPos home = weaver.getHomePosition();
			Level level = weaver.level();
			if (!level.isLoaded(home)) {
				return home;
			}
			BlockState state = level.getBlockState(home);
			boolean pad = state.getBlock() instanceof WeaverPadBlock;
			if (state.getBlock() instanceof StrawBedBlock
				&& WeaverPadBlock.availableTo(level, home, weaver.getUUID())
				&& (pad || !WeaverEntity.isBedClaimed(level, home, weaver))) {
				return home;
			}
			weaver.releaseHome();
		}
		return findUnclaimedBed();
	}

	private @Nullable BlockPos resolvePlate(BlockPos bunk) {
		Level level = weaver.level();
		BlockPos saved = weaver.getHomePlate();
		if (saved != null && (!level.isLoaded(saved) || level.getBlockState(saved).is(ModBlocks.THOLIN_FIBER_HOME_PLATE))) {
			return saved;
		}
		BlockPos found = findHomePlate(level, bunk);
		weaver.setHomePlate(found);
		return found;
	}

	private @Nullable BlockPos findHomePlate(Level level, BlockPos bunk) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dx = -PLATE_SEARCH; dx <= PLATE_SEARCH; dx++) {
			for (int dy = -SEARCH_Y; dy <= SEARCH_Y; dy++) {
				for (int dz = -PLATE_SEARCH; dz <= PLATE_SEARCH; dz++) {
					cursor.set(bunk.getX() + dx, bunk.getY() + dy, bunk.getZ() + dz);
					if (!level.getBlockState(cursor).is(ModBlocks.THOLIN_FIBER_HOME_PLATE)) {
						continue;
					}
					double dist = cursor.distSqr(bunk);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	private @Nullable BlockPos findUnclaimedBed() {
		Level level = weaver.level();
		BlockPos origin = weaver.blockPosition();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dx = -SEARCH_XZ; dx <= SEARCH_XZ; dx++) {
			for (int dy = -SEARCH_Y; dy <= SEARCH_Y; dy++) {
				for (int dz = -SEARCH_XZ; dz <= SEARCH_XZ; dz++) {
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!(state.getBlock() instanceof StrawBedBlock)) {
						continue;
					}
					if (state.hasProperty(BlockStateProperties.BED_PART)
						&& state.getValue(BlockStateProperties.BED_PART) != BedPart.FOOT) {
						continue;
					}
					if (!WeaverPadBlock.availableTo(level, cursor, weaver.getUUID())) {
						continue;
					}
					if (!(state.getBlock() instanceof WeaverPadBlock) && WeaverEntity.isBedClaimed(level, cursor, weaver)) {
						continue;
					}
					double dist = weaver.distanceToSqr(cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		if (best != null && weaver.claimBed(best, findHomePlate(level, best))) {
			return best;
		}
		return null;
	}
}
