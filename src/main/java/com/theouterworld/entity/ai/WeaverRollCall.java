package com.theouterworld.entity.ai;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.entity.WeaverEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

/**
 * Once every morning, when the Weavers get up, the loaded colony leaps to open
 * ground around the helix, each Weaver on its own spot. The meeting does not run
 * when someone goes missing, and it does not run when the colony loads after the morning.
 */
public final class WeaverRollCall {
	/** Long enough for a home-plate leap to climb, fall, and land, plus one retry. */
	public static final int GATHER_TICKS = 600;
	/** The meeting starts any time in the first part of the morning, so a skipped tick cannot miss it. */
	private static final long MORNING_WINDOW = 3000L;

	private static final Map<Long, Call> CALLS = new HashMap<>();

	private WeaverRollCall() {
	}

	public static void tick(ServerLevel level, WeaverEntity weaver) {
		long colony = weaver.colonyId();
		if (colony == 0L) {
			return;
		}
		Call call = CALLS.computeIfAbsent(colony, id -> new Call());
		if (WeaverSchedule.isBedtime(level)) {
			if (call.gathering && !call.audited && call.center != null
				&& level.getGameTime() - call.started >= GATHER_TICKS) {
				WeaverHomes.condemnAbsentees(level, colony, call.center);
				call.audited = true;
			}
			call.gathering = false;
			call.nightSeen = true;
			return;
		}
		if (!call.seen) {
			call.seen = true;
			// Loaded during the morning counts as waking up. Loaded later in the day does not.
			if (WeaverSchedule.sinceWake(level) <= MORNING_WINDOW) {
				call.nightSeen = true;
			}
		}
		if (call.nightSeen) {
			call.nightSeen = false;
			call.gathering = true;
			call.audited = false;
			call.started = level.getGameTime();
			call.spots.clear();
			BlockPos hint = weaver.hasHomeHere() ? weaver.getHomePosition() : weaver.blockPosition();
			BlockPos center = WeaverColonies.centerOf(level, colony, hint);
			call.center = center != null ? center : hint;
			call.options = findSpots(level, call.center);
			OuterWorldMod.LOGGER.info(
				"Weaver morning meeting: colony {} centre {} with {} meeting spots",
				colony, call.center, call.options.size()
			);
		}
		if (call.gathering && !call.audited && call.center != null && level.getGameTime() - call.started >= GATHER_TICKS) {
			WeaverHomes.condemnAbsentees(level, colony, call.center);
			call.audited = true;
			call.gathering = false;
		}
	}

	public static boolean isGathering(long colony) {
		Call call = CALLS.get(colony);
		return call != null && call.gathering && !call.audited && call.center != null;
	}

	/** A different open patch of ground for each Weaver. Null when the morning has no safe spots. */
	public static @Nullable BlockPos meetingFor(ServerLevel level, WeaverEntity weaver) {
		Call call = CALLS.get(weaver.colonyId());
		if (call == null || !call.gathering || call.center == null) {
			return null;
		}
		BlockPos already = call.spots.get(weaver.getUUID());
		if (already != null) {
			return already;
		}
		if (call.options == null) {
			call.options = findSpots(level, call.center);
		}
		if (call.options.isEmpty()) {
			return null;
		}
		BlockPos pick = claimSpot(call, weaver.getUUID());
		call.spots.put(weaver.getUUID(), pick);
		return pick;
	}

	private static BlockPos claimSpot(Call call, UUID id) {
		List<BlockPos> options = call.options;
		int start = Math.floorMod(id.hashCode(), options.size());
		for (int gap : new int[] { 3, 2 }) {
			for (int i = 0; i < options.size(); i++) {
				BlockPos option = options.get((start + i) % options.size());
				if (!nearTaken(call.spots, option, gap)) {
					return option;
				}
			}
		}
		return options.get(start);
	}

	private static boolean nearTaken(Map<UUID, BlockPos> taken, BlockPos spot, int gap) {
		double limit = (double) gap * gap;
		for (BlockPos other : taken.values()) {
			if (other.distSqr(spot) < limit) {
				return true;
			}
		}
		return false;
	}

	/** Ground around the helix, under the fibre, not the spring itself. */
	private static List<BlockPos> findSpots(ServerLevel level, BlockPos center) {
		List<BlockPos> spots = ring(level, center, 16, 80, 3);
		if (spots.isEmpty()) {
			spots = ring(level, center, 12, 96, 2);
		}
		return spots;
	}

	private static List<BlockPos> ring(ServerLevel level, BlockPos center, int minRadius, int maxRadius, int headroom) {
		List<BlockPos> spots = new ArrayList<>();
		for (int radius = minRadius; radius <= maxRadius && (radius <= 48 || spots.size() < 12); radius += 4) {
			int steps = 12;
			for (int i = 0; i < steps; i++) {
				double angle = i * (Math.PI * 2.0 / steps) + radius * 0.17;
				int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
				int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
				if (!level.hasChunkAt(new BlockPos(x, center.getY(), z))) {
					continue;
				}
				BlockPos floor = groundFloor(level, x, z, headroom);
				if (floor != null) {
					spots.add(floor);
				}
			}
		}
		return spots;
	}

	private static @Nullable BlockPos groundFloor(ServerLevel level, int x, int z, int headroom) {
		int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int bottom = Math.max(level.getMinY(), top - 80);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = top - 1; y >= bottom; y--) {
			cursor.set(x, y, z);
			if (isMeetingFloor(level, cursor, headroom)) {
				return cursor.immutable();
			}
		}
		return null;
	}

	private static boolean isMeetingFloor(ServerLevel level, BlockPos floor, int headroom) {
		BlockState state = level.getBlockState(floor);
		if (isFiber(state) || state.getCollisionShape(level, floor, CollisionContext.empty()).isEmpty()) {
			return false;
		}
		if (!WeaverLeapSpot.isDry(level, floor)) {
			return false;
		}
		for (int up = 1; up <= headroom; up++) {
			BlockPos above = floor.above(up);
			BlockState head = level.getBlockState(above);
			if (isFiber(head) || !head.getCollisionShape(level, above, CollisionContext.empty()).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static boolean isFiber(BlockState state) {
		return state.is(ModBlocks.THOLIN_FIBER) || state.is(ModBlocks.THOLIN_FIBER_HOME_PLATE);
	}

	private static final class Call {
		private boolean seen;
		/** The colony was seen in bed, so the next waking tick starts the meeting. */
		private boolean nightSeen;
		private boolean gathering;
		private boolean audited;
		private long started;
		private @Nullable BlockPos center;
		private @Nullable List<BlockPos> options;
		private final Map<UUID, BlockPos> spots = new HashMap<>();
	}
}
