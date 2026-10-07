package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Sits on the finished Outerworld surface. Dunes are placed without updating the
 * heightmap, so the column is walked upward from that buried height until air.
 * Attempts average 12.225 per chunk (a count of 15 reduced by 18.5%).
 */
public class OxidizedBasaltPebbleFeature implements Feature {
	public static final MapCodec<OxidizedBasaltPebbleFeature> CODEC = MapCodec.unit(OxidizedBasaltPebbleFeature::new);

	public OxidizedBasaltPebbleFeature() {
	}

	@Override
	public MapCodec<OxidizedBasaltPebbleFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int x = origin.getX();
		int z = origin.getZ();
		int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int limit = Math.min(world.getMaxY() - 1, y + 96);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
		while (y < limit && !world.getBlockState(cursor).canBeReplaced()) {
			cursor.setY(++y);
		}
		if (y >= limit || !world.getBlockState(cursor).canBeReplaced()) {
			return false;
		}
		BlockPos floor = cursor.below();
		BlockState floorState = world.getBlockState(floor);
		if (floorState.canBeReplaced()) {
			return false;
		}
		if (!floorState.isFaceSturdy(world, floor, Direction.UP) && !floorState.isCollisionShapeFullBlock(world, floor)) {
			return false;
		}
		world.setBlock(cursor, ModBlocks.OXIDIZED_BASALT_PEBBLE.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}
}
