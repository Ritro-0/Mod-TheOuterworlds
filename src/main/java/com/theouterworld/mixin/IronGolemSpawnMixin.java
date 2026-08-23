package com.theouterworld.mixin;

import com.theouterworld.block.IronGolemBlocks;
import com.theouterworld.entity.OxidizableIronGolemEntity;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces vanilla iron golems with oxidizable ones in the Outerworld, and
 * whenever a golem is built from custom rusting iron blocks.
 */
@Mixin(ServerLevel.class)
public abstract class IronGolemSpawnMixin {

	@Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$interceptIronGolemSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (entity.getType() != EntityTypes.IRON_GOLEM || entity instanceof OxidizableIronGolemEntity) {
			return;
		}

		IronGolemBlocks.PendingSpawn pending = IronGolemBlocks.takePending();
		ServerLevel world = (ServerLevel) (Object) this;
		boolean inOuterworld = world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY);
		boolean fromCustomIron = pending != null && pending.custom();
		if (!inOuterworld && !fromCustomIron) {
			return;
		}

		IronGolem vanillaGolem = (IronGolem) entity;
		OxidizableIronGolemEntity oxidizableGolem = ModEntities.OXIDIZABLE_IRON_GOLEM.create(world, EntitySpawnReason.CONVERSION);
		if (oxidizableGolem == null) {
			return;
		}

		oxidizableGolem.snapTo(
			vanillaGolem.getX(),
			vanillaGolem.getY(),
			vanillaGolem.getZ(),
			vanillaGolem.getYRot(),
			vanillaGolem.getXRot()
		);
		oxidizableGolem.setHealth(vanillaGolem.getHealth());
		if (vanillaGolem.isPlayerCreated()) {
			oxidizableGolem.setPlayerCreated(true);
		}
		if (pending != null) {
			oxidizableGolem.setOxidationLevel(pending.oxidation());
			oxidizableGolem.setWaxed(pending.waxed());
		}

		world.addFreshEntityWithPassengers(oxidizableGolem);
		cir.setReturnValue(false);
	}
}
