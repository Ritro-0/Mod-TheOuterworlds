package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Colony navigation. Search far enough to wind the spiral, keep the real
 * target Y, and if the weaver is below its bed, path to a high perch first
 * so it can drop onto the pod instead of walking into the wall under it.
 */
public class WeaverPathNavigation extends GroundPathNavigation {
	private static final int VISITED_NODES = 4096;
	private static final int PERCH_OVERSHOOT = 8;
	private static final int PERCH_RADIUS = 20;
	private static final int WALL_STUCK_TICKS = 12;

	private boolean routing;
	private int wallTicks;

	public WeaverPathNavigation(Mob mob, Level level) {
		super(mob, level);
		this.setCanPathToTargetsBelowSurface(true);
		this.setRequiredPathLength(96.0F);
	}

	@Override
	protected PathFinder createPathFinder(int maxVisitedNodes) {
		this.nodeEvaluator = new WeaverNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		this.nodeEvaluator.setCanFloat(true);
		WeaverPathFinder finder = new WeaverPathFinder(this.nodeEvaluator, Math.max(maxVisitedNodes, VISITED_NODES));
		finder.setMaxVisitedNodes(VISITED_NODES);
		return finder;
	}

	@Override
	public float getMaxVerticalDistanceToWaypoint() {
		return 1.35F;
	}

	@Override
	protected double getGroundY(Vec3 target) {
		return target.y;
	}

	@Override
	public @Nullable Path createPath(BlockPos pos, int reachRange) {
		if (this.mob instanceof WeaverEntity weaver && weaver.isFlatApproach()) {
			return super.createPath(pos, reachRange);
		}
		if (this.routing) {
			return super.createPath(pos, reachRange);
		}
		this.routing = true;
		try {
			Path direct = super.createPath(pos, reachRange);
			if (direct != null && direct.canReach()) {
				return direct;
			}
			if (this.mob.getBlockY() < pos.getY() - 1) {
				BlockPos perch = findPerch(pos);
				if (perch != null && perch.distManhattan(pos) > reachRange) {
					Path climb = super.createPath(perch, 2);
					if (isBetterClimb(climb, direct)) {
						return climb;
					}
				}
			}
			return direct;
		} finally {
			this.routing = false;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.mob.horizontalCollision && this.mob.onGround() && this.path != null && !this.path.isDone()) {
			this.wallTicks++;
			if (this.wallTicks >= WALL_STUCK_TICKS) {
				this.wallTicks = 0;
				if (this.path.getNextNodeIndex() + 1 < this.path.getNodeCount()) {
					Node next = this.path.getNextNode();
					double aheadX = next.x + 0.5 - this.mob.getX();
					double aheadZ = next.z + 0.5 - this.mob.getZ();
					Vec3 look = this.mob.getLookAngle();
					if (aheadX * look.x + aheadZ * look.z > 0.15) {
						this.path.advance();
					}
				}
				this.recomputePath();
			}
		} else {
			this.wallTicks = 0;
		}
	}

	private boolean isBetterClimb(@Nullable Path climb, @Nullable Path direct) {
		if (climb == null) {
			return false;
		}
		if (climb.canReach()) {
			return true;
		}
		Node climbEnd = climb.getEndNode();
		if (climbEnd == null) {
			return false;
		}
		if (direct == null || direct.getEndNode() == null) {
			return climbEnd.y > this.mob.getBlockY();
		}
		return climbEnd.y > direct.getEndNode().y + 1;
	}

	/**
	 * Highest nearby standable block that still leans toward the destination.
	 * Used as a first waypoint when the bed sits in a pod above the weaver.
	 */
	private @Nullable BlockPos findPerch(BlockPos target) {
		BlockPos origin = this.mob.blockPosition();
		int minY = origin.getY() + 3;
		int maxY = Math.max(target.getY() + PERCH_OVERSHOOT, origin.getY() + 24);
		BlockPos best = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int y = minY; y <= maxY; y++) {
			for (int dx = -PERCH_RADIUS; dx <= PERCH_RADIUS; dx += 2) {
				for (int dz = -PERCH_RADIUS; dz <= PERCH_RADIUS; dz += 2) {
					if (dx * dx + dz * dz > PERCH_RADIUS * PERCH_RADIUS) {
						continue;
					}
					cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
					if (!isStandableWithHeadroom(cursor)) {
						continue;
					}
					double climb = y - origin.getY();
					double toTarget = Mth.sqrt(
						(float) cursor.distToCenterSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5)
					);
					double toMob = Mth.sqrt(dx * dx + dz * dz);
					double score = climb * 6.5 - toTarget * 0.3 - toMob * 0.45;
					if (score > bestScore) {
						bestScore = score;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	private boolean isStandableWithHeadroom(BlockPos feet) {
		BlockState floor = this.level.getBlockState(feet.below());
		if (floor.getCollisionShape(this.level, feet.below()).isEmpty()) {
			return false;
		}
		for (int i = 0; i < WeaverNodeEvaluator.HEADROOM; i++) {
			BlockPos air = feet.above(i);
			if (!this.level.getBlockState(air).getCollisionShape(this.level, air).isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
