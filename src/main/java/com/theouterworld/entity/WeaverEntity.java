package com.theouterworld.entity;

import com.theouterworld.block.WeaverPadBlock;
import com.theouterworld.entity.ai.WeaverColonies;
import com.theouterworld.entity.ai.WeaverAvoidPlayerGoal;
import com.theouterworld.entity.ai.WeaverBabyHomeGoal;
import com.theouterworld.entity.ai.WeaverBreedGoal;
import com.theouterworld.entity.ai.WeaverGiftGoal;
import com.theouterworld.entity.ai.WeaverHarvestStalkGoal;
import com.theouterworld.entity.ai.WeaverHomes;
import com.theouterworld.entity.ai.WeaverLookAtPlayerGoal;
import com.theouterworld.entity.ai.WeaverHomeLeap;
import com.theouterworld.entity.ai.WeaverLeapSpot;
import com.theouterworld.entity.ai.WeaverHousekeepingGoal;
import com.theouterworld.entity.ai.WeaverLeapAttackGoal;
import com.theouterworld.entity.ai.WeaverMoveControl;
import com.theouterworld.entity.ai.WeaverPathNavigation;
import com.theouterworld.entity.ai.WeaverRetreatGoal;
import com.theouterworld.entity.ai.WeaverReturnHomeGoal;
import com.theouterworld.entity.ai.WeaverSchedule;
import com.theouterworld.entity.ai.WeaverSleepGoal;
import com.theouterworld.entity.ai.WeaverStalkPickupGoal;
import com.theouterworld.entity.ai.WeaverVengeanceLeapGoal;
import com.theouterworld.entity.ai.WeaverKharaxReceptionGoal;
import com.theouterworld.entity.ai.WeaverWanderGoal;
import com.theouterworld.registry.ModSounds;
import com.theouterworld.world.WeaverAbsence;
import com.theouterworld.world.WeaverColonySavedData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.block.ModBlocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Neutral Amberworld insect. Packs wander until provoked, then each nearby weaver
 * charges, strikes once, and retreats on its own.
 */
public class WeaverEntity extends PathfinderMob {
	public static final float PACK_RANGE = 16.0F;
	public static final int RETREAT_MIN = 10;
	public static final int RETREAT_MAX = 18;
	/** Player jump strength is 0.42; twice the velocity is four times the height. */
	public static final double JUMP_STRENGTH = 0.84;
	public static final int WANDER_RADIUS = 36;
	public static final int HOME_ABANDON_DISTANCE = 512;
	/** How far a colony will travel from its centre to reach a wild stalk. */
	public static final int STALK_SEEK_RANGE = 96;
	public static final int STALK_CAP = 64;
	public static final int BREED_APPROACH = 1;
	public static final int BREED_SHARE = 2;
	public static final int BREED_SPIN = 3;
	public static final int BREED_FLIP = 4;
	/** Harvester is standing at a wild stalk, waiting for the other Weaver. */
	public static final int BREED_HOLD = 5;
	/** The other Weaver is in the home-plate leap toward that stalk. */
	public static final int BREED_LEAP = 6;
	public static final int STALK_COURT_RANGE = 50;
	/** A baby who has been kindly known for about half a minute, or handed a gift, grows up fond. */
	private static final int FOND_REGARD = 30;
	private static final int ADULT_AGE = 24000;
	private static final double MAX_PATH_JUMP = 10.0;
	private static final double MIN_HOP_BLOCKS = 1.15;
	private static final double AIR_DRAG = 0.91;
	private static final double GROUND_FRICTION = 0.6;
	private static final double MAX_LAUNCH_PUSH = 1.15;
	private static final int LEAP_RECOVERY_TICKS = 4;
	private static final float LEAP_POSE_SMOOTHING = 0.35F;
	private static final float AIRBORNE_SMOOTHING = 0.3F;
	private static final float INSPECT_SMOOTHING = 0.18F;

	private static final EntityDataAccessor<Boolean> DATA_AGGRESSIVE = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<Boolean> DATA_INSPECTING = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<ItemStack> DATA_CARRIED = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.ITEM_STACK
	);
	private static final EntityDataAccessor<Boolean> DATA_OFFERING = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<Boolean> DATA_BABY = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<Integer> DATA_BREED_PHASE = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.INT
	);
	private static final EntityDataAccessor<Float> DATA_FLIP = SynchedEntityData.defineId(
		WeaverEntity.class,
		EntityDataSerializers.FLOAT
	);

	private boolean aggressive;
	private boolean retreating;
	private @Nullable LivingEntity lastThreat;

	private float leapPose;
	private float leapPoseO;
	private float airborneAmount;
	private float airborneAmountO;
	private float inspectAmount;
	private float inspectAmountO;
	private float pendingJumpPower = -1.0F;
	private @Nullable ResourceKey<Level> homeDimension;
	private @Nullable BlockPos homePlate;
	private long colonyId;
	private final Set<String> furnishedBlocks = new HashSet<>();
	/** Last colony notice this Weaver has already heard. Older breaks stay queued until load. */
	private long furnitureCursor;
	private boolean homeLeaping;
	private LeapKind leapKind = LeapKind.NONE;
	private @Nullable BlockPos leapTarget;
	private boolean flatApproach;
	/** After the home-plate leap, pathfinding and jumping stay off until sleep. */
	private boolean deckBound;
	private @Nullable BlockPos deckTarget;
	private int stalkCount;
	private int babyAge;
	private boolean breedLeader;
	private float breedSpin = 1.0F;
	private boolean breedFoodShown;
	private @Nullable WeaverEntity breedPartner;
	private WeaverHomes.@Nullable Vacancy breedVacancy;
	private @Nullable BlockPos courtStalk;
	private @Nullable BlockPos courtLanding;
	private @Nullable BlockPos gatherLanding;
	private @Nullable BlockPos intendedBed;
	private @Nullable BlockPos intendedPlate;
	private @Nullable UUID bedReserver;
	private final Set<UUID> fondOf = new HashSet<>();
	private final Set<UUID> scorned = new HashSet<>();
	private final Set<UUID> bloodFeud = new HashSet<>();
	private final Map<UUID, Integer> regard = new HashMap<>();
	private boolean vengeanceLeap;
	private @Nullable UUID vengeancePlayer;
	/** Neutral Weavers flinch this long before a hit they can still forgive. */
	private int panicTicks;
	private @Nullable UUID receptionKharax;
	private @Nullable BlockPos receptionStand;
	private boolean receptionRecoiling;
	private boolean receptionPunched;
	private boolean receptionStruck;
	private int suppressPackAlertTicks;

	public WeaverEntity(EntityType<? extends WeaverEntity> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		this.moveControl = new WeaverMoveControl(this);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.MOVEMENT_SPEED, 0.28)
			.add(Attributes.FOLLOW_RANGE, 64.0)
			.add(Attributes.ATTACK_DAMAGE, 4.0)
			.add(Attributes.STEP_HEIGHT, 1.0)
			.add(Attributes.JUMP_STRENGTH, JUMP_STRENGTH)
			.add(Attributes.SAFE_FALL_DISTANCE, 12.0);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WeaverPathNavigation(this, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_AGGRESSIVE, false);
		builder.define(DATA_INSPECTING, false);
		builder.define(DATA_CARRIED, ItemStack.EMPTY);
		builder.define(DATA_OFFERING, false);
		builder.define(DATA_BABY, false);
		builder.define(DATA_BREED_PHASE, 0);
		builder.define(DATA_FLIP, 0.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(0, new WeaverKharaxReceptionGoal(this));
		this.goalSelector.addGoal(1, new WeaverBreedGoal(this));
		this.goalSelector.addGoal(1, new WeaverVengeanceLeapGoal(this));
		this.goalSelector.addGoal(1, new WeaverLeapAttackGoal(this));
		this.goalSelector.addGoal(2, new WeaverRetreatGoal(this));
		this.goalSelector.addGoal(3, new WeaverGiftGoal(this));
		this.goalSelector.addGoal(4, new WeaverBabyHomeGoal(this));
		this.goalSelector.addGoal(5, new WeaverSleepGoal(this));
		this.goalSelector.addGoal(6, new WeaverHousekeepingGoal(this));
		this.goalSelector.addGoal(7, new WeaverStalkPickupGoal(this));
		this.goalSelector.addGoal(7, new WeaverHarvestStalkGoal(this));
		this.goalSelector.addGoal(8, new WeaverReturnHomeGoal(this));
		this.goalSelector.addGoal(9, new WeaverAvoidPlayerGoal(this));
		this.goalSelector.addGoal(10, new WeaverWanderGoal(this));
		this.goalSelector.addGoal(11, new WeaverLookAtPlayerGoal(this));
		this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		if (this.level() instanceof ServerLevel serverLevel) {
			if (this.suppressPackAlertTicks > 0) {
				this.suppressPackAlertTicks--;
			}
			if (this.panicTicks > 0) {
				this.panicTicks--;
			}
			this.catchUpFurnishings();
			this.tickBloodFeud();
			if ((this.tickCount + this.getId()) % 20 == 0) {
				WeaverHomes.reconcile(serverLevel, this);
			}
		}
		super.tick();
		if (!this.level().isClientSide()) {
			if (this.isSleeping() && !WeaverSchedule.isBedtime(this.level())) {
				this.stopSleeping();
			}
			tickBaby();
			abandonHomeIfLost();
			stepOffBed();
			if (this.deckBound) {
				this.setJumping(false);
				this.getNavigation().stop();
			}
		}
		updateLeapPose();
		updateInspectPose();
	}

	private void updateLeapPose() {
		this.leapPoseO = this.leapPose;
		this.airborneAmountO = this.airborneAmount;
		boolean grounded = this.onGround();
		float airTarget = grounded ? 0.0F : 1.0F;
		float poseTarget = grounded ? 0.0F : Mth.clamp((float) this.getDeltaMovement().y * 2.5F, -1.0F, 1.0F);
		this.airborneAmount += (airTarget - this.airborneAmount) * AIRBORNE_SMOOTHING;
		this.leapPose += (poseTarget - this.leapPose) * LEAP_POSE_SMOOTHING;
	}

	public float getLeapPose(float tickProgress) {
		return Mth.lerp(tickProgress, this.leapPoseO, this.leapPose);
	}

	public float getAirborneAmount(float tickProgress) {
		return Mth.lerp(tickProgress, this.airborneAmountO, this.airborneAmount);
	}

	private void updateInspectPose() {
		this.inspectAmountO = this.inspectAmount;
		float target = this.entityData.get(DATA_INSPECTING) ? 1.0F : 0.0F;
		this.inspectAmount += (target - this.inspectAmount) * INSPECT_SMOOTHING;
	}

	public float getInspectAmount(float tickProgress) {
		return Mth.lerp(tickProgress, this.inspectAmountO, this.inspectAmount);
	}

	public boolean isInspecting() {
		return this.entityData.get(DATA_INSPECTING);
	}

	public void setInspecting(boolean inspecting) {
		this.entityData.set(DATA_INSPECTING, inspecting);
	}

	public ItemStack getCarriedItem() {
		return this.entityData.get(DATA_CARRIED);
	}

	public void setCarriedItem(ItemStack stack) {
		this.entityData.set(DATA_CARRIED, stack.copy());
	}

	public boolean isOfferingGift() {
		return this.entityData.get(DATA_OFFERING);
	}

	public void setOfferingGift(boolean offering) {
		this.entityData.set(DATA_OFFERING, offering);
	}

	@Override
	public boolean isBaby() {
		return this.entityData.get(DATA_BABY);
	}

	public void setBaby(boolean baby) {
		this.entityData.set(DATA_BABY, baby);
		if (baby) {
			this.babyAge = Math.max(this.babyAge, 1);
		}
		this.refreshDimensions();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (DATA_BABY.equals(key)) {
			this.refreshDimensions();
		}
		super.onSyncedDataUpdated(key);
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		EntityDimensions dimensions = super.getDefaultDimensions(pose);
		// The model still draws at half size. The box stays close to adult so a gift can be clicked.
		return this.isBaby() ? dimensions.scale(0.85F) : dimensions;
	}

	@Override
	public float getPickRadius() {
		return this.isBaby() ? 0.65F : super.getPickRadius();
	}

	public int getStalkCount() {
		return this.stalkCount;
	}

	/** @return stalks that did not fit and should be dropped */
	public int addStalks(ServerLevel level, int count) {
		if (count <= 0) {
			return 0;
		}
		int room = STALK_CAP - this.stalkCount;
		int kept = Math.min(room, count);
		this.stalkCount += kept;
		int overflow = count - kept;
		if (overflow > 0) {
			this.spawnAtLocation(level, new ItemStack(ModBlocks.THOLIN_STALK, overflow));
		}
		return overflow;
	}

	public boolean takeStalks(int count) {
		if (count <= 0 || this.stalkCount < count) {
			return false;
		}
		this.stalkCount -= count;
		return true;
	}

	public boolean canStartBreeding() {
		return this.stalkCount > 1
			&& !this.isBaby()
			&& !this.isBreeding()
			&& !this.isAggressive()
			&& !this.isRetreating()
			&& !this.isSleeping()
			&& !this.isOfferingGift()
			&& this.getCarriedItem().isEmpty();
	}

	public boolean isBreeding() {
		return this.getBreedPhase() != 0;
	}

	public int getBreedPhase() {
		return this.entityData.get(DATA_BREED_PHASE);
	}

	public void setBreedPhase(int phase) {
		this.entityData.set(DATA_BREED_PHASE, phase);
	}

	public float getBreedSpin() {
		return this.breedSpin;
	}

	public float getFlipProgress() {
		return this.entityData.get(DATA_FLIP);
	}

	public void setFlipProgress(float progress) {
		this.entityData.set(DATA_FLIP, progress);
	}

	public boolean isBreedLeader() {
		return this.breedLeader;
	}

	public boolean isBreedFoodShown() {
		return this.breedFoodShown;
	}

	public void setBreedFoodShown(boolean shown) {
		this.breedFoodShown = shown;
	}

	public @Nullable WeaverEntity getBreedPartner() {
		if (this.breedPartner != null && this.breedPartner.isAlive()) {
			return this.breedPartner;
		}
		return null;
	}

	public WeaverHomes.@Nullable Vacancy getBreedVacancy() {
		return this.breedVacancy;
	}

	public @Nullable BlockPos getCourtStalk() {
		return this.courtStalk;
	}

	public @Nullable BlockPos getCourtLanding() {
		return this.courtLanding;
	}

	/** Drops sleep, combat, and travel so a replacement courtship can start immediately. */
	public void dropGoalsForBreeding() {
		if (this.isSleeping()) {
			this.stopSleeping();
		}
		this.retreating = false;
		this.aggressive = false;
		this.entityData.set(DATA_AGGRESSIVE, false);
		this.vengeanceLeap = false;
		this.vengeancePlayer = null;
		this.setTarget(null);
		this.setFlatApproach(true);
		this.getNavigation().stop();
		if (this.isHomeLeaping() && this.leapKind != LeapKind.COURT) {
			this.endHomeLeap();
		}
	}

	/** Walk to a wild stalk and harvest it, or wait there when the food is already in hand. */
	public void beginStalkCourt(WeaverHomes.Vacancy vacancy, BlockPos stalk, boolean harvest) {
		this.dropGoalsForBreeding();
		this.breedPartner = null;
		this.breedVacancy = vacancy;
		this.breedLeader = true;
		this.breedSpin = 1.0F;
		this.courtStalk = stalk.immutable();
		this.courtLanding = null;
		this.setFlipProgress(0.0F);
		this.setBreedPhase(harvest ? BREED_APPROACH : BREED_HOLD);
	}

	/** Home-plate leap onto a block beside the Weaver who is waiting at the stalks. */
	public void beginCourtLeap(WeaverEntity leader, WeaverHomes.Vacancy vacancy, BlockPos landing) {
		this.dropGoalsForBreeding();
		this.breedPartner = leader;
		this.breedVacancy = vacancy;
		this.breedLeader = false;
		this.breedSpin = -1.0F;
		this.courtStalk = leader.courtStalk == null ? null : leader.courtStalk.immutable();
		this.courtLanding = landing.immutable();
		this.setFlipProgress(0.0F);
		this.setBreedPhase(BREED_LEAP);
		leader.breedPartner = this;
		this.getNavigation().stop();
		if (!this.launchLeap(LeapKind.COURT, landing)) {
			this.courtLanding = null;
			return;
		}
		this.courtLanding = this.leapTarget;
	}

	/** Already within hopping range, so they walk the rest of the way. */
	public void beginCourtApproach(WeaverEntity leader, WeaverHomes.Vacancy vacancy) {
		this.dropGoalsForBreeding();
		this.breedPartner = leader;
		this.breedVacancy = vacancy;
		this.breedLeader = false;
		this.breedSpin = -1.0F;
		this.courtStalk = leader.courtStalk == null ? null : leader.courtStalk.immutable();
		this.courtLanding = null;
		this.setFlipProgress(0.0F);
		this.setBreedPhase(BREED_LEAP);
		leader.breedPartner = this;
		this.getNavigation().stop();
	}

	/** Already standing near the harvester, so the courtship skips the leap. */
	public void beginCourtBeside(WeaverEntity leader, WeaverHomes.Vacancy vacancy) {
		this.dropGoalsForBreeding();
		this.breedPartner = leader;
		this.breedVacancy = vacancy;
		this.breedLeader = false;
		this.breedSpin = -1.0F;
		this.courtStalk = leader.courtStalk == null ? null : leader.courtStalk.immutable();
		this.courtLanding = null;
		this.setFlipProgress(0.0F);
		this.setBreedPhase(BREED_SHARE);
		leader.breedPartner = this;
		leader.setBreedPhase(BREED_SHARE);
		this.getNavigation().stop();
		leader.getNavigation().stop();
	}

	public void launchBackflip() {
		this.setDeltaMovement(0.0, this.getAttributeValue(Attributes.JUMP_STRENGTH), 0.0);
		this.syncVelocity = true;
	}

	public void finishBreeding() {
		this.clearBreedPose();
		this.breedVacancy = null;
	}

	public void abortBreeding() {
		WeaverHomes.Vacancy vacancy = this.breedVacancy;
		if (this.breedLeader && vacancy != null) {
			WeaverHomes.release(vacancy.bed(), this.getUUID());
		}
		this.clearBreedPose();
		this.breedVacancy = null;
	}

	private void clearBreedPose() {
		if (this.breedFoodShown) {
			this.setCarriedItem(ItemStack.EMPTY);
			this.breedFoodShown = false;
		}
		this.breedLeader = false;
		this.breedPartner = null;
		this.courtStalk = null;
		this.courtLanding = null;
		this.setFlatApproach(false);
		this.setBreedPhase(0);
		this.setFlipProgress(0.0F);
	}

	public void assignIntendedHome(BlockPos bed, @Nullable BlockPos plate, UUID reserver) {
		this.intendedBed = bed.immutable();
		this.intendedPlate = plate == null ? null : plate.immutable();
		this.bedReserver = reserver;
	}

	public @Nullable BlockPos getIntendedBed() {
		return this.intendedBed;
	}

	public @Nullable BlockPos getIntendedPlate() {
		return this.intendedPlate;
	}

	public void setIntendedPlate(@Nullable BlockPos plate) {
		this.intendedPlate = plate == null ? null : plate.immutable();
	}

	public void clearIntendedHome() {
		WeaverHomes.release(this.intendedBed, this.bedReserver);
		this.intendedBed = null;
		this.intendedPlate = null;
		this.bedReserver = null;
	}

	/** This Weaver personally likes the player and will not turn on them. */
	public boolean personallyTrusts(UUID player) {
		return player != null && this.fondOf.contains(player);
	}

	public boolean colonyHasBaby(ServerLevel level) {
		if (!this.isAlive()) {
			return false;
		}
		AABB box = this.getBoundingBox().inflate(128.0, 64.0, 128.0);
		long id = this.colonyId();
		for (WeaverEntity other : level.getEntitiesOfClass(WeaverEntity.class, box, Mob::isAlive)) {
			if (other != this && other.isBaby() && other.colonyId() == id) {
				return true;
			}
		}
		return false;
	}

	private void tickBaby() {
		if (!this.isBaby() || !(this.level() instanceof ServerLevel level)) {
			return;
		}
		this.babyAge++;
		if (this.tickCount % 20 == 0) {
			long id = this.colonyId();
			if (id != 0L) {
				WeaverColonySavedData.get(level).noteBaby(id, level.getGameTime());
			}
			this.growRegard(level);
		}
		if (this.babyAge >= ADULT_AGE) {
			this.growUp();
		}
	}

	private void growRegard(ServerLevel level) {
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		long id = this.colonyId();
		for (Player player : level.players()) {
			if (!player.isAlive() || player.isSpectator() || this.scorned.contains(player.getUUID())) {
				continue;
			}
			if (this.distanceToSqr(player) > 64.0) {
				continue;
			}
			if (id != 0L && data.isWary(id, player.getUUID())) {
				continue;
			}
			this.regard.merge(player.getUUID(), 1, Integer::sum);
		}
	}

	private void addRegard(UUID player, int amount) {
		if (player == null || this.scorned.contains(player) || !this.isBaby()) {
			return;
		}
		this.regard.merge(player, amount, Integer::sum);
	}

	private void scorn(UUID player) {
		if (player == null) {
			return;
		}
		this.scorned.add(player);
		this.regard.remove(player);
		this.fondOf.remove(player);
	}

	private void growUp() {
		for (Map.Entry<UUID, Integer> entry : this.regard.entrySet()) {
			if (entry.getValue() >= FOND_REGARD && !this.scorned.contains(entry.getKey())) {
				this.fondOf.add(entry.getKey());
			}
		}
		this.regard.clear();
		this.scorned.clear();
		this.babyAge = 0;
		this.setBaby(false);
	}

	/** Stable Anchor id. Generated Weavers keep the one they were spun with; others resolve it from position. */
	public long colonyId() {
		if (this.colonyId != 0L) {
			return this.colonyId;
		}
		if (this.level() instanceof ServerLevel server) {
			this.colonyId = WeaverColonies.idAt(server, this.blockPosition());
		}
		return this.colonyId;
	}

	public boolean hasFurnished(String blockKey) {
		return this.furnishedBlocks.contains(blockKey);
	}

	public void markFurnished(String blockKey) {
		this.furnishedBlocks.add(blockKey);
	}

	public void forgetFurnished(String blockKey) {
		this.furnishedBlocks.remove(blockKey);
	}

	public void retainFurnished(Set<String> stillThere) {
		this.furnishedBlocks.retainAll(stillThere);
	}

	public long furnitureCursor() {
		return this.furnitureCursor;
	}

	public void setFurnitureCursor(long cursor) {
		this.furnitureCursor = cursor;
	}

	/** Apply break notices the colony saved while this Weaver was away, or earlier this tick. */
	public void catchUpFurnishings() {
		if (this.colonyId == 0L || !(this.level() instanceof ServerLevel server)) {
			return;
		}
		WeaverColonySavedData.get(server).deliverFurnitureNotices(this.colonyId, this);
	}

	public void assignColony(long id) {
		if (id != 0L) {
			this.colonyId = id;
		}
	}

	public boolean hasKharaxReception() {
		return this.receptionKharax != null;
	}

	public @Nullable UUID getReceptionKharaxId() {
		return this.receptionKharax;
	}

	public @Nullable BlockPos getReceptionStand() {
		return this.receptionStand;
	}

	public boolean isReceptionRecoiling() {
		return this.receptionRecoiling;
	}

	public boolean hasReceptionPunched() {
		return this.receptionPunched;
	}

	public boolean wasReceptionStruck() {
		return this.receptionStruck;
	}

	public void markReceptionPunched() {
		this.receptionPunched = true;
	}

	/** Leave the current chore and come stand off to one side of this Kharax. */
	public void joinKharaxReception(KharaxEntity kharax, BlockPos stand) {
		if (this.isSleeping()) {
			this.stopSleeping();
		}
		if (this.isBreeding()) {
			WeaverEntity partner = this.getBreedPartner();
			this.abortBreeding();
			if (partner != null && partner.isBreeding()) {
				partner.abortBreeding();
			}
		}
		this.releaseDeck();
		this.setFlatApproach(false);
		this.retreating = false;
		this.aggressive = false;
		this.setTarget(null);
		this.entityData.set(DATA_AGGRESSIVE, false);
		this.vengeanceLeap = false;
		this.vengeancePlayer = null;
		this.setInspecting(false);
		this.getNavigation().stop();
		if (this.isHomeLeaping() && !this.isHomePlateLeap()) {
			this.endHomeLeap();
		}
		this.receptionKharax = kharax.getUUID();
		this.receptionStand = stand.immutable();
		this.receptionRecoiling = false;
		this.receptionPunched = false;
		this.receptionStruck = false;
		this.suppressPackAlertTicks = 400;
		this.launchReceptionLeap(stand);
	}

	/**
	 * One home-style arc to the stand. A night return already in the air is left alone;
	 * this flight is a gather leap, so fibre floors and the home plate still catch a Weaver.
	 */
	public void launchReceptionLeap(BlockPos stand) {
		if (this.isHomePlateLeap()) {
			return;
		}
		if (this.distanceToSqr(Vec3.atBottomCenterOf(stand)) <= 6.0) {
			return;
		}
		if (this.isHomeLeaping()) {
			this.endHomeLeap();
		}
		this.beginGatherLeap(stand.below());
	}

	/** Keep steering whatever leap is already in the air, without changing its target. */
	public void steerCurrentLeap() {
		if (this.leapTarget != null) {
			this.steerHomeLeap(this.leapTarget);
		}
	}

	public void beginReceptionRecoil() {
		this.receptionRecoiling = true;
		this.receptionPunched = false;
		this.suppressPackAlertTicks = 200;
		this.setInspecting(false);
		this.getNavigation().stop();
	}

	/** Back on the inspection mark. The circle is still gathered. */
	public void resumeReceptionInspect() {
		this.receptionRecoiling = false;
		this.receptionPunched = false;
		this.receptionStruck = false;
		this.setInspecting(true);
		this.getNavigation().stop();
	}

	public void clearKharaxReception() {
		if (this.leapKind == LeapKind.GATHER) {
			this.endGatherLeap();
		}
		this.receptionKharax = null;
		this.receptionStand = null;
		this.receptionRecoiling = false;
		this.receptionPunched = false;
		this.receptionStruck = false;
		this.setInspecting(false);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!this.isOfferingGift() || this.getCarriedItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (this.level() instanceof ServerLevel server
			&& WeaverColonySavedData.get(server).isUntrusted(this.colonyId(), player.getUUID())) {
			return InteractionResult.PASS;
		}
		if (this.level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		ItemStack gift = this.getCarriedItem().copy();
		this.setCarriedItem(ItemStack.EMPTY);
		this.setOfferingGift(false);
		if (!giveGift(player, hand, gift) && this.level() instanceof ServerLevel server) {
			this.spawnAtLocation(server, gift);
		}
		if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			com.theouterworld.advancement.ModAdvancements.onWeaverGift(serverPlayer);
		}
		if (this.isBaby()) {
			this.addRegard(player.getUUID(), FOND_REGARD);
		}
		this.level().playSound(
			null,
			this.getX(),
			this.getY(),
			this.getZ(),
			SoundEvents.ITEM_PICKUP,
			SoundSource.NEUTRAL,
			0.6F,
			1.0F
		);
		return InteractionResult.SUCCESS;
	}

	private static boolean giveGift(Player player, InteractionHand hand, ItemStack gift) {
		if (player.getItemInHand(hand).isEmpty()) {
			player.setItemInHand(hand, gift);
			return true;
		}
		InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		if (player.getItemInHand(other).isEmpty()) {
			player.setItemInHand(other, gift);
			return true;
		}
		return player.getInventory().add(gift);
	}

	public boolean claimBed(BlockPos bed) {
		return claimBed(bed, this.homePlate);
	}

	/** Home recorded during generation, before the chunk is visible to the live world. */
	public void assignGeneratedHome(BlockPos bed, @Nullable BlockPos plate) {
		this.homeDimension = this.level().dimension();
		this.setHomeTo(bed.immutable(), WANDER_RADIUS);
		this.homePlate = plate == null ? null : plate.immutable();
	}

	/** The pad remembers this Weaver. A bunk that already belongs to someone else is refused. */
	public boolean claimBed(BlockPos bed, @Nullable BlockPos plate) {
		Level level = this.level();
		if (!WeaverPadBlock.claim(level, bed, this.getUUID())) {
			return false;
		}
		if (this.hasHome() && !isSameBed(level, this.getHomePosition(), bed)) {
			WeaverPadBlock.release(level, this.getHomePosition(), this.getUUID());
		}
		this.homeDimension = level.dimension();
		this.setHomeTo(bed.immutable(), WANDER_RADIUS);
		this.homePlate = plate == null ? null : plate.immutable();
		return true;
	}

	/** A home on open ground. Does not claim a Weaver pad. */
	public void assignSurfaceHome(BlockPos stand, BlockPos plate) {
		if (this.hasHome()) {
			WeaverPadBlock.release(this.level(), this.getHomePosition(), this.getUUID());
		}
		this.homeDimension = this.level().dimension();
		this.setHomeTo(stand.immutable(), WANDER_RADIUS);
		this.homePlate = plate.immutable();
	}

	public @Nullable BlockPos getHomePlate() {
		return this.homePlate;
	}

	public void setHomePlate(@Nullable BlockPos plate) {
		this.homePlate = plate == null ? null : plate.immutable();
	}

	public void releaseHome() {
		if (this.hasHome()) {
			Level homeLevel = this.level();
			if (this.homeDimension != null && homeLevel instanceof ServerLevel here && !this.homeDimension.equals(here.dimension())) {
				ServerLevel home = here.getServer().getLevel(this.homeDimension);
				if (home != null) {
					homeLevel = home;
				}
			}
			WeaverPadBlock.release(homeLevel, this.getHomePosition(), this.getUUID());
		}
		this.clearHome();
		this.homeDimension = null;
		this.homePlate = null;
	}

	public boolean isHomeLeaping() {
		return this.homeLeaping;
	}

	/** While set, the homing walk searches the ground instead of climbing the colony. */
	public void setFlatApproach(boolean flatApproach) {
		this.flatApproach = flatApproach;
	}

	public boolean isFlatApproach() {
		return this.flatApproach;
	}

	public boolean isDeckBound() {
		return this.deckBound;
	}

	/** Straight move to the bed. The pathfinder is what jumps at the lintel. */
	public void bindToDeck(BlockPos bed) {
		this.deckBound = true;
		this.flatApproach = true;
		this.setJumping(false);
		this.getNavigation().stop();
		this.deckTarget = bed.immutable();
	}

	public void releaseDeck() {
		this.deckBound = false;
		this.deckTarget = null;
	}

	public @Nullable BlockPos getDeckTarget() {
		return this.deckTarget;
	}

	public boolean beginGatherLeap(BlockPos landing) {
		if (!this.launchLeap(LeapKind.GATHER, landing)) {
			return false;
		}
		this.gatherLanding = this.leapTarget;
		return true;
	}

	public void endGatherLeap() {
		this.gatherLanding = null;
		if (this.leapKind == LeapKind.GATHER) {
			this.endHomeLeap();
		}
	}

	public @Nullable BlockPos getGatherLanding() {
		return this.gatherLanding;
	}

	/** Leap toward a stalk or a mate. This is not the night return and not the meeting. */
	public boolean beginTravelLeap(BlockPos landing) {
		if (!this.launchLeap(LeapKind.COURT, landing)) {
			return false;
		}
		this.courtLanding = this.leapTarget;
		return true;
	}

	public void beginGiftLeap(BlockPos landing) {
		this.launchLeap(LeapKind.GIFT, landing);
	}

	public void beginVengeanceFlight(BlockPos landing) {
		this.launchLeap(LeapKind.VENGEANCE, landing);
	}

	public void setCourtLanding(BlockPos landing) {
		this.courtLanding = landing.immutable();
	}

	public void clearCourtLanding() {
		this.courtLanding = null;
	}

	public void beginHomeLeap(BlockPos plate) {
		this.launchLeap(LeapKind.HOME, plate);
	}

	public LeapKind leapKind() {
		return this.leapKind;
	}

	/** The night return to a home plate. Fibre does not hold this leap up. */
	public boolean isHomePlateLeap() {
		return this.homeLeaping && this.leapKind == LeapKind.HOME && !this.vengeanceLeap;
	}

	public void steerHomeLeap(BlockPos plate) {
		if (!this.homeLeaping || this.onGround() || plate == null) {
			return;
		}
		if (this.leapKind == LeapKind.HOME && this.leapTarget != null && !this.leapTarget.equals(plate)) {
			return;
		}
		if (this.leapKind == LeapKind.GATHER) {
			BlockPos gather = this.gatherLanding != null ? this.gatherLanding : this.leapTarget;
			if (gather != null && !gather.equals(plate)) {
				return;
			}
		}
		this.setDeltaMovement(WeaverHomeLeap.steer(this, plate));
		this.syncVelocity = true;
	}

	public void endHomeLeap() {
		if (this.leapKind == LeapKind.GATHER) {
			this.gatherLanding = null;
		}
		this.homeLeaping = false;
		this.leapKind = LeapKind.NONE;
		this.leapTarget = null;
		this.resetFallDistance();
	}

	private boolean launchLeap(LeapKind kind, BlockPos target) {
		if (target == null || !WeaverLeapSpot.isDry(this.level(), target)) {
			return false;
		}
		this.leapKind = kind;
		this.leapTarget = target.immutable();
		if (kind != LeapKind.GATHER) {
			this.gatherLanding = null;
		}
		this.homeLeaping = true;
		this.getNavigation().stop();
		this.setZza(0.0F);
		this.resetFallDistance();
		this.setDeltaMovement(WeaverHomeLeap.launchVelocity(this, target));
		this.syncVelocity = true;
		return true;
	}

	/** True when this body is inside fibre, not merely standing on it. */
	public boolean isEmbeddedInFiber() {
		BlockPos feet = BlockPos.containing(this.getX(), this.getY() + 0.2, this.getZ());
		BlockPos waist = BlockPos.containing(this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ());
		return this.level().getBlockState(feet).is(ModBlocks.THOLIN_FIBER)
			|| this.level().getBlockState(waist).is(ModBlocks.THOLIN_FIBER);
	}

	/**
	 * The whole home leap, climb included. Fibre overhead was stopping the rise
	 * because phasing used to wait until they were already falling.
	 */
	public boolean phasesThroughFiber() {
		return this.homeLeaping && !this.vengeanceLeap;
	}

	@Override
	public void onRemoval(Entity.RemovalReason reason) {
		if (!this.level().isClientSide()
			&& (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.CHANGED_DIMENSION)) {
			this.noteGone();
		}
		super.onRemoval(reason);
	}

	/** This Weaver's bunk is in the dimension they are standing in. Distance does not matter. */
	public boolean hasHomeHere() {
		if (!this.hasHome()) {
			return false;
		}
		return this.homeDimension == null || this.homeDimension.equals(this.level().dimension());
	}

	public boolean isHomeReachable() {
		if (!this.hasHomeHere()) {
			return false;
		}
		return this.distanceToSqr(Vec3.atCenterOf(this.getHomePosition())) <= (double) HOME_ABANDON_DISTANCE * HOME_ABANDON_DISTANCE;
	}

	/** A Weaver placed in the bed block cannot walk; the mattress is inside its body. */
	private void stepOffBed() {
		if (this.isSleeping()) {
			return;
		}
		BlockPos feet = this.blockPosition();
		BlockState bed = this.level().getBlockState(feet);
		if (!(bed.getBlock() instanceof AbstractBedBlock)) {
			return;
		}
		// Standing on top of a pad also rounds down into the bed block; only feet below the mattress are stuck.
		VoxelShape mattress = bed.getCollisionShape(this.level(), feet);
		if (mattress.isEmpty() || this.getY() >= feet.getY() + mattress.max(Direction.Axis.Y) - 1.0E-3) {
			return;
		}
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos to = feet.relative(dir);
			BlockPos ground = to.below();
			boolean open = this.level().getBlockState(to).getCollisionShape(this.level(), to).isEmpty()
				&& this.level().getBlockState(to.above()).getCollisionShape(this.level(), to.above()).isEmpty();
			boolean supported = !this.level().getBlockState(ground).getCollisionShape(this.level(), ground).isEmpty();
			if (open && supported && !(this.level().getBlockState(to).getBlock() instanceof AbstractBedBlock)) {
				this.teleportTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5);
				return;
			}
		}
	}

	private void abandonHomeIfLost() {
		if (!this.hasHome()) {
			return;
		}
		if (this.homeDimension != null && !this.homeDimension.equals(this.level().dimension())) {
			this.noteGone();
			return;
		}
		if (!WeaverSchedule.isBedtime(this.level())
			&& this.distanceToSqr(Vec3.atCenterOf(this.getHomePosition())) > (double) HOME_ABANDON_DISTANCE * HOME_ABANDON_DISTANCE) {
			this.noteGone();
		}
	}

	/** Records the loss and frees the pad so another Weaver can be born. */
	private void noteGone() {
		if (!(this.level() instanceof ServerLevel here)) {
			return;
		}
		long colony = this.colonyId;
		ServerLevel homeLevel = here;
		if (this.homeDimension != null) {
			ServerLevel home = here.getServer().getLevel(this.homeDimension);
			if (home != null) {
				homeLevel = home;
			}
		}
		if (colony == 0L && this.hasHome()) {
			BlockPos bed = this.getHomePosition();
			colony = WeaverColonies.generatedId(homeLevel, bed);
			if (colony == 0L) {
				colony = WeaverColonies.idAt(homeLevel, bed);
			}
		}
		if (colony == 0L) {
			return;
		}
		if (WeaverAbsence.get(homeLevel).mark(colony, this.getUUID()) && this.hasHome()) {
			this.releaseHome();
		}
	}

	public static boolean isBedClaimed(Level level, BlockPos pos, @Nullable WeaverEntity except) {
		AABB box = AABB.ofSize(Vec3.atCenterOf(pos), 96.0, 80.0, 96.0);
		for (WeaverEntity other : level.getEntitiesOfClass(WeaverEntity.class, box, Mob::isAlive)) {
			if (other == except || !other.hasHome()) {
				continue;
			}
			if (!isSameBed(level, other.getHomePosition(), pos)) {
				continue;
			}
			// Two Weavers sharing an old straw bed: the lower id keeps it.
			if (except != null && other.getUUID().compareTo(except.getUUID()) > 0) {
				continue;
			}
			return true;
		}
		return false;
	}

	public static boolean isSameBed(Level level, BlockPos a, BlockPos b) {
		if (a.equals(b)) {
			return true;
		}
		if (connectedBed(level, a).equals(b) || connectedBed(level, b).equals(a)) {
			return true;
		}
		return false;
	}

	private static BlockPos connectedBed(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof AbstractBedBlock) {
			return pos.relative(AbstractBedBlock.getConnectedDirection(state));
		}
		return pos;
	}

	/**
	 * Height this weaver can actually clear with its current jump strength and world gravity.
	 * Pathfinding uses a capped copy so Amberworld's 0.14g does not explode the node search.
	 */
	public double getPhysicalJumpHeight() {
		double gravity = Math.max(0.005, this.getGravity());
		double jumpPower = this.getAttributeValue(Attributes.JUMP_STRENGTH);
		return (jumpPower * jumpPower) / (2.0 * gravity);
	}

	public double getPathfindingJumpHeight() {
		return Mth.clamp(getPhysicalJumpHeight(), 1.125, MAX_PATH_JUMP);
	}

	private float jumpPowerForHeight(double blocks) {
		double gravity = Math.max(0.005, this.getGravity());
		double height = Mth.clamp(blocks, MIN_HOP_BLOCKS, getPhysicalJumpHeight());
		return (float) Math.sqrt(2.0 * gravity * height * 1.2);
	}

	@Override
	protected float getJumpPower() {
		if (this.pendingJumpPower >= 0.0F) {
			float power = this.pendingJumpPower;
			this.pendingJumpPower = -1.0F;
			return power;
		}
		return jumpPowerForHeight(MIN_HOP_BLOCKS);
	}

	@Override
	public void jumpFromGround() {
		if (this.flatApproach || this.deckBound) {
			return;
		}
		Path path = this.getNavigation().getPath();
		if (path != null && !path.isDone()) {
			Node next = path.getNextNode();
			double rise = next.y - this.getY();
			if (rise > this.maxUpStep() + 0.5) {
				this.pendingJumpPower = jumpPowerForHeight(rise);
			}
		}
		super.jumpFromGround();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
		return false;
	}

	@Override
	public int getMaxFallDistance() {
		return 64;
	}

	/**
	 * Directed leap that solves horizontal speed against the world's gravity, so a charge
	 * in Amberworld hangs in the air instead of slamming into the ground.
	 */
	/**
	 * A chase hop only as high as the ledge in front of them. Flat ground returns 0
	 * so the caller keeps walking. Full jump strength in Amberworld is a cannon shot.
	 */
	public int hopToward(double dirX, double dirZ, double horizontal, double rise) {
		if (rise <= this.maxUpStep() + 0.35) {
			return 0;
		}
		double jumpPower = this.jumpPowerForHeight(rise + 0.6);
		double push = Math.min(0.42, Math.max(0.16, horizontal * 0.12));
		this.setDeltaMovement(dirX * push, jumpPower, dirZ * push);
		this.syncVelocity = true;
		return 8;
	}

	public int launchLeap(double dirX, double dirZ, double distance) {
		double gravity = Math.max(0.005, this.getGravity());
		double launchSpeed = this.getAttributeValue(Attributes.JUMP_STRENGTH);
		double airTicks = 2.0 * launchSpeed / gravity;
		double glideTicks = Math.max(0.0, airTicks - 1.0);
		double glide = (1.0 - Math.pow(AIR_DRAG, glideTicks)) / (1.0 - AIR_DRAG);
		double carry = 1.0 + GROUND_FRICTION * AIR_DRAG * glide;
		double push = Math.min(MAX_LAUNCH_PUSH, distance / Math.max(1.0, carry));
		this.setDeltaMovement(dirX * push, launchSpeed, dirZ * push);
		this.syncVelocity = true;
		return LEAP_RECOVERY_TICKS;
	}

	public boolean isAggressive() {
		return this.aggressive;
	}

	public boolean isRetreating() {
		return this.retreating;
	}

	public boolean isClientAggressive() {
		return this.entityData.get(DATA_AGGRESSIVE);
	}

	public @Nullable LivingEntity getLastThreat() {
		return this.lastThreat;
	}

	public boolean isPanicking() {
		return this.panicTicks > 0;
	}

	public void beginAggression(LivingEntity target) {
		if (target instanceof Player player
			&& this.personallyTrusts(player.getUUID())
			&& !this.bloodFeud.contains(player.getUUID())) {
			return;
		}
		// Already fighting, or a grudge they will not drop: no flinch.
		if (!this.aggressive && !this.holdsBloodFeud(target)) {
			this.panicTicks = 10;
		}
		this.aggressive = true;
		this.retreating = false;
		this.lastThreat = target;
		this.setTarget(target);
		this.entityData.set(DATA_AGGRESSIVE, true);
	}

	public void beginRetreat(LivingEntity threat) {
		if (threat instanceof Player player && this.bloodFeud.contains(player.getUUID())) {
			this.beginAggression(player);
			return;
		}
		this.aggressive = false;
		this.retreating = true;
		this.lastThreat = threat;
		this.setTarget(null);
		this.entityData.set(DATA_AGGRESSIVE, false);
	}

	public void finishRetreat() {
		this.retreating = false;
		this.aggressive = false;
		this.setTarget(null);
		this.entityData.set(DATA_AGGRESSIVE, false);
	}

	public boolean holdsBloodFeud(LivingEntity target) {
		return target instanceof Player player && this.bloodFeud.contains(player.getUUID());
	}

	/** Drop the current chase. The grudge stays, and they take it up again when the player is close. */
	public void dropFeudChase() {
		this.aggressive = false;
		this.retreating = false;
		this.setTarget(null);
		this.entityData.set(DATA_AGGRESSIVE, false);
	}

	public boolean isVengeanceLeaping() {
		return this.vengeanceLeap;
	}

	public @Nullable Player getVengeancePlayer() {
		if (this.vengeancePlayer == null || !(this.level() instanceof ServerLevel level)) {
			return null;
		}
		for (Player player : level.players()) {
			if (this.vengeancePlayer.equals(player.getUUID()) && player.isAlive() && !player.isSpectator()) {
				return player;
			}
		}
		return null;
	}

	public void finishVengeanceLeap() {
		UUID id = this.vengeancePlayer;
		this.vengeanceLeap = false;
		this.vengeancePlayer = null;
		if (this.isHomeLeaping()) {
			this.endHomeLeap();
		}
		if (id == null || !(this.level() instanceof ServerLevel level)) {
			return;
		}
		for (Player player : level.players()) {
			if (id.equals(player.getUUID()) && player.isAlive() && !player.isSpectator()) {
				this.beginAggression(player);
				return;
			}
		}
	}

	/** Every loaded Weaver in this colony leaps once, then keeps a personal grudge. */
	private void rallyForBaby(ServerLevel level, Player player) {
		long id = this.colonyId();
		if (id == 0L) {
			return;
		}
		for (Entity entity : level.getAllEntities()) {
			if (entity instanceof WeaverEntity weaver && weaver.isAlive() && weaver.colonyId() == id) {
				weaver.joinBabyFeud(player);
			}
		}
	}

	private void joinBabyFeud(Player player) {
		this.fondOf.remove(player.getUUID());
		if (!this.bloodFeud.add(player.getUUID())) {
			return;
		}
		this.startVengeanceLeap(player);
	}

	private void startVengeanceLeap(Player player) {
		if (this.isSleeping()) {
			this.stopSleeping();
		}
		this.releaseDeck();
		this.setFlatApproach(false);
		if (this.isBreeding()) {
			WeaverEntity partner = this.getBreedPartner();
			this.abortBreeding();
			if (partner != null && partner.isBreeding()) {
				partner.abortBreeding();
			}
		}
		if (this.isOfferingGift()) {
			ItemStack gift = this.getCarriedItem().copy();
			this.setOfferingGift(false);
			this.setCarriedItem(ItemStack.EMPTY);
			if (!gift.isEmpty() && this.level() instanceof ServerLevel server) {
				this.spawnAtLocation(server, gift);
			}
		}
		this.retreating = false;
		this.aggressive = false;
		this.setTarget(null);
		this.entityData.set(DATA_AGGRESSIVE, false);
		this.vengeanceLeap = true;
		this.vengeancePlayer = player.getUUID();
		this.getNavigation().stop();
		this.beginVengeanceFlight(player.blockPosition().below());
	}

	private void tickBloodFeud() {
		if (this.vengeanceLeap || this.bloodFeud.isEmpty() || this.isAggressive() || this.isRetreating()) {
			return;
		}
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		double reach = this.getAttributeValue(Attributes.FOLLOW_RANGE);
		double reachSqr = reach * reach;
		Player nearest = null;
		double nearestSqr = reachSqr;
		for (Player player : level.players()) {
			if (!player.isAlive() || player.isSpectator() || !this.bloodFeud.contains(player.getUUID())) {
				continue;
			}
			if (!isThreatening(player)) {
				continue;
			}
			double distance = this.distanceToSqr(player);
			if (distance <= nearestSqr) {
				nearest = player;
				nearestSqr = distance;
			}
		}
		if (nearest != null) {
			this.beginAggression(nearest);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && source.getEntity() instanceof Player player && isThreatening(player)) {
			if (this.isBaby()) {
				this.scorn(player.getUUID());
				this.rallyForBaby(level, player);
			}
			long id = this.colonyId();
			if (id != 0L) {
				WeaverColonySavedData.get(level).notePlayerDamage(
					level,
					id,
					player.getUUID(),
					level.getGameTime(),
					!this.isAlive(),
					this.blockPosition()
				);
			}
		}
		boolean ceremonyHit = this.receptionKharax != null
			&& source.getEntity() instanceof KharaxEntity kharax
			&& this.receptionKharax.equals(kharax.getUUID());
		if (ceremonyHit) {
			this.receptionStruck = true;
		}
		if (!hurt || !this.isAlive()) {
			return hurt;
		}
		if (this.isBreeding()) {
			WeaverEntity partner = this.getBreedPartner();
			this.abortBreeding();
			if (partner != null) {
				partner.abortBreeding();
			}
		}
		if (this.isSleeping()) {
			this.stopSleeping();
		}
		if (source.getEntity() instanceof LivingEntity attacker
			&& attacker != this
			&& !(attacker instanceof WeaverEntity)
			&& !ceremonyHit
			&& this.suppressPackAlertTicks <= 0
			&& isThreatening(attacker)) {
			if (!(attacker instanceof Player fond && this.personallyTrusts(fond.getUUID()))) {
				alertPack(level, attacker);
			}
		}
		return hurt;
	}

	private void alertPack(ServerLevel level, LivingEntity attacker) {
		AABB packBox = this.getBoundingBox().inflate(PACK_RANGE);
		for (WeaverEntity weaver : level.getEntitiesOfClass(WeaverEntity.class, packBox, Mob::isAlive)) {
			if (attacker instanceof Player player && weaver.personallyTrusts(player.getUUID())) {
				continue;
			}
			weaver.beginAggression(attacker);
		}
	}

	private static boolean isThreatening(LivingEntity attacker) {
		if (attacker instanceof Player player) {
			return !player.isCreative() && !player.isSpectator() && player.gameMode().isSurvival();
		}
		return true;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.releaseHome();
		this.clearIntendedHome();
		if (this.isBreeding()) {
			this.abortBreeding();
		}
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		ItemStack carried = this.getCarriedItem();
		if (!carried.isEmpty()) {
			this.spawnAtLocation(level, carried.copy());
			this.setCarriedItem(ItemStack.EMPTY);
		}
		if (this.stalkCount > 0) {
			this.spawnAtLocation(level, new ItemStack(ModBlocks.THOLIN_STALK, this.stalkCount));
			this.stalkCount = 0;
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Aggressive", this.aggressive);
		output.putBoolean("Retreating", this.retreating);
		ItemStack carried = this.getCarriedItem();
		if (!carried.isEmpty()) {
			output.store("CarriedItem", ItemStack.CODEC, carried);
		}
		if (this.homeDimension != null) {
			output.store("HomeDimension", Level.RESOURCE_KEY_CODEC, this.homeDimension);
		}
		if (this.homePlate != null) {
			output.store("HomePlate", BlockPos.CODEC, this.homePlate);
		}
		if (this.colonyId != 0L) {
			output.putLong("ColonyId", this.colonyId);
		}
		output.putBoolean("OfferingGift", this.isOfferingGift());
		output.putInt("Stalks", this.stalkCount);
		output.putBoolean("Baby", this.isBaby());
		output.putInt("BabyAge", this.babyAge);
		if (!this.fondOf.isEmpty()) {
			output.store("FondOf", UUIDUtil.CODEC.listOf(), List.copyOf(this.fondOf));
		}
		if (!this.bloodFeud.isEmpty()) {
			output.store("BloodFeud", UUIDUtil.CODEC.listOf(), List.copyOf(this.bloodFeud));
		}
		if (!this.scorned.isEmpty()) {
			output.store("Scorned", UUIDUtil.CODEC.listOf(), List.copyOf(this.scorned));
		}
		if (!this.regard.isEmpty()) {
			output.store("Regard", Regard.CODEC.listOf(), regardList());
		}
		if (this.intendedBed != null) {
			output.store("IntendedBed", BlockPos.CODEC, this.intendedBed);
		}
		if (this.intendedPlate != null) {
			output.store("IntendedPlate", BlockPos.CODEC, this.intendedPlate);
		}
		if (this.bedReserver != null) {
			output.store("BedReserver", UUIDUtil.CODEC, this.bedReserver);
		}
		if (!this.furnishedBlocks.isEmpty()) {
			output.store("Furnished", Codec.STRING.listOf(), List.copyOf(this.furnishedBlocks));
		}
		if (this.furnitureCursor != 0L) {
			output.putLong("FurnitureCursor", this.furnitureCursor);
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.aggressive = input.getBooleanOr("Aggressive", false);
		this.retreating = input.getBooleanOr("Retreating", false);
		this.entityData.set(DATA_AGGRESSIVE, this.aggressive);
		this.setCarriedItem(input.read("CarriedItem", ItemStack.CODEC).orElse(ItemStack.EMPTY));
		this.homeDimension = input.read("HomeDimension", Level.RESOURCE_KEY_CODEC).orElse(null);
		this.homePlate = input.read("HomePlate", BlockPos.CODEC).orElse(null);
		this.colonyId = input.getLongOr("ColonyId", 0L);
		this.setOfferingGift(input.getBooleanOr("OfferingGift", false));
		this.stalkCount = input.getIntOr("Stalks", 0);
		this.babyAge = input.getIntOr("BabyAge", 0);
		this.setBaby(input.getBooleanOr("Baby", false));
		this.fondOf.clear();
		input.read("FondOf", UUIDUtil.CODEC.listOf()).ifPresent(this.fondOf::addAll);
		this.bloodFeud.clear();
		input.read("BloodFeud", UUIDUtil.CODEC.listOf()).ifPresent(this.bloodFeud::addAll);
		this.scorned.clear();
		input.read("Scorned", UUIDUtil.CODEC.listOf()).ifPresent(this.scorned::addAll);
		this.regard.clear();
		input.read("Regard", Regard.CODEC.listOf()).ifPresent(list -> {
			for (Regard entry : list) {
				this.regard.put(entry.player, entry.amount);
			}
		});
		this.intendedBed = input.read("IntendedBed", BlockPos.CODEC).orElse(null);
		this.intendedPlate = input.read("IntendedPlate", BlockPos.CODEC).orElse(null);
		this.bedReserver = input.read("BedReserver", UUIDUtil.CODEC).orElse(null);
		this.furnishedBlocks.clear();
		input.read("Furnished", Codec.STRING.listOf()).ifPresent(this.furnishedBlocks::addAll);
		this.furnitureCursor = input.getLongOr("FurnitureCursor", 0L);
		if (this.hasHome() && this.homeDimension == null) {
			this.homeDimension = this.level().dimension();
		}
	}

	private List<Regard> regardList() {
		List<Regard> list = new ArrayList<>(this.regard.size());
		this.regard.forEach((player, amount) -> list.add(new Regard(player, amount)));
		return list;
	}

	private record Regard(UUID player, int amount) {
		private static final Codec<Regard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("player").forGetter(Regard::player),
			Codec.INT.fieldOf("amount").forGetter(Regard::amount)
		).apply(instance, Regard::new));
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return this.isSleeping() ? null : ModSounds.WEAVER_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.WEAVER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.WEAVER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}

	/** Each leap keeps its own target. A meeting flight is never a night return. */
	public enum LeapKind {
		NONE,
		HOME,
		GATHER,
		COURT,
		GIFT,
		VENGEANCE
	}
}
