package com.theouterworld.item;

import com.theouterworld.mixin.LivingEntityAccessor;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Handheld aerostat: cancels world gravity and lifts at a dimension-independent rate.
 * Works only in Nearworld and the gas/ice giants (High/Deep/Far/Edge).
 * Space speeds ascent; sneak descends at the same natural rate. Stops rising near build height.
 * Disabled while submerged in liquid helium (same climb struggle as liquid hydrogen, no bypass).
 * Horizontal WASD control is amplified so strafing while rising/falling feels free.
 */
public class AerostatBalloonItem extends Item {
	public static final int DURABILITY = 256;
	/** Ticks of continuous hold between durability checks (~5 seconds). */
	public static final int DURABILITY_INTERVAL_TICKS = 100;
	/** Blocks/tick of natural lift — same feel in 0.16g and 2.5g once gravity is zeroed. */
	public static final double NATURAL_LIFT = 0.18;
	private static final double BUILD_HEIGHT_STOP_MARGIN = 6.0;
	/** Air-control strength while ballooning (blocks/tick^2 scale via moveRelative). */
	private static final float HORIZONTAL_CONTROL = 0.085F;
	private static final double MAX_HORIZONTAL_SPEED = 0.65;

	public AerostatBalloonItem(ResourceKey<Item> registryKey, Item.Properties properties) {
		super(properties
			.setId(registryKey)
			.stacksTo(1)
			.durability(DURABILITY)
			.enchantable(10)
			.repairable(ModItems.OSMIUM_FLAKE)
		);
	}

	public static boolean isHolding(LivingEntity entity) {
		return entity.getMainHandItem().getItem() instanceof AerostatBalloonItem
			|| entity.getOffhandItem().getItem() instanceof AerostatBalloonItem;
	}

	/** True when the balloon is held, in a usable dimension, and not blocked by liquid helium. */
	public static boolean isActive(LivingEntity entity) {
		return isHolding(entity)
			&& isUsableDimension(entity.level().dimension())
			&& !isBlockedByLiquidHelium(entity);
	}

	/** Nearworld sulfuric storms + gas/ice-giant cloud decks. */
	public static boolean isUsableDimension(ResourceKey<Level> dimension) {
		return ModDimensions.isNearworld(dimension)
			|| ModDimensions.isHighworld(dimension)
			|| ModDimensions.isDeepworld(dimension)
			|| ModDimensions.isFarworld(dimension)
			|| ModDimensions.isEdgeworld(dimension);
	}

	public static boolean isBlockedByLiquidHelium(LivingEntity entity) {
		return entity.getFluidHeight(ModTags.LIQUID_HELIUM) > 0.0;
	}

	/**
	 * Apply constant vertical velocity while held. Call from living-entity tick (client + server).
	 */
	public static void tickHeld(LivingEntity entity) {
		if (!(entity instanceof Player player) || player.isSpectator()) {
			return;
		}
		if (!isActive(player)) {
			return;
		}
		if (player.getAbilities().flying) {
			return;
		}

		double lift = NATURAL_LIFT;
		if (player.isShiftKeyDown()) {
			lift = -NATURAL_LIFT;
		} else if (((LivingEntityAccessor) player).theouterworlds$isJumping()) {
			lift = NATURAL_LIFT * 2.0;
		}

		double ceiling = player.level().getMaxY() - BUILD_HEIGHT_STOP_MARGIN;
		if (lift > 0.0 && player.getY() >= ceiling) {
			lift = 0.0;
		}

		// Soft retention + WASD thrust so sideways motion stays responsive in zero-g.
		Vec3 motion = player.getDeltaMovement();
		double hx = motion.x * 0.985;
		double hz = motion.z * 0.985;
		player.setDeltaMovement(hx, lift, hz);
		player.moveRelative(HORIZONTAL_CONTROL, new Vec3(player.xxa, 0.0, player.zza));

		Vec3 after = player.getDeltaMovement();
		double horiz = Math.sqrt(after.x * after.x + after.z * after.z);
		if (horiz > MAX_HORIZONTAL_SPEED) {
			double scale = MAX_HORIZONTAL_SPEED / horiz;
			after = new Vec3(after.x * scale, lift, after.z * scale);
		} else {
			after = new Vec3(after.x, lift, after.z);
		}
		player.setDeltaMovement(after);
		player.resetFallDistance();
		player.syncVelocity = true;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		if (!(entity instanceof ServerPlayer player)) {
			return;
		}
		if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
			return;
		}
		if (player.getAbilities().instabuild || player.isSpectator()) {
			return;
		}
		if (isBlockedByLiquidHelium(player)) {
			return;
		}
		if (player.tickCount % DURABILITY_INTERVAL_TICKS != 0) {
			return;
		}

		stack.hurtAndBreak(1, level, player, broken ->
			level.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.BUBBLE_POP,
				SoundSource.PLAYERS,
				1.0F,
				0.9F + player.getRandom().nextFloat() * 0.2F
			)
		);
	}
}
