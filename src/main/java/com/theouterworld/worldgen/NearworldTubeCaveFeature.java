package com.theouterworld.worldgen;

import com.theouterworld.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Nearworld tube caves: tunnels linking large caves to each other or to the surface.
 */
public class NearworldTubeCaveFeature extends Feature<NoneFeatureConfiguration> {
	public NearworldTubeCaveFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
		if (surfaceY < world.getMinY() + 24) {
			return false;
		}

		// Keep tubes buried so they open into larger caves; surface mouths are very rare.
		boolean carved = carveTube(world, random, origin, surfaceY, minX, minZ, maxX, maxZ, false);
		boolean shaft = false;
		if (random.nextFloat() < 0.025F) {
			shaft = punchSurfaceShaft(world, random, origin, surfaceY, minX, minZ, maxX, maxZ);
		}
		return carved || shaft;
	}

	static boolean carveTube(
		WorldGenLevel world,
		RandomSource random,
		BlockPos origin,
		int surfaceY,
		int minX,
		int minZ,
		int maxX,
		int maxZ,
		boolean mayBreachSurface
	) {
		int length = 36 + random.nextInt(72);
		int radius = 2 + random.nextInt(3);
		double x = origin.getX() + 0.5;
		double y = Math.min(surfaceY - (12 + random.nextInt(40)), surfaceY - 10);
		double z = origin.getZ() + 0.5;
		double yaw = random.nextDouble() * Math.PI * 2.0;
		double pitch = -0.08 + random.nextDouble() * 0.12;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean carved = false;

		for (int i = 0; i < length; i++) {
			int ix = (int) Math.floor(x);
			int iy = (int) Math.floor(y);
			int iz = (int) Math.floor(z);
			if (ix >= minX && ix <= maxX && iz >= minZ && iz <= maxZ) {
				carved |= carveSphere(world, cursor, ix, iy, iz, radius, minX, minZ, maxX, maxZ);
			}
			yaw += (random.nextDouble() - 0.5) * 0.38;
			pitch += (random.nextDouble() - 0.5) * 0.07;
			pitch = Math.max(-0.35, Math.min(0.2, pitch));
			x += Math.cos(yaw);
			z += Math.sin(yaw);
			y += pitch;
			if (!mayBreachSurface && y > surfaceY - 10.0) {
				y = surfaceY - 10.0;
				pitch = Math.min(pitch, -0.05);
			}
			if (y < world.getMinY() + 8) {
				break;
			}
			if (random.nextFloat() < 0.05F) {
				radius = Math.max(2, Math.min(5, radius + random.nextInt(3) - 1));
			}
		}
		return carved;
	}

	static boolean punchSurfaceShaft(
		WorldGenLevel world,
		RandomSource random,
		BlockPos origin,
		int surfaceY,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int x = Math.min(maxX, Math.max(minX, origin.getX() + random.nextInt(11) - 5));
		int z = Math.min(maxZ, Math.max(minZ, origin.getZ() + random.nextInt(11) - 5));
		int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		int bottom = Math.max(world.getMinY() + 8, top - (20 + random.nextInt(48)));
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean foundCave = false;
		for (int y = top - 3; y >= bottom; y--) {
			cursor.set(x, y, z);
			if (world.getBlockState(cursor).isAir()) {
				foundCave = true;
				bottom = y;
				break;
			}
		}
		if (!foundCave) {
			return false;
		}
		boolean carved = false;
		for (int y = bottom; y <= top; y++) {
			carved |= carveSphere(world, cursor, x, y, z, 2, minX, minZ, maxX, maxZ);
		}
		return carved;
	}

	static boolean carveSphere(
		WorldGenLevel world,
		BlockPos.MutableBlockPos cursor,
		int cx,
		int cy,
		int cz,
		int radius,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		boolean carved = false;
		int floorLimit = world.getMinY() + 5;
		int r2 = radius * radius;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dy * dy + dz * dz > r2) {
						continue;
					}
					int x = cx + dx;
					int z = cz + dz;
					if (x < minX || x > maxX || z < minZ || z > maxZ) {
						continue;
					}
					int y = cy + dy;
					if (y <= floorLimit) {
						continue;
					}
					cursor.set(x, y, z);
					BlockState state = world.getBlockState(cursor);
					if (state.is(Blocks.BEDROCK) || state.isAir()) {
						continue;
					}
					if (isReplaceable(state)) {
						world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
						carved = true;
					}
				}
			}
		}
		return carved;
	}

	static boolean isReplaceable(BlockState state) {
		return state.is(ModTags.LAVA_TUBE_REPLACEABLE)
			|| state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT)
			|| state.is(Blocks.OBSIDIAN);
	}
}
