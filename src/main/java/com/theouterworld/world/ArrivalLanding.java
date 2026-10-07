package com.theouterworld.world;

import com.theouterworld.block.AerogelCloudBlock;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.worldgen.PotatoworldsShape;
import com.theouterworld.worldgen.SpongeworldShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * Feet position for every arrival in a mod world.
 * Solid worlds use the sky heightmap. Gas giants land on their arrival cloud deck
 * near the source column. Spongeworld and Potatoworlds use that surface, or 0, 0
 * when the column has none.
 */
public final class ArrivalLanding {
	private static final int PIT_DROP = 12;

	private ArrivalLanding() {
	}

	/** Block the player stands in. */
	public static BlockPos playerFeet(ServerLevel world, int x, int z) {
		ResourceKey<Level> dimension = world.dimension();
		if (ModDimensions.isSun(dimension)) {
			return SunArrival.prepareLanding(world, x, z);
		}
		if (ModDimensions.isGasGiant(dimension)) {
			return cloudAnchor(world, x, z).above();
		}
		if (ModDimensions.isSpongeworld(dimension) || ModDimensions.isPotatoworlds(dimension)) {
			return porousFeet(world, x, z);
		}
		BlockPos feet = columnFeet(world, x, z);
		if (feet != null) {
			return feet;
		}
		return deck(world, x, z, Mth.clamp(world.getSeaLevel() + 2, world.getMinY() + 2, world.getMaxY() - 2));
	}

	/**
	 * Block a rift pad or rift replaces. On gas giants this is the cloud itself so the
	 * pad has something to sit in. Everywhere else it is the same air cell as {@link #playerFeet}.
	 */
	public static BlockPos structureCell(ServerLevel world, int x, int z) {
		if (ModDimensions.isGasGiant(world.dimension())) {
			return cloudAnchor(world, x, z);
		}
		return playerFeet(world, x, z);
	}

	public static BlockPos cloudAnchor(ServerLevel world, int x, int z) {
		CloudBand band = cloudBand(world.dimension());
		world.getChunk(x >> 4, z >> 4);
		BlockPos cloud = findCloud(world, x, z, band);
		if (cloud == null) {
			int y = band.bottom + Math.max(4, (band.top - band.bottom) / 2);
			cloud = new BlockPos(x, y, z);
		}
		ensureCloudPlatform(world, cloud.getX(), cloud.getZ(), cloud.getY(), band.block.defaultBlockState());
		return cloud;
	}

	public static boolean isStandOpen(ServerLevel world, BlockPos feet) {
		if (feet.getY() <= world.getMinY() || feet.getY() >= world.getMaxY() - 1) {
			return false;
		}
		BlockState body = world.getBlockState(feet);
		BlockState head = world.getBlockState(feet.above());
		if (!body.canBeReplaced() || !head.canBeReplaced()) {
			return false;
		}
		if (!body.getFluidState().isEmpty() || !head.getFluidState().isEmpty()) {
			return false;
		}
		BlockPos floor = feet.below();
		BlockState floorState = world.getBlockState(floor);
		return floorState.isFaceSturdy(world, floor, Direction.UP)
			|| floorState.isCollisionShapeFullBlock(world, floor);
	}

	private static BlockPos porousFeet(ServerLevel world, int x, int z) {
		BlockPos feet = columnFeet(world, x, z);
		if (feet != null) {
			return feet;
		}
		if (x != 0 || z != 0) {
			feet = columnFeet(world, 0, 0);
			if (feet != null) {
				return feet;
			}
		}
		int y = ModDimensions.isSpongeworld(world.dimension())
			? SpongeworldShape.CENTER_Y + (int) SpongeworldShape.RADIUS_Y
			: (int) PotatoworldsShape.PHOBOS.cy();
		return deck(world, 0, 0, Mth.clamp(y, world.getMinY() + 2, world.getMaxY() - 2));
	}

	@Nullable
	private static BlockPos columnFeet(ServerLevel world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		int solid = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		if (solid <= world.getMinY()) {
			return null;
		}
		int[] raised = higherNeighbor(world, x, z, solid);
		if (raised != null) {
			x = raised[0];
			z = raised[1];
			solid = raised[2];
		}
		BlockPos floor = new BlockPos(x, solid, z);
		BlockState floorState = world.getBlockState(floor);
		if (unsafeFluid(floorState)) {
			BlockPos dry = nearestDry(world, x, z);
			if (dry != null) {
				return dry;
			}
			return deck(world, x, z, Mth.clamp(Math.max(solid + 2, world.getSeaLevel() + 2), world.getMinY() + 2, world.getMaxY() - 2));
		}
		BlockPos feet = floor.above();
		if (feet.getY() >= world.getMaxY()) {
			return null;
		}
		openHeadroom(world, feet);
		return feet;
	}

	/** A skylight shaft reads as surface on its own column. Step to a nearby rim instead. */
	@Nullable
	private static int[] higherNeighbor(ServerLevel world, int x, int z, int solid) {
		int bestY = solid;
		int bestX = x;
		int bestZ = z;
		for (int dx = -8; dx <= 8; dx += 8) {
			for (int dz = -8; dz <= 8; dz += 8) {
				if (dx == 0 && dz == 0) {
					continue;
				}
				int nx = x + dx;
				int nz = z + dz;
				world.getChunk(nx >> 4, nz >> 4);
				int ny = world.getHeight(Heightmap.Types.WORLD_SURFACE, nx, nz);
				if (ny > bestY) {
					bestY = ny;
					bestX = nx;
					bestZ = nz;
				}
			}
		}
		if (bestY < solid + PIT_DROP) {
			return null;
		}
		return new int[] {bestX, bestZ, bestY};
	}

	@Nullable
	private static BlockPos nearestDry(ServerLevel world, int x, int z) {
		for (int r = 1; r <= 16; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					int nx = x + dx;
					int nz = z + dz;
					world.getChunk(nx >> 4, nz >> 4);
					int solid = world.getHeight(Heightmap.Types.WORLD_SURFACE, nx, nz);
					if (solid <= world.getMinY()) {
						continue;
					}
					BlockPos floor = new BlockPos(nx, solid, nz);
					BlockState floorState = world.getBlockState(floor);
					if (unsafeFluid(floorState) || floorState.canBeReplaced()) {
						continue;
					}
					BlockPos feet = floor.above();
					if (feet.getY() >= world.getMaxY()) {
						continue;
					}
					if (unsafeFluid(world.getBlockState(feet))) {
						continue;
					}
					openHeadroom(world, feet);
					return feet;
				}
			}
		}
		return null;
	}

	private static boolean unsafeFluid(BlockState state) {
		return !state.getFluidState().isEmpty() && !state.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER);
	}

	private static void openHeadroom(ServerLevel world, BlockPos feet) {
		if (!world.getBlockState(feet).canBeReplaced()) {
			world.setBlockAndUpdate(feet, Blocks.AIR.defaultBlockState());
		}
		BlockPos head = feet.above();
		if (head.getY() < world.getMaxY() && !world.getBlockState(head).canBeReplaced()) {
			world.setBlockAndUpdate(head, Blocks.AIR.defaultBlockState());
		}
	}

	private static BlockPos deck(ServerLevel world, int x, int z, int feetY) {
		world.getChunk(x >> 4, z >> 4);
		BlockPos feet = new BlockPos(x, feetY, z);
		BlockPos floor = feet.below();
		BlockState floorState = world.getBlockState(floor);
		if (floorState.canBeReplaced() || !floorState.isCollisionShapeFullBlock(world, floor)) {
			world.setBlockAndUpdate(floor, foundation(world));
		}
		openHeadroom(world, feet);
		return feet;
	}

	private static BlockState foundation(ServerLevel world) {
		ResourceKey<Level> dimension = world.dimension();
		if (ModDimensions.isEmberworld(dimension) || ModDimensions.isNearworld(dimension)) {
			return ModBlocks.SULFURIC_BASALT.defaultBlockState();
		}
		if (ModDimensions.isAmberworld(dimension) || ModDimensions.isBeyondlands(dimension) || ModDimensions.isBeyondlandsIi(dimension)) {
			return ModBlocks.THOLIN.defaultBlockState();
		}
		if (ModDimensions.isScarletlands(dimension)) {
			return ModBlocks.METHANE_ICE.defaultBlockState();
		}
		if (ModDimensions.isLonelands(dimension)) {
			return ModBlocks.DRY_ICE.defaultBlockState();
		}
		if (ModDimensions.isMoon(dimension)) {
			return ModBlocks.NORITE.defaultBlockState();
		}
		if (ModDimensions.isOuterworld(dimension) || ModDimensions.isPotatoworlds(dimension)) {
			return ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		}
		if (ModDimensions.isSpongeworld(dimension) || ModDimensions.isFrostworld(dimension)) {
			return Blocks.PACKED_ICE.defaultBlockState();
		}
		if (ModDimensions.isInnerworld(dimension)) {
			return ModBlocks.KOMATIITE.defaultBlockState();
		}
		return Blocks.STONE.defaultBlockState();
	}

	@Nullable
	private static BlockPos findCloud(ServerLevel world, int x, int z, CloudBand band) {
		BlockPos exact = cloudInColumn(world, x, z, band);
		if (exact != null) {
			return exact;
		}
		for (int r = 1; r <= 48; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					int cx = x + dx;
					int cz = z + dz;
					world.getChunk(cx >> 4, cz >> 4);
					BlockPos found = cloudInColumn(world, cx, cz, band);
					if (found != null) {
						return found;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static BlockPos cloudInColumn(ServerLevel world, int x, int z, CloudBand band) {
		BlockPos top = null;
		for (int y = band.bottom; y <= band.top; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (world.getBlockState(pos).is(band.block)) {
				top = pos;
			}
		}
		return top;
	}

	public static void ensureCloudPlatform(ServerLevel world, int x, int z, int y, BlockState cloud) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 8) {
					continue;
				}
				cursor.set(x + dx, y, z + dz);
				world.getChunk(cursor);
				BlockState state = world.getBlockState(cursor);
				if (state.isAir() || state.getBlock() instanceof AerogelCloudBlock) {
					world.setBlock(cursor, cloud, Block.UPDATE_ALL);
				}
				cursor.set(x + dx, y - 1, z + dz);
				if (world.getBlockState(cursor).isAir()) {
					world.setBlock(cursor, cloud, Block.UPDATE_ALL);
				}
			}
		}
	}

	private static CloudBand cloudBand(ResourceKey<Level> dimension) {
		if (ModDimensions.isHighworld(dimension)) {
			return new CloudBand(ModBlocks.AMMONIA_CLOUD, HighworldLayers.AMMONIA_BOTTOM_Y, HighworldLayers.AMMONIA_TOP_Y);
		}
		if (ModDimensions.isDeepworld(dimension)) {
			return new CloudBand(ModBlocks.METHANE_CLOUD, DeepworldLayers.METHANE_BOTTOM_Y, DeepworldLayers.METHANE_TOP_Y);
		}
		if (ModDimensions.isFarworld(dimension)) {
			return new CloudBand(ModBlocks.METHANE_CLOUD, FarworldLayers.METHANE_UPPER_BOTTOM_Y, FarworldLayers.METHANE_UPPER_TOP_Y);
		}
		return new CloudBand(ModBlocks.METHANE_CLOUD, EdgeworldLayers.METHANE_UPPER_BOTTOM_Y, EdgeworldLayers.METHANE_UPPER_TOP_Y);
	}

	private record CloudBand(Block block, int bottom, int top) {
	}
}
