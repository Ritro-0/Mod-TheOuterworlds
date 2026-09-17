package com.theouterworld.world;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.RiftPadBlock;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.worldgen.PotatoworldsShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Instant surface teleport for the Quantum Pod — no destination pad is placed.
 */
public final class QuantumPodTravel {
	private QuantumPodTravel() {
	}

	public static boolean isTravelWorld(ResourceKey<Level> dimension) {
		return RiftPadBlock.isSupportedDimension(dimension);
	}

	public static boolean begin(ServerPlayer player, ResourceKey<Level> dest) {
		if (!isTravelWorld(player.level().dimension()) || !isTravelWorld(dest)) {
			return false;
		}
		ServerLevel destWorld = player.level().getServer().getLevel(dest);
		if (destWorld == null) {
			return false;
		}

		BlockPos source = player.blockPosition();
		BlockPos surface = findLanding(destWorld, source.getX(), source.getZ());
		Vec3 landing = Vec3.atBottomCenterOf(surface).add(0.0, 0.01, 0.0);

		ServerLevel origin = (ServerLevel) player.level();
		origin.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.1F);
		spawnBurst(origin, player.position());

		player.teleport(new TeleportTransition(
			destWorld,
			landing,
			player.getDeltaMovement(),
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		destWorld.playSound(null, surface, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 1.25F);
		spawnBurst(destWorld, landing);
		if (ModDimensions.isSun(dest)) {
			SunArrival.onArrived(player);
		}
		return true;
	}

	private static BlockPos findLanding(ServerLevel world, int x, int z) {
		if (ModDimensions.isPotatoworlds(world.dimension())) {
			return new BlockPos(
				PotatoworldsShape.phobosDeckX(),
				PotatoworldsShape.phobosDeckY() + 1,
				PotatoworldsShape.phobosDeckZ()
			);
		}
		if (ModDimensions.isSpongeworld(world.dimension())) {
			return findSpongeworldLanding(world);
		}
		if (ModDimensions.isSun(world.dimension())) {
			return SunArrival.prepareLanding(world, x, z);
		}
		return findColumnSurface(world, x, z);
	}

	/**
	 * Always land on the Spongeworld body surface near origin — never map Overworld X/Z.
	 */
	private static BlockPos findSpongeworldLanding(ServerLevel world) {
		for (int r = 0; r <= 48; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (r > 0 && Math.abs(dx) != r && Math.abs(dz) != r) {
						continue;
					}
					BlockPos found = findColumnSurfaceOrNull(world, dx, dz);
					if (found != null) {
						return found;
					}
				}
			}
		}
		int deckY = com.theouterworld.worldgen.SpongeworldShape.CENTER_Y
			+ (int) (com.theouterworld.worldgen.SpongeworldShape.RADIUS_Y * 0.55);
		BlockPos deck = new BlockPos(0, deckY, 0);
		ensureSafeDeck(world, deck);
		return deck;
	}

	@Nullable
	private static BlockPos findColumnSurfaceOrNull(ServerLevel world, int x, int z) {
		int top = world.getMaxY();
		int bottom = world.getMinY();
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (groundState.isAir() || !groundState.isCollisionShapeFullBlock(world, ground)) {
				continue;
			}
			if (groundState.is(Blocks.BEDROCK) && y < bottom + 8) {
				continue;
			}
			BlockPos above = ground.above();
			if (world.getBlockState(above).canBeReplaced()
				&& world.getBlockState(above.above()).canBeReplaced()) {
				return above;
			}
		}
		return null;
	}

	private static BlockPos findColumnSurface(ServerLevel world, int x, int z) {
		int top = world.getMaxY();
		int bottom = world.getMinY();
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (groundState.isAir() || !groundState.isCollisionShapeFullBlock(world, ground)) {
				continue;
			}
			if (groundState.is(Blocks.BEDROCK) && y < bottom + 8) {
				continue;
			}
			BlockPos above = ground.above();
			if (world.getBlockState(above).canBeReplaced()
				&& world.getBlockState(above.above()).canBeReplaced()) {
				ensureSafeDeck(world, above);
				return above;
			}
		}
		BlockPos fallback = new BlockPos(x, Math.max(bottom + 16, 48), z);
		ensureSafeDeck(world, fallback);
		return fallback;
	}

	private static void ensureSafeDeck(ServerLevel world, BlockPos standPos) {
		BlockPos below = standPos.below();
		BlockState belowState = world.getBlockState(below);
		if (belowState.isAir() || !belowState.isCollisionShapeFullBlock(world, below)) {
			world.setBlockAndUpdate(below, foundation(world));
		}
		if (!world.getBlockState(standPos).canBeReplaced()) {
			world.setBlockAndUpdate(standPos, Blocks.AIR.defaultBlockState());
		}
		BlockPos head = standPos.above();
		if (!world.getBlockState(head).canBeReplaced()) {
			world.setBlockAndUpdate(head, Blocks.AIR.defaultBlockState());
		}
	}

	private static BlockState foundation(ServerLevel world) {
		if (ModDimensions.isWanderlands(world.dimension()) || ModDimensions.isPotatoworlds(world.dimension())) {
			return ModBlocks.ANORTHOSITE.defaultBlockState();
		}
		if (ModDimensions.isBeyondlands(world.dimension()) || ModDimensions.isBeyondlandsIi(world.dimension())
			|| ModDimensions.isSpinlands(world.dimension())) {
			return ModBlocks.THOLIN.defaultBlockState();
		}
		if (ModDimensions.isScarletlands(world.dimension())) {
			return ModBlocks.METHANE_ICE.defaultBlockState();
		}
		if (ModDimensions.isLonelands(world.dimension())) {
			return ModBlocks.DRY_ICE.defaultBlockState();
		}
		if (ModDimensions.isMoon(world.dimension())) {
			return ModBlocks.NORITE.defaultBlockState();
		}
		if (ModDimensions.isOuterworld(world.dimension())) {
			return ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		}
		if (ModDimensions.isFrostworld(world.dimension()) || ModDimensions.isSpongeworld(world.dimension())) {
			return Blocks.PACKED_ICE.defaultBlockState();
		}
		return ModBlocks.ANORTHOSITE.defaultBlockState();
	}

	private static void spawnBurst(ServerLevel world, Vec3 at) {
		world.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0, at.z, 18, 0.35, 0.6, 0.35, 0.02);
		world.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 0.5, at.z, 24, 0.4, 0.5, 0.4, 0.4);
	}
}
