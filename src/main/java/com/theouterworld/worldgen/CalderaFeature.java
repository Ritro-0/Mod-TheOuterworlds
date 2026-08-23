package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CalderaFeature extends Feature<NoneFeatureConfiguration> {
	public CalderaFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;

		int centerX = origin.getX();
		int centerZ = origin.getZ();
		if (centerX < minX || centerX > maxX || centerZ < minZ || centerZ > maxZ) {
			return false;
		}
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
		if (surfaceY < 120) {
			return false;
		}

		int radius = 10 + random.nextInt(7);
		int depth = 3 + random.nextInt(3);
		BlockState basalt = ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minY = world.getMinY() + 1;

		int x0 = Math.max(minX, centerX - radius);
		int x1 = Math.min(maxX, centerX + radius);
		int z0 = Math.max(minZ, centerZ - radius);
		int z1 = Math.min(maxZ, centerZ + radius);

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double dist = Math.hypot(x - centerX, z - centerZ);
				if (dist > radius) {
					continue;
				}
				double t = dist / radius;
				int floorY = Math.max(minY, surfaceY - (int) Math.round(depth * (1.0 - t * t)));
				for (int y = surfaceY; y > floorY; y--) {
					cursor.set(x, y, z);
					if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
						world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
					}
				}
				cursor.set(x, floorY, z);
				if (!world.getBlockState(cursor).is(Blocks.BEDROCK)) {
					world.setBlock(cursor, basalt, 2);
				}
			}
		}
		return true;
	}
}
