package com.theouterworld.entity.ai;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Colony A*: climb first, then drop. Vanilla's Euclidean heuristic hugs the
 * nearest wall because that node is "closest" to a bed in a pod twenty blocks
 * up. We treat gaining altitude (even overshooting the target Y) as the
 * remaining work until the weaver is above the destination, then path in XZ
 * and fall onto the pod.
 */
public class WeaverPathFinder extends PathFinder {
	private static final float HEURISTIC_WEIGHT = 1.12F;
	private static final int OVERSHOOT = 6;
	private static final float CLIMB_Y_WEIGHT = 2.6F;
	private static final float CLIMB_XZ_WEIGHT = 0.2F;
	private static final float DROP_Y_WEIGHT = 0.28F;
	private static final float UNDER_POD_PENALTY = 10.0F;

	private final NodeEvaluator evaluator;
	private final Node[] neighbors = new Node[48];
	private final BinaryHeap openSet = new BinaryHeap();
	private int maxVisitedNodes;
	private boolean flat;

	public WeaverPathFinder(NodeEvaluator evaluator, int maxVisitedNodes) {
		super(evaluator, maxVisitedNodes);
		this.evaluator = evaluator;
		this.maxVisitedNodes = maxVisitedNodes;
	}

	@Override
	public void setMaxVisitedNodes(int maxVisitedNodes) {
		int capped = Math.max(maxVisitedNodes, 4096);
		super.setMaxVisitedNodes(capped);
		this.maxVisitedNodes = capped;
	}

	@Override
	public @Nullable Path findPath(
		PathNavigationRegion level,
		Mob entity,
		Set<BlockPos> targets,
		float maxPathLength,
		int reachRange,
		float maxVisitedNodesMultiplier
	) {
		this.openSet.clear();
		this.evaluator.prepare(level, entity);
		Node from = this.evaluator.getStart();
		if (from == null) {
			this.evaluator.done();
			return null;
		}

		Map<Target, BlockPos> tos = targets.stream()
			.collect(Collectors.toMap(pos -> this.evaluator.getTarget(pos.getX(), pos.getY(), pos.getZ()), Function.identity()));
		Path path = search(from, tos, maxPathLength, reachRange, maxVisitedNodesMultiplier);
		this.evaluator.done();
		return path;
	}

	private @Nullable Path search(
		Node from,
		Map<Target, BlockPos> targetMap,
		float maxPathLength,
		int reachRange,
		float maxVisitedNodesMultiplier
	) {
		Set<Target> targets = targetMap.keySet();
		this.flat = this.evaluator instanceof WeaverNodeEvaluator nodes && nodes.flatApproach();
		from.g = 0.0F;
		from.h = bestH(from, targets);
		from.f = from.h;
		this.openSet.clear();
		this.openSet.insert(from);

		int count = 0;
		Set<Target> reached = Sets.newHashSetWithExpectedSize(targets.size());
		int budget = Math.max(64, (int) (this.maxVisitedNodes * maxVisitedNodesMultiplier));

		while (!this.openSet.isEmpty()) {
			if (++count >= budget) {
				break;
			}

			Node current = this.openSet.pop();
			current.closed = true;

			for (Target target : targets) {
				if (current.distanceManhattan(target) <= reachRange) {
					target.setReached();
					reached.add(target);
				}
			}
			if (!reached.isEmpty()) {
				break;
			}
			if (current.distanceTo(from) >= maxPathLength) {
				continue;
			}

			int neighborCount = this.evaluator.getNeighbors(this.neighbors, current);
			for (int i = 0; i < neighborCount; i++) {
				Node neighbor = this.neighbors[i];
				float step = this.flat ? current.distanceTo(neighbor) : stepCost(current, neighbor);
				neighbor.walkedDistance = current.walkedDistance + step;
				float tentativeG = current.g + step + neighbor.costMalus;
				if (neighbor.walkedDistance < maxPathLength && (!neighbor.inOpenSet() || tentativeG < neighbor.g)) {
					neighbor.cameFrom = current;
					neighbor.g = tentativeG;
					neighbor.h = bestH(neighbor, targets) * HEURISTIC_WEIGHT;
					if (neighbor.inOpenSet()) {
						this.openSet.changeCost(neighbor, neighbor.g + neighbor.h);
					} else {
						neighbor.f = neighbor.g + neighbor.h;
						this.openSet.insert(neighbor);
					}
				}
			}
		}

		Optional<Path> best = !reached.isEmpty()
			? reached.stream()
				.map(target -> reconstruct(target.getBestNode(), targetMap.get(target), true))
				.min(Comparator.comparingInt(Path::getNodeCount))
			: targets.stream()
				.map(target -> reconstruct(target.getBestNode(), targetMap.get(target), false))
				.filter(path -> path.getEndNode() != null)
				.min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
		return best.orElse(null);
	}

	/**
	 * Prefer rising over walking. A 2-up parkour hop should beat shuffling
	 * along the floor toward a wall.
	 */
	private static float stepCost(Node from, Node to) {
		float dist = from.distanceTo(to);
		if (to.y > from.y) {
			// A single step through a doorway must not be cheaper than walking it.
			return dist * (to.y - from.y <= 1 ? 1.15F : 0.85F);
		}
		if (to.y < from.y) {
			return dist * 0.78F;
		}
		return dist;
	}

	private float bestH(Node from, Set<Target> targets) {
		float best = Float.MAX_VALUE;
		for (Target target : targets) {
			float h = this.flat ? from.distanceTo(target) : climbFirstH(from, target);
			target.updateBest(h, from);
			best = Math.min(best, h);
		}
		return best;
	}

	/**
	 * While below the destination, remaining work is mostly vertical — even
	 * detouring away from the bed to a spiral is cheaper than hugging the
	 * wall. Once at or above the bed, drop and close in XZ.
	 */
	private static float climbFirstH(Node from, Target target) {
		float dx = target.x - from.x;
		float dz = target.z - from.z;
		float xz = Mth.sqrt(dx * dx + dz * dz);
		int preferredY = target.y + OVERSHOOT;

		if (from.y < target.y - 1 && xz <= 4.0F) {
			return (target.y - from.y) * CLIMB_Y_WEIGHT + UNDER_POD_PENALTY + xz;
		}
		if (from.y < preferredY && xz > 1.5F) {
			return (preferredY - from.y) * CLIMB_Y_WEIGHT + xz * CLIMB_XZ_WEIGHT;
		}
		float drop = Math.max(0.0F, from.y - target.y);
		return xz + drop * DROP_Y_WEIGHT;
	}

	private static Path reconstruct(@Nullable Node closest, BlockPos target, boolean reached) {
		List<Node> nodes = Lists.newArrayList();
		Node node = closest;
		if (node == null) {
			return new Path(nodes, target, false);
		}
		nodes.add(0, node);
		while (node.cameFrom != null) {
			node = node.cameFrom;
			nodes.add(0, node);
		}
		return new Path(nodes, target, reached);
	}
}
