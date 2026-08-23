package com.theouterworld.entity;

import com.theouterworld.entity.ai.KharaxHopAttackGoal;
import com.theouterworld.entity.ai.KharaxHopMoveControl;
import com.theouterworld.entity.ai.KharaxRetreatGoal;
import com.theouterworld.entity.ai.KharaxReturnHomeGoal;
import com.theouterworld.entity.ai.KharaxSpookGoal;
import com.theouterworld.entity.ai.KharaxWarnGoal;
import com.theouterworld.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
	/** Inside this range the warning is abandoned and the kharax commits immediately. */
	public static final float PROXIMITY_AGGRO_RANGE = 5.0F;
	public static final int WARN_DURATION_TICKS = 160;
	/** A kharax that has already been struck only postures briefly before attacking. */
	public static final int PROVOKED_WARN_TICKS = 40;
	public static final int RETREAT_MIN = 15;
	public static final int RETREAT_MAX = 30;
	public static final float WARNING_SOUND_VOLUME = 1.0F;

	/** Horizontal velocity retained per airborne tick. */
	private static final double AIR_DRAG = 0.91;
	/** Friction of the block underfoot, still applied on the tick the leap starts. */
	private static final double GROUND_FRICTION = 0.6;
	private static final double HOP_APEX = 1.0;
	private static final double MAX_HOP_APEX = 3.0;
	private static final double HOP_REACH = 2.9;
	private static final double LUNGE_REACH = 5.0;
	private static final double MAX_LAUNCH_PUSH = 1.2;
	private static final double MIN_EFFORT = 0.6;
	private static final double MAX_EFFORT = 1.7;
	/** Ground time between leaps. Airtime is bounded separately by having to land first. */
	private static final int HOP_RECOVERY_TICKS = 3;

	private static final float HOP_POSE_SMOOTHING = 0.35F;
	private static final float AIRBORNE_SMOOTHING = 0.3F;

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
	private boolean provoked;
	private boolean warningSoundPlaying;
	private @Nullable LivingEntity lastThreat;

	private float hopPose;
	private float hopPoseO;
	private float airborneAmount;
	private float airborneAmountO;

	public KharaxEntity(EntityType<? extends KharaxEntity> type, Level level) {
		super(type, level);
		this.xpReward = 10;
		this.moveControl = new KharaxHopMoveControl(this);
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
		updateHopPose();
	}

	/**
	 * Eases the leap pose toward where the arc currently is. Reading vertical velocity straight
	 * off the entity only changes twenty times a second, which renders as a visible stutter, so
	 * the pose is smoothed here and interpolated again per frame.
	 */
	private void updateHopPose() {
		this.hopPoseO = this.hopPose;
		this.airborneAmountO = this.airborneAmount;
		boolean grounded = this.onGround();
		float airTarget = grounded ? 0.0F : 1.0F;
		float poseTarget = grounded ? 0.0F : Mth.clamp((float) this.getDeltaMovement().y * 2.5F, -1.0F, 1.0F);
		this.airborneAmount += (airTarget - this.airborneAmount) * AIRBORNE_SMOOTHING;
		this.hopPose += (poseTarget - this.hopPose) * HOP_POSE_SMOOTHING;
	}

	/** Signed leap phase: positive while rising, negative while falling, zero on the ground. */
	public float getHopPose(float tickProgress) {
		return Mth.lerp(tickProgress, this.hopPoseO, this.hopPose);
	}

	/** Eased 0-1 blend between the grounded walk cycle and the airborne pose. */
	public float getAirborneAmount(float tickProgress) {
		return Mth.lerp(tickProgress, this.airborneAmountO, this.airborneAmount);
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

	public boolean isProvoked() {
		return this.provoked;
	}

	/**
	 * Launches a single leap toward a direction, solving for the world's actual gravity so the
	 * arc reads the same in Outerworld gravity as it would on Earth-normal. Weaker gravity means
	 * a longer airtime, and airtime is what carries the hop - so the push gets softer, not harder.
	 *
	 * @return ticks to wait before the next hop may be launched
	 */
	public int launchHop(double dirX, double dirZ, double distance, double rise, double speedModifier) {
		double gravity = Math.max(0.005, this.getGravity());
		double effort = Mth.clamp(speedModifier, MIN_EFFORT, MAX_EFFORT);
		// Apex barely scales with urgency: a flatter, faster arc closes distance, where a taller
		// one only buys airtime. Reach does the scaling instead.
		double apex = Mth.clamp(
			Math.max(HOP_APEX * Mth.clamp(effort, 0.8, 1.25), rise + 0.5),
			HOP_APEX,
			MAX_HOP_APEX
		);
		double launchSpeed = Math.sqrt(2.0 * gravity * apex);
		double airTicks = 2.0 * launchSpeed / gravity;

		// Distance covered per unit of launch speed: the opening tick is still slowed by the
		// block underfoot, every tick after that only by air drag.
		double glideTicks = Math.max(0.0, airTicks - 1.0);
		double glide = (1.0 - Math.pow(AIR_DRAG, glideTicks)) / (1.0 - AIR_DRAG);
		double carry = 1.0 + GROUND_FRICTION * AIR_DRAG * glide;

		double reach = Math.min(distance, HOP_REACH * effort);
		double push = Math.min(MAX_LAUNCH_PUSH, reach / Math.max(1.0, carry));

		// Deliberately not setJumping: vanilla's jump would overwrite the solved launch speed
		// with a fixed 0.42, which is exactly the gravity-blind behaviour this replaces.
		this.setDeltaMovement(dirX * push, launchSpeed, dirZ * push);
		this.hurtMarked = true;
		// Only the ground recovery is returned: the next leap also waits on landing, so a hop cut
		// short by a wall or a rise chains straight into the next one instead of idling.
		return HOP_RECOVERY_TICKS;
	}

	/** A committed pounce: same solver, but allowed to cover the full gap to a target. */
	public int launchLunge(double dirX, double dirZ, double distance) {
		return launchHop(dirX, dirZ, Math.min(distance, LUNGE_REACH), 0.0, MAX_EFFORT);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (!hurt || !this.isAlive()) {
			return hurt;
		}
		if (source.getEntity() instanceof LivingEntity attacker && attacker != this) {
			// Once struck it stops bluffing for good: the posture stays, the retreat stays,
			// but walking away no longer calls it off.
			this.provoked = true;
			if (!this.aggressive) {
				this.beginAggression(attacker);
			}
		}
		return hurt;
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
		output.putBoolean("Provoked", this.provoked);
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
		this.provoked = input.getBooleanOr("Provoked", false);
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
		this.playSound(ModSounds.KHARAX_CLICKS, WARNING_SOUND_VOLUME, 0.95F + this.random.nextFloat() * 0.1F);
		this.warningSoundPlaying = true;
	}

	/**
	 * The clicks track is cut to the length of an uninterrupted warning, so any early exit has to
	 * silence it deliberately - a stop-sound packet is the only way to cut a sample already playing.
	 * Reaches twice the audible radius so listeners who have since backed away are still covered.
	 */
	public void stopWarningSound() {
		if (!this.warningSoundPlaying) {
			return;
		}
		this.warningSoundPlaying = false;
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(
			ModSounds.KHARAX_CLICKS.location(),
			this.getSoundSource()
		);
		double reach = ModSounds.KHARAX_CLICKS.getRange(WARNING_SOUND_VOLUME) * 2.0;
		double reachSq = reach * reach;
		for (ServerPlayer listener : serverLevel.getPlayers(player -> player.distanceToSqr(this) <= reachSq)) {
			listener.connection.send(packet);
		}
	}

	@Override
	public void die(DamageSource source) {
		stopWarningSound();
		super.die(source);
	}

	@Override
	public void remove(RemovalReason reason) {
		stopWarningSound();
		super.remove(reason);
	}
}
