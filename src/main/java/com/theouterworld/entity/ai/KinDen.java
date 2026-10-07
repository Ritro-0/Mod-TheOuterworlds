package com.theouterworld.entity.ai;

import com.theouterworld.block.KharaxShellBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModTags;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The little shed dome an adopted Kharax raises near an Anchor.
 * Interior is a 5 by 5 standing room, three blocks clear at the walls and taller
 * under the crown, with a 3 by 3 door facing the colony.
 */
public final class KinDen {
	private static final int WALL = 3;
	private static final int SEARCHES = 48;

	private KinDen() {
	}

	public record Site(BlockPos origin, Direction door, boolean raised) {
	}

	public enum Stage {
		STAIR,
		FLOOR,
		SHELL
	}

	public record Placement(BlockPos pos, boolean spore, Stage stage) {
	}

	public static Site findSite(ServerLevel level, BlockPos anchor, LivingEntity self) {
		Site open = scan(level, anchor, self, 12.0, 12.0, true);
		if (open != null) {
			return open;
		}
		Site loose = scan(level, anchor, self, 8.0, 24.0, false);
		if (loose != null) {
			return loose;
		}
		return findRaised(level, anchor, self);
	}

	/** A couple of blocks up, where the standing room is open air. */
	public static Site findRaised(ServerLevel level, BlockPos anchor, LivingEntity self) {
		RandomSource random = self.getRandom();
		Site fallback = null;
		for (int attempt = 0; attempt < SEARCHES; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = 8.0 + random.nextDouble() * 20.0;
			int x = anchor.getX() + (int) Math.round(Math.cos(angle) * dist);
			int z = anchor.getZ() + (int) Math.round(Math.sin(angle) * dist);
			BlockPos feet = feetOn(level, x, z);
			if (feet == null) {
				continue;
			}
			for (int lift = 2; lift <= 8; lift += 2) {
				BlockPos origin = feet.above(lift);
				if (origin.getY() >= level.getMaxY() - 8) {
					break;
				}
				if (fallback == null) {
					fallback = new Site(origin.immutable(), downhill(level, origin), true);
				}
				if (hullClear(level, origin)) {
					return new Site(origin.immutable(), downhill(level, origin), true);
				}
			}
		}
		if (fallback != null) {
			return fallback;
		}
		BlockPos feet = feetOn(level, self.getBlockX(), self.getBlockZ());
		if (feet == null) {
			feet = self.blockPosition();
		}
		BlockPos origin = feet.above(2);
		return new Site(origin.immutable(), downhill(level, origin), true);
	}

	/** True when the walls and the standing room are open, not buried in a terrace. */
	public static boolean hullClear(ServerLevel level, BlockPos origin) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy <= 2; dy++) {
			for (int dx = -WALL; dx <= WALL; dx++) {
				for (int dz = -WALL; dz <= WALL; dz++) {
					int ring = Math.max(Math.abs(dx), Math.abs(dz));
					if (ring > 2 && ring != WALL) {
						continue;
					}
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!state.canBeReplaced() || !state.getFluidState().isEmpty() || state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static Direction downhill(ServerLevel level, BlockPos origin) {
		Direction best = Direction.SOUTH;
		int lowest = Integer.MAX_VALUE;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			int top = solidTop(
				level,
				origin.getX() + direction.getStepX() * (WALL + 2),
				origin.getZ() + direction.getStepZ() * (WALL + 2)
			);
			if (top < lowest) {
				lowest = top;
				best = direction;
			}
		}
		return best;
	}

	private static @Nullable Site scan(
		ServerLevel level,
		BlockPos anchor,
		LivingEntity self,
		double minDist,
		double extraDist,
		boolean strict
	) {
		RandomSource random = self.getRandom();
		for (int attempt = 0; attempt < SEARCHES; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = minDist + random.nextDouble() * extraDist;
			int x = anchor.getX() + (int) Math.round(Math.cos(angle) * dist);
			int z = anchor.getZ() + (int) Math.round(Math.sin(angle) * dist);
			BlockPos origin = feetOn(level, x, z);
			if (origin == null || origin.getY() >= level.getMaxY() - 8) {
				continue;
			}
			if (fits(level, origin, strict)) {
				return new Site(origin.immutable(), toward(origin, anchor), false);
			}
		}
		return null;
	}

	/** Stairs from the ground up, then the floor, then the shell from the bottom. */
	public static List<Placement> plan(ServerLevel level, BlockPos origin, Direction door, boolean raised) {
		List<Placement> plan = new ArrayList<>();
		Set<Long> occupied = new HashSet<>();
		if (raised) {
			List<BlockPos> stairs = new ArrayList<>();
			addStairs(level, origin, door, stairs, occupied);
			stairs.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()));
			for (BlockPos step : stairs) {
				plan.add(new Placement(step, false, Stage.STAIR));
			}
			List<BlockPos> floor = new ArrayList<>();
			for (int dx = -WALL; dx <= WALL; dx++) {
				for (int dz = -WALL; dz <= WALL; dz++) {
					BlockPos tile = origin.offset(dx, -1, dz);
					if (occupied.add(tile.asLong())) {
						floor.add(tile);
					}
				}
			}
			floor.sort(Comparator.comparingInt((BlockPos pos) -> {
				int dx = Math.abs(pos.getX() - origin.getX());
				int dz = Math.abs(pos.getZ() - origin.getZ());
				return -Math.max(dx, dz);
			}));
			for (BlockPos tile : floor) {
				plan.add(new Placement(tile, false, Stage.FLOOR));
			}
		}
		List<BlockPos> shell = new ArrayList<>();
		for (int dy = 0; dy <= 5; dy++) {
			for (int dx = -WALL; dx <= WALL; dx++) {
				for (int dz = -WALL; dz <= WALL; dz++) {
					if (!shellCell(dx, dy, dz) || doorway(dx, dy, dz, door)) {
						continue;
					}
					BlockPos pos = origin.offset(dx, dy, dz);
					shell.add(pos);
					occupied.add(pos.asLong());
				}
			}
		}
		shell.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).thenComparingInt(pos -> {
			int dx = Math.abs(pos.getX() - origin.getX());
			int dz = Math.abs(pos.getZ() - origin.getZ());
			return Math.max(dx, dz);
		}));
		RandomSource random = RandomSource.create(origin.asLong() ^ (door.get2DDataValue() * 0x9E3779B97L));
		for (BlockPos pos : shell) {
			plan.add(new Placement(pos, false, Stage.SHELL));
			if (random.nextFloat() >= 0.38F) {
				continue;
			}
			BlockPos spore = pos.relative(outward(pos, origin));
			if (occupied.contains(spore.asLong()) || !sporeFits(origin, spore, door)) {
				continue;
			}
			occupied.add(spore.asLong());
			plan.add(new Placement(spore, true, Stage.SHELL));
		}
		return plan;
	}

	/** Three-wide steps from the threshold down until they sit on the ground. */
	private static void addStairs(
		ServerLevel level,
		BlockPos origin,
		Direction door,
		List<BlockPos> shell,
		Set<Long> occupied
	) {
		int fx = door.getStepX();
		int fz = door.getStepZ();
		int px = door.getClockWise().getStepX();
		int pz = door.getClockWise().getStepZ();
		for (int along = 1; along <= 12; along++) {
			int cx = origin.getX() + fx * (WALL + along);
			int cz = origin.getZ() + fz * (WALL + along);
			int stepY = origin.getY() - along;
			int ground = solidTop(level, cx, cz);
			if (stepY <= ground) {
				break;
			}
			for (int side = -1; side <= 1; side++) {
				BlockPos step = new BlockPos(cx + px * side, stepY, cz + pz * side);
				if (occupied.add(step.asLong())) {
					shell.add(step);
				}
			}
			if (stepY == ground + 1) {
				break;
			}
		}
	}

	public static BlockPos stand(BlockPos origin, Direction door) {
		return origin.relative(door, 5);
	}

	/** Where to stand while laying this stair or floor tile. Never a heightmap cell. */
	public static BlockPos workSpot(BlockPos origin, Direction door, BlockPos block, Stage stage) {
		if (stage == Stage.STAIR) {
			return block.relative(door);
		}
		if (stage == Stage.FLOOR) {
			return block.above();
		}
		return origin;
	}

	/**
	 * The first open block above solid ground. The heightmap's value is sometimes the
	 * ground and sometimes the air above it; either way the walls sit on the surface.
	 */
	private static @Nullable BlockPos feetOn(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		if (y <= level.getMinY()) {
			return null;
		}
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
		if (!level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
			cursor.move(Direction.UP);
		}
		int guard = 0;
		while (cursor.getY() > level.getMinY() + 1
			&& level.getBlockState(cursor.below()).getCollisionShape(level, cursor.below()).isEmpty()
			&& guard++ < 4) {
			cursor.move(Direction.DOWN);
		}
		if (cursor.getY() >= level.getMaxY()) {
			return null;
		}
		return cursor.immutable();
	}

	public static boolean canPlace(ServerLevel level, Placement placement) {
		if (satisfied(level, placement)) {
			return true;
		}
		BlockPos pos = placement.pos();
		if (!level.isLoaded(pos) || pos.getY() >= level.getMaxY()) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		return state.canBeReplaced() && state.getFluidState().isEmpty() && !state.is(ModTags.WEAVER_ANCHOR_PARTS);
	}

	private static int solidTop(ServerLevel level, int x, int z) {
		BlockPos feet = feetOn(level, x, z);
		return feet == null ? level.getMinY() : feet.getY() - 1;
	}

	public static boolean satisfied(ServerLevel level, Placement placement) {
		BlockState state = level.getBlockState(placement.pos());
		return placement.spore() ? state.is(ModBlocks.KHARAX_SPORE) : state.is(ModBlocks.KHARAX_SHED);
	}

	/** @return true when the block is the one the plan asked for */
	public static boolean tryPlace(ServerLevel level, Placement placement, LivingEntity self) {
		if (satisfied(level, placement)) {
			return true;
		}
		BlockPos pos = placement.pos();
		if (!level.isLoaded(pos) || pos.getY() >= level.getMaxY()) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (!state.canBeReplaced() || !state.getFluidState().isEmpty() || state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
			return false;
		}
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), LivingEntity::isAlive)) {
			if (living != self) {
				return false;
			}
		}
		BlockState placed = (placement.spore() ? ModBlocks.KHARAX_SPORE : ModBlocks.KHARAX_SHED).defaultBlockState();
		level.setBlock(pos, placed, 3);
		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, net.minecraft.world.level.block.Block.getId(placed));
		level.playSound(
			null,
			pos,
			net.minecraft.sounds.SoundEvents.SLIME_SQUISH,
			net.minecraft.sounds.SoundSource.BLOCKS,
			0.4F,
			placement.spore() ? 1.45F : 0.82F
		);
		return true;
	}

	/** Strict spots want open sky outside the tower. A loose spot only needs room to stand. */
	private static boolean fits(ServerLevel level, BlockPos origin, boolean strict) {
		if (!level.isLoaded(origin)) {
			return false;
		}
		if (strict && (!level.canSeeSky(origin) || WeaverAnchors.isInsideAnchor(level, origin))) {
			return false;
		}
		return columnOpen(level, origin.below(), 4) && hullClear(level, origin);
	}

	private static boolean columnOpen(ServerLevel level, BlockPos ground, int airAbove) {
		if (!level.isLoaded(ground)) {
			return false;
		}
		BlockState floor = level.getBlockState(ground);
		if (floor.getCollisionShape(level, ground).isEmpty()
			|| !floor.getFluidState().isEmpty()
			|| floor.is(ModTags.WEAVER_ANCHOR_PARTS)
			|| floor.getBlock() instanceof KharaxShellBlock) {
			return false;
		}
		for (int up = 1; up <= airAbove; up++) {
			BlockPos air = ground.above(up);
			BlockState state = level.getBlockState(air);
			if (!state.canBeReplaced() || !state.getFluidState().isEmpty() || state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
				return false;
			}
		}
		return true;
	}

	private static boolean shellCell(int dx, int dy, int dz) {
		int ring = Math.max(Math.abs(dx), Math.abs(dz));
		if (dy >= 0 && dy <= 2 && ring == WALL) {
			return true;
		}
		if (dy == 3 && ring >= 2 && ring <= WALL) {
			return true;
		}
		if (dy == 4 && ring <= 1) {
			return true;
		}
		return dy == 5 && ring == 0;
	}

	/** The 3 wide, 3 tall opening, and the two blocks of path just outside it. */
	private static boolean doorway(int dx, int dy, int dz, Direction door) {
		if (dy < 0 || dy > 2) {
			return false;
		}
		int along = dx * door.getStepX() + dz * door.getStepZ();
		int side = dx * door.getClockWise().getStepX() + dz * door.getClockWise().getStepZ();
		return along == WALL && Math.abs(side) <= 1;
	}

	private static boolean sporeFits(BlockPos origin, BlockPos spore, Direction door) {
		int dx = spore.getX() - origin.getX();
		int dy = spore.getY() - origin.getY();
		int dz = spore.getZ() - origin.getZ();
		if (interior(dx, dy, dz) || reservedPath(dx, dy, dz, door)) {
			return false;
		}
		return Math.max(Math.abs(dx), Math.abs(dz)) <= WALL + 1 && dy >= 0 && dy <= 6;
	}

	private static boolean interior(int dx, int dy, int dz) {
		int ring = Math.max(Math.abs(dx), Math.abs(dz));
		if (dy <= 2 && ring <= 2) {
			return true;
		}
		if (dy == 3 && ring <= 1) {
			return true;
		}
		return dy == 4 && ring == 0;
	}

	private static boolean reservedPath(int dx, int dy, int dz, Direction door) {
		if (dy < 0 || dy > 2) {
			return false;
		}
		int along = dx * door.getStepX() + dz * door.getStepZ();
		int side = dx * door.getClockWise().getStepX() + dz * door.getClockWise().getStepZ();
		return along >= WALL && along <= WALL + 2 && Math.abs(side) <= 1;
	}

	private static Direction outward(BlockPos pos, BlockPos origin) {
		int dx = pos.getX() - origin.getX();
		int dy = pos.getY() - origin.getY();
		int dz = pos.getZ() - origin.getZ();
		int ring = Math.max(Math.abs(dx), Math.abs(dz));
		if (dy >= 4 || (dy >= 3 && ring <= 1)) {
			return Direction.UP;
		}
		if (Math.abs(dx) >= Math.abs(dz) && dx != 0) {
			return dx > 0 ? Direction.EAST : Direction.WEST;
		}
		if (dz != 0) {
			return dz > 0 ? Direction.SOUTH : Direction.NORTH;
		}
		return Direction.UP;
	}

	private static Direction toward(BlockPos from, BlockPos anchor) {
		int dx = anchor.getX() - from.getX();
		int dz = anchor.getZ() - from.getZ();
		if (Math.abs(dx) > Math.abs(dz)) {
			return dx > 0 ? Direction.EAST : Direction.WEST;
		}
		if (dz != 0) {
			return dz > 0 ? Direction.SOUTH : Direction.NORTH;
		}
		return Direction.SOUTH;
	}
}
