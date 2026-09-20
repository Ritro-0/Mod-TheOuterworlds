package com.theouterworld.mixin;

import com.theouterworld.block.ModBlocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla always fills y &lt; -54 with lava. Frostworld's subsurface ocean should
 * sit on bedrock instead of a lava layer.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class FrostworldFluidPickerMixin {
	@Inject(method = "createFluidPicker", at = @At("RETURN"), cancellable = true)
	private static void theouterworlds$waterOnBedrock(
		NoiseGeneratorSettings settings,
		CallbackInfoReturnable<Aquifer.FluidPicker> cir
	) {
		if (!settings.defaultBlock().is(ModBlocks.CARBONIC_ICE)) {
			return;
		}
		Aquifer.FluidStatus ocean = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
		cir.setReturnValue((x, y, z) -> ocean);
	}
}
