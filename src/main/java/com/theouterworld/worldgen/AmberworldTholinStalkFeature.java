package com.theouterworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.TholinStalkBlock;
import com.theouterworld.entity.ai.WeaverColonies;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Tholin stalks in small patches on Amberworld's methane shore. Every stalk
 * stands on a block that shares a side with methane, read from the real blocks.
 * Pond inlets are skipped. Patches beside a Weaver Anchor are more common and ripe.
 */
public class AmberworldTholinStalkFeature implements Feature {
	public static final MapCodec<AmberworldTholinStalkFeature> CODEC = MapCodec.unit(AmberworldTholinStalkFeature::new);

	private static final long SALT = 44017L;
	/** One shore patch per this many blocks. The lip is not a solid line. */
	private static final int PATCH_CELL = 40;
	private static final double ANCHOR_PATCH_CHANCE = 0.42;
	private static final int ORIGIN_SEARCH = 8;

	public AmberworldTholinStalkFeature() {
	}

	@Override
	public MapCodec<AmberworldTholinStalkFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		if (!ModDimensions.isAmberworld(world.getLevel().dimension())) {
			return false;
		}
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		RandomState noise = world.getLevel().getChunkSource().randomState();
		long seed = world.getSeed() + SALT;
		int cx = minX + 8;
		int cz = minZ + 8;
		boolean anchor = nearAnchor(world, cx, cz)
			&& AmberworldAnchorFeature.boostsStalks(chunkGenerator, noise, world, cx, cz);
		if (anchor) {
			if (WorldgenNoise.hash(seed + 1, minX, minZ) > ANCHOR_PATCH_CHANCE) {
				return false;
			}
			return placePatch(world, chunkGenerator, noise, seed, minX, minZ, cx, cz, true);
		}
		int cellX = Math.floorDiv(minX, PATCH_CELL);
		int cellZ = Math.floorDiv(minZ, PATCH_CELL);
		int centerX = cellX * PATCH_CELL + (int) Math.floor(WorldgenNoise.hash(seed + 3, cellX, cellZ) * PATCH_CELL);
		int centerZ = cellZ * PATCH_CELL + (int) Math.floor(WorldgenNoise.hash(seed + 5, cellX, cellZ) * PATCH_CELL);
		if (centerX < minX || centerX > minX + 15 || centerZ < minZ || centerZ > minZ + 15) {
			return false;
		}
		return placePatch(world, chunkGenerator, noise, seed, minX, minZ, centerX, centerZ, false);
	}

	private static boolean placePatch(
		WorldGenLevel world,
		ChunkGenerator chunkGenerator,
		RandomState noise,
		long seed,
		int minX,
		int minZ,
		int aroundX,
		int aroundZ,
		boolean anchor
	) {
		BlockPos start = null;
		for (int radius = 0; radius <= ORIGIN_SEARCH && start == null; radius++) {
			for (int dx = -radius; dx <= radius && start == null; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
						continue;
					}
					int x = aroundX + dx;
					int z = aroundZ + dz;
					if (x < minX || x > minX + 15 || z < minZ || z > minZ + 15) {
						continue;
					}
					BlockPos plant = shorePlant(world, x, z);
					if (plant == null
						|| AmberworldMethanePondFeature.nearPondShore(chunkGenerator, noise, world, world.getSeed(), x, z)) {
						continue;
					}
					start = plant;
					break;
				}
			}
		}
		if (start == null) {
			return false;
		}
		int plants = anchor
			? 4 + (int) Math.floor(WorldgenNoise.hash(seed + 41, start.getX(), start.getZ()) * 3.0)
			: 2 + (int) Math.floor(WorldgenNoise.hash(seed + 41, start.getX(), start.getZ()) * 2.0);
		boolean wantMature = anchor || WorldgenNoise.hash(seed + 43, start.getX(), start.getZ()) < 0.4;
		boolean placed = false;
		boolean matured = false;
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				int x = start.getX() + dx;
				int z = start.getZ() + dz;
				if (x < minX || x > minX + 15 || z < minZ || z > minZ + 15) {
					continue;
				}
				if (!(dx == 0 && dz == 0) && WorldgenNoise.hash(seed + 53, x, z) > 0.6) {
					continue;
				}
				BlockPos plant = shorePlant(world, x, z);
				if (plant == null) {
					continue;
				}
				boolean matureSlot = wantMature && !matured;
				if (plantOne(world, seed, plant, anchor, matureSlot)) {
					placed = true;
					matured |= matureSlot;
					if (--plants <= 0) {
						return true;
					}
				}
			}
		}
		return placed;
	}

	/**
	 * The air cell on top of this column, when the ground block under it shares a
	 * side with methane. A terrace one block above the fluid does not qualify.
	 */
	private static BlockPos shorePlant(WorldGenLevel world, int x, int z) {
		int top = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		BlockPos plant = new BlockPos(x, top, z);
		BlockPos ground = plant.below();
		if (!world.getBlockState(plant).isAir()) {
			return null;
		}
		if (!world.getFluidState(ground).isEmpty()
			|| !world.getBlockState(ground).isFaceSturdy(world, ground, Direction.UP)) {
			return null;
		}
		return TholinStalkBlock.hasMethane(world, ground) ? plant : null;
	}

	private static boolean plantOne(WorldGenLevel world, long seed, BlockPos plant, boolean anchor, boolean matureSlot) {
		int x = plant.getX();
		int z = plant.getZ();
		double heightRoll = WorldgenNoise.hash(seed + 3, x, z);
		int height;
		boolean ripe;
		if (matureSlot) {
			height = 3;
			ripe = true;
		} else if (anchor) {
			height = heightRoll < 0.2 ? 1 : heightRoll < 0.5 ? 2 : 3;
			ripe = height == 3 && WorldgenNoise.hash(seed + 9, x, z) < 0.75;
		} else {
			height = heightRoll < 0.5 ? 1 : heightRoll < 0.82 ? 2 : 3;
			ripe = height == 3 && WorldgenNoise.hash(seed + 9, x, z) < 0.35;
		}
		BlockState stalk = ModBlocks.THOLIN_STALK.defaultBlockState().setValue(TholinStalkBlock.WILD, true);
		boolean any = false;
		for (int i = 0; i < height; i++) {
			BlockPos at = plant.above(i);
			if (!world.getBlockState(at).isAir()) {
				break;
			}
			boolean tip = ripe && i == height - 1;
			world.setBlock(at, tip ? stalk.setValue(TholinStalkBlock.MATURE, true) : stalk, 2);
			any = true;
		}
		return any;
	}

	/** Horizontal ring around an Anchor centre. Skips the height checks until a chunk is actually close. */
	private static boolean nearAnchor(WorldGenLevel world, int x, int z) {
		long salt = world.getSeed() + WeaverColonies.SEED_SALT;
		int cell = WeaverColonies.CELL;
		int jitter = WeaverColonies.JITTER;
		int cellX = Math.floorDiv(x, cell);
		int cellZ = Math.floorDiv(z, cell);
		for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
			for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
				int centerX = cx * cell + cell / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(salt + 3, cx, cz)) * jitter);
				int centerZ = cz * cell + cell / 2
					+ (int) (WorldgenNoise.signed(WorldgenNoise.hash(salt + 5, cx, cz)) * jitter);
				long dx = (long) x - centerX;
				long dz = (long) z - centerZ;
				long dist = dx * dx + dz * dz;
				if (dist >= 12L * 12L && dist <= 96L * 96L) {
					return true;
				}
			}
		}
		return false;
	}
}
