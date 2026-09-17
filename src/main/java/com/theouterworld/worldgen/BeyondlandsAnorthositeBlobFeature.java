package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Occasional anorthosite surface blobs on BEYONDLANDS (Pluto) tholin crust.
 */
public class BeyondlandsAnorthositeBlobFeature extends Feature<NoneFeatureConfiguration> {
	public BeyondlandsAnorthositeBlobFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		if (random.nextFloat() > 0.22F) {
			return false;
		}

		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;
		ChunkAccess chunk = world.getChunk(origin);

		int x = origin.getX() + random.nextInt(16);
		int z = origin.getZ() + random.nextInt(16);
		int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
		BlockState surface = world.getBlockState(cursor);
		if (!surface.is(ModBlocks.THOLIN) && !surface.is(ModBlocks.NITROGEN_ICE)) {
			return false;
		}

		BlockState anorthosite = ModBlocks.ANORTHOSITE.defaultBlockState();
		int radius = 2 + random.nextInt(3);
		boolean placed = false;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
					continue;
				}
				int sx = x + dx;
				int sz = z + dz;
				if (sx < minX || sx > maxX || sz < minZ || sz > maxZ) {
					continue;
				}
				int sy = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sx, sz);
				cursor.set(sx, sy, sz);
				BlockState top = world.getBlockState(cursor);
				if (!top.is(ModBlocks.THOLIN) && !top.is(ModBlocks.NITROGEN_ICE)) {
					continue;
				}
				world.setBlock(cursor, anorthosite, 2);
				for (int dy = 1; dy <= 1 + random.nextInt(2); dy++) {
					cursor.set(sx, sy - dy, sz);
					BlockState below = world.getBlockState(cursor);
					if (below.is(ModBlocks.THOLIN) || below.is(ModBlocks.NITROGEN_ICE)) {
						world.setBlock(cursor, anorthosite, 2);
					}
				}
				placed = true;
			}
		}
		return placed;
	}
}
