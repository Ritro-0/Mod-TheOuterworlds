package com.theouterworld.entity;

import com.theouterworld.entity.ai.KinDen;
import com.theouterworld.entity.ai.KinShedBuildGoal;
import com.theouterworld.entity.ai.KinShedSleepGoal;
import com.theouterworld.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * A Kharax the colony took in. The work is a Weaver's, except the night:
 * it builds a shed near the Anchor and waits inside, with no pad.
 */
public class KinKharaxEntity extends WeaverEntity {
	private static final int DEN_WAIT = 200;

	private @Nullable BlockPos denAnchor;
	private @Nullable BlockPos denOrigin;
	private int denFacing;
	private int denPause;
	private boolean denRaised;
	private boolean denBuilt;
	private boolean denning;
	private final List<KinDen.Placement> denPlan = new ArrayList<>();
	private int denCursor;
	private boolean denPlanReady;

	public KinKharaxEntity(EntityType<? extends KinKharaxEntity> type, Level level) {
		super(type, level);
		this.xpReward = 8;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 64.0)
			.add(Attributes.MOVEMENT_SPEED, 0.28)
			.add(Attributes.FOLLOW_RANGE, 64.0)
			.add(Attributes.ATTACK_DAMAGE, 5.0)
			.add(Attributes.STEP_HEIGHT, 1.0)
			.add(Attributes.JUMP_STRENGTH, JUMP_STRENGTH)
			.add(Attributes.SAFE_FALL_DISTANCE, 12.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(2, new KinShedBuildGoal(this));
		this.goalSelector.addGoal(5, new KinShedSleepGoal(this));
	}

	/** Hearts have played. Stay put, then raise a hut near this Anchor. */
	public void planDen(BlockPos anchor) {
		this.denAnchor = anchor.immutable();
		this.denOrigin = null;
		this.denPause = DEN_WAIT;
		this.denRaised = false;
		this.denBuilt = false;
		this.denning = false;
		this.denPlan.clear();
		this.denCursor = 0;
		this.denPlanReady = false;
	}

	public boolean wantsDen() {
		return this.denAnchor != null && !this.denBuilt;
	}

	public int denPauseLeft() {
		return this.denPause;
	}

	public boolean hasDenSite() {
		return this.denOrigin != null;
	}

	public boolean chooseDenSite(ServerLevel level) {
		if (this.denAnchor == null) {
			return false;
		}
		if (this.denOrigin != null && (this.denRaised || this.denCursor > 0 || KinDen.hullClear(level, this.denOrigin))) {
			return true;
		}
		KinDen.Site site = this.denOrigin != null
			? KinDen.findRaised(level, this.denAnchor, this)
			: KinDen.findSite(level, this.denAnchor, this);
		this.denOrigin = site.origin();
		this.denFacing = site.door().get2DDataValue();
		this.denRaised = site.raised();
		this.denPlan.clear();
		this.denCursor = 0;
		this.denPlanReady = false;
		return true;
	}

	public @Nullable BlockPos denOrigin() {
		return this.denOrigin;
	}

	/** The ground block under the room. The night leap lands on it. */
	public @Nullable BlockPos denFloor() {
		return this.denOrigin == null ? null : this.denOrigin.below();
	}

	public Direction denDoor() {
		return Direction.from2DDataValue(this.denFacing);
	}

	public boolean denBuilt() {
		return this.denBuilt;
	}

	public boolean isDenning() {
		return this.denning;
	}

	public void setDenning(boolean denning) {
		this.denning = denning;
	}

	public KinDen.@Nullable Placement peekDenBlock(ServerLevel level) {
		this.prepareDenPlan();
		while (this.denCursor < this.denPlan.size() && KinDen.satisfied(level, this.denPlan.get(this.denCursor))) {
			this.denCursor++;
		}
		if (this.denCursor >= this.denPlan.size()) {
			return null;
		}
		return this.denPlan.get(this.denCursor);
	}

	public void advanceDen() {
		this.denCursor++;
	}

	public void completeDen() {
		if (this.denOrigin == null || this.denBuilt) {
			return;
		}
		this.denBuilt = true;
		this.denPlan.clear();
		this.assignSurfaceHome(this.denOrigin, this.denOrigin.below());
	}

	private void prepareDenPlan() {
		if (this.denPlanReady || this.denOrigin == null) {
			return;
		}
		this.denPlan.clear();
		if (this.level() instanceof ServerLevel server) {
			this.denPlan.addAll(KinDen.plan(server, this.denOrigin, this.denDoor(), this.denRaised));
		}
		this.denPlanReady = true;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && this.denPause > 0) {
			this.denPause--;
		}
		if (this.denning && this.getPose() != Pose.STANDING) {
			this.setPose(Pose.STANDING);
		}
	}

	@Override
	public boolean isSleeping() {
		return this.denning || super.isSleeping();
	}

	@Override
	public void stopSleeping() {
		this.denning = false;
		super.stopSleeping();
	}

	@Override
	public boolean canStartBreeding() {
		return false;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return this.denning ? null : ModSounds.KHARAX_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.KHARAX_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.KHARAX_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.SPIDER_STEP, 0.12F, 1.05F);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.denAnchor != null) {
			output.store("DenAnchor", BlockPos.CODEC, this.denAnchor);
		}
		if (this.denOrigin != null) {
			output.store("DenOrigin", BlockPos.CODEC, this.denOrigin);
		}
		output.putInt("DenFacing", this.denFacing);
		output.putInt("DenPause", this.denPause);
		output.putBoolean("DenRaised", this.denRaised);
		output.putBoolean("DenBuilt", this.denBuilt);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.denAnchor = input.read("DenAnchor", BlockPos.CODEC).orElse(null);
		this.denOrigin = input.read("DenOrigin", BlockPos.CODEC).orElse(null);
		this.denFacing = input.getIntOr("DenFacing", 0);
		this.denPause = input.getIntOr("DenPause", 0);
		this.denRaised = input.getBooleanOr("DenRaised", false);
		this.denBuilt = input.getBooleanOr("DenBuilt", false);
		this.denPlan.clear();
		this.denCursor = 0;
		this.denPlanReady = false;
		if (this.denBuilt && this.denOrigin != null) {
			this.assignSurfaceHome(this.denOrigin, this.denOrigin.below());
		}
	}
}
