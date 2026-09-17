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
		ensureMoonBeam(server, data, concentratorPos);
	}

	public static void deactivate(ServerLevel overworld, BlockPos concentratorPos) {
		MinecraftServer server = overworld.getServer();
		BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
		if (data == null) {
			return;
		}
		data.removeConcentrator(concentratorPos);
		removeMoonBeams(server, data, concentratorPos.getX(), concentratorPos.getZ());
	}

	/**
	 * Loads the destination chunk, ensures a return beam exists when travelling to the Moon,
	 * and returns a safe standing position at surface height (or on the overworld concentrator).
	 */
	public static Vec3 prepareArrival(MinecraftServer server, int x, int z, boolean toMoon) {
		if (toMoon) {
			ServerLevel moon = server.getLevel(ModDimensions.MOON_WORLD_KEY);
			if (moon == null) {
				return new Vec3(x + 0.5, 64.0, z + 0.5);
			}
			moon.getChunk(x >> 4, z >> 4);
			BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
			if (data != null) {
				ensureMoonBeam(server, data, new BlockPos(x, 64, z));
			}
			return surfaceStandPos(moon, x, z);
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
		LevelChunk chunk = loadedChunk(level, x >> 4, z >> 4);
		if (chunk == null) {
			level.getChunk(x >> 4, z >> 4);
			chunk = loadedChunk(level, x >> 4, z >> 4);
		}
		int y = heightOn(chunk, level, x, z);
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
		if (!ModDimensions.isMoon(world.dimension())) {
			return;
		}
		int chunkX = chunk.getPos().x();
		int chunkZ = chunk.getPos().z();
		// Placing or removing blocks here re-enters chunk loading on the same thread and hangs.
		world.getServer().execute(() -> reconcileMoonChunk(world, chunkX, chunkZ));
	}

	private static void reconcileMoonChunk(ServerLevel world, int chunkX, int chunkZ) {
		if (loadedChunk(world, chunkX, chunkZ) == null) {
			return;
		}
		MinecraftServer server = world.getServer();
		BeaconConcentratorSavedData data = BeaconConcentratorSavedData.get(server);
		if (data == null) {
			return;
		}

		for (BlockPos concentrator : data.concentratorsInChunk(chunkX, chunkZ)) {
			ensureMoonBeam(server, data, concentrator);
		}

		for (BlockPos beamPos : data.innerBeamsInChunk(chunkX, chunkZ)) {
			if (data.hasConcentratorAt(beamPos.getX(), beamPos.getZ())) {
				if (!world.getBlockState(beamPos).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
					ensureMoonBeam(server, data, beamPos);
				}
			} else {
				removeBeamBlock(world, data, beamPos);
			}
		}
	}

	private static void ensureMoonBeam(MinecraftServer server, BeaconConcentratorSavedData data, BlockPos concentratorPos) {
		ServerLevel moon = server.getLevel(ModDimensions.MOON_WORLD_KEY);
		if (moon == null) {
			return;
		}
		int chunkX = concentratorPos.getX() >> 4;
		int chunkZ = concentratorPos.getZ() >> 4;
		LevelChunk chunk = loadedChunk(moon, chunkX, chunkZ);
		if (chunk == null) {
			return;
		}

		for (BlockPos existing : data.innerBeamsAt(concentratorPos.getX(), concentratorPos.getZ())) {
			if (moon.getBlockState(existing).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
				return;
			}
			data.forgetInnerBeam(existing);
		}

		BlockPos placeAt = findBeamPos(moon, chunk, concentratorPos);
		moon.setBlock(placeAt, ModBlocks.CONCENTRATED_BEACON_BEAM.defaultBlockState(), Block.UPDATE_CLIENTS);
		data.rememberInnerBeam(placeAt);
	}

	private static void removeMoonBeams(MinecraftServer server, BeaconConcentratorSavedData data, int x, int z) {
		ServerLevel moon = server.getLevel(ModDimensions.MOON_WORLD_KEY);
		LevelChunk chunk = moon == null ? null : loadedChunk(moon, x >> 4, z >> 4);
		if (moon == null || chunk == null) {
			return;
		}
		for (BlockPos beamPos : data.innerBeamsAt(x, z)) {
			removeBeamBlock(moon, data, beamPos);
		}
		int minY = moon.getMinY();
		int maxY = moon.getMaxY();
		for (int y = minY; y <= maxY; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (moon.getBlockState(pos).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
				removeBeamBlock(moon, data, pos);
			}
		}
	}

	private static void removeBeamBlock(ServerLevel world, BeaconConcentratorSavedData data, BlockPos pos) {
		if (world.getBlockState(pos).is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
			world.removeBlock(pos, false);
		}
		data.forgetInnerBeam(pos);
	}

	private static BlockPos findBeamPos(ServerLevel level, LevelChunk chunk, BlockPos hint) {
		int x = hint.getX();
		int z = hint.getZ();
		int y = heightOn(chunk, level, x, z);
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

	private static int heightOn(@Nullable LevelChunk chunk, ServerLevel level, int x, int z) {
		if (chunk != null) {
			return chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		}
		return level.getMinY() + 1;
	}

	@Nullable
	private static LevelChunk loadedChunk(ServerLevel level, int chunkX, int chunkZ) {
		return level.getChunkSource().getChunkNow(chunkX, chunkZ);
	}
}
