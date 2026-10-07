package com.theouterworld.world;

import com.theouterworld.entity.OceanVentEntity;
import com.theouterworld.entity.StrandHydraEntity;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Frostworld fish use the vanilla water-spawn cycle. Extra checks keep each
 * species where it already belongs: feeders by vents, hydras on ravine bedrock,
 * and the rest in open water.
 */
public final class FrostworldFishSpawns {
	private static final int VENT_RANGE = 18;
	private static final int HYDRA_SPACING = 18;
	/** Keep jellies off the water the player is looking through. */
	private static final double JELLY_PLAYER_RANGE = 32.0;

	private FrostworldFishSpawns() {
	}

	public static <T extends Mob> boolean driftmite(
		EntityType<T> type,
		ServerLevelAccessor level,
		EntitySpawnReason reason,
		BlockPos pos,
		RandomSource random
	) {
		return inFrostworld(level) && surfaceSchool(level, pos);
	}

	public static <T extends Mob> boolean jelly(
		EntityType<T> type,
		ServerLevelAccessor level,
		EntitySpawnReason reason,
		BlockPos pos,
		RandomSource random
	) {
		if (!inFrostworld(level) || !level.getFluidState(pos).is(FluidTags.WATER)) {
			return false;
		}
		AABB nearPlayer = new AABB(pos).inflate(JELLY_PLAYER_RANGE);
		return level.getLevel().getEntitiesOfClass(Player.class, nearPlayer, Entity::isAlive).isEmpty();
	}

	public static <T extends Mob> boolean feeder(
		EntityType<T> type,
		ServerLevelAccessor level,
		EntitySpawnReason reason,
		BlockPos pos,
		RandomSource random
	) {
		if (!inFrostworld(level) || !level.getFluidState(pos).is(FluidTags.WATER)) {
			return false;
		}
		AABB near = new AABB(pos).inflate(VENT_RANGE, 12.0, VENT_RANGE);
		return !level.getLevel().getEntitiesOfClass(OceanVentEntity.class, near, Entity::isAlive).isEmpty();
	}

	public static <T extends Mob> boolean hydra(
		EntityType<T> type,
		ServerLevelAccessor level,
		EntitySpawnReason reason,
		BlockPos pos,
		RandomSource random
	) {
		if (!inFrostworld(level) || !openToBedrock(level, pos)) {
			return false;
		}
		AABB spaced = new AABB(pos).inflate(HYDRA_SPACING);
		return level.getLevel().getEntitiesOfClass(StrandHydraEntity.class, spaced, Mob::isAlive).isEmpty();
	}

	private static boolean inFrostworld(ServerLevelAccessor level) {
		return ModDimensions.isFrostworld(level.getLevel().dimension());
	}

	/** Same band as cod: within 13 blocks of sea level, water below, water above. */
	private static boolean surfaceSchool(ServerLevelAccessor level, BlockPos pos) {
		int sea = level.getSeaLevel();
		if (pos.getY() < sea - 13 || pos.getY() > sea) {
			return false;
		}
		return level.getFluidState(pos.below()).is(FluidTags.WATER)
			&& level.getBlockState(pos.above()).is(Blocks.WATER);
	}

	/**
	 * Open water with bedrock straight below. A hydra placed in that column
	 * sinks onto the ravine floor.
	 */
	private static boolean openToBedrock(ServerLevelAccessor level, BlockPos pos) {
		if (!level.getFluidState(pos).is(FluidTags.WATER)) {
			return false;
		}
		BlockPos.MutableBlockPos cursor = pos.mutable();
		for (int i = 0; i < 160; i++) {
			cursor.move(Direction.DOWN);
			if (level.getFluidState(cursor).is(FluidTags.WATER)) {
				continue;
			}
			return level.getBlockState(cursor).is(Blocks.BEDROCK);
		}
		return false;
	}
}
