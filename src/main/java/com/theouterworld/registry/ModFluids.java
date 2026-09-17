package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.fluid.LiquidAmmoniaFluid;
import com.theouterworld.fluid.LiquidHeliumFluid;
import com.theouterworld.fluid.LiquidHydrogenFluid;
import com.theouterworld.fluid.LiquidMethaneFluid;
import com.theouterworld.fluid.MercuryFluid;
import com.theouterworld.fluid.MetallicHydrogenFluid;
import com.theouterworld.fluid.SolarPlasmaFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class ModFluids {
	public static final FlowingFluid FLOWING_MERCURY = register("flowing_mercury", new MercuryFluid.Flowing());
	public static final FlowingFluid MERCURY = register("mercury", new MercuryFluid.Source());
	public static final FlowingFluid FLOWING_LIQUID_HYDROGEN = register("flowing_liquid_hydrogen", new LiquidHydrogenFluid.Flowing());
	public static final FlowingFluid LIQUID_HYDROGEN = register("liquid_hydrogen", new LiquidHydrogenFluid.Source());
	public static final FlowingFluid FLOWING_LIQUID_HELIUM = register("flowing_liquid_helium", new LiquidHeliumFluid.Flowing());
	public static final FlowingFluid LIQUID_HELIUM = register("liquid_helium", new LiquidHeliumFluid.Source());
	public static final FlowingFluid FLOWING_METALLIC_HYDROGEN = register("flowing_metallic_hydrogen", new MetallicHydrogenFluid.Flowing());
	public static final FlowingFluid METALLIC_HYDROGEN = register("metallic_hydrogen", new MetallicHydrogenFluid.Source());
	public static final FlowingFluid FLOWING_LIQUID_AMMONIA = register("flowing_liquid_ammonia", new LiquidAmmoniaFluid.Flowing());
	public static final FlowingFluid LIQUID_AMMONIA = register("liquid_ammonia", new LiquidAmmoniaFluid.Source());
	public static final FlowingFluid FLOWING_LIQUID_METHANE = register("flowing_liquid_methane", new LiquidMethaneFluid.Flowing());
	public static final FlowingFluid LIQUID_METHANE = register("liquid_methane", new LiquidMethaneFluid.Source());
	public static final FlowingFluid FLOWING_SOLAR_PLASMA = register("flowing_solar_plasma", new SolarPlasmaFluid.Flowing());
	public static final FlowingFluid SOLAR_PLASMA = register("solar_plasma", new SolarPlasmaFluid.Source());

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
