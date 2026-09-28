package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Ground pathfinding with a hard 3-block headroom check and two-block sidesteps
 * so a weaver can go around a wall. Vertical travel is a step or a stair, not a hop.
 */
public class WeaverNodeEvaluator extends WalkNodeEvaluator {
	public static final int HEADROOM = 3;

	@Override
	public void prepare(PathNavigationRegion level, Mob entity) {
		super.prepare(level, entity);
		this.entityHeight = Math.max(HEADROOM, Mth.ceil(entity.getBbHeight()));
	}

	public boolean flatApproach() {
		return this.mob instanceof WeaverEntity weaver && weaver.isFlatApproach();
	}

	@Override
	public int getNeighbors(Node[] neighbors, Node pos) {
		int count = super.getNeighbors(neighbors, pos);
		if (!(this.mob instanceof WeaverEntity)) {
			return count;
		}

		double floor = this.getFloorLevel(new BlockPos(pos.x, pos.y, pos.z));
		PathType here = this.getCachedPathType(pos.x, pos.y, pos.z);

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (count >= neighbors.length) {
				break;
			}
			int x2 = pos.x + dir.getStepX() * 2;
			int z2 = pos.z + dir.getStepZ() * 2;
			count = addUnique(neighbors, count, pos, this.findAcceptedNode(x2, pos.y, z2, 0, floor, dir, here));
		}
		return count;
	}

	private int addUnique(Node[] neighbors, int count, Node current, Node candidate) {
		if (!this.isNeighborValid(candidate, current)) {
			return count;
		}
		for (int i = 0; i < count; i++) {
			if (neighbors[i].equals(candidate)) {
				return count;
			}
		}
		neighbors[count] = candidate;
		return count + 1;
	}
}
