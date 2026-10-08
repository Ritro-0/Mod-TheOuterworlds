package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Sits on the finished Outerworld surface. Dunes, craters and calderas reshape the ground after
 * the heightmap was primed, so the column is walked up out of buried ground before placing.
 * It never walks down, and needs open air above, so cave floors are rejected. A failed spot
 * retries elsewhere in the chunk so every chunk gets its share.
 *
 * <p>Skipped: Cryocaps, the gypsum cave biomes, and gypsum floors.
 */
public class OxidizedBasaltPebbleFeature implements Feature {
	public static final MapCodec<OxidizedBasaltPebbleFeature> CODEC = MapCodec.unit(OxidizedBasaltPebbleFeature::new);

	private static final int ATTEMPTS = 4;
	private static final int MAX_CLIMB = 96;
	private static final int ROOF_CHECK = 12;
	private static final ResourceKey<Biome> CRYOCAPS = ResourceKey.create(
		Registries.BIOME,
		OuterWorldMod.id("outerworld_cryocaps")
	);
	private static final ResourceKey<Biome> SULFUR_CAVES = ResourceKey.create(
		Registries.BIOME,
		OuterWorldMod.id("sulfur_caves")
	);
	private static final ResourceKey<Biome> MINERAL_CAVERNS = ResourceKey.create(
		Registries.BIOME,
		OuterWorldMod.id("mineral_caverns")
	);

	public OxidizedBasaltPebbleFeature() {
	}

	@Override
	public MapCodec<OxidizedBasaltPebbleFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		int chunkX = origin.getX() & ~15;
		int chunkZ = origin.getZ() & ~15;
		for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
			int x = attempt == 0 ? origin.getX() : chunkX + random.nextInt(16);
			int z = attempt == 0 ? origin.getZ() : chunkZ + random.nextInt(16);
			if (tryPlace(world, x, z)) {
				return true;
			}
		}
		return false;
	}

	private static boolean tryPlace(WorldGenLevel world, int x, int z) {
		BlockPos cell = openSurface(world, x, z);
		if (cell == null) {
			return false;
		}
		world.setBlock(cell, ModBlocks.OXIDIZED_BASALT_PEBBLE.defaultBlockState(), Block.UPDATE_CLIENTS);
		return true;
	}

	/**
	 * The open air cell sitting on the true surface of this column, or null when the column is a
	 * cave floor, fluid, gypsum, or in an excluded biome. The block below it is the surface block.
	 */
	static BlockPos openSurface(WorldGenLevel world, int x, int z) {
		int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int top = Math.min(world.getMaxY() - 1, y + MAX_CLIMB);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
		// Climb out of ground a dune buried. Never walk down: that is what dropped pebbles onto cave floors.
		while (cursor.getY() < top && !isOpen(world.getBlockState(cursor))) {
			cursor.move(Direction.UP);
		}
		if (!isOpen(world.getBlockState(cursor))) {
			return null;
		}
		BlockPos floor = cursor.below();
		BlockState floorState = world.getBlockState(floor);
		if (isOpen(floorState) || !floorState.getFluidState().isEmpty()) {
			return null;
		}
		if (floorState.is(ModBlocks.GYPSUM_BLOCK) || !floorState.isFaceSturdy(world, floor, Direction.UP)) {
			return null;
		}
		if (world.getBiome(cursor).is(CRYOCAPS)
			|| world.getBiome(cursor).is(SULFUR_CAVES)
			|| world.getBiome(cursor).is(MINERAL_CAVERNS)) {
			return null;
		}
		// A cave floor has a roof close overhead; the real surface has open air above it.
		BlockPos.MutableBlockPos above = cursor.mutable();
		for (int i = 0; i < ROOF_CHECK; i++) {
			above.move(Direction.UP);
			if (above.getY() >= world.getMaxY()) {
				break;
			}
			if (!isOpen(world.getBlockState(above))) {
				return null;
			}
		}
		return cursor.immutable();
	}

	private static boolean isOpen(BlockState state) {
		return state.canBeReplaced() && state.getFluidState().isEmpty();
	}
}
