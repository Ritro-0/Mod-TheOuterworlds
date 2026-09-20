package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.DeepworldLayers;
import com.theouterworld.world.EdgeworldLayers;
import com.theouterworld.world.FarworldLayers;
import com.theouterworld.world.HighworldLayers;
import com.theouterworld.world.RiftPadLinksSavedData;
import com.theouterworld.world.SunArrival;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class RiftPadBlockEntity extends BlockEntity {
	private static final int CHARGE_TICKS = 40;

	private int chargeTicks;
	@Nullable
	private UUID travelerId;
	@Nullable
	private ResourceKey<Level> destination;
	@Nullable
	private GlobalPos linkedPad;

	public RiftPadBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RIFT_PAD, pos, state);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, RiftPadBlockEntity entity) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}
		if (entity.chargeTicks <= 0 || entity.travelerId == null || entity.destination == null) {
			return;
		}

		entity.chargeTicks--;
		spawnBeaconParticles(serverWorld, pos);

		if (entity.chargeTicks > 0) {
			entity.setChanged();
			return;
		}

		ServerPlayer traveler = serverWorld.getServer().getPlayerList().getPlayer(entity.travelerId);
		ResourceKey<Level> destKey = entity.destination;
		entity.travelerId = null;
		entity.destination = null;
		entity.setChanged();

		if (traveler == null || traveler.isRemoved()) {
			return;
		}
		completeTeleport(serverWorld, pos, state, entity, traveler, destKey);
	}

	public boolean beginVisit(ServerPlayer player, ResourceKey<Level> dest) {
		if (this.chargeTicks > 0) {
			return false;
		}
		if (!RiftPadBlock.isSupportedDimension(player.level().dimension())
			|| !RiftPadBlock.isRiftPadDestination(dest)) {
			return false;
		}
		this.travelerId = player.getUUID();
		this.destination = dest;
		this.chargeTicks = CHARGE_TICKS;
		setChanged();
		if (this.level instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.85F);
			spawnBeaconParticles(serverLevel, this.worldPosition);
		}
		return true;
	}

	public void onBroken() {
		if (this.linkedPad == null) {
			return;
		}
		if (!(this.level instanceof ServerLevel serverLevel)) {
			return;
		}
		RiftPadLinksSavedData data = RiftPadLinksSavedData.get(serverLevel.getServer());
		if (data != null) {
			data.queueDeletion(this.linkedPad);
		}
		this.linkedPad = null;
		setChanged();
	}

	private static void completeTeleport(
		ServerLevel sourceWorld,
		BlockPos sourcePos,
		BlockState sourceState,
		RiftPadBlockEntity sourcePad,
		ServerPlayer player,
		ResourceKey<Level> destKey
	) {
		ServerLevel destWorld = sourceWorld.getServer().getLevel(destKey);
		if (destWorld == null) {
			return;
		}

		RiftPadLinksSavedData links = RiftPadLinksSavedData.get(sourceWorld.getServer());
		if (links != null) {
			links.processPending(sourceWorld.getServer());
		}

		// Remove previously linked destination pad if it still exists at the old spot.
		if (sourcePad.linkedPad != null && sourcePad.linkedPad.dimension().equals(destKey)) {
			BlockPos old = sourcePad.linkedPad.pos();
			destWorld.getChunk(old);
			if (destWorld.getBlockState(old).is(ModBlocks.RIFT_PAD)) {
				destWorld.setBlock(old, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}

		BlockPos destPadPos = placeDestinationPad(destWorld, sourcePos, sourceState.getValue(RiftPadBlock.FACING));
		Direction facing = sourceState.getValue(RiftPadBlock.FACING);
		Vec3 landing;
		if (ModDimensions.isGasGiant(destKey)) {
			// Stand on the pad itself so survival players are never left floating in clouds.
			landing = Vec3.atBottomCenterOf(destPadPos).add(0.0, 1.01, 0.0);
		} else {
			landing = Vec3.atBottomCenterOf(destPadPos).add(facing.getStepX() * 1.5, 0.0, facing.getStepZ() * 1.5);
		}

		// Link both pads.
		GlobalPos destGlobal = GlobalPos.of(destKey, destPadPos);
		GlobalPos sourceGlobal = GlobalPos.of(sourceWorld.dimension(), sourcePos);
		sourcePad.linkedPad = destGlobal;
		sourcePad.setChanged();
		if (destWorld.getBlockEntity(destPadPos) instanceof RiftPadBlockEntity destPad) {
			destPad.linkedPad = sourceGlobal;
			destPad.setChanged();
		}

		player.teleport(new TeleportTransition(
			destWorld,
			landing,
			player.getDeltaMovement(),
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		destWorld.playSound(null, destPadPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.9F, 1.2F);
		if (ModDimensions.isSun(destKey)) {
			SunArrival.onArrived(player);
		}
	}

	private static BlockPos placeDestinationPad(ServerLevel destWorld, BlockPos sourcePos, Direction facing) {
		BlockPos existing = findExistingPad(destWorld, sourcePos.getX(), sourcePos.getZ());
		if (existing != null) {
			if (ModDimensions.isGasGiant(destWorld.dimension())) {
				ensureCloudPlatform(destWorld, existing.getX(), existing.getZ(), existing.getY(), deckCloud(destWorld));
			}
			return existing;
		}

		BlockPos surface = findSurface(destWorld, sourcePos.getX(), sourcePos.getZ());
		BlockState pad = ModBlocks.RIFT_PAD.defaultBlockState().setValue(RiftPadBlock.FACING, facing);

		if (ModDimensions.isGasGiant(destWorld.dimension())) {
			BlockState cloud = deckCloud(destWorld);
			ensureCloudPlatform(destWorld, surface.getX(), surface.getZ(), surface.getY(), cloud);
			destWorld.setBlock(surface, pad, Block.UPDATE_ALL);
			return surface;
		}

		BlockState below = destWorld.getBlockState(surface.below());
		if (below.isAir() || !below.isCollisionShapeFullBlock(destWorld, surface.below()) || isDangerousFluid(below)) {
			destWorld.setBlockAndUpdate(surface.below(), foundation(destWorld));
		}

		// Emberworld / Amberworld: clear lava or methane from the pad cell and headroom.
		if (ModDimensions.isEmberworld(destWorld.dimension()) || ModDimensions.isAmberworld(destWorld.dimension())) {
			clearFluidColumn(destWorld, surface);
		}

		destWorld.setBlock(surface, pad, Block.UPDATE_ALL);
		return surface;
	}

	@Nullable
	private static BlockPos findExistingPad(ServerLevel world, int x, int z) {
		int minY;
		int maxY;
		if (ModDimensions.isHighworld(world.dimension())) {
			minY = HighworldLayers.AMMONIA_BOTTOM_Y - 2;
			maxY = HighworldLayers.AMMONIA_TOP_Y + 4;
		} else if (ModDimensions.isDeepworld(world.dimension())) {
			minY = DeepworldLayers.METHANE_BOTTOM_Y - 2;
			maxY = DeepworldLayers.METHANE_TOP_Y + 4;
		} else if (ModDimensions.isFarworld(world.dimension())) {
			minY = FarworldLayers.METHANE_UPPER_BOTTOM_Y - 2;
			maxY = FarworldLayers.METHANE_UPPER_TOP_Y + 4;
		} else if (ModDimensions.isEdgeworld(world.dimension())) {
			minY = EdgeworldLayers.METHANE_UPPER_BOTTOM_Y - 2;
			maxY = EdgeworldLayers.METHANE_UPPER_TOP_Y + 4;
		} else {
			minY = world.getMinY();
			maxY = world.getMaxY();
		}
		for (int y = maxY; y >= minY; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (world.getBlockState(pos).is(ModBlocks.RIFT_PAD)) {
				return pos;
			}
		}
		return null;
	}

	private static BlockPos findSurface(ServerLevel world, int x, int z) {
		if (ModDimensions.isHighworld(world.dimension())) {
			return findGasGiantCloudPadPos(
				world, x, z,
				ModBlocks.AMMONIA_CLOUD,
				HighworldLayers.AMMONIA_BOTTOM_Y,
				HighworldLayers.AMMONIA_TOP_Y
			);
		}
		if (ModDimensions.isDeepworld(world.dimension())) {
			return findGasGiantCloudPadPos(
				world, x, z,
				ModBlocks.METHANE_CLOUD,
				DeepworldLayers.METHANE_BOTTOM_Y,
				DeepworldLayers.METHANE_TOP_Y
			);
		}
		if (ModDimensions.isFarworld(world.dimension())) {
			return findGasGiantCloudPadPos(
				world, x, z,
				ModBlocks.METHANE_CLOUD,
				FarworldLayers.METHANE_UPPER_BOTTOM_Y,
				FarworldLayers.METHANE_UPPER_TOP_Y
			);
		}
		if (ModDimensions.isEdgeworld(world.dimension())) {
			return findGasGiantCloudPadPos(
				world, x, z,
				ModBlocks.METHANE_CLOUD,
				EdgeworldLayers.METHANE_UPPER_BOTTOM_Y,
				EdgeworldLayers.METHANE_UPPER_TOP_Y
			);
		}
		if (ModDimensions.isEmberworld(world.dimension())) {
			return findEmberworldSafePadPos(world, x, z);
		}
		if (ModDimensions.isAmberworld(world.dimension())) {
			return findAmberworldSafePadPos(world, x, z);
		}
		if (ModDimensions.isSpongeworld(world.dimension())) {
			return findSpongeworldSafePadPos(world, x, z);
		}
		if (ModDimensions.isPotatoworlds(world.dimension())) {
			return findPotatoworldsSafePadPos(world, x, z);
		}

		int top = world.getMaxY();
		int bottom = world.getMinY();
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (groundState.isAir() || !groundState.isCollisionShapeFullBlock(world, ground)) {
				continue;
			}
			BlockPos above = ground.above();
			if (world.getBlockState(above).canBeReplaced()) {
				return above;
			}
		}
		return new BlockPos(x, Math.max(bottom + 64, 64), z);
	}

	/**
	 * Prefer solid ground with non-lava air above sea level. Search nearby columns,
	 * then synthesize a sulfuric-basalt pad deck above the lava ocean if needed.
	 */
	private static BlockPos findEmberworldSafePadPos(ServerLevel world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		BlockPos local = findDrySolidAbove(world, x, z);
		if (local != null) {
			return local;
		}
		for (int r = 1; r <= 32; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findDrySolidAbove(world, x + dx, z + dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		int safeY = Math.max(76, world.getSeaLevel() + 5);
		BlockPos deck = new BlockPos(x, safeY, z);
		ensureEmberworldPlatform(world, deck);
		return deck;
	}

	@Nullable
	private static BlockPos findDrySolidAbove(ServerLevel world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		int top = world.getMaxY();
		int bottom = Math.max(world.getMinY(), world.getSeaLevel());
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (!groundState.isCollisionShapeFullBlock(world, ground) || isDangerousFluid(groundState)) {
				continue;
			}
			BlockPos above = ground.above();
			BlockState aboveState = world.getBlockState(above);
			if (!aboveState.canBeReplaced() || isDangerousFluid(aboveState)) {
				continue;
			}
			BlockState head = world.getBlockState(above.above());
			if (isDangerousFluid(head)) {
				continue;
			}
			return above;
		}
		return null;
	}

	private static void ensureEmberworldPlatform(ServerLevel world, BlockPos padPos) {
		BlockState foundation = ModBlocks.SULFURIC_BASALT.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 8) {
					continue;
				}
				cursor.set(padPos.getX() + dx, padPos.getY() - 1, padPos.getZ() + dz);
				world.setBlock(cursor, foundation, Block.UPDATE_ALL);
				cursor.setY(padPos.getY());
				if (isDangerousFluid(world.getBlockState(cursor)) || world.getBlockState(cursor).canBeReplaced()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
				cursor.setY(padPos.getY() + 1);
				if (isDangerousFluid(world.getBlockState(cursor))) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
	}

	/**
	 * Prefer dry tholin/regolith above methane seas. Search nearby columns,
	 * then synthesize a tholin pad deck above sea level if needed.
	 */
	private static BlockPos findAmberworldSafePadPos(ServerLevel world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		BlockPos local = findDrySolidAboveSea(world, x, z);
		if (local != null) {
			return local;
		}
		for (int r = 1; r <= 32; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findDrySolidAboveSea(world, x + dx, z + dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		int safeY = Math.max(world.getSeaLevel() + 1, 64);
		BlockPos deck = new BlockPos(x, safeY, z);
		ensureAmberworldPlatform(world, deck);
		return deck;
	}

	@Nullable
	private static BlockPos findDrySolidAboveSea(ServerLevel world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		int top = world.getMaxY();
		int bottom = Math.max(world.getMinY(), world.getSeaLevel());
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (!groundState.isCollisionShapeFullBlock(world, ground) || !groundState.getFluidState().isEmpty()) {
				continue;
			}
			BlockPos above = ground.above();
			BlockState aboveState = world.getBlockState(above);
			if (!aboveState.canBeReplaced() || !aboveState.getFluidState().isEmpty()) {
				continue;
			}
			BlockState head = world.getBlockState(above.above());
			if (!head.getFluidState().isEmpty()) {
				continue;
			}
			return above;
		}
		return null;
	}

	private static void ensureAmberworldPlatform(ServerLevel world, BlockPos padPos) {
		BlockState foundation = ModBlocks.THOLIN.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 8) {
					continue;
				}
				cursor.set(padPos.getX() + dx, padPos.getY() - 1, padPos.getZ() + dz);
				world.setBlock(cursor, foundation, Block.UPDATE_ALL);
				cursor.setY(padPos.getY());
				if (!world.getBlockState(cursor).getFluidState().isEmpty() || world.getBlockState(cursor).canBeReplaced()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
				cursor.setY(padPos.getY() + 1);
				if (!world.getBlockState(cursor).getFluidState().isEmpty()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
	}

	/**
	 * Prefer solid ice on the Hyperion potato near the arrival column, then near origin.
	 * Fallback: synthesize a packed-ice deck on the outer shell.
	 */
	private static BlockPos findSpongeworldSafePadPos(ServerLevel world, int x, int z) {
		BlockPos local = findDrySolidAnyY(world, x, z);
		if (local != null) {
			return local;
		}
		for (int r = 1; r <= 48; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findDrySolidAnyY(world, x + dx, z + dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		for (int r = 0; r <= 32; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (r > 0 && Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findDrySolidAnyY(world, dx, dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		int deckY = com.theouterworld.worldgen.SpongeworldShape.CENTER_Y
			+ (int) (com.theouterworld.worldgen.SpongeworldShape.RADIUS_Y * 0.55);
		BlockPos deck = new BlockPos(0, deckY, 0);
		ensureSpongeworldPlatform(world, deck);
		return deck;
	}

	/**
	 * Prefer solid rock on Phobos near the arrival column, then near Phobos center.
	 * Fallback: synthesize a regolith deck on Phobos' outer shell.
	 */
	private static BlockPos findPotatoworldsSafePadPos(ServerLevel world, int x, int z) {
		BlockPos local = findDrySolidAnyY(world, x, z);
		if (local != null) {
			return local;
		}
		for (int r = 1; r <= 48; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findDrySolidAnyY(world, x + dx, z + dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		int originX = com.theouterworld.worldgen.PotatoworldsShape.phobosDeckX();
		int originZ = com.theouterworld.worldgen.PotatoworldsShape.phobosDeckZ();
		for (int r = 0; r <= 32; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (r > 0 && Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findDrySolidAnyY(world, originX + dx, originZ + dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		BlockPos deck = new BlockPos(
			com.theouterworld.worldgen.PotatoworldsShape.phobosDeckX(),
			com.theouterworld.worldgen.PotatoworldsShape.phobosDeckY(),
			com.theouterworld.worldgen.PotatoworldsShape.phobosDeckZ()
		);
		ensurePotatoworldsPlatform(world, deck);
		return deck;
	}

	@Nullable
	private static BlockPos findDrySolidAnyY(ServerLevel world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		int top = world.getMaxY();
		int bottom = world.getMinY();
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (!groundState.isCollisionShapeFullBlock(world, ground) || !groundState.getFluidState().isEmpty()) {
				continue;
			}
			BlockPos above = ground.above();
			BlockState aboveState = world.getBlockState(above);
			if (!aboveState.canBeReplaced() || !aboveState.getFluidState().isEmpty()) {
				continue;
			}
			BlockState head = world.getBlockState(above.above());
			if (!head.canBeReplaced() || !head.getFluidState().isEmpty()) {
				continue;
			}
			return above;
		}
		return null;
	}

	private static void ensureSpongeworldPlatform(ServerLevel world, BlockPos padPos) {
		BlockState foundation = Blocks.PACKED_ICE.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 8) {
					continue;
				}
				cursor.set(padPos.getX() + dx, padPos.getY() - 1, padPos.getZ() + dz);
				world.setBlock(cursor, foundation, Block.UPDATE_ALL);
				cursor.setY(padPos.getY());
				if (world.getBlockState(cursor).canBeReplaced()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
				cursor.setY(padPos.getY() + 1);
				if (world.getBlockState(cursor).canBeReplaced()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
	}

	private static void ensurePotatoworldsPlatform(ServerLevel world, BlockPos padPos) {
		BlockState foundation = ModBlocks.REGOLITH.defaultBlockState();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 8) {
					continue;
				}
				cursor.set(padPos.getX() + dx, padPos.getY() - 1, padPos.getZ() + dz);
				world.setBlock(cursor, foundation, Block.UPDATE_ALL);
				cursor.setY(padPos.getY());
				if (world.getBlockState(cursor).canBeReplaced()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
				cursor.setY(padPos.getY() + 1);
				if (world.getBlockState(cursor).canBeReplaced()) {
					world.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
	}

	private static void clearFluidColumn(ServerLevel world, BlockPos padPos) {
		for (int dy = 0; dy <= 2; dy++) {
			BlockPos pos = padPos.above(dy);
			if (!world.getBlockState(pos).getFluidState().isEmpty() || isDangerousFluid(world.getBlockState(pos))) {
				world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
	}

	private static boolean isDangerousFluid(BlockState state) {
		return state.getFluidState().is(Fluids.LAVA) || state.getFluidState().is(Fluids.FLOWING_LAVA);
	}

	/**
	 * Locate a cloud in the arrival deck (wide search), otherwise synthesize a mid-deck platform.
	 * Always returns a position that will receive a cloud platform before the pad is placed.
	 */
	private static BlockPos findGasGiantCloudPadPos(
		ServerLevel world,
		int x,
		int z,
		Block cloudBlock,
		int yBottom,
		int yTop
	) {
		world.getChunk(x >> 4, z >> 4);

		for (int y = yTop; y >= yBottom; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (world.getBlockState(pos).is(cloudBlock)) {
				return pos;
			}
		}

		for (int r = 1; r <= 48; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					int cx = x + dx;
					int cz = z + dz;
					world.getChunk(cx >> 4, cz >> 4);
					for (int y = yTop; y >= yBottom; y--) {
						BlockPos pos = new BlockPos(cx, y, cz);
						if (world.getBlockState(pos).is(cloudBlock)) {
							return pos;
						}
					}
				}
			}
		}

		// No natural cloud nearby — invent a landing deck at a stable mid-band height.
		return new BlockPos(x, yBottom + Math.max(4, (yTop - yBottom) / 2), z);
	}

	/** Fill a small methane/ammonia platform so the pad is never alone in empty air. */
	private static void ensureCloudPlatform(ServerLevel world, int x, int z, int y, BlockState cloud) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 8) {
					continue;
				}
				cursor.set(x + dx, y, z + dz);
				world.getChunk(cursor);
				BlockState state = world.getBlockState(cursor);
				if (state.isAir() || state.getBlock() instanceof AerogelCloudBlock) {
					world.setBlock(cursor, cloud, Block.UPDATE_ALL);
				}
				// Soften the underside so the deck reads as a cloud bank.
				cursor.set(x + dx, y - 1, z + dz);
				BlockState below = world.getBlockState(cursor);
				if (below.isAir()) {
					world.setBlock(cursor, cloud, Block.UPDATE_ALL);
				}
			}
		}
	}

	private static BlockState deckCloud(ServerLevel world) {
		if (ModDimensions.isHighworld(world.dimension())) {
			return ModBlocks.AMMONIA_CLOUD.defaultBlockState();
		}
		return ModBlocks.METHANE_CLOUD.defaultBlockState();
	}

	private static BlockState foundation(ServerLevel targetWorld) {
		if (ModDimensions.isHighworld(targetWorld.dimension())) {
			return ModBlocks.AMMONIA_CLOUD.defaultBlockState();
		}
		if (ModDimensions.isDeepworld(targetWorld.dimension())
			|| ModDimensions.isFarworld(targetWorld.dimension())
			|| ModDimensions.isEdgeworld(targetWorld.dimension())) {
			return ModBlocks.METHANE_CLOUD.defaultBlockState();
		}
		if (ModDimensions.isMoon(targetWorld.dimension())) {
			return ModBlocks.NORITE.defaultBlockState();
		}
		if (ModDimensions.isInnerworld(targetWorld.dimension())) {
			return ModBlocks.KOMATIITE.defaultBlockState();
		}
		if (ModDimensions.isNearworld(targetWorld.dimension())
			|| ModDimensions.isEmberworld(targetWorld.dimension())) {
			return ModBlocks.SULFURIC_BASALT.defaultBlockState();
		}
		if (ModDimensions.isFrostworld(targetWorld.dimension())) {
			return ModBlocks.CARBONIC_ICE.defaultBlockState();
		}
		if (ModDimensions.isAmberworld(targetWorld.dimension())) {
			return ModBlocks.THOLIN.defaultBlockState();
		}
		if (ModDimensions.isSpongeworld(targetWorld.dimension())) {
			return Blocks.PACKED_ICE.defaultBlockState();
		}
		if (ModDimensions.isPotatoworlds(targetWorld.dimension())) {
			return ModBlocks.REGOLITH.defaultBlockState();
		}
		if (ModDimensions.isWanderlands(targetWorld.dimension())) {
			return ModBlocks.ANORTHOSITE.defaultBlockState();
		}
		if (ModDimensions.isBeyondlands(targetWorld.dimension())
			|| ModDimensions.isBeyondlandsIi(targetWorld.dimension())
			|| ModDimensions.isSpinlands(targetWorld.dimension())) {
			return ModBlocks.THOLIN.defaultBlockState();
		}
		if (ModDimensions.isScarletlands(targetWorld.dimension())) {
			return ModBlocks.METHANE_ICE.defaultBlockState();
		}
		if (ModDimensions.isLonelands(targetWorld.dimension())) {
			return ModBlocks.DRY_ICE.defaultBlockState();
		}
		if (ModDimensions.isOuterworld(targetWorld.dimension())) {
			return ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		}
		return Blocks.STONE.defaultBlockState();
	}

	private static void spawnBeaconParticles(ServerLevel world, BlockPos pos) {
		RandomSource random = world.getRandom();
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 1.6;
		double cz = pos.getZ() + 0.5;
		for (int i = 0; i < 14; i++) {
			double ox = (random.nextDouble() - 0.5) * 0.2;
			double oz = (random.nextDouble() - 0.5) * 0.2;
			world.sendParticles(ParticleTypes.END_ROD, cx + ox, cy, cz + oz, 0, 0.0, 1.8 + random.nextDouble(), 0.0, 1.0);
			world.sendParticles(ParticleTypes.FIREWORK, cx + ox, cy, cz + oz, 0, 0.0, 2.2 + random.nextDouble(), 0.0, 1.0);
			world.sendParticles(ParticleTypes.GLOW, cx, cy + i * 0.35, cz, 0, 0.0, 0.6, 0.0, 1.0);
		}
		world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 4, 0.1, 0.4, 0.1, 0.02);
	}

	@Nullable
	public GlobalPos getLinkedPad() {
		return this.linkedPad;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		output.putInt("ChargeTicks", this.chargeTicks);
		if (this.travelerId != null) {
			output.putString("Traveler", this.travelerId.toString());
		}
		if (this.destination != null) {
			output.putString("Destination", this.destination.identifier().toString());
		}
		if (this.linkedPad != null) {
			output.putString("LinkedDim", this.linkedPad.dimension().identifier().toString());
			output.putInt("LinkedX", this.linkedPad.pos().getX());
			output.putInt("LinkedY", this.linkedPad.pos().getY());
			output.putInt("LinkedZ", this.linkedPad.pos().getZ());
		}
		super.saveAdditional(output);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.chargeTicks = Math.max(0, input.getIntOr("ChargeTicks", 0));
		String traveler = input.getStringOr("Traveler", "");
		if (!traveler.isEmpty()) {
			try {
				this.travelerId = UUID.fromString(traveler);
			} catch (IllegalArgumentException ignored) {
				this.travelerId = null;
			}
		} else {
			this.travelerId = null;
		}
		this.destination = parseDimension(input.getStringOr("Destination", "")).orElse(null);
		String linkedDim = input.getStringOr("LinkedDim", "");
		if (!linkedDim.isEmpty()) {
			parseDimension(linkedDim).ifPresent(dim ->
				this.linkedPad = GlobalPos.of(
					dim,
					new BlockPos(
						input.getIntOr("LinkedX", 0),
						input.getIntOr("LinkedY", 0),
						input.getIntOr("LinkedZ", 0)
					)
				)
			);
		} else {
			this.linkedPad = null;
		}
	}

	private static Optional<ResourceKey<Level>> parseDimension(String id) {
		if (id == null || id.isEmpty()) {
			return Optional.empty();
		}
		Identifier identifier = Identifier.tryParse(id);
		if (identifier == null) {
			return Optional.empty();
		}
		return Optional.of(ResourceKey.create(Registries.DIMENSION, identifier));
	}
}
