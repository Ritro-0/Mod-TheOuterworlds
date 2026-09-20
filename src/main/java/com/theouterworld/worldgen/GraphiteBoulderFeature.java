package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Irregular graphite mounds that breach the Innerworld surface like weathered
 * ore boulders — not flat floor stains.
 */
public class GraphiteBoulderFeature implements Feature {
	public static final MapCodec<GraphiteBoulderFeature> CODEC = MapCodec.unit(GraphiteBoulderFeature::new);

	public GraphiteBoulderFeature() {
	}

	@Override
	public MapCodec<GraphiteBoulderFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockState graphite = ModBlocks.GRAPHITE.defaultBlockState();
		BlockState fill = ModBlocks.KOMATIITE.defaultBlockState();

		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
		BlockPos ground = new BlockPos(origin.getX(), surfaceY - 1, origin.getZ());
		BlockState groundState = world.getBlockState(ground);
		if (!groundState.is(ModBlocks.MAGNESIAN_REGOLITH)
			&& !groundState.is(ModBlocks.KOMATIITE)
			&& !groundState.is(ModBlocks.ENSTATITE)
			&& !groundState.is(ModBlocks.GRAPHITE)) {
			return false;
		}

		double radiusX = 1.6 + random.nextDouble() * 2.4;
		double radiusZ = 1.5 + random.nextDouble() * 2.2;
		double radiusY = 1.2 + random.nextDouble() * 1.8;
		double bury = 0.45 + random.nextDouble() * 0.4;
		int centerY = surfaceY - (int) Math.round(radiusY * bury);
		int minY = world.getMinY() + 1;

		int x0 = (int) Math.floor(origin.getX() - radiusX - 1);
		int x1 = (int) Math.ceil(origin.getX() + radiusX + 1);
		int z0 = (int) Math.floor(origin.getZ() - radiusZ - 1);
		int z1 = (int) Math.ceil(origin.getZ() + radiusZ + 1);
		int y0 = Math.max(minY, (int) Math.floor(centerY - radiusY - 1));
		int y1 = (int) Math.ceil(centerY + radiusY + 1);

		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				for (int y = y0; y <= y1; y++) {
					double nx = (x - origin.getX()) / radiusX;
					double ny = (y - centerY) / radiusY;
					double nz = (z - origin.getZ()) / radiusZ;
					double dist = nx * nx + ny * ny + nz * nz;
					// Soft irregular rim so it doesn't read as a perfect sphere.
					double wobble = 0.12 * Math.sin(x * 0.7 + z * 0.5) + 0.08 * Math.cos(y * 0.9 + x * 0.3);
					if (dist > 1.0 + wobble) {
						continue;
					}
					cursor.set(x, y, z);
					BlockState current = world.getBlockState(cursor);
					if (current.is(Blocks.BEDROCK)) {
						continue;
					}
					if (current.isAir() || current.canBeReplaced()
						|| current.is(ModBlocks.MAGNESIAN_REGOLITH)
						|| current.is(ModBlocks.KOMATIITE)
						|| current.is(ModBlocks.ENSTATITE)) {
						// Keep a bit of host rock on the underside so it feels rooted.
						if (ny < -0.55 && dist > 0.72 && random.nextFloat() < 0.45F) {
							world.setBlock(cursor, fill, 2);
						} else {
							world.setBlock(cursor, graphite, 2);
						}
						placed = true;
					}
				}
			}
		}
		return placed;
	}
}
