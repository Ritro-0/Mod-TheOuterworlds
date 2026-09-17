package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places Phobos and Deimos as solid potato bodies in Potatoworlds empty space.
 * Phobos: bedrock core, thin anorthosite, 1-block regolith (bald patches + craters bare).
 * Deimos: same stack with tholin veneer and no impact craters.
 */
public class PotatoworldsBodyFeature extends Feature<NoneFeatureConfiguration> {
	private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
	private static final BlockState ANORTHOSITE = ModBlocks.ANORTHOSITE.defaultBlockState();
	private static final BlockState REGOLITH = ModBlocks.REGOLITH.defaultBlockState();
	private static final BlockState THOLIN = ModBlocks.THOLIN.defaultBlockState();

	public PotatoworldsBodyFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		if (!PotatoworldsShape.chunkMayIntersect(minX, minZ)) {
			return false;
		}

		int yStart = Math.max(world.getMinY(), PotatoworldsShape.yStart());
		int yEnd = Math.min(world.getMaxY(), PotatoworldsShape.yEnd());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean placed = false;

		for (int x = minX; x < minX + 16; x++) {
			for (int z = minZ; z < minZ + 16; z++) {
				if (!PotatoworldsShape.columnMayIntersect(x, z)) {
					continue;
				}
				for (int y = yStart; y <= yEnd; y++) {
					PotatoworldsShape.Eval sample = PotatoworldsShape.evaluate(x, y, z);
					if (sample.density() <= 0.0 || sample.body() == null) {
						continue;
					}
					cursor.set(x, y, z);
					world.setBlock(cursor, pickBlock(sample, x, y, z), 2);
					placed = true;
				}
			}
		}
		return placed;
	}

	private static BlockState pickBlock(PotatoworldsShape.Eval sample, int x, int y, int z) {
		if (PotatoworldsShape.hasSurfaceCover(sample, x, y, z)) {
			return sample.body().isPhobos() ? REGOLITH : THOLIN;
		}
		if (PotatoworldsShape.isBedrockCore(sample)) {
			return BEDROCK;
		}
		return ANORTHOSITE;
	}
}
