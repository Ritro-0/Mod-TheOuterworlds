package com.theouterworld.entity;

import com.theouterworld.entity.ai.KharaxHopAttackGoal;
import com.theouterworld.entity.ai.KharaxRetreatGoal;
import com.theouterworld.entity.ai.KharaxReturnHomeGoal;
import com.theouterworld.entity.ai.KharaxSpookGoal;
import com.theouterworld.entity.ai.KharaxWarnGoal;
import com.theouterworld.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Burrowing Outerworld bug that nests in Kharax Dens. Warns nearby survival players,
 * hop-attacks, then retreats and cools off before warning again.
 */
public class KharaxEntity extends PathfinderMob {
	public static final float WARN_RANGE = 15.0F;
	public static final int WARN_DURATION_TICKS = 160;
	public static final int RETREAT_MIN = 15;
	public static final int RETREAT_MAX = 30;

	private static final EntityDataAccessor<Boolean> DATA_WARNING = SynchedEntityData.defineId(
		KharaxEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<Boolean> DATA_SPOOKING = SynchedEntityData.defineId(
		KharaxEntity.class,
		EntityDataSerializers.BOOLEAN
	);

	private @Nullable BlockPos homePos;
	private boolean aggressive;
	private boolean retreating;
	private @Nullable LivingEntity lastThreat;

	public KharaxEntity(EntityType<? extends KharaxEntity> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 100.0)
			.add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.ATTACK_DAMAGE, 6.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_WARNING, false);
		builder.define(DATA_SPOOKING, false);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new KharaxWarnGoal(this));
		this.goalSelector.addGoal(2, new KharaxHopAttackGoal(this));
		this.goalSelector.addGoal(3, new KharaxRetreatGoal(this));
		this.goalSelector.addGoal(4, new KharaxSpookGoal(this));
		this.goalSelector.addGoal(5, new KharaxReturnHomeGoal(this, 1.15, 40.0F));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && this.homePos == null && this.tickCount == 1) {
			this.homePos = this.blockPosition();
		}
	}

	public @Nullable BlockPos getHomePos() {
		return this.homePos;
	}

	public void setHomePos(@Nullable BlockPos homePos) {
		this.homePos = homePos;
	}

	public boolean isWarning() {
		return this.entityData.get(DATA_WARNING);
	}

	public void setWarning(boolean warning) {
		this.entityData.set(DATA_WARNING, warning);
	}

	public boolean isSpooking() {
		return this.entityData.get(DATA_SPOOKING);
	}

	public void setSpooking(boolean spooking) {
		this.entityData.set(DATA_SPOOKING, spooking);
	}

	public boolean isAggressive() {
		return this.aggressive;
	}

	public boolean isRetreating() {
		return this.retreating;
	}

	public @Nullable LivingEntity getLastThreat() {
		return this.lastThreat;
	}

	public void beginAggression(LivingEntity target) {
		this.aggressive = true;
		this.retreating = false;
		this.lastThreat = target;
		this.setTarget(target);
		this.setWarning(false);
		this.setSpooking(false);
	}

	public void beginRetreat(LivingEntity threat) {
		this.aggressive = false;
		this.retreating = true;
		this.lastThreat = threat;
		this.setTarget(null);
	}

	public void finishRetreat() {
		this.retreating = false;
		this.aggressive = false;
		this.setTarget(null);
	}

	public @Nullable Player findThreateningPlayer(float range) {
		Player nearest = this.level().getNearestPlayer(this, range);
		if (nearest == null || nearest.isCreative() || nearest.isSpectator() || !nearest.gameMode().isSurvival()) {
			return null;
		}
		return nearest;
	}

	@Override
	protected int getBaseExperienceReward(ServerLevel level) {
		return 10;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.homePos != null) {
			output.putInt("HomeX", this.homePos.getX());
			output.putInt("HomeY", this.homePos.getY());
			output.putInt("HomeZ", this.homePos.getZ());
		}
		output.putBoolean("Aggressive", this.aggressive);
		output.putBoolean("Retreating", this.retreating);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		if (input.getInt("HomeX").isPresent()) {
			this.homePos = new BlockPos(
				input.getIntOr("HomeX", 0),
				input.getIntOr("HomeY", 0),
				input.getIntOr("HomeZ", 0)
			);
		}
		this.aggressive = input.getBooleanOr("Aggressive", false);
		this.retreating = input.getBooleanOr("Retreating", false);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return isWarning() ? null : ModSounds.KHARAX_IDLE;
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
		this.playSound(SoundEvents.SPIDER_STEP, 0.12F, 1.15F);
	}

	public void playWarningSound() {
		this.playSound(ModSounds.KHARAX_CLICKS, 1.0F, 0.95F + this.random.nextFloat() * 0.1F);
	}
}
