package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.worldgen.BasaltBoulderClusterFeature;
import com.theouterworld.worldgen.CalderaFeature;
import com.theouterworld.worldgen.CraterFeature;
import com.theouterworld.worldgen.ErgDuneFeature;
import com.theouterworld.worldgen.InnerworldCraterFeature;
import com.theouterworld.worldgen.KharaxPillarFeature;
import com.theouterworld.worldgen.LavaTubeFeature;
import com.theouterworld.worldgen.OxidizedBasaltPebbleFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ModFeatures {
	public static final Feature<NoneFeatureConfiguration> CRATER = register("crater", new CraterFeature());
	public static final Feature<NoneFeatureConfiguration> INNERWORLD_CRATER = register("innerworld_crater", new InnerworldCraterFeature());
	public static final Feature<NoneFeatureConfiguration> ERG_DUNE = register("erg_dune", new ErgDuneFeature());
	public static final Feature<NoneFeatureConfiguration> BASALT_BOULDER_CLUSTER = register("basalt_boulder_cluster", new BasaltBoulderClusterFeature());
	public static final Feature<NoneFeatureConfiguration> CALDERA = register("caldera", new CalderaFeature());
	public static final Feature<NoneFeatureConfiguration> LAVA_TUBE = register("lava_tube", new LavaTubeFeature());
	public static final Feature<NoneFeatureConfiguration> OXIDIZED_BASALT_PEBBLE = register("oxidized_basalt_pebble", new OxidizedBasaltPebbleFeature());
	public static final Feature<NoneFeatureConfiguration> KHARAX_PILLAR = register("kharax_pillar", new KharaxPillarFeature());

	private static Feature<NoneFeatureConfiguration> register(String name, Feature<NoneFeatureConfiguration> feature) {
		return Registry.register(BuiltInRegistries.FEATURE, OuterWorldMod.id(name), feature);
	}

	public static void registerModFeatures() {
		OuterWorldMod.LOGGER.info("Registering features for {}", OuterWorldMod.MOD_ID);
	}
}
