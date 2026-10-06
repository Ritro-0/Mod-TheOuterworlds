package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.WeaverPadBlock;
import com.theouterworld.block.WeaverPadBlockEntity;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.world.WeaverAbsence;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/** Empty Weaver Pads in a loaded colony, and the home plate that belongs to each one. */
public final class WeaverHomes {
	/** Far enough to cover a colony when the Weaver has been led off toward the stalks. */
	private static final int PAD_RANGE = 128;
	private static final int PLATE_SEARCH = 20;
	private static final int PLATE_Y = 12;
	private static final Map<Long, UUID> RESERVED = new HashMap<>();

	private WeaverHomes() {
	}

	public record Vacancy(BlockPos bed, @Nullable BlockPos plate) {
	}

	public static boolean reserve(BlockPos bed, UUID holder) {
		synchronized (RESERVED) {
			UUID current = RESERVED.get(bed.asLong());
			if (current != null && !current.equals(holder)) {
				return false;
			}
			RESERVED.put(bed.asLong(), holder);
			return true;
		}
	}

	public static void release(@Nullable BlockPos bed, UUID holder) {
		if (bed == null || holder == null) {
			return;
		}
		synchronized (RESERVED) {
			if (holder.equals(RESERVED.get(bed.asLong()))) {
				RESERVED.remove(bed.asLong());
			}
		}
	}

	/**
	 * A pad whose loaded owner lives in this dimension but is homed somewhere else is cleared.
	 * Death, a dimension change, and walking out of range wait for the morning head count.
	 */
	public static void reconcile(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		if (colony == 0L) {
			return;
		}
		BlockPos origin = colonyOrigin(weaver);
		int minCx = (origin.getX() - PAD_RANGE) >> 4;
		int maxCx = (origin.getX() + PAD_RANGE) >> 4;
		int minCz = (origin.getZ() - PAD_RANGE) >> 4;
		int maxCz = (origin.getZ() + PAD_RANGE) >> 4;
		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (var entry : chunk.getBlockEntities().entrySet()) {
					if (!(entry.getValue() instanceof WeaverPadBlockEntity pad)) {
						continue;
					}
					UUID owner = pad.getOwner();
					BlockPos pos = entry.getKey();
					if (owner == null || origin.distSqr(pos) > (double) PAD_RANGE * PAD_RANGE) {
						continue;
					}
					if (!sameColony(level, pos, colony)) {
						continue;
					}
					WeaverEntity who = findLoaded(level, owner);
					if (who == null || who.level() != level) {
						continue;
					}
					boolean homeIsThisPad = who.hasHome() && WeaverEntity.isSameBed(level, who.getHomePosition(), pos);
					if (!homeIsThisPad) {
						WeaverPadBlock.release(level, pos, owner);
					}
				}
			}
		}
	}

	/**
	 * After the morning leap window. A pad opens only when its Weaver is dead, in another
	 * dimension, or farther than {@link WeaverEntity#HOME_ABANDON_DISTANCE} from the colony.
	 * Failing to land at the meeting spot is not absence: a Weaver still alive in this
	 * dimension and inside that range keeps the bunk, even if pathfinding left them behind.
	 */
	public static void condemnAbsentees(ServerLevel level, long colony, BlockPos center) {
		if (colony == 0L) {
			return;
		}
		WeaverAbsence saved = WeaverAbsence.get(level);
		double limit = (double) WeaverEntity.HOME_ABANDON_DISTANCE * WeaverEntity.HOME_ABANDON_DISTANCE;
		int minCx = (center.getX() - PAD_RANGE) >> 4;
		int maxCx = (center.getX() + PAD_RANGE) >> 4;
		int minCz = (center.getZ() - PAD_RANGE) >> 4;
		int maxCz = (center.getZ() + PAD_RANGE) >> 4;
		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (var entry : chunk.getBlockEntities().entrySet()) {
					if (!(entry.getValue() instanceof WeaverPadBlockEntity pad)) {
						continue;
					}
					UUID owner = pad.getOwner();
					BlockPos pos = entry.getKey();
					if (owner == null || center.distSqr(pos) > (double) PAD_RANGE * PAD_RANGE) {
						continue;
					}
					if (!sameColony(level, pos, colony)) {
						continue;
					}
					WeaverEntity who = findLoaded(level, owner);
					if (who != null && who.isAlive() && who.level() == level
						&& who.distanceToSqr(center.getX() + 0.5, center.getY(), center.getZ() + 0.5) <= limit) {
						saved.clear(colony, owner);
						if (!who.hasHome() || !WeaverEntity.isSameBed(level, who.getHomePosition(), pos)) {
							who.claimBed(pos, findPlate(level, pos));
						}
						continue;
					}
					boolean otherDimension = who != null && who.isAlive() && who.level() != level;
					boolean tooFar = who != null && who.isAlive() && who.level() == level
						&& who.distanceToSqr(center.getX() + 0.5, center.getY(), center.getZ() + 0.5) > limit;
					boolean recordedGone = saved.isAbsent(colony, owner);
					if (!otherDimension && !tooFar && !recordedGone) {
						continue;
					}
					WeaverPadBlock.release(level, pos, owner);
					release(pos, owner);
					if (who != null && who.hasHome() && WeaverEntity.isSameBed(level, who.getHomePosition(), pos)) {
						who.releaseHome();
					}
					saved.clear(colony, owner);
				}
			}
		}
	}

	/** An unowned pad in this colony. Used when a Weaver has no bunk of their own. */
	public static @Nullable BlockPos nearestFreePad(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		BlockPos origin = weaver.blockPosition();
		int minCx = (origin.getX() - PAD_RANGE) >> 4;
		int maxCx = (origin.getX() + PAD_RANGE) >> 4;
		int minCz = (origin.getZ() - PAD_RANGE) >> 4;
		int maxCz = (origin.getZ() + PAD_RANGE) >> 4;
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (var entry : chunk.getBlockEntities().entrySet()) {
					if (!(entry.getValue() instanceof WeaverPadBlockEntity pad) || pad.getOwner() != null) {
						continue;
					}
					BlockPos pos = entry.getKey();
					BlockState state = level.getBlockState(pos);
					if (!(state.getBlock() instanceof WeaverPadBlock)) {
						continue;
					}
					if (state.hasProperty(BlockStateProperties.BED_PART)
						&& state.getValue(BlockStateProperties.BED_PART) != BedPart.FOOT) {
						continue;
					}
					if (colony != 0L && !sameColony(level, pos, colony)) {
						continue;
					}
					synchronized (RESERVED) {
						if (RESERVED.containsKey(pos.asLong())) {
							continue;
						}
					}
					double dist = weaver.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					if (dist < bestDist) {
						bestDist = dist;
						best = pos.immutable();
					}
				}
			}
		}
		return best;
	}

	public static @Nullable Vacancy findVacancy(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		if (colony == 0L) {
			return null;
		}
		reconcile(level, weaver);
		BlockPos origin = colonyOrigin(weaver);
		int minCx = (origin.getX() - PAD_RANGE) >> 4;
		int maxCx = (origin.getX() + PAD_RANGE) >> 4;
		int minCz = (origin.getZ() - PAD_RANGE) >> 4;
		int maxCz = (origin.getZ() + PAD_RANGE) >> 4;
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (var entry : chunk.getBlockEntities().entrySet()) {
					if (!(entry.getValue() instanceof WeaverPadBlockEntity pad) || pad.getOwner() != null) {
						continue;
					}
					BlockPos pos = entry.getKey();
					if (origin.distSqr(pos) > (double) PAD_RANGE * PAD_RANGE) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (!(state.getBlock() instanceof WeaverPadBlock)) {
						continue;
					}
					if (state.hasProperty(BlockStateProperties.BED_PART)
						&& state.getValue(BlockStateProperties.BED_PART) != BedPart.FOOT) {
						continue;
					}
					if (!sameColony(level, pos, colony)) {
						continue;
					}
					synchronized (RESERVED) {
						if (RESERVED.containsKey(pos.asLong())) {
							continue;
						}
					}
					double dist = weaver.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					if (dist < bestDist) {
						bestDist = dist;
						best = pos.immutable();
					}
				}
			}
		}
		if (best == null) {
			return null;
		}
		return new Vacancy(best, findPlate(level, best));
	}

	public static @Nullable BlockPos findPlate(Level level, BlockPos bunk) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dx = -PLATE_SEARCH; dx <= PLATE_SEARCH; dx++) {
			for (int dy = -PLATE_Y; dy <= PLATE_Y; dy++) {
				for (int dz = -PLATE_SEARCH; dz <= PLATE_SEARCH; dz++) {
					cursor.set(bunk.getX() + dx, bunk.getY() + dy, bunk.getZ() + dz);
					if (!level.getBlockState(cursor).is(ModBlocks.THOLIN_FIBER_HOME_PLATE)) {
						continue;
					}
					double dist = cursor.distSqr(bunk);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	/** Pads live at the colony. A Weaver led away still finds them from its home, not its feet. */
	private static BlockPos colonyOrigin(WeaverEntity weaver) {
		if (weaver.hasHome() && weaver.level() instanceof ServerLevel level) {
			BlockPos home = weaver.getHomePosition();
			if (home != null && level.isLoaded(home)) {
				return home;
			}
		}
		return weaver.blockPosition();
	}

	private static @Nullable WeaverEntity findLoaded(ServerLevel level, UUID id) {
		for (ServerLevel world : level.getServer().getAllLevels()) {
			Entity entity = world.getEntity(id);
			if (entity instanceof WeaverEntity weaver && weaver.isAlive()) {
				return weaver;
			}
		}
		return null;
	}

	private static boolean sameColony(ServerLevel level, BlockPos pos, long colony) {
		long generated = WeaverColonies.generatedId(level, pos);
		if (generated != 0L) {
			return generated == colony;
		}
		return WeaverColonies.idAt(level, pos) == colony;
	}
}
