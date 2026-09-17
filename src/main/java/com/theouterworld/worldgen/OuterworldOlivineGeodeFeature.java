package com.theouterworld.worldgen;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places the olivine geode configured feature only in Outerworld. Other dimensions
 * (ice dwarfs especially) must never grow olivine, even if a host block matches.
 */
public class OuterworldOlivineGeodeFeature extends Feature<NoneFeatureConfiguration> {
	private static final ResourceKey<ConfiguredFeature<?, ?>> OLIVINE_GEODE =
		ResourceKey.create(Registries.CONFIGURED_FEATURE, OuterWorldMod.id("olivine_geode"));

	public OuterworldOlivineGeodeFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		if (!ModDimensions.isOuterworld(world.getLevel().dimension())) {
			return false;
		}
		Holder.Reference<ConfiguredFeature<?, ?>> geode = world.registryAccess()
			.lookupOrThrow(Registries.CONFIGURED_FEATURE)
			.get(OLIVINE_GEODE)
			.orElse(null);
		if (geode == null) {
			return false;
		}
		return geode.value().place(world, context.chunkGenerator(), context.random(), context.origin());
	}
}
