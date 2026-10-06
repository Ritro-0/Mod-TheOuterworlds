package com.theouterworld.world;

import com.theouterworld.entity.DriftmiteEntity;
import com.theouterworld.entity.FeederEntity;
import com.theouterworld.entity.FrostworldJellyEntity;
import com.theouterworld.entity.OceanVentEntity;
import com.theouterworld.entity.StrandHydraEntity;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModEntities;
import com.theouterworld.worldgen.OceanFloor;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Places a pack of Frostworld fish when a loaded stretch of ocean has none left.
 * A living pack is left alone. Respawns refill a barren area after the fish are gone.
 */
public final class FrostworldFaunaSpawner {
	private static final int PERIOD = 600;
	private static final double EMPTY_RANGE = 96.0;

	private FrostworldFaunaSpawner() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(FrostworldFaunaSpawner::tick);
	}

	public static void spawnFeederSchool(ServerLevel level, Vec3 center, int count) {
		int placed = 0;
		for (int attempt = 0; attempt < count * 8 && placed < count; attempt++) {
			double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
			double radius = 2.0 + level.getRandom().nextDouble() * 12.0;
			double x = center.x + Math.cos(angle) * radius;
			double y = center.y + 0.4 + level.getRandom().nextDouble() * 6.0;
			double z = center.z + Math.sin(angle) * radius;
			if (spawn(level, ModEntities.FEEDER, x, y, z, 1)) {
				placed++;
			}
		}
	}

	private static void tick(ServerLevel level) {
		if (!ModDimensions.isFrostworld(level.dimension()) || level.getGameTime() % PERIOD != 0) {
			return;
		}
		for (ServerPlayer player : level.players()) {
			if (player.isSpectator() || !player.isAlive()) {
				continue;
			}
			refill(level, player);
		}
	}

	private static void refill(ServerLevel level, ServerPlayer player) {
		RandomSource random = level.getRandom();
		if (level.getEntitiesOfClass(DriftmiteEntity.class, player.getBoundingBox().inflate(EMPTY_RANGE)).isEmpty()) {
			spawnDriftmitePack(level, player, random);
		}
		if (level.getEntitiesOfClass(FrostworldJellyEntity.class, player.getBoundingBox().inflate(EMPTY_RANGE)).isEmpty()) {
			tryJellyCluster(level, player, random);
		}
		if (level.getEntitiesOfClass(StrandHydraEntity.class, player.getBoundingBox().inflate(64.0)).isEmpty()) {
			tryHydra(level, player, random);
		}
		for (OceanVentEntity vent : level.getEntitiesOfClass(OceanVentEntity.class, player.getBoundingBox().inflate(64.0))) {
			if (level.getEntitiesOfClass(FeederEntity.class, vent.getBoundingBox().inflate(28.0)).isEmpty()) {
				spawnFeederSchool(level, vent.position(), 8 + random.nextInt(4));
			}
		}
	}

	private static void spawnDriftmitePack(ServerLevel level, ServerPlayer player, RandomSource random) {
		BlockPos center = randomColumn(player, random, 28, 56);
		int floor = OceanFloor.surfaceY(level, center.getX(), center.getZ());
		int base = floor == Integer.MIN_VALUE ? 28 : Mth.clamp(floor + 8, 6, 78);
		int wanted = 8 + random.nextInt(4);
		int placed = 0;
		for (int attempt = 0; attempt < wanted * 6 && placed < wanted; attempt++) {
			int x = center.getX() + random.nextInt(13) - 6;
			int z = center.getZ() + random.nextInt(13) - 6;
			int y = base + random.nextInt(9) - 2;
			if (y >= 88) {
				continue;
			}
			if (spawn(level, ModEntities.DRIFTMITE, x + 0.5, y, z + 0.5, 1)) {
				placed++;
			}
		}
	}

	private static void tryJellyCluster(ServerLevel level, ServerPlayer player, RandomSource random) {
		BlockPos origin = randomColumn(player, random, 28, 56);
		int floor = OceanFloor.surfaceY(level, origin.getX(), origin.getZ());
		if (floor == Integer.MIN_VALUE || !waterColumn(level, new BlockPos(origin.getX(), floor + 1, origin.getZ()), 2)) {
			return;
		}
		int wanted = 6 + random.nextInt(3);
		Set<BlockPos> used = new HashSet<>();
		int placed = 0;
		for (int attempt = 0; attempt < 28 && placed < wanted; attempt++) {
			int x = origin.getX() + random.nextInt(15) - 7;
			int z = origin.getZ() + random.nextInt(15) - 7;
			int surface = OceanFloor.surfaceY(level, x, z);
			if (surface == Integer.MIN_VALUE) {
				continue;
			}
			BlockPos feet = new BlockPos(x, surface + 1, z);
			if (used.contains(feet) || !waterColumn(level, feet, 2)) {
				continue;
			}
			if (!level.getEntitiesOfClass(
				FrostworldJellyEntity.class,
				new AABB(x, feet.getY(), z, x + 1.0, feet.getY() + 2.0, z + 1.0)
			).isEmpty()) {
				continue;
			}
			if (spawn(level, ModEntities.FROSTWORLD_JELLY, x + 0.5, feet.getY(), z + 0.5, 2)) {
				used.add(feet);
				placed++;
			}
		}
	}

	private static void tryHydra(ServerLevel level, ServerPlayer player, RandomSource random) {
		for (int attempt = 0; attempt < 20; attempt++) {
			BlockPos column = randomColumn(player, random, 16, 56);
			int floor = OceanFloor.surfaceY(level, column.getX(), column.getZ());
			if (floor == Integer.MIN_VALUE) {
				continue;
			}
			BlockPos ground = new BlockPos(column.getX(), floor, column.getZ());
			if (!level.getBlockState(ground).is(Blocks.BEDROCK)) {
				continue;
			}
			BlockPos feet = ground.above();
			if (!waterColumn(level, feet, 3)) {
				continue;
			}
			if (!level.getEntitiesOfClass(StrandHydraEntity.class, new AABB(feet).inflate(40.0)).isEmpty()) {
				continue;
			}
			if (spawn(level, ModEntities.STRAND_HYDRA, feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 3)) {
				return;
			}
		}
	}

	private static BlockPos randomColumn(ServerPlayer player, RandomSource random, int minDistance, int maxDistance) {
		double angle = random.nextDouble() * Math.PI * 2.0;
		double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
		int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
		int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
		return new BlockPos(x, player.getBlockY(), z);
	}

	private static boolean waterColumn(ServerLevel level, BlockPos feet, int height) {
		if (!level.isLoaded(feet)) {
			return false;
		}
		for (int i = 0; i < height; i++) {
			if (!level.getFluidState(feet.above(i)).is(FluidTags.WATER)) {
				return false;
			}
		}
		return true;
	}

	private static <T extends Mob> boolean spawn(ServerLevel level, EntityType<T> type, double x, double y, double z, int waterHeight) {
		BlockPos feet = BlockPos.containing(x, y, z);
		if (!waterColumn(level, feet, waterHeight)) {
			return false;
		}
		T mob = type.create(level, EntitySpawnReason.NATURAL);
		if (mob == null) {
			return false;
		}
		mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (!level.isUnobstructed(mob)) {
			return false;
		}
		mob.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), EntitySpawnReason.NATURAL, null);
		level.addFreshEntity(mob);
		return true;
	}
}
