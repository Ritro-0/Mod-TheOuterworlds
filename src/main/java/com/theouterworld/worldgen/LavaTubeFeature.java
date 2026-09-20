package com.theouterworld.worldgen;

import com.theouterworld.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Ancient lava tubes: long, sparse tunnels with obsidian flow-banding.
 * Rarely breach the surface; usually stay buried or meet deeper caves.
 */
public class LavaTubeFeature implements Feature {
	public static final MapCodec<LavaTubeFeature> CODEC = MapCodec.unit(LavaTubeFeature::new);

	public LavaTubeFeature() {
	}

	@Override
	public MapCodec<LavaTubeFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
		if (surfaceY < world.getMinY() + 24) {
			return false;
		}

		boolean carved = carveTube(world, random, origin, surfaceY, minX, minZ, maxX, maxZ);
		boolean decorated = decorateObsidianFlows(world, random, world.getSeed(), minX, minZ, maxX, maxZ, surfaceY);
		boolean shaft = false;
		// Surface mouths are intentional but rare — most tubes stay sealed underground.
		if (random.nextFloat() < 0.08F) {
			shaft = punchSurfaceShaft(world, random, origin, surfaceY, minX, minZ, maxX, maxZ);
		}
		return carved || decorated || shaft;
	}

	private static boolean carveTube(
		WorldGenLevel world,
		RandomSource random,
		BlockPos origin,
		int surfaceY,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		int length = 28 + random.nextInt(64);
		int radius = 1 + random.nextInt(3);
		double x = origin.getX() + 0.5;
		// Keep tubes well below the surface so accidental daylight is uncommon.
		double y = Math.min(surfaceY - (18 + random.nextInt(36)), surfaceY - 14);
		double z = origin.getZ() + 0.5;
		double yaw = random.nextDouble() * Math.PI * 2.0;
		double pitch = -0.12 + random.nextDouble() * 0.08;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean carved = false;

		for (int i = 0; i < length; i++) {
			int ix = (int) Math.floor(x);
			int iy = (int) Math.floor(y);
			int iz = (int) Math.floor(z);
			if (ix >= minX && ix <= maxX && iz >= minZ && iz <= maxZ) {
				carved |= carveSphere(world, cursor, ix, iy, iz, radius, minX, minZ, maxX, maxZ, random);
			}
			yaw += (random.nextDouble() - 0.5) * 0.42;
			pitch += (random.nextDouble() - 0.5) * 0.06;
			pitch = Math.max(-0.28, Math.min(0.06, pitch));
			x += Math.cos(yaw);
			z += Math.sin(yaw);
			y += pitch;
			if (y > surfaceY - 12.0) {
				y = surfaceY - 12.0;
				pitch = Math.min(pitch, -0.05);
			}
			if (y < world.getMinY() + 8) {
				break;
			}
			if (random.nextFloat() < 0.04F) {
				radius = Math.max(1, Math.min(4, radius + random.nextInt(3) - 1));
			}
		}
		return carved;
	}

	private static boolean punchSurfaceShaft(
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
		int bottom = Math.max(world.getMinY() + 8, top - (18 + random.nextInt(40)));
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean foundCave = false;
		for (int y = top - 4; y >= bottom; y--) {
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
		int radius = 1;
		boolean carved = false;
		for (int y = bottom; y <= top; y++) {
			carved |= carveSphere(world, cursor, x, y, z, radius, minX, minZ, maxX, maxZ, random);
		}
		return carved;
	}

	private static boolean decorateObsidianFlows(
		WorldGenLevel world,
		RandomSource random,
		long seed,
		int minX,
		int minZ,
		int maxX,
		int maxZ,
		int surfaceY
	) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos support = new BlockPos.MutableBlockPos();
		boolean placed = false;
		int minY = world.getMinY() + 5;
		int maxY = Math.max(minY + 1, surfaceY);
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y < maxY; y++) {
					cursor.set(x, y, z);
					if (!world.getBlockState(cursor).isAir()) {
						continue;
					}
					boolean confined = false;
					for (int dy = 1; dy <= 6; dy++) {
						if (!world.getBlockState(cursor.above(dy)).isAir()) {
							confined = true;
							break;
						}
					}
					if (!confined) {
						continue;
					}
					support.set(x, y - 1, z);
					BlockState floor = world.getBlockState(support);
					if (!isTubeReplaceable(floor)) {
						continue;
					}
					double flow = WorldgenNoise.valueNoise(seed + 7741L, x * 0.07, z * 0.07 + y * 0.03);
					double ribbon = WorldgenNoise.valueNoise(seed + 9103L, x * 0.18 + y * 0.11, z * 0.18);
					boolean lavaSheet = flow > 0.58 && flow < 0.86;
					boolean braid = ribbon > 0.72;
					if (lavaSheet || braid) {
						world.setBlock(support, Blocks.OBSIDIAN.defaultBlockState(), 2);
						placed = true;
						if (random.nextFloat() < 0.18F) {
							placeWallFlow(world, cursor, random, minX, minZ, maxX, maxZ);
						}
					}
				}
			}
		}
		return placed;
	}

	private static void placeWallFlow(
		WorldGenLevel world,
		BlockPos.MutableBlockPos air,
		RandomSource random,
		int minX,
		int minZ,
		int maxX,
		int maxZ
	) {
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockPos wall = air.relative(direction);
		if (wall.getX() < minX || wall.getX() > maxX || wall.getZ() < minZ || wall.getZ() > maxZ) {
			return;
		}
		if (isTubeReplaceable(world.getBlockState(wall))) {
			world.setBlock(wall, Blocks.OBSIDIAN.defaultBlockState(), 2);
		}
	}

	private static boolean carveSphere(
		WorldGenLevel world,
		BlockPos.MutableBlockPos cursor,
		int cx,
		int cy,
		int cz,
		int radius,
		int minX,
		int minZ,
		int maxX,
		int maxZ,
		RandomSource random
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
					if (isTubeReplaceable(state)) {
						world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
						carved = true;
						if (dy == -radius && random.nextFloat() < 0.22F) {
							cursor.set(x, y - 1, z);
							BlockState below = world.getBlockState(cursor);
							if (!below.is(Blocks.BEDROCK) && isTubeReplaceable(below)) {
								world.setBlock(cursor, Blocks.OBSIDIAN.defaultBlockState(), 2);
							}
						}
					}
				}
			}
		}
		return carved;
	}

	private static boolean isTubeReplaceable(BlockState state) {
		return state.is(ModTags.LAVA_TUBE_REPLACEABLE) || state.is(Blocks.OBSIDIAN);
	}
}
