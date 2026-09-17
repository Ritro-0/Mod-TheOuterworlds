package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.worldgen.AmberworldMethanePondFeature;
import com.theouterworld.worldgen.SpongeworldBodyFeature;
import com.theouterworld.worldgen.PotatoworldsBodyFeature;
import com.theouterworld.worldgen.AnhydriteCavePaintFeature;
import com.theouterworld.worldgen.BasaltBoulderClusterFeature;
import com.theouterworld.worldgen.CalderaFeature;
import com.theouterworld.worldgen.CraterFeature;
import com.theouterworld.worldgen.ErgDuneFeature;
import com.theouterworld.worldgen.GraphiteBoulderFeature;
import com.theouterworld.worldgen.InnerworldCraterFeature;
import com.theouterworld.worldgen.KharaxPillarFeature;
import com.theouterworld.worldgen.LavaTubeFeature;
import com.theouterworld.worldgen.MoonCraterFeature;
import com.theouterworld.worldgen.NearworldLavaLakeFeature;
import com.theouterworld.worldgen.NearworldLavaTubeFeature;
import com.theouterworld.worldgen.NearworldOsmiumUnderLavaFeature;
import com.theouterworld.worldgen.NearworldTubeCaveFeature;
import com.theouterworld.worldgen.NearworldUnderVolcanoOlivineGeodeFeature;
import com.theouterworld.worldgen.NearworldVolcanoFeature;
import com.theouterworld.worldgen.DeepworldCloudDeckFeature;
import com.theouterworld.worldgen.DeepworldHeliumLayerFeature;
import com.theouterworld.worldgen.EdgeworldCloudDeckFeature;
import com.theouterworld.worldgen.EmberworldLavaPondFeature;
import com.theouterworld.worldgen.FarworldCloudDeckFeature;
import com.theouterworld.worldgen.FrostworldIceCrackFeature;
import com.theouterworld.worldgen.HighworldCloudDeckFeature;
import com.theouterworld.worldgen.IonicAmmoniaDepositFeature;
import com.theouterworld.worldgen.IonicMethaneDepositFeature;
import com.theouterworld.worldgen.MetallicHeliumDepositFeature;
import com.theouterworld.worldgen.MetallicHydrogenDepositFeature;
import com.theouterworld.worldgen.BeyondlandsAnorthositeBlobFeature;
import com.theouterworld.worldgen.BeyondlandsCaveFeature;
import com.theouterworld.worldgen.ScarletlandsCraterFeature;
import com.theouterworld.worldgen.ScarletlandsTholinBlobFeature;
import com.theouterworld.worldgen.WanderlandsCraterFeature;
import com.theouterworld.worldgen.WanderlandsIceCavernFeature;
import com.theouterworld.worldgen.OuterworldOlivineGeodeFeature;
import com.theouterworld.worldgen.OxidizedBasaltPebbleFeature;
import com.theouterworld.worldgen.SunLayersFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ModFeatures {
	public static final Feature<NoneFeatureConfiguration> CRATER = register("crater", new CraterFeature());
	public static final Feature<NoneFeatureConfiguration> MOON_CRATER = register("moon_crater", new MoonCraterFeature());
	public static final Feature<NoneFeatureConfiguration> INNERWORLD_CRATER = register("innerworld_crater", new InnerworldCraterFeature());
	public static final Feature<NoneFeatureConfiguration> GRAPHITE_BOULDER = register("graphite_boulder", new GraphiteBoulderFeature());
	public static final Feature<NoneFeatureConfiguration> ERG_DUNE = register("erg_dune", new ErgDuneFeature());
	public static final Feature<NoneFeatureConfiguration> BASALT_BOULDER_CLUSTER = register("basalt_boulder_cluster", new BasaltBoulderClusterFeature());
	public static final Feature<NoneFeatureConfiguration> CALDERA = register("caldera", new CalderaFeature());
	public static final Feature<NoneFeatureConfiguration> LAVA_TUBE = register("lava_tube", new LavaTubeFeature());
	public static final Feature<NoneFeatureConfiguration> OXIDIZED_BASALT_PEBBLE = register("oxidized_basalt_pebble", new OxidizedBasaltPebbleFeature());
	public static final Feature<NoneFeatureConfiguration> KHARAX_PILLAR = register("kharax_pillar", new KharaxPillarFeature());
	public static final Feature<NoneFeatureConfiguration> NEARWORLD_VOLCANO = register("nearworld_volcano", new NearworldVolcanoFeature());
	public static final Feature<NoneFeatureConfiguration> NEARWORLD_LAVA_LAKE = register("nearworld_lava_lake", new NearworldLavaLakeFeature());
	public static final Feature<NoneFeatureConfiguration> NEARWORLD_TUBE_CAVE = register("nearworld_tube_cave", new NearworldTubeCaveFeature());
	public static final Feature<NoneFeatureConfiguration> NEARWORLD_LAVA_TUBE = register("nearworld_lava_tube", new NearworldLavaTubeFeature());
	public static final Feature<NoneFeatureConfiguration> NEARWORLD_OSMIUM_UNDER_LAVA = register("nearworld_osmium_under_lava", new NearworldOsmiumUnderLavaFeature());
	public static final Feature<NoneFeatureConfiguration> ANHYDRITE_CAVE_PAINT = register("anhydrite_cave_paint", new AnhydriteCavePaintFeature());
	public static final Feature<NoneFeatureConfiguration> NEARWORLD_UNDER_VOLCANO_OLIVINE_GEODE =
		register("nearworld_under_volcano_olivine_geode", new NearworldUnderVolcanoOlivineGeodeFeature());
	public static final Feature<NoneFeatureConfiguration> HIGHWORLD_CLOUD_DECK =
		register("highworld_cloud_deck", new HighworldCloudDeckFeature());
	public static final Feature<NoneFeatureConfiguration> METALLIC_HYDROGEN_DEPOSIT =
		register("metallic_hydrogen_deposit", new MetallicHydrogenDepositFeature());
	public static final Feature<NoneFeatureConfiguration> DEEPWORLD_CLOUD_DECK =
		register("deepworld_cloud_deck", new DeepworldCloudDeckFeature());
	public static final Feature<NoneFeatureConfiguration> DEEPWORLD_HELIUM_LAYER =
		register("deepworld_helium_layer", new DeepworldHeliumLayerFeature());
	public static final Feature<NoneFeatureConfiguration> METALLIC_HELIUM_DEPOSIT =
		register("metallic_helium_deposit", new MetallicHeliumDepositFeature());
	public static final Feature<NoneFeatureConfiguration> FARWORLD_CLOUD_DECK =
		register("farworld_cloud_deck", new FarworldCloudDeckFeature());
	public static final Feature<NoneFeatureConfiguration> IONIC_AMMONIA_DEPOSIT =
		register("ionic_ammonia_deposit", new IonicAmmoniaDepositFeature());
	public static final Feature<NoneFeatureConfiguration> EDGEWORLD_CLOUD_DECK =
		register("edgeworld_cloud_deck", new EdgeworldCloudDeckFeature());
	public static final Feature<NoneFeatureConfiguration> IONIC_METHANE_DEPOSIT =
		register("ionic_methane_deposit", new IonicMethaneDepositFeature());
	public static final Feature<NoneFeatureConfiguration> EMBERWORLD_LAVA_POND =
		register("emberworld_lava_pond", new EmberworldLavaPondFeature());
	public static final Feature<NoneFeatureConfiguration> FROSTWORLD_ICE_CRACK =
		register("frostworld_ice_crack", new FrostworldIceCrackFeature());
	public static final Feature<NoneFeatureConfiguration> AMBERWORLD_METHANE_POND =
		register("amberworld_methane_pond", new AmberworldMethanePondFeature());
	public static final Feature<NoneFeatureConfiguration> SPONGEWORLD_BODY =
		register("spongeworld_body", new SpongeworldBodyFeature());
	public static final Feature<NoneFeatureConfiguration> POTATOWORLDS_BODY =
		register("potatoworlds_body", new PotatoworldsBodyFeature());
	public static final Feature<NoneFeatureConfiguration> WANDERLANDS_CRATER =
		register("wanderlands_crater", new WanderlandsCraterFeature());
	public static final Feature<NoneFeatureConfiguration> WANDERLANDS_ICE_CAVERN =
		register("wanderlands_ice_cavern", new WanderlandsIceCavernFeature());
	public static final Feature<NoneFeatureConfiguration> BEYONDLANDS_CAVE =
		register("beyondlands_cave", new BeyondlandsCaveFeature());
	public static final Feature<NoneFeatureConfiguration> BEYONDLANDS_ANORTHOSITE_BLOB =
		register("beyondlands_anorthosite_blob", new BeyondlandsAnorthositeBlobFeature());
	public static final Feature<NoneFeatureConfiguration> SCARLETLANDS_CRATER =
		register("scarletlands_crater", new ScarletlandsCraterFeature());
	public static final Feature<NoneFeatureConfiguration> SCARLETLANDS_THOLIN_BLOB =
		register("scarletlands_tholin_blob", new ScarletlandsTholinBlobFeature());
	public static final Feature<NoneFeatureConfiguration> OUTERWORLD_OLIVINE_GEODE =
		register("outerworld_olivine_geode", new OuterworldOlivineGeodeFeature());
	public static final Feature<NoneFeatureConfiguration> SUN_LAYERS =
		register("sun_layers", new SunLayersFeature());

	private static Feature<NoneFeatureConfiguration> register(String name, Feature<NoneFeatureConfiguration> feature) {
		return Registry.register(BuiltInRegistries.FEATURE, OuterWorldMod.id(name), feature);
	}

	public static void registerModFeatures() {
		OuterWorldMod.LOGGER.info("Registering features for {}", OuterWorldMod.MOD_ID);
	}
}
