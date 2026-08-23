package com.theouterworld.worldgen;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.entity.KharaxEntity;
import com.theouterworld.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jspecify.annotations.Nullable;

/**
 * Paints a compact Kharax den (cluster of shed pillars + spores) onto an existing
 * underground cave. Chunk-local only; does not carve new chambers.
 */
public class KharaxPillarFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MIN_CAVE_HEIGHT = 5;
	private static final int MAX_PILLAR_HEIGHT = 18;
	private static final int MIN_DEPTH_BELOW_SURFACE = 12;
	private static final int DEN_RADIUS = 6;

	public KharaxPillarFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		int maxX = minX + 15;
		int maxZ = minZ + 15;

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int cx = Mth.clamp(origin.getX(), minX + DEN_RADIUS, maxX - DEN_RADIUS);
		int cz = Mth.clamp(origin.getZ(), minZ + DEN_RADIUS, maxZ - DEN_RADIUS);
		CaveSpot center = findEnclosedCaveSpot(world, cx, cz, origin.getY(), cursor);
		if (center == null) {
			return false;
		}

		boolean placed = false;
		int pillars = 8 + random.nextInt(6);

		for (int i = 0; i < pillars; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = random.nextDouble() * DEN_RADIUS;
			int x = Mth.clamp(cx + Mth.floor(Math.cos(angle) * dist), minX + 1, maxX - 1);
			int z = Mth.clamp(cz + Mth.floor(Math.sin(angle) * dist), minZ + 1, maxZ - 1);
			CaveSpot spot = findEnclosedCaveSpot(world, x, z, center.floorY(), cursor);
			if (spot == null) {
				continue;
			}
			placed |= placePillar(world, random, x, z, spot, minX, maxX, minZ, maxZ);
		}

		if (random.nextFloat() < 0.85F) {
			placed |= placeHanger(world, random, cx, cz, center, minX, maxX, minZ, maxZ);
		}
		for (int i = 0; i < 3; i++) {
			placed |= placeFloorMound(world, random, cx, cz, center.floorY(), minX, maxX, minZ, maxZ);
		}

		if (placed) {
			spawnKharax(world, random, cx, cz, center.floorY(), minX, maxX, minZ, maxZ);
		}
		return placed;
	}

	private static boolean placePillar(
		WorldGenLevel world,
		RandomSource random,
		int x,
		int z,
		CaveSpot spot,
		int minX,
		int maxX,
		int minZ,
		int maxZ
	) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int open = spot.ceilingY() - spot.floorY();
		// Prefer floor-to-ceiling in modest chambers; cap height in huge caves.
		int pillarHeight = open <= MAX_PILLAR_HEIGHT
			? open
			: 8 + random.nextInt(MAX_PILLAR_HEIGHT - 7);
		double yaw = random.nextDouble() * Math.PI * 2.0;
		double sway = 0.06 + random.nextDouble() * 0.1;
		double px = x + 0.5;
		double pz = z + 0.5;
		int radius = random.nextFloat() < 0.2F ? 2 : 1;
		boolean placed = false;

		for (int h = 0; h <= pillarHeight; h++) {
			px += Math.cos(yaw) * sway;
			pz += Math.sin(yaw) * sway;
			yaw += (random.nextDouble() - 0.5) * 0.3;
			int ix = Mth.floor(px);
			int iz = Mth.floor(pz);
			int iy = spot.floorY() + h;
			if (iy > spot.ceilingY()) {
				break;
			}
			int localRadius = (h == 0 || h == pillarHeight) ? 1 : radius;

			for (int dx = -localRadius; dx <= localRadius; dx++) {
				for (int dz = -localRadius; dz <= localRadius; dz++) {
					if (localRadius > 1 && dx * dx + dz * dz > localRadius * localRadius) {
						continue;
					}
					int bx = ix + dx;
					int bz = iz + dz;
					if (bx < minX || bx > maxX || bz < minZ || bz > maxZ) {
						continue;
					}
					cursor.set(bx, iy, bz);
					if (!isReplaceableAir(world.getBlockState(cursor))) {
						continue;
					}
					world.setBlock(cursor, ModBlocks.KHARAX_SHED.defaultBlockState(), 2);
					placed = true;
					if (random.nextFloat() < 0.4F) {
						placeSpore(world, cursor.immutable(), minX, maxX, minZ, maxZ, random);
					}
				}
			}
		}
		return placed;
	}

	private static boolean placeHanger(
		WorldGenLevel world,
		RandomSource random,
		int x,
		int z,
		CaveSpot spot,
		int minX,
		int maxX,
		int minZ,
		int maxZ
	) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int hx = Mth.clamp(x + random.nextInt(5) - 2, minX + 1, maxX - 1);
		int hz = Mth.clamp(z + random.nextInt(5) - 2, minZ + 1, maxZ - 1);
		CaveSpot local = findEnclosedCaveSpot(world, hx, hz, spot.floorY(), cursor);
		if (local == null) {
			return false;
		}
		int length = Math.min(local.ceilingY() - local.floorY() - 1, 3 + random.nextInt(5));
		boolean placed = false;
		for (int h = 0; h < length; h++) {
			int iy = local.ceilingY() - h;
			if (iy <= local.floorY()) {
				break;
			}
			cursor.set(hx, iy, hz);
			if (!isReplaceableAir(world.getBlockState(cursor))) {
				break;
			}
			world.setBlock(cursor, ModBlocks.KHARAX_SHED.defaultBlockState(), 2);
			placed = true;
			if (random.nextFloat() < 0.45F) {
				placeSpore(world, cursor.immutable(), minX, maxX, minZ, maxZ, random);
			}
		}
		return placed;
	}

	private static boolean placeFloorMound(
		WorldGenLevel world,
		RandomSource random,
		int cx,
		int cz,
		int searchY,
		int minX,
		int maxX,
		int minZ,
		int maxZ
	) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int x = Mth.clamp(cx + random.nextInt(DEN_RADIUS * 2 + 1) - DEN_RADIUS, minX + 1, maxX - 1);
		int z = Mth.clamp(cz + random.nextInt(DEN_RADIUS * 2 + 1) - DEN_RADIUS, minZ + 1, maxZ - 1);
		CaveSpot spot = findEnclosedCaveSpot(world, x, z, searchY, cursor);
		if (spot == null) {
			return false;
		}
		boolean placed = false;
		int radius = 1 + random.nextInt(2);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius || random.nextFloat() > 0.65F) {
					continue;
				}
				int px = x + dx;
				int pz = z + dz;
				if (px < minX || px > maxX || pz < minZ || pz > maxZ) {
					continue;
				}
				cursor.set(px, spot.floorY(), pz);
				if (!isReplaceableAir(world.getBlockState(cursor))) {
					continue;
				}
				world.setBlock(cursor, ModBlocks.KHARAX_SHED.defaultBlockState(), 2);
				placed = true;
				if (random.nextFloat() < 0.5F) {
					cursor.set(px, spot.floorY() + 1, pz);
					if (isReplaceableAir(world.getBlockState(cursor))) {
						world.setBlock(cursor, ModBlocks.KHARAX_SPORE.defaultBlockState(), 2);
					}
				}
			}
		}
		return placed;
	}

	private static void placeSpore(
		WorldGenLevel world,
		BlockPos shedPos,
		int minX,
		int maxX,
		int minZ,
		int maxZ,
		RandomSource random
	) {
		Direction direction = Direction.values()[random.nextInt(6)];
		BlockPos sporePos = shedPos.relative(direction);
		if (sporePos.getX() < minX || sporePos.getX() > maxX || sporePos.getZ() < minZ || sporePos.getZ() > maxZ) {
			return;
		}
		if (isReplaceableAir(world.getBlockState(sporePos))) {
			world.setBlock(sporePos, ModBlocks.KHARAX_SPORE.defaultBlockState(), 2);
		}
	}

	private static void spawnKharax(
		WorldGenLevel world,
		RandomSource random,
		int cx,
		int cz,
		int searchY,
		int minX,
		int maxX,
		int minZ,
		int maxZ
	) {
		if (!(world.getLevel() instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos stand = findSpawnStand(world, cx, cz, searchY, minX, maxX, minZ, maxZ, random);
		if (stand == null) {
			return;
		}
		KharaxEntity kharax = ModEntities.KHARAX.create(serverLevel, EntitySpawnReason.STRUCTURE);
		if (kharax == null) {
			return;
		}
		kharax.snapTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
		if (!serverLevel.noCollision(kharax) || !kharax.checkSpawnObstruction(serverLevel)) {
			// Try a few nearby offsets before giving up.
			boolean fitted = false;
			for (int attempt = 0; attempt < 12; attempt++) {
				int ox = stand.getX() + random.nextInt(5) - 2;
				int oz = stand.getZ() + random.nextInt(5) - 2;
				if (ox < minX + 1 || ox > maxX - 1 || oz < minZ + 1 || oz > maxZ - 1) {
					continue;
				}
				BlockPos alt = findColumnStand(world, ox, oz, stand.getY());
				if (alt == null) {
					continue;
				}
				kharax.snapTo(alt.getX() + 0.5, alt.getY(), alt.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
				if (serverLevel.noCollision(kharax) && kharax.checkSpawnObstruction(serverLevel)) {
					stand = alt;
					fitted = true;
					break;
				}
			}
			if (!fitted) {
				kharax.discard();
				return;
			}
		}
		kharax.setHomePos(stand.immutable());
		kharax.setPersistenceRequired();
		serverLevel.addFreshEntityWithPassengers(kharax);
	}

	/**
	 * Finds open floor space near the den center after pillars are placed — never the pillar column itself.
	 */
	private static @Nullable BlockPos findSpawnStand(
		WorldGenLevel world,
		int cx,
		int cz,
		int searchY,
		int minX,
		int maxX,
		int minZ,
		int maxZ,
		RandomSource random
	) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		// Prefer spots a bit away from the densest center.
		for (int attempt = 0; attempt < 24; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = 1.5 + random.nextDouble() * (DEN_RADIUS - 1.0);
			int x = Mth.clamp(cx + Mth.floor(Math.cos(angle) * dist), minX + 1, maxX - 1);
			int z = Mth.clamp(cz + Mth.floor(Math.sin(angle) * dist), minZ + 1, maxZ - 1);
			CaveSpot spot = findEnclosedCaveSpot(world, x, z, searchY, cursor);
			if (spot == null) {
				continue;
			}
			BlockPos stand = findColumnStand(world, x, z, spot.floorY());
			if (stand != null) {
				return stand;
			}
		}
		return findColumnStand(world, cx, cz, searchY);
	}

	private static @Nullable BlockPos findColumnStand(WorldGenLevel world, int x, int z, int nearY) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int from = nearY + 4;
		int to = nearY - 6;
		for (int y = from; y >= to; y--) {
			cursor.set(x, y - 1, z);
			BlockState floor = world.getBlockState(cursor);
			if (!floor.isSolidRender() && !floor.is(ModBlocks.KHARAX_SHED)) {
				continue;
			}
			// Need two air blocks for the Kharax body (feet + head).
			cursor.set(x, y, z);
			if (!isReplaceableAir(world.getBlockState(cursor))) {
				continue;
			}
			cursor.set(x, y + 1, z);
			if (!isReplaceableAir(world.getBlockState(cursor))) {
				continue;
			}
			// Reject standing inside a pillar trunk (shed at feet level means occupied).
			return new BlockPos(x, y, z);
		}
		return null;
	}

	/**
	 * Finds a real underground cave column: air with solid below, solid ceiling above,
	 * and well below the world surface. Tall open caves are allowed; pillar height is capped separately.
	 */
	private static CaveSpot findEnclosedCaveSpot(
		WorldGenLevel world,
		int x,
		int z,
		int searchY,
		BlockPos.MutableBlockPos cursor
	) {
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		int minY = world.getMinY() + 8;
		int from = Math.min(searchY + 16, surfaceY - MIN_DEPTH_BELOW_SURFACE);
		int to = Math.max(minY, searchY - 32);
		if (from <= to) {
			return null;
		}

		for (int y = from; y >= to; y--) {
			cursor.set(x, y, z);
			if (!world.getBlockState(cursor).isAir()) {
				continue;
			}
			cursor.set(x, y - 1, z);
			BlockState below = world.getBlockState(cursor);
			if (!below.isSolidRender() && !below.is(ModBlocks.KHARAX_SHED)) {
				continue;
			}

			int ceilingY = -1;
			int maxCeil = Math.min(y + 48, surfaceY - MIN_DEPTH_BELOW_SURFACE);
			for (int cy = y + 1; cy <= maxCeil; cy++) {
				cursor.set(x, cy, z);
				if (!world.getBlockState(cursor).isAir()) {
					ceilingY = cy - 1;
					break;
				}
			}
			if (ceilingY < 0) {
				continue;
			}
			int open = ceilingY - y;
			if (open < MIN_CAVE_HEIGHT) {
				continue;
			}
			if (ceilingY > surfaceY - MIN_DEPTH_BELOW_SURFACE) {
				continue;
			}
			return new CaveSpot(y, ceilingY);
		}
		return null;
	}

	private static boolean isReplaceableAir(BlockState state) {
		return state.isAir() || state.canBeReplaced();
	}

	private record CaveSpot(int floorY, int ceilingY) {}
}
