package com.theouterworld.mixin;

import com.theouterworld.entity.WeaverKharaxDeathMessages;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatTracker.class)
public class CombatTrackerDeathMessageMixin {
	@Shadow
	private List<CombatEntry> entries;

	@Shadow
	@Final
	private LivingEntity mob;

	@Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true, require = 1)
	private void theouterworlds$weaverKharaxDeath(CallbackInfoReturnable<Component> cir) {
		Component message = WeaverKharaxDeathMessages.forKill(this.mob, this.entries);
		if (message != null) {
			cir.setReturnValue(message);
		}
	}
}
