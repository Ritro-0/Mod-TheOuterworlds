package com.theouterworld.video.patrick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * TEMP VIDEO FEATURE — scans for a Patrick build within {@link PatrickTemplate#SEARCH_RADIUS}.
 */
public final class PatrickMatcher {
	private static final int SCAN_Y_PAD = 32;

	private PatrickMatcher() {
	}

	@Nullable
	public static Match findNear(ServerLevel world, BlockPos center) {
		for (PatrickVideo.SpawnRecord recent : PatrickVideo.recentSpawns()) {
			if (recent.origin().closerThan(center, PatrickTemplate.SEARCH_RADIUS + 16)
				&& matches(world, recent.origin(), recent.rotation())) {
				return new Match(recent.origin(), recent.rotation());
			}
		}

		int radius = PatrickTemplate.SEARCH_RADIUS;
		int minX = center.getX() - radius;
		int maxX = center.getX() + radius;
		int minY = center.getY() - SCAN_Y_PAD;
		int maxY = center.getY() + SCAN_Y_PAD;
		int minZ = center.getZ() - radius;
		int maxZ = center.getZ() + radius;

		List<BlockPos> redBricks = new ArrayList<>();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (world.getBlockState(pos).is(Blocks.RED_NETHER_BRICKS)) {
						redBricks.add(pos.immutable());
					}
				}
			}
		}

		List<PatrickTemplate.BlockEntry> fingerprints = PatrickTemplate.blocks().stream()
			.filter(entry -> entry.state().is(Blocks.RED_NETHER_BRICKS))
			.toList();

		Set<Long> tried = new HashSet<>();
		for (BlockPos brick : redBricks) {
			for (Rotation rotation : Rotation.values()) {
				for (PatrickTemplate.BlockEntry finger : fingerprints) {
					BlockPos local = PatrickTemplate.transform(finger.x(), finger.y(), finger.z(), rotation);
					BlockPos origin = brick.subtract(local);
					long key = origin.asLong() ^ (((long) rotation.ordinal()) << 48);
					if (!tried.add(key)) {
						continue;
					}
					if (matches(world, origin, rotation)) {
						return new Match(origin, rotation);
					}
				}
			}
		}
		return null;
	}

	public static boolean matches(ServerLevel world, BlockPos origin, Rotation rotation) {
		for (PatrickTemplate.PlacedBlock placed : PatrickTemplate.placedBlocks(rotation)) {
			BlockPos worldPos = origin.offset(placed.local());
			BlockState actual = world.getBlockState(worldPos);
			if (!PatrickTemplate.statesMatch(actual, placed.state())) {
				return false;
			}
		}
		return !findEyeFrames(world, origin, rotation).isEmpty();
	}

	/**
	 * Prefer exact template eye slots; otherwise accept any 2+ redstone glow frames on/near the head.
	 */
	public static List<GlowItemFrame> findEyeFrames(ServerLevel world, BlockPos origin, Rotation rotation) {
		AABB box = structureBox(origin, rotation).inflate(1.5);
		List<GlowItemFrame> frames = world.getEntitiesOfClass(
			GlowItemFrame.class,
			box,
			frame -> frame.getItem().is(Items.REDSTONE_BLOCK) && frame.isAlive()
		);
		if (frames.isEmpty()) {
			return List.of();
		}

		List<GlowItemFrame> exact = new ArrayList<>();
		for (PatrickTemplate.PlacedFrame want : PatrickTemplate.placedFrames(rotation)) {
			BlockPos attach = origin.offset(want.local());
			for (GlowItemFrame frame : frames) {
				if (frame.getPos().equals(attach) && frame.getDirection() == want.facing()) {
					exact.add(frame);
					break;
				}
			}
		}
		if (exact.size() >= 2) {
			return exact;
		}

		// Manual eyes / slightly wrong attach still count if there are two near the head.
		AABB head = headBox(origin, rotation).inflate(1.25);
		List<GlowItemFrame> onHead = frames.stream()
			.filter(frame -> head.contains(frame.position()))
			.limit(2)
			.toList();
		// One eye is enough to awaken; two is preferred for lasers from both sides.
		return onHead.isEmpty() ? List.of() : onHead;
	}

	public static List<Vec3> eyeOrigins(ServerLevel world, BlockPos origin, Rotation rotation) {
		List<GlowItemFrame> frames = findEyeFrames(world, origin, rotation);
		if (!frames.isEmpty()) {
			return frames.stream().map(GlowItemFrame::position).toList();
		}
		List<Vec3> fallback = new ArrayList<>();
		for (PatrickTemplate.PlacedFrame frame : PatrickTemplate.placedFrames(rotation)) {
			BlockPos attach = origin.offset(frame.local());
			fallback.add(Vec3.atCenterOf(attach).add(
				frame.facing().getStepX() * 0.55,
				frame.facing().getStepY() * 0.55,
				frame.facing().getStepZ() * 0.55
			));
		}
		return fallback;
	}

	public static AABB structureBox(BlockPos origin, Rotation rotation) {
		var size = PatrickTemplate.sizeFor(rotation);
		return new AABB(
			origin.getX(),
			origin.getY(),
			origin.getZ(),
			origin.getX() + size.getX(),
			origin.getY() + size.getY(),
			origin.getZ() + size.getZ()
		);
	}

	public static AABB headBox(BlockPos origin, Rotation rotation) {
		BlockPos a = origin.offset(PatrickTemplate.transform(6, 5, 1, rotation));
		BlockPos b = origin.offset(PatrickTemplate.transform(7, 7, 2, rotation));
		return new AABB(
			Math.min(a.getX(), b.getX()),
			Math.min(a.getY(), b.getY()),
			Math.min(a.getZ(), b.getZ()),
			Math.max(a.getX(), b.getX()) + 1,
			Math.max(a.getY(), b.getY()) + 1,
			Math.max(a.getZ(), b.getZ()) + 1
		);
	}

	public static Vec3 lookTarget(BlockPos origin, Rotation rotation) {
		BlockPos a = origin.offset(PatrickTemplate.transform(6, 6, 1, rotation));
		BlockPos b = origin.offset(PatrickTemplate.transform(7, 6, 2, rotation));
		return new Vec3(
			(a.getX() + b.getX() + 1) * 0.5,
			Math.max(a.getY(), b.getY()) + 0.55,
			(a.getZ() + b.getZ() + 1) * 0.5
		);
	}

	public record Match(BlockPos origin, Rotation rotation) {
	}
}
