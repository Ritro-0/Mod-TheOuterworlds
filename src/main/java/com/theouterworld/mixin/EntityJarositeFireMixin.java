package com.theouterworld.mixin;

import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GraphiteProtection;
import com.theouterworld.util.IridiumProtection;
import com.theouterworld.util.JarositeProtection;
import com.theouterworld.world.DeepworldLayers;
import com.theouterworld.world.EdgeworldLayers;
import com.theouterworld.world.FarworldLayers;
import com.theouterworld.world.HighworldLayers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityJarositeFireMixin {
	@Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
	private void theouterworlds$jarositeFireImmune(CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if (self instanceof LivingEntity living) {
			if (JarositeProtection.hasJarositeTrim(living) || GraphiteProtection.hasHeatShield(living)) {
				cir.setReturnValue(true);
				return;
			}
			if (IridiumProtection.hasIridiumArmor(living)) {
				// Gas giants / liquid hydrogen demand a 3-piece set.
				if (ModDimensions.isGasGiant(living.level().dimension())) {
					int required = ModDimensions.isDeepworld(living.level().dimension())
						? DeepworldLayers.DEEPWORLD_IRIDIUM_PIECES
						: ModDimensions.isFarworld(living.level().dimension())
							? FarworldLayers.FARWORLD_IRIDIUM_PIECES
							: ModDimensions.isEdgeworld(living.level().dimension())
								? EdgeworldLayers.EDGEWORLD_IRIDIUM_PIECES
								: HighworldLayers.HIGHWORLD_IRIDIUM_PIECES;
					if (IridiumProtection.countIridiumPieces(living) >= required) {
						cir.setReturnValue(true);
					}
					return;
				}
				cir.setReturnValue(true);
				return;
			}
		}
		if (self instanceof ItemEntity item) {
			if (JarositeProtection.isProtectedItem(item.getItem())
				|| GraphiteProtection.hasGraphiteTrim(item.getItem())
				|| IridiumProtection.isIridiumArmor(item.getItem())) {
				cir.setReturnValue(true);
			}
		}
	}
}
