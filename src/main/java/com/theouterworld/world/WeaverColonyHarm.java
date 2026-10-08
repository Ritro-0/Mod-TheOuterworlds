package com.theouterworld.world;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.entity.ai.WeaverAnchors;
import com.theouterworld.entity.ai.WeaverColonies;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.registry.ModTags;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Per-player ruin of one Anchor: mass structure breaks, destructive fluids, and
 * attributable blasts. Other Anchors keep their own records.
 */
public final class WeaverColonyHarm {
	private static final int SOURCE_MEMORY = 12000;
	private static final int MAX_SOURCES = 128;
	private static final int FLOW_RANGE = 12;
	private static final int FLOW_VISITS = 256;

	private static final List<FluidSource> SOURCES = new ArrayList<>();

	private WeaverColonyHarm() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world instanceof ServerLevel level) {
				noteStructureBreak(level, pos, state, player);
			}
		});
	}

	public static void noteStructureBreak(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		if (!surviving(player) || !state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
			return;
		}
		long id = WeaverColonies.colonyOwning(level, pos);
		if (id == 0L) {
			return;
		}
		String block = state.getBlock().getName().getString();
		WeaverColonySavedData.get(level).noteStructureBreak(level, id, player.getUUID(), pos, block);
	}

	public static void notePlaced(ServerLevel level, BlockPos pos, Player player) {
		if (!surviving(player) || !WeaverAnchors.isLitter(level, pos)) {
			return;
		}
		if (!WeaverAnchors.isInsideAnchor(level, pos) && !adjacentAnchor(level, pos)) {
			return;
		}
		long id = WeaverColonies.colonyOwning(level, pos);
		if (id == 0L) {
			return;
		}
		WeaverColonySavedData.get(level).notePlacement(id, pos, player.getUUID());
	}

	public static void onPlayerFluid(Player player, ServerLevel level, BlockPos pos, Fluid fluid) {
		if (!surviving(player) || kind(fluid) == 0) {
			return;
		}
		if (!fluidPresent(level, pos, fluid)) {
			return;
		}
		remember(level, pos, player.getUUID(), kind(fluid));
		if (!touchesAnchor(level, pos)) {
			return;
		}
		long id = WeaverColonies.colonyOwning(level, pos);
		if (id != 0L) {
			WeaverColonySavedData.get(level).markPlayerHostile(
				level,
				id,
				player.getUUID(),
				pos,
				"put " + fluidName(kind(fluid)) + " on the Anchor. You are now hostile"
			);
		}
	}

	public static void onFluidSpread(ServerLevel level, BlockPos dest, Direction from, Fluid fluid) {
		int fluidKind = kind(fluid);
		if (fluidKind == 0 || !hasRecentSource(level, fluidKind)) {
			return;
		}
		if (!touchesAnchor(level, dest)) {
			return;
		}
		UUID player = sourcePlayer(level, dest.relative(from.getOpposite()), fluidKind);
		if (player == null) {
			return;
		}
		long id = WeaverColonies.colonyOwning(level, dest);
		if (id == 0L) {
			id = WeaverColonies.colonyOwning(level, dest.relative(from.getOpposite()));
		}
		if (id != 0L) {
			WeaverColonySavedData.get(level).markPlayerHostile(
				level,
				id,
				player,
				dest,
				fluidName(fluidKind) + " reached the Anchor. You are now hostile"
			);
		}
	}

	public static void onDetonation(ServerLevel level, Player player, List<BlockPos> blocks, @Nullable Vec3 center) {
		if (!surviving(player)) {
			return;
		}
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		UUID uuid = player.getUUID();
		Map<Long, Integer> ruined = new HashMap<>();
		Map<Long, BlockPos> where = new HashMap<>();
		for (BlockPos pos : blocks) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(ModTags.WEAVER_ANCHOR_PARTS)) {
				continue;
			}
			long id = WeaverColonies.colonyOwning(level, pos);
			if (id != 0L) {
				ruined.merge(id, 1, Integer::sum);
				where.putIfAbsent(id, pos);
			}
		}
		for (Map.Entry<Long, Integer> entry : ruined.entrySet()) {
			data.noteExplosion(level, entry.getKey(), uuid, entry.getValue(), where.get(entry.getKey()));
		}
		if (center != null) {
			BlockPos at = BlockPos.containing(center);
			long id = colonyAtBlast(level, at);
			if (id != 0L) {
				data.markPlayerHostile(level, id, uuid, at, "detonated inside the Anchor. You are now hostile");
			}
		}
	}

	private static long colonyAtBlast(ServerLevel level, BlockPos pos) {
		if (touchesAnchor(level, pos)) {
			long id = WeaverColonies.colonyOwning(level, pos);
			if (id != 0L) {
				return id;
			}
		}
		for (Direction direction : Direction.values()) {
			BlockPos next = pos.relative(direction);
			if (!level.getBlockState(next).is(ModTags.WEAVER_ANCHOR_PARTS)) {
				continue;
			}
			long id = WeaverColonies.colonyOwning(level, next);
			if (id != 0L) {
				return id;
			}
		}
		return 0L;
	}

	private static boolean surviving(Player player) {
		return player != null && !player.isSpectator() && !player.isCreative() && player.gameMode().isSurvival();
	}

	private static boolean touchesAnchor(ServerLevel level, BlockPos pos) {
		if (level.getBlockState(pos).is(ModTags.WEAVER_ANCHOR_PARTS) || adjacentAnchor(level, pos)) {
			return WeaverColonies.generatedId(level, pos) != 0L || WeaverAnchors.isInsideAnchor(level, pos);
		}
		if (WeaverColonies.generatedId(level, pos) == 0L) {
			return false;
		}
		return WeaverAnchors.isInsideAnchor(level, pos);
	}

	private static boolean adjacentAnchor(ServerLevel level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (level.getBlockState(pos.relative(direction)).is(ModTags.WEAVER_ANCHOR_PARTS)) {
				return true;
			}
		}
		return false;
	}

	private static boolean fluidPresent(ServerLevel level, BlockPos pos, Fluid fluid) {
		Fluid there = level.getFluidState(pos).getType();
		if (kind(there) != 0 && kind(there) == kind(fluid)) {
			return true;
		}
		BlockState state = level.getBlockState(pos);
		return state.is(ModBlocks.MERCURY) || state.is(ModBlocks.MERCURY_BLOCK);
	}

	private static String fluidName(int fluidKind) {
		return switch (fluidKind) {
			case 1 -> "water";
			case 2 -> "lava";
			case 3 -> "mercury";
			default -> "fluid";
		};
	}

	private static int kind(Fluid fluid) {
		if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
			return 1;
		}
		if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
			return 2;
		}
		if (fluid == ModFluids.MERCURY || fluid == ModFluids.FLOWING_MERCURY) {
			return 3;
		}
		return 0;
	}

	private static void remember(ServerLevel level, BlockPos pos, UUID player, int fluidKind) {
		long now = level.getGameTime();
		SOURCES.removeIf(source -> now - source.gameTime > SOURCE_MEMORY);
		while (SOURCES.size() >= MAX_SOURCES) {
			SOURCES.remove(0);
		}
		SOURCES.add(new FluidSource(level.dimension(), pos.asLong(), player, now, fluidKind));
	}

	private static boolean hasRecentSource(ServerLevel level, int fluidKind) {
		long now = level.getGameTime();
		ResourceKey<Level> dimension = level.dimension();
		for (FluidSource source : SOURCES) {
			if (source.kind == fluidKind && source.dimension.equals(dimension) && now - source.gameTime <= SOURCE_MEMORY) {
				return true;
			}
		}
		return false;
	}

	private static @Nullable UUID sourcePlayer(ServerLevel level, BlockPos origin, int fluidKind) {
		long now = level.getGameTime();
		ResourceKey<Level> dimension = level.dimension();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<Long> seen = new HashSet<>();
		queue.add(origin);
		seen.add(origin.asLong());
		while (!queue.isEmpty() && seen.size() <= FLOW_VISITS) {
			BlockPos pos = queue.removeFirst();
			for (FluidSource source : SOURCES) {
				if (source.kind == fluidKind
					&& source.pos == pos.asLong()
					&& source.dimension.equals(dimension)
					&& now - source.gameTime <= SOURCE_MEMORY) {
					return source.player;
				}
			}
			if (Math.abs(pos.getX() - origin.getX()) > FLOW_RANGE || Math.abs(pos.getY() - origin.getY()) > FLOW_RANGE
				|| Math.abs(pos.getZ() - origin.getZ()) > FLOW_RANGE) {
				continue;
			}
			for (Direction direction : Direction.values()) {
				BlockPos next = pos.relative(direction);
				if (!seen.add(next.asLong()) || !level.hasChunkAt(next)) {
					continue;
				}
				if (kind(level.getFluidState(next).getType()) != fluidKind
					&& !(fluidKind == 3 && (level.getBlockState(next).is(ModBlocks.MERCURY) || level.getBlockState(next).is(ModBlocks.MERCURY_BLOCK)))) {
					continue;
				}
				queue.add(next);
			}
		}
		return null;
	}

	private record FluidSource(ResourceKey<Level> dimension, long pos, UUID player, long gameTime, int kind) {
	}
}
