package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.TholinStalkBlock;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.registry.ModEntities;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A pad opened by the morning head count sends one Weaver to a wild stalk
 * (or they wait where they picked stalks up). A different Weaver home-leaps
 * beside them, and only then do they share, spin, and backflip.
 */
public class WeaverBreedGoal extends Goal {
	private static final int SHARE_TICKS = 28;
	private static final int SPIN_TICKS = 36;
	private static final int FLIP_TIMEOUT = 50;
	private static final int APPROACH_TIMEOUT = 800;
	private static final double WALK_SPEED = 1.05;

	private final WeaverEntity weaver;
	private @Nullable WeaverEntity partner;
	private WeaverHomes.@Nullable Vacancy vacancy;
	private @Nullable BlockPos stalk;
	private boolean harvestTip;
	private int phaseTicks;
	private int lastPhase;
	private boolean leftGround;
	private boolean launched;
	private boolean consumed;
	private int leapRetries;
	private boolean harvestLeaped;
	private boolean harvestLeftGround;
	private static long seekStamp;
	private static long seekColony;
	private static TholinStalkBlock.@Nullable WildStand seekStand;
	private int scanPause;

	public WeaverBreedGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.weaver.isBreeding()) {
			if (interrupted(this.weaver)) {
				abortPair(this.weaver);
				return false;
			}
			return true;
		}
		if (this.weaver.isLeashed() || WeaverSchedule.isBedtime(this.weaver.level())) {
			return false;
		}
		if (this.scanPause > 0) {
			this.scanPause--;
			return false;
		}
		if (!freeToCourt(this.weaver) || !(this.weaver.level() instanceof ServerLevel level)) {
			return false;
		}
		if (WeaverRollCall.isGathering(this.weaver.colonyId())) {
			this.scanPause = 20;
			return false;
		}
		if (colonyAlreadyCourting(level, this.weaver)) {
			this.scanPause = 20;
			return false;
		}
		WeaverHomes.Vacancy open = WeaverHomes.findVacancy(level, this.weaver);
		if (open == null) {
			this.scanPause = 40;
			return false;
		}
		if (this.weaver.getStalkCount() < 2 && WeaverStalkPickupGoal.nearby(this.weaver, WeaverStalkPickupGoal.SEEK)) {
			this.scanPause = 10;
			return false;
		}
		if (colonyHasFood(level)) {
			if (this.weaver.getStalkCount() < 2 || !isBestFed(level)) {
				this.scanPause = 20;
				return false;
			}
			if (!WeaverHomes.reserve(open.bed(), this.weaver.getUUID())) {
				this.scanPause = 20;
				return false;
			}
			this.vacancy = open;
			this.stalk = this.weaver.blockPosition();
			this.harvestTip = false;
			return true;
		}
		TholinStalkBlock.WildStand stand = seekStand(level, this.weaver);
		if (stand == null || !isClosestTo(level, stand.pos())) {
			this.scanPause = 30;
			return false;
		}
		if (!stand.mature() && this.weaver.getStalkCount() < 2) {
			this.scanPause = 30;
			return false;
		}
		if (!WeaverHomes.reserve(open.bed(), this.weaver.getUUID())) {
			this.scanPause = 20;
			return false;
		}
		this.vacancy = open;
		this.stalk = stand.pos();
		this.harvestTip = stand.mature();
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.weaver.isBreeding() && !interrupted(this.weaver);
	}

	/**
	 * A lead drags them, so courting stops instead of fighting it. Night sends
	 * everyone home; only a dance already under way between two adjacent Weavers finishes.
	 */
	private static boolean interrupted(WeaverEntity weaver) {
		if (weaver.isLeashed()) {
			return true;
		}
		WeaverEntity partner = weaver.getBreedPartner();
		if (partner != null && partner.isLeashed()) {
			return true;
		}
		if (!WeaverSchedule.isBedtime(weaver.level())) {
			return false;
		}
		int phase = weaver.getBreedPhase();
		return phase != WeaverEntity.BREED_SHARE
			&& phase != WeaverEntity.BREED_SPIN
			&& phase != WeaverEntity.BREED_FLIP;
	}

	private static void abortPair(WeaverEntity weaver) {
		WeaverEntity partner = weaver.getBreedPartner();
		if (weaver.isHomeLeaping() && weaver.leapKind() == WeaverEntity.LeapKind.COURT) {
			weaver.endHomeLeap();
		}
		weaver.abortBreeding();
		if (partner != null && partner.isBreeding()) {
			if (partner.isHomeLeaping() && partner.leapKind() == WeaverEntity.LeapKind.COURT) {
				partner.endHomeLeap();
			}
			partner.abortBreeding();
		}
	}

	@Override
	public void start() {
		if (this.weaver.isBreeding()) {
			this.partner = this.weaver.getBreedPartner();
			this.vacancy = this.weaver.getBreedVacancy();
			this.stalk = this.weaver.getCourtStalk();
			return;
		}
		WeaverHomes.Vacancy open = this.vacancy;
		BlockPos tip = this.stalk;
		if (open == null || tip == null) {
			WeaverHomes.release(open == null ? null : open.bed(), this.weaver.getUUID());
			return;
		}
		this.phaseTicks = 0;
		this.lastPhase = 0;
		this.leftGround = false;
		this.launched = false;
		this.consumed = false;
		this.leapRetries = 0;
		this.harvestLeaped = false;
		this.harvestLeftGround = false;
		this.weaver.beginStalkCourt(open, tip, this.harvestTip);
	}

	@Override
	public void stop() {
		if (this.weaver.isBreeding() && interrupted(this.weaver)) {
			abortPair(this.weaver);
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
		int phase = this.weaver.getBreedPhase();
		if (phase != this.lastPhase) {
			this.phaseTicks = 0;
			this.leftGround = false;
			this.launched = false;
			this.lastPhase = phase;
		}
		if (!this.weaver.isBreedLeader()) {
			tickArrival(level);
			return;
		}
		if (phase == WeaverEntity.BREED_APPROACH) {
			tickHarvest(level);
		} else if (phase == WeaverEntity.BREED_HOLD) {
			tickHold(level);
		} else if (phase == WeaverEntity.BREED_SHARE) {
			tickShare(level);
		} else if (phase == WeaverEntity.BREED_SPIN) {
			tickSpin();
		} else if (phase == WeaverEntity.BREED_FLIP) {
			tickFlip(level);
		}
	}

	private void tickHarvest(ServerLevel level) {
		BlockPos tip = this.stalk != null ? this.stalk : this.weaver.getCourtStalk();
		this.stalk = tip;
		if (tip == null || !isWildTip(level, tip)) {
			TholinStalkBlock.WildStand again = TholinStalkBlock.nearestWild(
				level, this.weaver.blockPosition(), WeaverEntity.STALK_COURT_RANGE
			);
			if (again == null) {
				this.weaver.abortBreeding();
				this.scanPause = 40;
				return;
			}
			WeaverHomes.Vacancy open = this.weaver.getBreedVacancy();
			if (open == null) {
				this.weaver.abortBreeding();
				return;
			}
			this.stalk = again.pos();
			boolean harvest = again.mature() || this.weaver.getStalkCount() < 2;
			this.weaver.beginStalkCourt(open, again.pos(), harvest);
			if (!harvest) {
				return;
			}
			tip = again.pos();
		}
		double dx = tip.getX() + 0.5 - this.weaver.getX();
		double dz = tip.getZ() + 0.5 - this.weaver.getZ();
		double dy = tip.getY() - this.weaver.getY();
		if (!this.harvestLeaped && dx * dx + dz * dz > 18.0 * 18.0) {
			BlockPos landing = findLanding(level, tip, this.weaver);
			if (landing != null) {
				this.harvestLeaped = true;
				this.harvestLeftGround = false;
				if (!this.weaver.beginTravelLeap(landing)) {
					this.harvestLeaped = false;
					return;
				}
				return;
			}
		}
		if (this.harvestLeaped && this.weaver.isHomeLeaping()) {
			BlockPos landing = this.weaver.getCourtLanding();
			if (landing != null) {
				this.weaver.steerHomeLeap(landing);
			}
			if (!this.weaver.onGround()) {
				this.harvestLeftGround = true;
			}
			if (this.harvestLeftGround && this.weaver.onGround()) {
				this.weaver.endHomeLeap();
				this.weaver.clearCourtLanding();
			}
			return;
		}
		if (dx * dx + dz * dz <= 2.4 * 2.4 && Math.abs(dy) <= 3.0) {
			int got = TholinStalkBlock.pluck(level, tip, false);
			if (got > 0) {
				this.weaver.addStalks(level, got);
			}
			if (this.weaver.getStalkCount() < 2) {
				this.weaver.abortBreeding();
				this.scanPause = 40;
				return;
			}
			this.weaver.getNavigation().stop();
			this.weaver.setBreedPhase(WeaverEntity.BREED_HOLD);
			return;
		}
		if (++this.phaseTicks > APPROACH_TIMEOUT) {
			this.weaver.abortBreeding();
			this.scanPause = 40;
			return;
		}
		this.weaver.getNavigation().moveTo(tip.getX() + 0.5, tip.getY(), tip.getZ() + 0.5, WALK_SPEED);
	}

	private void tickHold(ServerLevel level) {
		this.weaver.getNavigation().stop();
		this.weaver.setZza(0.0F);
		Vec3 motion = this.weaver.getDeltaMovement();
		this.weaver.setDeltaMovement(motion.x * 0.2, motion.y, motion.z * 0.2);
		WeaverEntity other = this.partner != null ? this.partner : this.weaver.getBreedPartner();
		this.partner = other;
		if (other == null || !other.isAlive() || other.level() != level || !other.isBreeding()) {
			this.partner = null;
			if (this.phaseTicks < 3 || this.weaver.tickCount % 10 == 0) {
				assignLeaper(level);
			}
			this.phaseTicks++;
			return;
		}
		this.weaver.getLookControl().setLookAt(other, 40.0F, 40.0F);
	}

	private void assignLeaper(ServerLevel level) {
		WeaverHomes.Vacancy open = this.vacancy != null ? this.vacancy : this.weaver.getBreedVacancy();
		BlockPos tip = this.stalk != null ? this.stalk : this.weaver.getCourtStalk();
		if (open == null || tip == null) {
			this.weaver.abortBreeding();
			return;
		}
		WeaverEntity leaper = nearestLeaper(level);
		BlockPos landing = leaper == null ? null : findLanding(level, tip, leaper);
		if (leaper == null || landing == null) {
			return;
		}
		this.partner = leaper;
		this.vacancy = open;
		leaper.beginCourtLeap(this.weaver, open, landing);
	}

	private void tickArrival(ServerLevel level) {
		WeaverEntity lead = this.weaver.getBreedPartner();
		if (lead == null || !lead.isAlive() || lead.level() != level) {
			this.weaver.abortBreeding();
			return;
		}
		int phase = this.weaver.getBreedPhase();
		if (phase == WeaverEntity.BREED_LEAP) {
			BlockPos landing = this.weaver.getCourtLanding();
			if (landing == null) {
				this.weaver.abortBreeding();
				lead.abortBreeding();
				return;
			}
			this.weaver.getNavigation().stop();
			this.weaver.getLookControl().setLookAt(lead, 40.0F, 40.0F);
			if (this.weaver.isHomeLeaping()) {
				this.weaver.steerHomeLeap(landing);
				if (!this.weaver.onGround()) {
					this.leftGround = true;
				}
				boolean landed = this.leftGround && this.weaver.onGround();
				if (!landed && ++this.phaseTicks <= 400) {
					return;
				}
				this.weaver.endHomeLeap();
			}
			if (!this.leftGround) {
				if (!this.weaver.beginTravelLeap(landing)) {
					this.weaver.abortBreeding();
					lead.abortBreeding();
				}
				return;
			}
			if (!this.weaver.onGround()) {
				return;
			}
			if (!besideEachOther(this.weaver, lead)) {
				BlockPos again = landingBeside(level, lead);
				if (again == null || this.leapRetries >= 3) {
					this.weaver.abortBreeding();
					lead.abortBreeding();
					return;
				}
				this.leapRetries++;
				this.leftGround = false;
				this.phaseTicks = 0;
				this.weaver.beginTravelLeap(again);
				return;
			}
			lead.setBreedPhase(WeaverEntity.BREED_SHARE);
			this.weaver.setBreedPhase(WeaverEntity.BREED_SHARE);
			return;
		}
		this.weaver.getNavigation().stop();
		this.weaver.getLookControl().setLookAt(lead, 40.0F, 40.0F);
		if (phase == WeaverEntity.BREED_SPIN) {
			this.weaver.setDeltaMovement(0.0, this.weaver.getDeltaMovement().y, 0.0);
			float yaw = Mth.wrapDegrees(this.weaver.getYRot() + 52.0F * this.weaver.getBreedSpin());
			this.weaver.setYRot(yaw);
			this.weaver.setYHeadRot(yaw);
			this.weaver.yBodyRot = yaw;
		}
	}

	private void tickShare(ServerLevel level) {
		WeaverEntity other = this.partner != null ? this.partner : this.weaver.getBreedPartner();
		this.partner = other;
		if (other == null || !other.isAlive()) {
			this.weaver.abortBreeding();
			return;
		}
		if (!besideEachOther(this.weaver, other)) {
			this.weaver.abortBreeding();
			other.abortBreeding();
			return;
		}
		hearts(level);
		this.weaver.getNavigation().stop();
		other.getNavigation().stop();
		face(other);
		if (++this.phaseTicks < SHARE_TICKS) {
			if (!this.weaver.isBreedFoodShown()) {
				holdFood(this.weaver);
				holdFood(other);
			}
			return;
		}
		if (!this.consumed) {
			if (!this.weaver.takeStalks(2)) {
				this.weaver.abortBreeding();
				other.abortBreeding();
				return;
			}
			this.consumed = true;
		}
		clearFood(this.weaver);
		clearFood(other);
		this.weaver.setBreedPhase(WeaverEntity.BREED_SPIN);
		other.setBreedPhase(WeaverEntity.BREED_SPIN);
	}

	private void tickSpin() {
		WeaverEntity other = this.partner != null ? this.partner : this.weaver.getBreedPartner();
		this.partner = other;
		if (other == null || !other.isAlive()) {
			this.weaver.abortBreeding();
			return;
		}
		this.weaver.getNavigation().stop();
		this.weaver.setDeltaMovement(0.0, this.weaver.getDeltaMovement().y, 0.0);
		float yaw = Mth.wrapDegrees(this.weaver.getYRot() + 52.0F * this.weaver.getBreedSpin());
		this.weaver.setYRot(yaw);
		this.weaver.setYHeadRot(yaw);
		this.weaver.yBodyRot = yaw;
		if (++this.phaseTicks < SPIN_TICKS) {
			return;
		}
		this.weaver.setBreedPhase(WeaverEntity.BREED_FLIP);
		other.setBreedPhase(WeaverEntity.BREED_FLIP);
		this.weaver.launchBackflip();
		other.launchBackflip();
		this.launched = true;
	}

	private void tickFlip(ServerLevel level) {
		WeaverEntity other = this.partner != null ? this.partner : this.weaver.getBreedPartner();
		this.partner = other;
		if (other == null || !other.isAlive()) {
			this.weaver.abortBreeding();
			return;
		}
		if (!this.launched) {
			this.weaver.launchBackflip();
			other.launchBackflip();
			this.launched = true;
		}
		if (!this.weaver.onGround()) {
			this.leftGround = true;
		}
		float progress = Mth.clamp(this.phaseTicks / 16.0F, 0.0F, 1.0F);
		this.weaver.setFlipProgress(progress);
		other.setFlipProgress(progress);
		this.phaseTicks++;
		boolean landed = this.leftGround && this.weaver.onGround();
		if (!landed && this.phaseTicks < FLIP_TIMEOUT) {
			return;
		}
		boom(level, other);
	}

	private void boom(ServerLevel level, WeaverEntity other) {
		double x = (this.weaver.getX() + other.getX()) * 0.5;
		double y = this.weaver.getY() + 0.4;
		double z = (this.weaver.getZ() + other.getZ()) * 0.5;
		level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.75F, 1.2F);
		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
		level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 10, 0.5, 0.35, 0.5, 0.02);
		WeaverHomes.Vacancy open = this.weaver.getBreedVacancy();
		spawnBaby(level, x, this.weaver.getY(), z, open);
		this.weaver.finishBreeding();
		other.finishBreeding();
	}

	private void spawnBaby(ServerLevel level, double x, double y, double z, WeaverHomes.@Nullable Vacancy open) {
		WeaverEntity baby = ModEntities.WEAVER.create(level, EntitySpawnReason.BREEDING);
		if (baby == null) {
			if (open != null) {
				WeaverHomes.release(open.bed(), this.weaver.getUUID());
			}
			return;
		}
		baby.snapTo(x, y, z, this.weaver.getYRot(), 0.0F);
		baby.setPersistenceRequired();
		baby.assignColony(this.weaver.colonyId());
		baby.setBaby(true);
		if (open != null) {
			baby.assignIntendedHome(open.bed(), open.plate(), this.weaver.getUUID());
		}
		level.addFreshEntity(baby);
		if (open != null && open.plate() != null) {
			baby.beginHomeLeap(open.plate());
		}
	}

	private void hearts(ServerLevel level) {
		if (this.weaver.tickCount % 8 != 0) {
			return;
		}
		level.sendParticles(ParticleTypes.HEART, this.weaver.getX(), this.weaver.getY() + 1.3, this.weaver.getZ(), 1, 0.25, 0.25, 0.25, 0.0);
	}

	private void face(WeaverEntity other) {
		double dx = other.getX() - this.weaver.getX();
		double dz = other.getZ() - this.weaver.getZ();
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		this.weaver.setYRot(yaw);
		this.weaver.yBodyRot = yaw;
		this.weaver.setYHeadRot(yaw);
	}

	private boolean isClosestTo(ServerLevel level, BlockPos tip) {
		double mine = this.weaver.distanceToSqr(tip.getX() + 0.5, tip.getY(), tip.getZ() + 0.5);
		long colony = this.weaver.colonyId();
		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof WeaverEntity other) || other == this.weaver || !other.isAlive()) {
				continue;
			}
			if (other.colonyId() != colony || !freeToCourt(other)) {
				continue;
			}
			double dist = other.distanceToSqr(tip.getX() + 0.5, tip.getY(), tip.getZ() + 0.5);
			if (dist + 0.01 < mine) {
				return false;
			}
		}
		return true;
	}

	private boolean colonyHasFood(ServerLevel level) {
		long colony = this.weaver.colonyId();
		for (Entity entity : level.getAllEntities()) {
			if (entity instanceof WeaverEntity other
				&& other.isAlive()
				&& other.colonyId() == colony
				&& other.level() == level
				&& freeToCourt(other)
				&& !other.isBreeding()
				&& other.getStalkCount() >= 2) {
				return true;
			}
		}
		return false;
	}

	private boolean isBestFed(ServerLevel level) {
		int mine = this.weaver.getStalkCount();
		if (mine < 2) {
			return false;
		}
		BlockPos center = WeaverColonies.centerOf(level, this.weaver.colonyId(), this.weaver.blockPosition());
		double mineDist = center == null
			? 0.0
			: this.weaver.distanceToSqr(center.getX() + 0.5, this.weaver.getY(), center.getZ() + 0.5);
		long colony = this.weaver.colonyId();
		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof WeaverEntity other) || other == this.weaver || !other.isAlive()) {
				continue;
			}
			if (other.colonyId() != colony || other.level() != level || !freeToCourt(other) || other.isBreeding()) {
				continue;
			}
			int theirs = other.getStalkCount();
			if (theirs > mine) {
				return false;
			}
			if (theirs < mine || center == null) {
				continue;
			}
			double dist = other.distanceToSqr(center.getX() + 0.5, other.getY(), center.getZ() + 0.5);
			if (dist + 0.01 < mineDist) {
				return false;
			}
		}
		return true;
	}

	private static TholinStalkBlock.@Nullable WildStand seekStand(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		long stamp = level.getGameTime() / 40L;
		if (seekColony == colony && seekStamp == stamp) {
			return seekStand;
		}
		BlockPos origin = WeaverColonies.centerOf(level, colony, weaver.blockPosition());
		if (origin == null) {
			origin = weaver.blockPosition();
		}
		TholinStalkBlock.WildStand local = TholinStalkBlock.nearestWild(level, weaver.blockPosition(), WeaverEntity.STALK_COURT_RANGE);
		TholinStalkBlock.WildStand wide = TholinStalkBlock.nearestWildSurface(level, origin, WeaverEntity.STALK_SEEK_RANGE);
		if (wide != null && wide.mature()) {
			seekStand = wide;
		} else if (local != null && (local.mature() || wide == null)) {
			seekStand = local;
		} else {
			seekStand = wide != null ? wide : local;
		}
		seekColony = colony;
		seekStamp = stamp;
		return seekStand;
	}

	private @Nullable WeaverEntity nearestLeaper(ServerLevel level) {
		long colony = this.weaver.colonyId();
		WeaverEntity nearest = null;
		double nearestSqr = Double.MAX_VALUE;
		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof WeaverEntity other) || other == this.weaver || !other.isAlive()) {
				continue;
			}
			if (other.colonyId() != colony || other.level() != level || !freeToCourt(other) || other.isBreeding()) {
				continue;
			}
			double dist = this.weaver.distanceToSqr(other);
			if (dist < nearestSqr) {
				nearest = other;
				nearestSqr = dist;
			}
		}
		return nearest;
	}

	private static boolean colonyAlreadyCourting(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		for (Entity entity : level.getAllEntities()) {
			if (entity instanceof WeaverEntity other
				&& other != weaver
				&& other.isAlive()
				&& other.colonyId() == colony
				&& other.isBreedLeader()
				&& other.isBreeding()) {
				return true;
			}
		}
		return false;
	}

	private static boolean freeToCourt(WeaverEntity weaver) {
		return !weaver.isBaby()
			&& !weaver.isAggressive()
			&& !weaver.isRetreating()
			&& !weaver.isSleeping()
			&& !weaver.isOfferingGift()
			&& !weaver.isVengeanceLeaping()
			&& weaver.getCarriedItem().isEmpty();
	}

	private static boolean isWildTip(ServerLevel level, BlockPos tip) {
		BlockState state = level.getBlockState(tip);
		return state.is(ModBlocks.THOLIN_STALK)
			&& state.getValue(TholinStalkBlock.MATURE)
			&& state.getValue(TholinStalkBlock.WILD)
			&& level.getBlockState(tip.above()).isAir();
	}

	/** Close enough to hand food over: a few blocks apart and standing on the same level. */
	private static boolean besideEachOther(WeaverEntity a, WeaverEntity b) {
		double dx = a.getX() - b.getX();
		double dz = a.getZ() - b.getZ();
		return a.onGround()
			&& dx * dx + dz * dz <= 3.5 * 3.5
			&& Math.abs(a.getY() - b.getY()) <= 1.5;
	}

	/** Dry ground at the lead's own level, within two blocks of them. */
	private static @Nullable BlockPos landingBeside(ServerLevel level, WeaverEntity lead) {
		BlockPos feet = lead.blockPosition();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}
				for (int dy = -2; dy <= 0; dy++) {
					cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
					BlockState ground = level.getBlockState(cursor);
					if (ground.is(ModBlocks.THOLIN_STALK) || ground.getCollisionShape(level, cursor).isEmpty()) {
						continue;
					}
					if (!WeaverLeapSpot.isDry(level, cursor)) {
						continue;
					}
					BlockPos stand = cursor.above();
					if (!level.getBlockState(stand).getCollisionShape(level, stand).isEmpty()
						|| !level.getBlockState(stand.above()).getCollisionShape(level, stand.above()).isEmpty()) {
						continue;
					}
					double dist = lead.distanceToSqr(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	/** Solid ground beside the stalk, with room for a Weaver to land. */
	private static @Nullable BlockPos findLanding(ServerLevel level, BlockPos stalk, WeaverEntity leaper) {
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}
				for (int dy = -3; dy <= 2; dy++) {
					cursor.set(stalk.getX() + dx, stalk.getY() + dy, stalk.getZ() + dz);
					BlockState ground = level.getBlockState(cursor);
					if (ground.is(ModBlocks.THOLIN_STALK) || ground.getCollisionShape(level, cursor).isEmpty()) {
						continue;
					}
					if (!WeaverLeapSpot.isDry(level, cursor)) {
						continue;
					}
					BlockPos feet = cursor.above();
					if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
						continue;
					}
					if (!level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) {
						continue;
					}
					if (!level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty()) {
						continue;
					}
					double dist = leaper.distanceToSqr(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		if (best != null) {
			return best;
		}
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos ground = stalk.below().relative(direction);
			if (!level.getBlockState(ground).getCollisionShape(level, ground).isEmpty()
				&& level.getBlockState(ground.above()).getCollisionShape(level, ground.above()).isEmpty()
				&& WeaverLeapSpot.isDry(level, ground)) {
				return ground.immutable();
			}
		}
		return null;
	}

	private static void holdFood(WeaverEntity weaver) {
		weaver.setCarriedItem(new ItemStack(ModBlocks.THOLIN_STALK));
		weaver.setBreedFoodShown(true);
	}

	private static void clearFood(WeaverEntity weaver) {
		if (weaver.isBreedFoodShown()) {
			weaver.setCarriedItem(ItemStack.EMPTY);
			weaver.setBreedFoodShown(false);
		}
	}
}
