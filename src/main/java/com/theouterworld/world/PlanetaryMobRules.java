package com.theouterworld.world;

import com.theouterworld.registry.ModDimensions;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Which vanilla mobs skip which planetary hazards.
 * Pet wolves instead use doggy glass for air and wolf armor for pressure and heat.
 */
public final class PlanetaryMobRules {
	private static final Set<EntityType<?>> NO_BREATHE = Set.of(
		EntityTypes.CAMEL_HUSK,
		EntityTypes.SKELETON_HORSE,
		EntityTypes.SULFUR_CUBE,
		EntityTypes.ZOMBIE_HORSE,
		EntityTypes.SLIME,
		EntityTypes.MAGMA_CUBE,
		EntityTypes.DROWNED,
		EntityTypes.ENDERMAN,
		EntityTypes.IRON_GOLEM,
		EntityTypes.ZOMBIE_NAUTILUS,
		EntityTypes.BOGGED,
		EntityTypes.BREEZE,
		EntityTypes.CREAKING,
		EntityTypes.CREEPER,
		EntityTypes.ENDERMITE,
		EntityTypes.HUSK,
		EntityTypes.PARCHED,
		EntityTypes.PHANTOM,
		EntityTypes.SHULKER,
		EntityTypes.SKELETON,
		EntityTypes.STRAY,
		EntityTypes.WARDEN,
		EntityTypes.ZOGLIN,
		EntityTypes.WITHER_SKELETON,
		EntityTypes.ZOMBIE,
		EntityTypes.ZOMBIE_VILLAGER,
		EntityTypes.WITHER,
		EntityTypes.ENDER_DRAGON,
		EntityTypes.GIANT
	);

	private static final Set<EntityType<?>> HEAT_IMMUNE = Set.of(
		EntityTypes.SULFUR_CUBE,
		EntityTypes.IRON_GOLEM,
		EntityTypes.PIGLIN,
		EntityTypes.ZOMBIFIED_PIGLIN,
		EntityTypes.BLAZE,
		EntityTypes.PIGLIN_BRUTE,
		EntityTypes.ENDERMAN,
		EntityTypes.ENDERMITE,
		EntityTypes.MAGMA_CUBE,
		EntityTypes.HOGLIN,
		EntityTypes.ZOGLIN,
		EntityTypes.WARDEN,
		EntityTypes.WITHER,
		EntityTypes.ENDER_DRAGON
	);

	/** Fine in Emberworld: vacuum, pressure, and heat. */
	private static final Set<EntityType<?>> EMBER_NATIVE = Set.of(
		EntityTypes.BLAZE,
		EntityTypes.MAGMA_CUBE,
		EntityTypes.STRIDER
	);

	/** Water-breathers that live in a Frostworld ocean even with vacuum above it. */
	private static final Set<EntityType<?>> FROSTWORLD_AQUATIC = Set.of(
		EntityTypes.AXOLOTL,
		EntityTypes.COD,
		EntityTypes.SALMON,
		EntityTypes.SQUID,
		EntityTypes.GLOW_SQUID,
		EntityTypes.TADPOLE,
		EntityTypes.TROPICAL_FISH,
		EntityTypes.NAUTILUS,
		EntityTypes.PUFFERFISH,
		EntityTypes.ELDER_GUARDIAN,
		EntityTypes.GUARDIAN
	);

	private PlanetaryMobRules() {
	}

	public static boolean isVanillaMob(Entity entity) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return id != null && "minecraft".equals(id.getNamespace());
	}

	public static boolean ignoresVacuum(LivingEntity entity) {
		if (NO_BREATHE.contains(entity.getType())) {
			return true;
		}
		return isEmberNativeHere(entity);
	}

	public static boolean ignoresHeat(LivingEntity entity) {
		if (HEAT_IMMUNE.contains(entity.getType()) || hasWolfArmor(entity)) {
			return true;
		}
		return isEmberNativeHere(entity);
	}

	public static boolean ignoresPressure(LivingEntity entity) {
		if (hasWolfArmor(entity)) {
			return true;
		}
		Level level = entity.level();
		return level != null
			&& ModDimensions.isEmberworld(level.dimension())
			&& EMBER_NATIVE.contains(entity.getType());
	}

	public static boolean breathesFrostworldOcean(LivingEntity entity) {
		Level level = entity.level();
		return level != null
			&& ModDimensions.isFrostworld(level.dimension())
			&& FROSTWORLD_AQUATIC.contains(entity.getType())
			&& entity.isInWater();
	}

	public static boolean hasWolfArmor(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.BODY).is(Items.WOLF_ARMOR);
	}

	private static boolean isEmberNativeHere(LivingEntity entity) {
		Level level = entity.level();
		return level != null
			&& ModDimensions.isEmberworld(level.dimension())
			&& EMBER_NATIVE.contains(entity.getType());
	}
}
