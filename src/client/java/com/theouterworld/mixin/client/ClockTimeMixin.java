package com.theouterworld.mixin.client;

import com.theouterworld.world.DimensionClocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The clock item only has a daytime face in the overworld. Everywhere else it
 * spins at random, so mod dimensions supply the needle from the sky they draw.
 */
@Mixin(net.minecraft.client.renderer.item.properties.numeric.Time.class)
public class ClockTimeMixin {
	@Inject(method = "calculate", at = @At("RETURN"), cancellable = true)
	private void theouterworlds$visualClock(
		ItemStack stack,
		ClientLevel level,
		int seed,
		ItemOwner owner,
		CallbackInfoReturnable<Float> cir
	) {
		Level world = level;
		if (owner != null && owner.level() != null) {
			world = owner.level();
		}
		if (world == null) {
			return;
		}
		float angle = DimensionClocks.clockAngle(world);
		if (!Float.isNaN(angle)) {
			cir.setReturnValue(angle);
		}
	}
}
