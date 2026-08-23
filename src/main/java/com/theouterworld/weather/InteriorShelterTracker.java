package com.theouterworld.weather;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.network.InteriorShelterSyncPacket;
import com.theouterworld.registry.ModDimensions;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Player-driven block updates in the Outerworld queue a flood-fill.
 * Sealed air volumes are stored as interior shelter safezones and looked up by position
 * (no movement scanning) — dust storms treat those cells as immune.
 */
public final class InteriorShelterTracker {
	private static final Map<UUID, PendingUpdate> pending = new HashMap<>();

	private InteriorShelterTracker() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
				queuePlayerUpdate(serverLevel, pos, serverPlayer);
			}
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClientSide()
				&& world instanceof ServerLevel serverLevel
				&& player instanceof ServerPlayer serverPlayer
				&& serverLevel.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
				queuePlayerUpdate(serverLevel, hitResult.getBlockPos(), serverPlayer);
			}
			return InteractionResult.PASS;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> processPending());
	}

	public static void onBlockPlaced(Level level, BlockPos placedPos, @Nullable LivingEntity placer) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (!serverLevel.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}
		if (!(placer instanceof ServerPlayer player)) {
			return;
		}
		queuePlayerUpdate(serverLevel, placedPos, player);
	}

	private static void queuePlayerUpdate(ServerLevel level, BlockPos pos, ServerPlayer player) {
		pending.put(player.getUUID(), new PendingUpdate(level, pos.immutable(), player.getUUID()));
	}

	private static void processPending() {
		if (pending.isEmpty()) {
			return;
		}

		Iterator<Map.Entry<UUID, PendingUpdate>> iterator = pending.entrySet().iterator();
		while (iterator.hasNext()) {
			PendingUpdate update = iterator.next().getValue();
			iterator.remove();

			ServerPlayer player = update.level().getServer().getPlayerList().getPlayer(update.playerId());
			if (player == null || player.level() != update.level()) {
				continue;
			}
			if (!update.level().dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
				continue;
			}

			ServerLevel level = update.level();
			InteriorShelterSavedData data = InteriorShelterSavedData.get(level);
			List<InteriorShelterSavedData.Region> removed = data.removeAround(update.pos());
			if (!removed.isEmpty()) {
				syncToLevel(level);
			}

			// Re-probe former interior cells so nearby outdoor edits don't permanently wipe shelter.
			for (InteriorShelterSavedData.Region region : removed) {
				rescanRegionSeeds(level, region);
			}
			// Discover / update from air next to the changed block (sealing a room from outside).
			scanAdjacentAir(level, update.pos());
			scanFromPlayer(level, player);
		}
	}

	private static void rescanRegionSeeds(ServerLevel level, InteriorShelterSavedData.Region region) {
		List<Long> cells = region.cells();
		if (cells.isEmpty()) {
			return;
		}
		// First cell plus a mid cell covers small rooms and reduces stale-corner misses.
		tryAddFromSeed(level, BlockPos.of(cells.get(0)));
		if (cells.size() > 1) {
			tryAddFromSeed(level, BlockPos.of(cells.get(cells.size() / 2)));
		}
	}

	private static void scanAdjacentAir(ServerLevel level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbor = pos.relative(direction);
			if (level.getBlockState(neighbor).isAir()) {
				tryAddFromSeed(level, neighbor);
			}
		}
	}

	private static void tryAddFromSeed(ServerLevel level, BlockPos seed) {
		if (!level.getBlockState(seed).isAir()) {
			return;
		}
		InteriorFloodFiller.ScanResult scan = InteriorFloodFiller.scan(level, seed);
		if (!scan.sealed()) {
			return;
		}
		InteriorFloodFiller.Result result = new InteriorFloodFiller.Result(scan.visitedAir());
		InteriorShelterSavedData data = InteriorShelterSavedData.get(level);
		if (data.addRegion(
			result.cells(),
			result.minX(),
			result.minY(),
			result.minZ(),
			result.maxX(),
			result.maxY(),
			result.maxZ()
		)) {
			syncToLevel(level);
			OuterWorldMod.LOGGER.debug("Marked sealed interior ({} cells) at {}", scan.visitedAir().size(), seed);
		}
	}

	/** Used by {@code /duststorm interiors} and {@code rescan}. */
	public static InteriorFloodFiller.ScanResult scanFromPlayer(ServerLevel level, ServerPlayer player) {
		if (!level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return new InteriorFloodFiller.ScanResult(false, Set.of());
		}

		BlockPos seed = BlockPos.containing(player.getX(), player.getY(), player.getZ());
		InteriorFloodFiller.ScanResult scan = InteriorFloodFiller.scan(level, seed);
		InteriorShelterSavedData data = InteriorShelterSavedData.get(level);

		if (scan.sealed()) {
			InteriorFloodFiller.Result result = new InteriorFloodFiller.Result(scan.visitedAir());
			boolean changed = data.addRegion(
				result.cells(),
				result.minX(),
				result.minY(),
				result.minZ(),
				result.maxX(),
				result.maxY(),
				result.maxZ()
			);
			if (changed) {
				syncToLevel(level);
				OuterWorldMod.LOGGER.debug(
					"Marked sealed interior ({} cells) near {}",
					scan.visitedAir().size(),
					player.getName().getString()
				);
			}
		} else if (data.isInterior(seed) && !data.removeAround(seed).isEmpty()) {
			syncToLevel(level);
		}

		return scan;
	}

	public static void syncToPlayer(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		if (!level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			ServerPlayNetworking.send(player, new InteriorShelterSyncPacket(new long[0]));
			return;
		}
		ServerPlayNetworking.send(player, new InteriorShelterSyncPacket(toArray(InteriorShelterSavedData.get(level).allCells())));
	}

	public static void syncToLevel(ServerLevel level) {
		if (!level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}
		InteriorShelterSyncPacket packet = new InteriorShelterSyncPacket(toArray(InteriorShelterSavedData.get(level).allCells()));
		PlayerLookup.level(level).forEach(p -> ServerPlayNetworking.send(p, packet));
	}

	public static boolean isInterior(ServerLevel level, BlockPos pos) {
		if (!level.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return false;
		}
		return InteriorShelterSavedData.get(level).isInterior(pos);
	}

	private static long[] toArray(LongSet cells) {
		return cells.toLongArray();
	}

	private record PendingUpdate(ServerLevel level, BlockPos pos, UUID playerId) {
	}
}
