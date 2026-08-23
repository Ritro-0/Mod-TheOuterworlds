package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BasaltBoulderClusterFeature extends Feature<NoneFeatureConfiguration> {
	public BasaltBoulderClusterFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		BlockState basalt = ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		int count = 3 + random.nextInt(4);
		boolean placed = false;

		for (int i = 0; i < count; i++) {
			int x = Math.max(minX, Math.min(maxX, origin.getX() + random.nextInt(7) - 3));
			int z = Math.max(minZ, Math.min(maxZ, origin.getZ() + random.nextInt(7) - 3));
			int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
			BlockPos ground = new BlockPos(x, y - 1, z);
			if (!world.getBlockState(ground).is(ModBlocks.REGOLITH)
				&& !world.getBlockState(ground).is(ModBlocks.OXIDIZED_BASALT)
				&& !world.getBlockState(ground).is(ModBlocks.ANORTHOSITE)) {
				continue;
			}
			int pile = 1 + random.nextInt(3);
			for (int dy = 0; dy < pile; dy++) {
				world.setBlock(new BlockPos(x, y + dy, z), basalt, 2);
			}
			if (random.nextBoolean()) {
				int ox = Math.max(minX, Math.min(maxX, x + random.nextInt(3) - 1));
				int oz = Math.max(minZ, Math.min(maxZ, z + random.nextInt(3) - 1));
				world.setBlock(new BlockPos(ox, y, oz), basalt, 2);
			}
			placed = true;
		}
		return placed;
	}
}
