package com.theouterworld.entity;

import com.theouterworld.block.WeaverPadBlock;
import com.theouterworld.entity.ai.WeaverHomeLeap;
import com.theouterworld.entity.ai.WeaverHousekeepingGoal;
import com.theouterworld.entity.ai.WeaverLeapAttackGoal;
import com.theouterworld.entity.ai.WeaverMoveControl;
import com.theouterworld.entity.ai.WeaverPathNavigation;
import com.theouterworld.entity.ai.WeaverRetreatGoal;
import com.theouterworld.entity.ai.WeaverReturnHomeGoal;
import com.theouterworld.entity.ai.WeaverSleepGoal;
import com.theouterworld.entity.ai.WeaverWanderGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
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
	private boolean homeLeaping;
	private boolean flatApproach;
	/** After the home-plate leap, pathfinding and jumping stay off until sleep. */
	private boolean deckBound;
	private @Nullable BlockPos deckTarget;

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
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new WeaverLeapAttackGoal(this));
		this.goalSelector.addGoal(2, new WeaverRetreatGoal(this));
		this.goalSelector.addGoal(3, new WeaverSleepGoal(this));
		this.goalSelector.addGoal(4, new WeaverHousekeepingGoal(this));
		this.goalSelector.addGoal(5, new WeaverReturnHomeGoal(this));
		this.goalSelector.addGoal(6, new WeaverWanderGoal(this));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide()) {
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

	public @Nullable BlockPos getHomePlate() {
		return this.homePlate;
	}

	public void setHomePlate(@Nullable BlockPos plate) {
		this.homePlate = plate == null ? null : plate.immutable();
	}

	public void releaseHome() {
		if (this.hasHome()) {
			WeaverPadBlock.release(this.level(), this.getHomePosition(), this.getUUID());
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

	public void beginHomeLeap(BlockPos plate) {
		this.homeLeaping = true;
		this.getNavigation().stop();
		this.setZza(0.0F);
		this.resetFallDistance();
		this.setDeltaMovement(WeaverHomeLeap.launchVelocity(this, plate));
		this.syncVelocity = true;
	}

	public void steerHomeLeap(BlockPos plate) {
		if (!this.homeLeaping || this.onGround()) {
			return;
		}
		this.setDeltaMovement(WeaverHomeLeap.steer(this, plate));
		this.syncVelocity = true;
	}

	public void endHomeLeap() {
		this.homeLeaping = false;
		this.resetFallDistance();
	}

	/** Falling toward a home plate: tholin fibre lets the body through until the feet meet the plate. */
	public boolean phasesThroughFiber() {
		if (!this.homeLeaping || this.homePlate == null || this.getDeltaMovement().y > 0.12) {
			return false;
		}
		return this.getY() > this.homePlate.getY();
	}

	public boolean isHomeReachable() {
		if (!this.hasHome()) {
			return false;
		}
		if (this.homeDimension != null && !this.homeDimension.equals(this.level().dimension())) {
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
		if (!(this.level().getBlockState(feet).getBlock() instanceof AbstractBedBlock)) {
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
			releaseHome();
			return;
		}
		if (this.distanceToSqr(Vec3.atCenterOf(this.getHomePosition())) > (double) HOME_ABANDON_DISTANCE * HOME_ABANDON_DISTANCE) {
			releaseHome();
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
	public int getMaxFallDistance() {
		return Math.max(super.getMaxFallDistance(), Mth.ceil(getPathfindingJumpHeight()));
	}

	/**
	 * Directed leap that solves horizontal speed against the world's gravity, so a charge
	 * in Amberworld hangs in the air instead of slamming into the ground.
	 */
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

	public void beginAggression(LivingEntity target) {
		this.aggressive = true;
		this.retreating = false;
		this.lastThreat = target;
		this.setTarget(target);
		this.entityData.set(DATA_AGGRESSIVE, true);
	}

	public void beginRetreat(LivingEntity threat) {
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

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (!hurt || !this.isAlive()) {
			return hurt;
		}
		if (this.isSleeping()) {
			this.stopSleeping();
		}
		if (source.getEntity() instanceof LivingEntity attacker
			&& attacker != this
			&& !(attacker instanceof WeaverEntity)
			&& isThreatening(attacker)) {
			alertPack(level, attacker);
		}
		return hurt;
	}

	private void alertPack(ServerLevel level, LivingEntity attacker) {
		AABB packBox = this.getBoundingBox().inflate(PACK_RANGE);
		for (WeaverEntity weaver : level.getEntitiesOfClass(WeaverEntity.class, packBox, Mob::isAlive)) {
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
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		ItemStack carried = this.getCarriedItem();
		if (!carried.isEmpty()) {
			this.spawnAtLocation(level, carried.copy());
			this.setCarriedItem(ItemStack.EMPTY);
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
		if (this.hasHome() && this.homeDimension == null) {
			this.homeDimension = this.level().dimension();
		}
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.EMPTY;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.EMPTY;
	}

	@Override
	protected void playHurtSound(DamageSource source) {
	}

	@Override
	public void playAmbientSound() {
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}
}
