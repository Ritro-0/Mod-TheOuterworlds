package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Single-block Raw Osmium patches directly beneath floor lava pools in Nearworld sulfur caves.
 * Only targets the underside of an open lava surface (air above), never wall veins.
 */
public class NearworldOsmiumUnderLavaFeature implements Feature {
	public static final MapCodec<NearworldOsmiumUnderLavaFeature> CODEC = MapCodec.unit(NearworldOsmiumUnderLavaFeature::new);

	public NearworldOsmiumUnderLavaFeature() {
	}

	@Override
	public MapCodec<NearworldOsmiumUnderLavaFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean placed = false;
		int minY = world.getMinY() + 1;
		int maxY = Math.min(world.getMaxY(), 64);

		int columns = 8 + random.nextInt(8);
		for (int i = 0; i < columns; i++) {
			int x = origin.getX() + random.nextInt(16);
			int z = origin.getZ() + random.nextInt(16);
			int startY = Math.max(minY, origin.getY() - 12);
			int endY = Math.min(maxY, origin.getY() + 12);
			for (int y = endY; y >= startY; y--) {
				cursor.set(x, y, z);
				if (!isLava(world, cursor)) {
					continue;
				}
				if (!isOpenCavePoolSurface(world, cursor)) {
					continue;
				}
				BlockPos below = cursor.below();
				BlockState floor = world.getBlockState(below);
				if (!isOsmiumReplaceable(floor)) {
					continue;
				}
				world.setBlock(below, ModBlocks.RAW_OSMIUM.defaultBlockState(), 2);
				placed = true;
				break;
			}
			if (placed && random.nextFloat() < 0.7f) {
				break;
			}
		}
		return placed;
	}

	private static boolean isLava(WorldGenLevel world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.is(Blocks.LAVA) || state.getFluidState().is(Fluids.LAVA);
	}

	/** Floor pool: tall air above, and at least two open horizontal sides so wall blobs are skipped. */
	private static boolean isOpenCavePoolSurface(WorldGenLevel world, BlockPos lava) {
		for (int dy = 1; dy <= 3; dy++) {
			if (!world.getBlockState(lava.above(dy)).isAir()) {
				return false;
			}
		}
		int openSides = 0;
		for (Direction dir : Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new)) {
			BlockState neighbor = world.getBlockState(lava.relative(dir));
			if (neighbor.isAir() || isLava(world, lava.relative(dir))) {
				openSides++;
			}
		}
		return openSides >= 2;
	}

	private static boolean isOsmiumReplaceable(BlockState state) {
		return state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT)
			|| state.is(Blocks.MAGMA_BLOCK)
			|| state.is(Blocks.SULFUR)
			|| state.is(Blocks.CINNABAR)
			|| state.is(ModBlocks.SULFURIC_BASALT)
			|| state.is(BlockTags.BASE_STONE_OVERWORLD)
			|| state.is(BlockTags.BASE_STONE_NETHER);
	}
}
