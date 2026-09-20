package com.theouterworld.worldgen;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;

/**
 * Places the olivine geode configured feature only in Outerworld. Other dimensions
 * (ice dwarfs especially) must never grow olivine, even if a host block matches.
 */
public class OuterworldOlivineGeodeFeature implements Feature {
	public static final MapCodec<OuterworldOlivineGeodeFeature> CODEC = MapCodec.unit(OuterworldOlivineGeodeFeature::new);

	private static final ResourceKey<Feature> OLIVINE_GEODE =
		ResourceKey.create(Registries.FEATURE, OuterWorldMod.id("olivine_geode"));

	public OuterworldOlivineGeodeFeature() {
	}

	@Override
	public MapCodec<OuterworldOlivineGeodeFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		if (!ModDimensions.isOuterworld(world.getLevel().dimension())) {
			return false;
		}
		Holder.Reference<Feature> geode = world.registryAccess()
			.lookupOrThrow(Registries.FEATURE)
			.get(OLIVINE_GEODE)
			.orElse(null);
		if (geode == null) {
			return false;
		}
		return geode.value().place(world, chunkGenerator, random, origin);
	}
}
