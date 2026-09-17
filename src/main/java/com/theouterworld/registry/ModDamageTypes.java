package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
	public static final ResourceKey<DamageType> VACUUM = ResourceKey.create(
		Registries.DAMAGE_TYPE,
		OuterWorldMod.id("vacuum")
	);

	public static final ResourceKey<DamageType> SOLAR_IRRADIATION = ResourceKey.create(
		Registries.DAMAGE_TYPE,
		OuterWorldMod.id("solar_irradiation")
	);

	public static final ResourceKey<DamageType> EXTREME_PRESSURE = ResourceKey.create(
		Registries.DAMAGE_TYPE,
		OuterWorldMod.id("extreme_pressure")
	);

	public static final ResourceKey<DamageType> SOLAR_DISSOLUTION = ResourceKey.create(
		Registries.DAMAGE_TYPE,
		OuterWorldMod.id("solar_dissolution")
	);

	private ModDamageTypes() {
	}
}
