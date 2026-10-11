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
 * retries elsewhere in the chunk so every chunk gets its share. A valid spot near other pebbles
 * is thinned out (without retrying), so they scatter instead of forming huge patches.
 *
 * <p>Skipped: Cryocaps, the gypsum cave biomes, and gypsum floors.
 */
public class OxidizedBasaltPebbleFeature implements Feature {
	public static final MapCodec<OxidizedBasaltPebbleFeature> CODEC = MapCodec.unit(OxidizedBasaltPebbleFeature::new);

	private static final int ATTEMPTS = 4;
	private static final int MAX_CLIMB = 96;
	private static final int ROOF_CHECK = 12;
	/** How far (horizontally) another pebble counts as nearby, and how far up or down. */
	private static final int CROWD_RADIUS = 3;
	private static final int CROWD_HEIGHT = 2;
	/** Chance of keeping a pebble is this value raised to the number of pebbles nearby. */
	private static final float CROWD_KEEP_CHANCE = 0.65F;
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
			BlockPos cell = openSurface(world, x, z);
			if (cell == null) {
				continue;
			}
			// A good spot that is crowded by other pebbles is simply skipped; retrying would
			// just move the same pebble next to a different cluster.
			if (isCrowded(world, cell, random)) {
				return false;
			}
			world.setBlock(cell, ModBlocks.OXIDIZED_BASALT_PEBBLE.defaultBlockState(), Block.UPDATE_CLIENTS);
			return true;
		}
		return false;
	}

	/** Rolls against the number of pebbles near {@code cell}; each one makes another less likely. */
	private static boolean isCrowded(WorldGenLevel world, BlockPos cell, RandomSource random) {
		int nearby = 0;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -CROWD_RADIUS; dx <= CROWD_RADIUS; dx++) {
			for (int dz = -CROWD_RADIUS; dz <= CROWD_RADIUS; dz++) {
				for (int dy = -CROWD_HEIGHT; dy <= CROWD_HEIGHT; dy++) {
					cursor.set(cell.getX() + dx, cell.getY() + dy, cell.getZ() + dz);
					if (world.getBlockState(cursor).is(ModBlocks.OXIDIZED_BASALT_PEBBLE)) {
						nearby++;
					}
				}
			}
		}
		if (nearby == 0) {
			return false;
		}
		return random.nextFloat() >= (float) Math.pow(CROWD_KEEP_CHANCE, nearby);
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
