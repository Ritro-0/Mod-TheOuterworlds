package com.theouterworld.registry;

import com.mojang.serialization.MapCodec;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.worldgen.AmberworldMethanePondFeature;
import com.theouterworld.worldgen.AnhydriteCavePaintFeature;
import com.theouterworld.worldgen.BasaltBoulderClusterFeature;
import com.theouterworld.worldgen.BeyondlandsAnorthositeBlobFeature;
import com.theouterworld.worldgen.BeyondlandsCaveFeature;
import com.theouterworld.worldgen.CalderaFeature;
import com.theouterworld.worldgen.CraterFeature;
import com.theouterworld.worldgen.DeepworldCloudDeckFeature;
import com.theouterworld.worldgen.DeepworldHeliumLayerFeature;
import com.theouterworld.worldgen.EdgeworldCloudDeckFeature;
import com.theouterworld.worldgen.EmberworldLavaPondFeature;
import com.theouterworld.worldgen.ErgDuneFeature;
import com.theouterworld.worldgen.FarworldCloudDeckFeature;
import com.theouterworld.worldgen.FrostworldIceCrackFeature;
import com.theouterworld.worldgen.GraphiteBoulderFeature;
import com.theouterworld.worldgen.HighworldCloudDeckFeature;
import com.theouterworld.worldgen.InnerworldCraterFeature;
import com.theouterworld.worldgen.IonicAmmoniaDepositFeature;
import com.theouterworld.worldgen.IonicMethaneDepositFeature;
import com.theouterworld.worldgen.KharaxPillarFeature;
import com.theouterworld.worldgen.LavaTubeFeature;
import com.theouterworld.worldgen.MetallicHeliumDepositFeature;
import com.theouterworld.worldgen.MetallicHydrogenDepositFeature;
import com.theouterworld.worldgen.MoonCraterFeature;
import com.theouterworld.worldgen.NearworldCaveLavaPoolFeature;
import com.theouterworld.worldgen.NearworldLavaLakeFeature;
import com.theouterworld.worldgen.NearworldLavaTubeFeature;
import com.theouterworld.worldgen.NearworldOsmiumUnderLavaFeature;
import com.theouterworld.worldgen.NearworldTubeCaveFeature;
import com.theouterworld.worldgen.NearworldUnderVolcanoOlivineGeodeFeature;
import com.theouterworld.worldgen.NearworldVolcanoFeature;
import com.theouterworld.worldgen.OuterworldOlivineGeodeFeature;
import com.theouterworld.worldgen.OxidizedBasaltPebbleFeature;
import com.theouterworld.worldgen.PotatoworldsBodyFeature;
import com.theouterworld.worldgen.ScarletlandsCraterFeature;
import com.theouterworld.worldgen.ScarletlandsTholinBlobFeature;
import com.theouterworld.worldgen.SpongeworldBodyFeature;
import com.theouterworld.worldgen.SunLayersFeature;
import com.theouterworld.worldgen.WanderlandsCraterFeature;
import com.theouterworld.worldgen.WanderlandsIceCavernFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ModFeatures {
	public static final MapCodec<CraterFeature> CRATER = register("crater", CraterFeature.CODEC);
	public static final MapCodec<MoonCraterFeature> MOON_CRATER = register("moon_crater", MoonCraterFeature.CODEC);
	public static final MapCodec<InnerworldCraterFeature> INNERWORLD_CRATER = register("innerworld_crater", InnerworldCraterFeature.CODEC);
	public static final MapCodec<GraphiteBoulderFeature> GRAPHITE_BOULDER = register("graphite_boulder", GraphiteBoulderFeature.CODEC);
	public static final MapCodec<ErgDuneFeature> ERG_DUNE = register("erg_dune", ErgDuneFeature.CODEC);
	public static final MapCodec<BasaltBoulderClusterFeature> BASALT_BOULDER_CLUSTER = register("basalt_boulder_cluster", BasaltBoulderClusterFeature.CODEC);
	public static final MapCodec<CalderaFeature> CALDERA = register("caldera", CalderaFeature.CODEC);
	public static final MapCodec<LavaTubeFeature> LAVA_TUBE = register("lava_tube", LavaTubeFeature.CODEC);
	public static final MapCodec<OxidizedBasaltPebbleFeature> OXIDIZED_BASALT_PEBBLE = register("oxidized_basalt_pebble", OxidizedBasaltPebbleFeature.CODEC);
	public static final MapCodec<KharaxPillarFeature> KHARAX_PILLAR = register("kharax_pillar", KharaxPillarFeature.CODEC);
	public static final MapCodec<NearworldVolcanoFeature> NEARWORLD_VOLCANO = register("nearworld_volcano", NearworldVolcanoFeature.CODEC);
	public static final MapCodec<NearworldLavaLakeFeature> NEARWORLD_LAVA_LAKE = register("nearworld_lava_lake", NearworldLavaLakeFeature.CODEC);
	public static final MapCodec<NearworldCaveLavaPoolFeature> NEARWORLD_CAVE_LAVA_POOL = register("nearworld_cave_lava_pool", NearworldCaveLavaPoolFeature.CODEC);
	public static final MapCodec<NearworldTubeCaveFeature> NEARWORLD_TUBE_CAVE = register("nearworld_tube_cave", NearworldTubeCaveFeature.CODEC);
	public static final MapCodec<NearworldLavaTubeFeature> NEARWORLD_LAVA_TUBE = register("nearworld_lava_tube", NearworldLavaTubeFeature.CODEC);
	public static final MapCodec<NearworldOsmiumUnderLavaFeature> NEARWORLD_OSMIUM_UNDER_LAVA = register("nearworld_osmium_under_lava", NearworldOsmiumUnderLavaFeature.CODEC);
	public static final MapCodec<AnhydriteCavePaintFeature> ANHYDRITE_CAVE_PAINT = register("anhydrite_cave_paint", AnhydriteCavePaintFeature.CODEC);
	public static final MapCodec<NearworldUnderVolcanoOlivineGeodeFeature> NEARWORLD_UNDER_VOLCANO_OLIVINE_GEODE = register("nearworld_under_volcano_olivine_geode", NearworldUnderVolcanoOlivineGeodeFeature.CODEC);
	public static final MapCodec<HighworldCloudDeckFeature> HIGHWORLD_CLOUD_DECK = register("highworld_cloud_deck", HighworldCloudDeckFeature.CODEC);
	public static final MapCodec<MetallicHydrogenDepositFeature> METALLIC_HYDROGEN_DEPOSIT = register("metallic_hydrogen_deposit", MetallicHydrogenDepositFeature.CODEC);
	public static final MapCodec<DeepworldCloudDeckFeature> DEEPWORLD_CLOUD_DECK = register("deepworld_cloud_deck", DeepworldCloudDeckFeature.CODEC);
	public static final MapCodec<DeepworldHeliumLayerFeature> DEEPWORLD_HELIUM_LAYER = register("deepworld_helium_layer", DeepworldHeliumLayerFeature.CODEC);
	public static final MapCodec<MetallicHeliumDepositFeature> METALLIC_HELIUM_DEPOSIT = register("metallic_helium_deposit", MetallicHeliumDepositFeature.CODEC);
	public static final MapCodec<FarworldCloudDeckFeature> FARWORLD_CLOUD_DECK = register("farworld_cloud_deck", FarworldCloudDeckFeature.CODEC);
	public static final MapCodec<IonicAmmoniaDepositFeature> IONIC_AMMONIA_DEPOSIT = register("ionic_ammonia_deposit", IonicAmmoniaDepositFeature.CODEC);
	public static final MapCodec<EdgeworldCloudDeckFeature> EDGEWORLD_CLOUD_DECK = register("edgeworld_cloud_deck", EdgeworldCloudDeckFeature.CODEC);
	public static final MapCodec<IonicMethaneDepositFeature> IONIC_METHANE_DEPOSIT = register("ionic_methane_deposit", IonicMethaneDepositFeature.CODEC);
	public static final MapCodec<EmberworldLavaPondFeature> EMBERWORLD_LAVA_POND = register("emberworld_lava_pond", EmberworldLavaPondFeature.CODEC);
	public static final MapCodec<FrostworldIceCrackFeature> FROSTWORLD_ICE_CRACK = register("frostworld_ice_crack", FrostworldIceCrackFeature.CODEC);
	public static final MapCodec<AmberworldMethanePondFeature> AMBERWORLD_METHANE_POND = register("amberworld_methane_pond", AmberworldMethanePondFeature.CODEC);
	public static final MapCodec<SpongeworldBodyFeature> SPONGEWORLD_BODY = register("spongeworld_body", SpongeworldBodyFeature.CODEC);
	public static final MapCodec<PotatoworldsBodyFeature> POTATOWORLDS_BODY = register("potatoworlds_body", PotatoworldsBodyFeature.CODEC);
	public static final MapCodec<WanderlandsCraterFeature> WANDERLANDS_CRATER = register("wanderlands_crater", WanderlandsCraterFeature.CODEC);
	public static final MapCodec<WanderlandsIceCavernFeature> WANDERLANDS_ICE_CAVERN = register("wanderlands_ice_cavern", WanderlandsIceCavernFeature.CODEC);
	public static final MapCodec<BeyondlandsCaveFeature> BEYONDLANDS_CAVE = register("beyondlands_cave", BeyondlandsCaveFeature.CODEC);
	public static final MapCodec<BeyondlandsAnorthositeBlobFeature> BEYONDLANDS_ANORTHOSITE_BLOB = register("beyondlands_anorthosite_blob", BeyondlandsAnorthositeBlobFeature.CODEC);
	public static final MapCodec<ScarletlandsCraterFeature> SCARLETLANDS_CRATER = register("scarletlands_crater", ScarletlandsCraterFeature.CODEC);
	public static final MapCodec<ScarletlandsTholinBlobFeature> SCARLETLANDS_THOLIN_BLOB = register("scarletlands_tholin_blob", ScarletlandsTholinBlobFeature.CODEC);
	public static final MapCodec<OuterworldOlivineGeodeFeature> OUTERWORLD_OLIVINE_GEODE = register("outerworld_olivine_geode", OuterworldOlivineGeodeFeature.CODEC);
	public static final MapCodec<SunLayersFeature> SUN_LAYERS = register("sun_layers", SunLayersFeature.CODEC);

	private static <F extends Feature> MapCodec<F> register(String name, MapCodec<F> codec) {
		return Registry.register(BuiltInRegistries.FEATURE_TYPE, OuterWorldMod.id(name), codec);
	}

	public static void registerModFeatures() {
		OuterWorldMod.LOGGER.info("Registering features for {}", OuterWorldMod.MOD_ID);
	}
}
