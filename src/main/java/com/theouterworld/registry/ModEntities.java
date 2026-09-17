package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.KharaxEntity;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.entity.OpalineNickelFlailEntity;
import com.theouterworld.entity.OxidizableIronGolemEntity;
import com.theouterworld.entity.PrimedPerchlorateCharge;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModEntities {
	public static final EntityType<OxidizableIronGolemEntity> OXIDIZABLE_IRON_GOLEM = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		OuterWorldMod.id("oxidizable_iron_golem"),
		EntityType.Builder.of(OxidizableIronGolemEntity::new, MobCategory.MISC)
			.sized(1.4f, 2.7f)
			.clientTrackingRange(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, OuterWorldMod.id("oxidizable_iron_golem")))
	);

	public static final EntityType<PrimedPerchlorateCharge> PRIMED_PERCHLORATE_CHARGE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		OuterWorldMod.id("primed_perchlorate_charge"),
		EntityType.Builder.of(PrimedPerchlorateCharge::create, MobCategory.MISC)
			.sized(0.98f, 0.98f)
			.eyeHeight(0.15f)
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, OuterWorldMod.id("primed_perchlorate_charge")))
	);

	public static final EntityType<OpalineNickelFlailEntity> OPALINE_NICKEL_FLAIL = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		OuterWorldMod.id("opaline_nickel_flail"),
		EntityType.Builder.of(OpalineNickelFlailEntity::create, MobCategory.MISC)
			.sized(0.4f, 0.4f)
			.clientTrackingRange(64)
			.updateInterval(1)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, OuterWorldMod.id("opaline_nickel_flail")))
	);

	public static final EntityType<KharaxEntity> KHARAX = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		OuterWorldMod.id("kharax"),
		EntityType.Builder.of(KharaxEntity::new, MobCategory.CREATURE)
			.sized(1.2f, 1.35f)
			.eyeHeight(1.05f)
			.clientTrackingRange(8)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, OuterWorldMod.id("kharax")))
	);

	public static void registerModEntities() {
		OuterWorldMod.LOGGER.info("Registering entities for {}", OuterWorldMod.MOD_ID);
		FabricDefaultAttributeRegistry.register(
			OXIDIZABLE_IRON_GOLEM,
			OxidizableIronGolemEntity.createOxidizableIronGolemAttributes()
		);
		FabricDefaultAttributeRegistry.register(KHARAX, KharaxEntity.createAttributes());
		SpawnPlacements.register(
			KHARAX,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) ->
				ModDimensions.isOuterworld(world.getLevel().dimension())
					&& Mob.checkMobSpawnRules(type, world, reason, pos, random)
		);
	}
}
