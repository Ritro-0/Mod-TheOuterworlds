package com.theouterworld.world;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BeaconConcentratorPortals {
	private BeaconConcentratorPortals() {
	}

	public static void register() {
		ServerChunkEvents.CHUNK_LOAD.register(BeaconConcentratorPortals::onChunkLoad);
	}

	public static void activate(ServerLevel overworld, BlockPos concentratorPos) {
		MinecraftServer server = overworld.getServer();
		BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
		if (data == null) {
			return;
		}
		data.addConcentrator(concentratorPos);
		ensureInnerworldBeam(server, data, concentratorPos);
	}

	public static void deactivate(ServerLevel overworld, BlockPos concentratorPos) {
		MinecraftServer server = overworld.getServer();
		BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
		if (data == null) {
			return;
		}
		data.removeConcentrator(concentratorPos);
		removeInnerworldBeams(server, data, concentratorPos.getX(), concentratorPos.getZ());
	}

	/**
	 * Loads the destination chunk, ensures a return beam exists when travelling to the Innerworld,
	 * and returns a safe standing position at surface height (or on the overworld concentrator).
	 */
	public static Vec3 prepareArrival(MinecraftServer server, int x, int z, boolean toInnerworld) {
		if (toInnerworld) {
			ServerLevel innerworld = server.getLevel(ModDimensions.INNERWORLD_WORLD_KEY);
			if (innerworld == null) {
				return new Vec3(x + 0.5, 64.0, z + 0.5);
			}
			innerworld.getChunk(x >> 4, z >> 4);
			BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
			if (data != null) {
				ensureInnerworldBeam(server, data, new BlockPos(x, 64, z));
			}
			return surfaceStandPos(innerworld, x, z);
		}

		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return new Vec3(x + 0.5, 64.0, z + 0.5);
		}
		overworld.getChunk(x >> 4, z >> 4);
		BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
		if (data != null) {
			BlockPos concentrator = data.findConcentratorAt(x, z);
			if (concentrator != null && overworld.getBlockState(concentrator).is(ModBlocks.BEACON_CONCENTRATOR)) {
				return Vec3.atBottomCenterOf(concentrator.above());
			}
		}
		return surfaceStandPos(overworld, x, z);
	}

	public static Vec3 surfaceStandPos(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		y = Mth.clamp(y, level.getMinY() + 1, level.getMaxY() - 1);
		BlockPos feet = new BlockPos(x, y, z);
		for (int i = 0; i < 24 && feet.getY() < level.getMaxY(); i++) {
			if (isStandable(level, feet)) {
				return new Vec3(x + 0.5, feet.getY(), z + 0.5);
			}
			feet = feet.above();
		}
		return new Vec3(x + 0.5, y, z + 0.5);
	}

	private static boolean isStandable(ServerLevel level, BlockPos feet) {
		return feet.getY() < level.getMaxY()
			&& level.getBlockState(feet).canBeReplaced()
			&& level.getBlockState(feet.above()).canBeReplaced();
	}

	private static void onChunkLoad(ServerLevel world, LevelChunk chunk, boolean newChunk) {
		if (!ModDimensions.isInnerworld(world.dimension())) {
			return;
		}
		MinecraftServer server = world.getServer();
		BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
		if (data == null) {
			return;
		}
		int chunkX = chunk.getPos().x();
		int chunkZ = chunk.getPos().z();

		for (BlockPos concentrator : data.concentratorsInChunk(chunkX, chunkZ)) {
			ensureInnerworldBeam(server, data, concentrator);
		}

		for (BlockPos beamPos : data.innerBeamsInChunk(chunkX, chunkZ)) {
			if (data.hasConcentratorAt(beamPos.getX(), beamPos.getZ())) {
				if (!world.getBlockState(beamPos).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
					ensureInnerworldBeam(server, data, beamPos);
				}
			} else {
				removeBeamBlock(world, data, beamPos);
			}
		}
	}

	private static void ensureInnerworldBeam(MinecraftServer server, BeaconConcentratorSavedData data, BlockPos concentratorPos) {
		ServerLevel innerworld = server.getLevel(ModDimensions.INNERWORLD_WORLD_KEY);
		if (innerworld == null) {
			return;
		}
		int chunkX = concentratorPos.getX() >> 4;
		int chunkZ = concentratorPos.getZ() >> 4;
		if (!innerworld.hasChunk(chunkX, chunkZ)) {
			return;
		}

		for (BlockPos existing : data.innerBeamsAt(concentratorPos.getX(), concentratorPos.getZ())) {
			if (innerworld.getBlockState(existing).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
				return;
			}
			data.forgetInnerBeam(existing);
		}

		BlockPos placeAt = findBeamPos(innerworld, concentratorPos);
		innerworld.setBlock(placeAt, ModBlocks.CONCENTRATED_BEACON_BEAM.defaultBlockState(), Block.UPDATE_CLIENTS);
		data.rememberInnerBeam(placeAt);
	}

	private static void removeInnerworldBeams(MinecraftServer server, BeaconConcentratorSavedData data, int x, int z) {
		ServerLevel innerworld = server.getLevel(ModDimensions.INNERWORLD_WORLD_KEY);
		if (innerworld == null || !innerworld.hasChunk(x >> 4, z >> 4)) {
			return;
		}
		for (BlockPos beamPos : data.innerBeamsAt(x, z)) {
			removeBeamBlock(innerworld, data, beamPos);
		}
		int minY = innerworld.getMinY();
		int maxY = innerworld.getMaxY();
		for (int y = minY; y <= maxY; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (innerworld.getBlockState(pos).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
				removeBeamBlock(innerworld, data, pos);
			}
		}
	}

	private static void removeBeamBlock(ServerLevel world, BeaconConcentratorSavedData data, BlockPos pos) {
		if (world.getBlockState(pos).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
			world.removeBlock(pos, false);
		}
		data.forgetInnerBeam(pos);
	}

	private static BlockPos findBeamPos(ServerLevel level, BlockPos hint) {
		int x = hint.getX();
		int z = hint.getZ();
		level.getChunk(x >> 4, z >> 4);
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		y = Mth.clamp(y, level.getMinY() + 1, level.getMaxY());
		BlockPos at = new BlockPos(x, y, z);
		if (level.getBlockState(at).is(ModBlocks.CONCENTRATED_BEACON_BEAM) || level.getBlockState(at).canBeReplaced()) {
			return at;
		}
		for (int dy = 1; dy < 8; dy++) {
			BlockPos up = at.above(dy);
			if (up.getY() > level.getMaxY()) {
				break;
			}
			BlockState state = level.getBlockState(up);
			if (state.is(ModBlocks.CONCENTRATED_BEACON_BEAM) || state.canBeReplaced()) {
				return up;
			}
		}
		return at;
	}
}
