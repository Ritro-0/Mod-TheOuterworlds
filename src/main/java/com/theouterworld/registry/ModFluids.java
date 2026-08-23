package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.fluid.MercuryFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class ModFluids {
	public static final FlowingFluid FLOWING_MERCURY = register("flowing_mercury", new MercuryFluid.Flowing());
	public static final FlowingFluid MERCURY = register("mercury", new MercuryFluid.Source());

	private static <T extends Fluid> T register(String name, T fluid) {
		T registered = Registry.register(BuiltInRegistries.FLUID, OuterWorldMod.id(name), fluid);
		for (FluidState state : registered.getStateDefinition().getPossibleStates()) {
			Fluid.FLUID_STATE_REGISTRY.add(state);
		}
		return registered;
	}

	public static void registerModFluids() {
		OuterWorldMod.LOGGER.info("Registering fluids for {}", OuterWorldMod.MOD_ID);
	}
}
