package com.theouterworld.world;

import com.theouterworld.registry.ModTags;
import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Dense liquid mercury: water-like swimming with stronger flow pull and heavier upward movement.
 */
public final class MercuryFluidInteractions {
	private static final float HORIZONTAL_SLOWDOWN = 0.62F;
	private static final float VERTICAL_SLOWDOWN = 0.50F;
	private static final float MOVEMENT_SPEED = 0.018F;
	private static final double FLOWING_PUSH_SCALE = 0.021D;

	private MercuryFluidInteractions() {
	}

	public static void register() {
		FluidBehavior baseBehavior = FluidBehavior.simple()
			.allowSwimming(true)
			.movementSpeed(MOVEMENT_SPEED)
			.movementSlowdown(HORIZONTAL_SLOWDOWN, VERTICAL_SLOWDOWN)
			.flowingPushScale(FLOWING_PUSH_SCALE)
			.build();

		EntityFluidInteractionRegistry.register(ModTags.MERCURY, new FluidBehavior() {
			@Override
			public void handleFluidInteractionUpdate(
				net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> fluid,
				Entity entity,
				net.minecraft.world.entity.EntityFluidInteraction interaction,
				boolean canPushEntity
			) {
				baseBehavior.handleFluidInteractionUpdate(fluid, entity, interaction, canPushEntity);
			}

			@Override
			public void travelInFluid(
				net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> fluid,
				LivingEntity entity,
				net.minecraft.world.phys.Vec3 input,
				double baseGravity,
				boolean isFalling,
				double oldY
			) {
				baseBehavior.travelInFluid(fluid, entity, input, baseGravity, isFalling, oldY);
			}

			@Override
			public void onFluidEntered(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> fluid, Entity entity, boolean firstTick) {
				baseBehavior.onFluidEntered(fluid, entity, firstTick);
				applyMercuryNausea(entity);
			}

			@Override
			public void onFluidExited(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> fluid, Entity entity) {
				baseBehavior.onFluidExited(fluid, entity);
				removeMercuryNausea(entity);
			}
		});
	}

	private static void applyMercuryNausea(Entity entity) {
		if (entity instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, MobEffectInstance.INFINITE_DURATION, 1, false, true, true));
		}
	}

	private static void removeMercuryNausea(Entity entity) {
		if (entity instanceof LivingEntity living) {
			MobEffectInstance nausea = living.getEffect(MobEffects.NAUSEA);
			if (nausea != null && nausea.getAmplifier() >= 1 && nausea.isInfiniteDuration()) {
				living.removeEffect(MobEffects.NAUSEA);
			}
		}
	}
}
