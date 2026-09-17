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
 * Fills each intersecting chunk with Hyperion's ovular icy sponge body.
 * Materials follow geology: ice bulk, carbonic deposits, tholin crater floors.
 */
public class SpongeworldBodyFeature extends Feature<NoneFeatureConfiguration> {
	private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
	private static final BlockState CARBONIC_ICE = ModBlocks.CARBONIC_ICE.defaultBlockState();
	private static final BlockState THOLIN = ModBlocks.THOLIN.defaultBlockState();

	public SpongeworldBodyFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		if (!SpongeworldShape.chunkMayIntersect(minX, minZ)) {
			return false;
		}

		int yStart = Math.max(world.getMinY(), SpongeworldShape.yStart());
		int yEnd = Math.min(world.getMaxY(), SpongeworldShape.yEnd());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean placed = false;

		for (int x = minX; x < minX + 16; x++) {
			for (int z = minZ; z < minZ + 16; z++) {
				if (!SpongeworldShape.columnMayIntersect(x, z)) {
					continue;
				}
				for (int y = yStart; y <= yEnd; y++) {
					SpongeworldShape.Eval sample = SpongeworldShape.evaluate(x, y, z);
					if (sample.density() <= 0.0) {
						continue;
					}
					cursor.set(x, y, z);
					world.setBlock(cursor, pickBlock(x, y, z), 2);
					placed = true;
				}
			}
		}
		return placed;
	}

	private static BlockState pickBlock(int x, int y, int z) {
		if (SpongeworldShape.isOuterTholinPatch(x, y, z)) {
			return THOLIN;
		}
		if (SpongeworldShape.isCarbonicDeposit(x, y, z)) {
			return CARBONIC_ICE;
		}
		return PACKED_ICE;
	}
}
