package com.theouterworld.entity;

import com.theouterworld.item.ModItems;
import com.theouterworld.item.OpalineNickelFlailItem;
import com.theouterworld.registry.ModEntities;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class OpalineNickelFlailEntity extends ThrowableProjectile {
	public static final float THROW_SPEED = 1.5F;
	public static final double MAX_RANGE = 40.0;
	public static final float MIN_THROWN_DAMAGE = 10.0F;
	public static final float MAX_THROWN_DAMAGE = 22.0F;
	private static final double RETURN_SPEED = 1.75;
	private static final EntityDataAccessor<Boolean> DATA_FROM_OFFHAND = SynchedEntityData.defineId(
		OpalineNickelFlailEntity.class,
		EntityDataSerializers.BOOLEAN
	);

	private Vec3 origin = Vec3.ZERO;
	private boolean returning;
	private ItemStack weapon = ItemStack.EMPTY;
	private final Set<UUID> hitEntities = new HashSet<>();

	public OpalineNickelFlailEntity(EntityType<OpalineNickelFlailEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	public static OpalineNickelFlailEntity create(EntityType<OpalineNickelFlailEntity> type, Level level) {
		return new OpalineNickelFlailEntity(type, level);
	}

	public OpalineNickelFlailEntity(Level level, LivingEntity owner, ItemStack weapon, InteractionHand hand) {
		super(ModEntities.OPALINE_NICKEL_FLAIL, owner.getX(), owner.getEyeY() - 0.1, owner.getZ(), level);
		this.setOwner(owner);
		this.origin = this.position();
		this.weapon = weapon.copy();
		this.setThrownHand(hand);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_FROM_OFFHAND, false);
	}

	public InteractionHand getThrownHand() {
		return this.entityData.get(DATA_FROM_OFFHAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	private void setThrownHand(InteractionHand hand) {
		this.entityData.set(DATA_FROM_OFFHAND, hand == InteractionHand.OFF_HAND);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0;
	}

	@Override
	protected float getAirDrag() {
		return 1.0F;
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return false;
	}

	@Override
	public void tick() {
		if (this.origin == Vec3.ZERO) {
			this.origin = this.position();
		}

		Entity owner = this.getOwner();
		if (!this.level().isClientSide() && (owner == null || !owner.isAlive() || this.weapon.isEmpty() && this.tickCount > 2)) {
			this.discard();
			return;
		}

		if (this.returning) {
			this.xo = this.getX();
			this.yo = this.getY();
			this.zo = this.getZ();
			this.tickReturn(owner);
			this.hurtEntitiesAlongPath();
			this.updateRotation();
			this.applyEffectsFromBlocks();
			super.baseTick();
			return;
		}

		super.tick();
		this.hurtEntitiesAlongPath();
		if (!this.returning && this.position().distanceTo(this.origin) >= MAX_RANGE) {
			this.startReturning();
		}
	}

	private void tickReturn(Entity owner) {
		if (owner == null) {
			this.discard();
			return;
		}
		Vec3 target = new Vec3(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
		Vec3 delta = target.subtract(this.position());
		double distance = delta.length();
		if (distance < 1.0) {
			this.uncastOwnerItem();
			this.discard();
			return;
		}
		this.setDeltaMovement(delta.normalize().scale(RETURN_SPEED));
		this.setPos(this.position().add(this.getDeltaMovement()));
	}

	private void startReturning() {
		this.returning = true;
		this.setDeltaMovement(Vec3.ZERO);
		this.hitEntities.clear();
	}

	private void hurtEntitiesAlongPath() {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		AABB box = this.getBoundingBox().inflate(0.35);
		for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, box, this::canDamage)) {
			if (!this.hitEntities.add(living.getUUID())) {
				continue;
			}
			double dist = this.origin.distanceTo(this.position());
			float damage = this.computeDamage(living, dist);
			DamageSource source = this.damageSource();
			if (this.returning) {
				living.setInvulnerableTime(0);
			}
			boolean hit = living.hurtServer(serverLevel, source, damage);
			if (hit) {
				serverLevel.playSound(
					null,
					living.getX(),
					living.getY(),
					living.getZ(),
					SoundEvents.PLAYER_ATTACK_CRIT,
					SoundSource.PLAYERS,
					0.8F,
					1.1F
				);
			}
		}
	}

	private boolean canDamage(LivingEntity living) {
		Entity owner = this.getOwner();
		if (living == owner || !living.isAlive() || living.isSpectator()) {
			return false;
		}
		return owner == null || !living.isAlliedTo(owner);
	}

	private float computeDamage(LivingEntity target, double dist) {
		double clamped = Math.min(Math.max(dist, 0.0), MAX_RANGE);
		float rangeFactor = (float) (clamped / MAX_RANGE);
		float base = MIN_THROWN_DAMAGE + (MAX_THROWN_DAMAGE - MIN_THROWN_DAMAGE) * rangeFactor;
		if (this.level() instanceof ServerLevel serverLevel && !this.weapon.isEmpty()) {
			DamageSource source = this.damageSource();
			// Density V at ~35+ blocks ≈ 52 damage. Divisor 2.8 keeps 40-block hits near ~58.
			base += EnchantmentHelper.modifyFallBasedDamage(serverLevel, this.weapon, target, source, 0.0F)
				* (float) (clamped / 2.8);
		}
		return base;
	}

	private DamageSource damageSource() {
		Entity owner = this.getOwner();
		if (owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().generic();
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		if (!this.returning) {
			this.startReturning();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!this.level().isClientSide()) {
			this.uncastOwnerItem();
		}
		super.remove(reason);
	}

	private void uncastOwnerItem() {
		if (!(this.getOwner() instanceof Player player)) {
			return;
		}
		uncastIfMatching(player.getMainHandItem());
		uncastIfMatching(player.getOffhandItem());
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			uncastIfMatching(player.getInventory().getItem(i));
		}
	}

	private static void uncastIfMatching(ItemStack stack) {
		if (stack.is(ModItems.OPALINE_NICKEL_FLAIL) && OpalineNickelFlailItem.isCast(stack)) {
			OpalineNickelFlailItem.setCast(stack, false);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putDouble("OriginX", this.origin.x);
		output.putDouble("OriginY", this.origin.y);
		output.putDouble("OriginZ", this.origin.z);
		output.putBoolean("Returning", this.returning);
		output.putByte("Hand", (byte) this.getThrownHand().ordinal());
		if (!this.weapon.isEmpty()) {
			output.store("Weapon", ItemStack.CODEC, this.weapon);
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.origin = new Vec3(
			input.getDoubleOr("OriginX", this.getX()),
			input.getDoubleOr("OriginY", this.getY()),
			input.getDoubleOr("OriginZ", this.getZ())
		);
		this.returning = input.getBooleanOr("Returning", false);
		this.setThrownHand(input.getByteOr("Hand", (byte) 0) == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
		this.weapon = input.read("Weapon", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		this.setNoGravity(true);
	}
}
