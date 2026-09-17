package com.theouterworld.world;

import com.theouterworld.registry.ModTags;
import com.theouterworld.util.IridiumProtection;
import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

/**
 * Liquid methane: same swim / burn profile as liquid hydrogen.
 */
public final class LiquidMethaneFluidInteractions {
	private static final double ASCEND_VELOCITY_SCALE = 0.22;
	private static final double ASCEND_VELOCITY_CAP = 0.035;

	private LiquidMethaneFluidInteractions() {
	}

	public static void register() {
		FluidBehavior baseBehavior = FluidBehavior.simple()
			.allowSwimming(true)
			.allowSprinting(true)
			.allowMovingDown(true)
			.movementSpeed(entity -> entity.isSwimming() ? 0.036F : 0.022F)
			.movementSlowdown(entity -> entity.isSwimming() || entity.isSprinting() ? 0.9F : 0.8F)
			.gravityMultiplier(1.0F / 40.0F)
			.flowingPushScale(0.014D)
			.fallDistanceModifier(0.0F)
			.build();

		EntityFluidInteractionRegistry.register(ModTags.LIQUID_METHANE, new FluidBehavior() {
			@Override
			public void handleFluidInteractionUpdate(
				TagKey<Fluid> fluid,
				Entity entity,
				net.minecraft.world.entity.EntityFluidInteraction interaction,
				boolean canPushEntity
			) {
				baseBehavior.handleFluidInteractionUpdate(fluid, entity, interaction, canPushEntity);
				burnEntity(entity);
			}

			@Override
			public void travelInFluid(
				TagKey<Fluid> fluid,
				LivingEntity entity,
				Vec3 input,
				double baseGravity,
				boolean isFalling,
				double oldY
			) {
				baseBehavior.travelInFluid(fluid, entity, input, baseGravity, isFalling, oldY);
				Vec3 motion = entity.getDeltaMovement();
				if (motion.y > 0.0) {
					double climb = Math.min(motion.y * ASCEND_VELOCITY_SCALE, ASCEND_VELOCITY_CAP);
					entity.setDeltaMovement(motion.x, climb, motion.z);
				}
			}

			@Override
			public boolean canSprintInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
				return true;
			}

			@Override
			public boolean canSwimInFluid(TagKey<Fluid> fluid, Entity entity) {
				return true;
			}

			@Override
			public boolean canMoveDownInFluid(TagKey<Fluid> fluid, Entity entity) {
				return true;
			}

			@Override
			public void onFluidEntered(TagKey<Fluid> fluid, Entity entity, boolean firstTick) {
				baseBehavior.onFluidEntered(fluid, entity, firstTick);
				burnEntity(entity);
			}

			@Override
			public void onFluidExited(TagKey<Fluid> fluid, Entity entity) {
				baseBehavior.onFluidExited(fluid, entity);
			}
		});
	}

	private static void burnEntity(Entity entity) {
		if (entity.level().isClientSide()) {
			return;
		}
		// Amberworld (Titan) methane is cold and non-flammable for players.
		if (com.theouterworld.registry.ModDimensions.isAmberworld(entity.level().dimension())) {
			return;
		}
		if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
			return;
		}
		if (entity instanceof ItemEntity itemEntity
			&& itemEntity.getItem().has(DataComponents.DAMAGE_RESISTANT)) {
			entity.clearFire();
			return;
		}
		if (entity instanceof LivingEntity living
			&& IridiumProtection.countIridiumPieces(living) >= EdgeworldLayers.EDGEWORLD_IRIDIUM_PIECES) {
			entity.clearFire();
			return;
		}

		entity.igniteForSeconds(15.0F);
		if (entity.level() instanceof ServerLevel server && entity.tickCount % 10 == 0) {
			entity.hurtServer(server, entity.damageSources().lava(), 4.0F);
		}
	}
}
