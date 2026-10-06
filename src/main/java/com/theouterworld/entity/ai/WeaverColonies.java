package com.theouterworld.entity.ai;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.WeaverNetBlockEntity;
import com.theouterworld.world.WeaverColonySavedData;
import com.theouterworld.worldgen.WorldgenNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared Anchor identity and the geometry Weavers use to hang nets beside walkways.
 *
 * <p>Colony ids are derived from the same cell hash Amberworld Anchor generation uses,
 * so every chunk of one Anchor agrees on a single id without scanning other Weavers.
 */
public final class WeaverColonies {
	public static final int CELL = 416;
	public static final int JITTER = 96;
	public static final int RADIUS = 52;
	/** Salt mixed into the world seed by {@code AmberworldAnchorFeature}. */
	public static final long SEED_SALT = 5514011L;

	/** Block entities, counting both blocks of a hanging column. Generation stays under this. */
	public static final int MAX_NETS = 24;
	/** Extra nets a colony may spin after generation, however many chunks are loaded. */
	public static final int MAX_EXPANSIONS = 6;
	/** Five minutes between expansions, so dumped curiosities cannot grow the weave instantly. */
	public static final int EXPANSION_COOLDOWN = 6000;
	public static final int WALKWAY_RANGE = 2;
	public static final int NET_REACH_XZ = 40;
	public static final int NET_REACH_Y = 32;

	private WeaverColonies() {
	}

	public static long id(long worldSeed, int cellX, int cellZ) {
		long n = worldSeed + SEED_SALT;
		n ^= (long) cellX * 341873128712L;
		n ^= (long) cellZ * 132897987541L;
		n ^= n >>> 33;
		n *= 0xff51afd7ed558ccdL;
		n ^= n >>> 33;
		n *= 0xc4ceb9fe1a85ec53L;
		n ^= n >>> 33;
		return n == 0L ? 1L : n;
	}

	/**
	 * Colony that owns this position: the generated Anchor whose centre is closest,
	 * when one stands within {@link #RADIUS}, otherwise the cell the position falls in.
	 */
	public static long idAt(ServerLevel level, BlockPos pos) {
		long generated = generatedId(level, pos);
		if (generated != 0L) {
			return generated;
		}
		int cellX = Math.floorDiv(pos.getX(), CELL);
		int cellZ = Math.floorDiv(pos.getZ(), CELL);
		return id(level.getSeed(), cellX, cellZ);
	}

	/** Anchor centre for a known colony id, searched from a nearby hint. */
	public static @Nullable BlockPos centerOf(ServerLevel level, long colonyId, BlockPos hint) {
		if (colonyId == 0L) {
			return null;
		}
		long worldSeed = level.getSeed();
		long salt = worldSeed + SEED_SALT;
		int cellX = Math.floorDiv(hint.getX(), CELL);
		int cellZ = Math.floorDiv(hint.getZ(), CELL);
		for (int cx = cellX - 2; cx <= cellX + 2; cx++) {
			for (int cz = cellZ - 2; cz <= cellZ + 2; cz++) {
				if (id(worldSeed, cx, cz) != colonyId) {
					continue;
				}
				int centerX = cx * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(salt + 3, cx, cz)) * JITTER);
				int centerZ = cz * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(salt + 5, cx, cz)) * JITTER);
				return new BlockPos(centerX, hint.getY(), centerZ);
			}
		}
		return null;
	}

	/** Generated Anchor whose centre is within {@link #RADIUS}, or 0 when the position is outside every Anchor. */
	public static long generatedId(ServerLevel level, BlockPos pos) {
		long worldSeed = level.getSeed();
		long salt = worldSeed + SEED_SALT;
		int cellX = Math.floorDiv(pos.getX(), CELL);
		int cellZ = Math.floorDiv(pos.getZ(), CELL);
		long bestId = 0L;
		long bestDist = Long.MAX_VALUE;
		for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
			for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
				int centerX = cx * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(salt + 3, cx, cz)) * JITTER);
				int centerZ = cz * CELL + CELL / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(salt + 5, cx, cz)) * JITTER);
				long dx = (long) pos.getX() - centerX;
				long dz = (long) pos.getZ() - centerZ;
				long dist = dx * dx + dz * dz;
				if (dist <= (long) RADIUS * RADIUS && dist < bestDist) {
					bestDist = dist;
					bestId = id(worldSeed, cx, cz);
				}
			}
		}
		return bestId;
	}

	/**
	 * Colony this structure block belongs to. Positions outside an Anchor, and spots that
	 * are not standing in Anchor material, do not belong to any colony.
	 */
	public static long colonyOwning(ServerLevel level, BlockPos pos) {
		long generated = generatedId(level, pos);
		if (generated != 0L) {
			return generated;
		}
		if (WeaverAnchors.isInsideAnchor(level, pos)) {
			return idAt(level, pos);
		}
		return 0L;
	}

	public static void noteBroken(Level level, BlockPos pos, Player player, boolean severe) {
		if (!(level instanceof ServerLevel server) || player.isSpectator() || player.isCreative()) {
			return;
		}
		if (!player.gameMode().isSurvival()) {
			return;
		}
		long id = 0L;
		if (level.getBlockEntity(pos) instanceof WeaverNetBlockEntity net) {
			id = net.getColonyId();
		}
		if (id == 0L) {
			id = idAt(server, pos);
		}
		WeaverColonySavedData.get(server).noteHarm(id, server.getGameTime(), severe);
	}

	/** Nets of this colony within housekeeping reach. Untagged nets inside that reach are adopted. */
	public static List<BlockPos> findNets(Level level, BlockPos origin, long colonyId) {
		List<BlockPos> nets = new ArrayList<>();
		for (BlockPos pos : BlockPos.withinClippedManhattan(origin, NET_REACH_XZ, NET_REACH_Y, NET_REACH_XZ)) {
			if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(ModBlocks.WEAVER_NET)) {
				continue;
			}
			if (!(level.getBlockEntity(pos) instanceof WeaverNetBlockEntity net)) {
				continue;
			}
			long owner = net.getColonyId();
			if (owner != 0L && owner != colonyId) {
				continue;
			}
			if (owner == 0L && colonyId != 0L) {
				net.setColonyId(colonyId);
			}
			nets.add(pos.immutable());
		}
		return nets;
	}

	public static @Nullable BlockPos nearestOpenNet(Level level, Vec3 from, BlockPos origin, long colonyId, ItemStack stack) {
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos pos : findNets(level, origin, colonyId)) {
			if (!(level.getBlockEntity(pos) instanceof WeaverNetBlockEntity net) || !net.hasRoomFor(stack)) {
				continue;
			}
			double dist = from.distanceToSqr(Vec3.atCenterOf(pos));
			if (dist < bestDist) {
				bestDist = dist;
				best = pos;
			}
		}
		return best;
	}

	/** Feet position beside the net, on a real floor with headroom. Null when nothing nearby can be stood on. */
	public static @Nullable Vec3 standBeside(Level level, BlockPos net, double fromX, double fromY, double fromZ) {
		BlockPos.MutableBlockPos floor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos beside = net.relative(dir);
			for (int dy = -3; dy <= 1; dy++) {
				floor.set(beside.getX(), net.getY() + dy, beside.getZ());
				if (!openStand(level, floor)) {
					continue;
				}
				double dist = square(fromX - (floor.getX() + 0.5), fromY - (floor.getY() + 1.0), fromZ - (floor.getZ() + 0.5));
				if (dist < bestDist) {
					bestDist = dist;
					best = floor.immutable();
				}
			}
		}
		if (best == null) {
			return null;
		}
		return new Vec3(best.getX() + 0.5, best.getY() + 1.0, best.getZ() + 0.5);
	}

	public static boolean openStand(Level level, BlockPos floor) {
		if (level.getBlockState(floor).getCollisionShape(level, floor).isEmpty()) {
			return false;
		}
		for (int up = 1; up <= 3; up++) {
			BlockPos above = floor.above(up);
			BlockState state = level.getBlockState(above);
			if (state.is(ModBlocks.WEAVER_NET) || !state.getCollisionShape(level, above).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/** True when a walkable fibre deck sits within {@code range} blocks horizontally. */
	public static boolean nearWalkway(Level level, BlockPos pos, int range) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -4; dy <= 2; dy++) {
					cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if ((state.is(ModBlocks.THOLIN_FIBER) || state.is(ModBlocks.THOLIN_FIBER_HOME_PLATE))
						&& openStand(level, cursor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Headroom above a deck. A net here would snag anyone walking the Anchor. */
	public static boolean blocksWalkway(Level level, BlockPos pos) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dy = 1; dy <= 3; dy++) {
			cursor.set(pos.getX(), pos.getY() - dy, pos.getZ());
			BlockState state = level.getBlockState(cursor);
			if ((state.is(ModBlocks.THOLIN_FIBER) || state.is(ModBlocks.THOLIN_FIBER_HOME_PLATE))
				&& openStand(level, cursor)) {
				return true;
			}
		}
		return false;
	}

	/** The clear shaft above a home plate. Filling it traps the leap home. */
	public static boolean inLeapShaft(Level level, BlockPos pos) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dy = 1; dy <= 42; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					cursor.set(pos.getX() + dx, pos.getY() - dy, pos.getZ() + dz);
					if (level.getBlockState(cursor).is(ModBlocks.THOLIN_FIBER_HOME_PLATE)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static double square(double x, double y, double z) {
		return x * x + y * y + z * z;
	}
}
