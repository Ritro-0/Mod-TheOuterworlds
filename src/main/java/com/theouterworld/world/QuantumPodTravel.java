package com.theouterworld.world;

import com.theouterworld.block.RiftPadBlock;
import com.theouterworld.registry.ModDimensions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

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
		ResourceKey<Level> from = origin.dimension();
		List<Entity> companions = leashedTo(player);
		for (Entity companion : companions) {
			if (companion instanceof Leashable leashable) {
				leashable.removeLeash();
			}
		}
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
		bringCompanions(destWorld, player, companions, landing);
		if (ModDimensions.isSun(dest)) {
			SunArrival.onArrived(player);
		}
		com.theouterworld.advancement.ModAdvancements.onQuantumTravel(player, from, dest);
		return true;
	}

	private static BlockPos findLanding(ServerLevel world, int x, int z) {
		return ArrivalLanding.playerFeet(world, x, z);
	}

	private static List<Entity> leashedTo(ServerPlayer player) {
		List<Entity> companions = new ArrayList<>();
		for (Leashable leashable : Leashable.leashableLeashedTo(player)) {
			if (leashable instanceof Entity entity && entity != player && entity.isAlive()) {
				companions.add(entity);
			}
		}
		return companions;
	}

	/** The lead comes with them. Each body gets its own open column beside the player. */
	private static void bringCompanions(ServerLevel dest, ServerPlayer player, List<Entity> companions, Vec3 playerLanding) {
		int slot = 0;
		for (Entity companion : companions) {
			if (!companion.isAlive() && companion.isRemoved()) {
				continue;
			}
			Vec3 spot = companionLanding(dest, companion, playerLanding, slot++);
			Entity arrived = companion.teleport(new TeleportTransition(
				dest,
				spot,
				Vec3.ZERO,
				companion.getYRot(),
				companion.getXRot(),
				TeleportTransition.DO_NOTHING
			));
			Entity pet = arrived != null ? arrived : companion;
			if (pet instanceof Leashable leashable && pet.isAlive() && pet.level() == player.level()) {
				leashable.setLeashedTo(player, true);
				dest.sendParticles(ParticleTypes.PORTAL, pet.getX(), pet.getY() + pet.getBbHeight() * 0.5, pet.getZ(), 12, 0.3, 0.4, 0.3, 0.2);
			} else {
				player.spawnAtLocation(dest, new ItemStack(Items.LEAD));
			}
		}
	}

	private static Vec3 companionLanding(ServerLevel world, Entity entity, Vec3 playerLanding, int slot) {
		int body = Math.max(2, Mth.ceil(entity.getBbHeight()));
		int baseX = Mth.floor(playerLanding.x);
		int baseZ = Mth.floor(playerLanding.z);
		for (int ring = 1; ring <= 5; ring++) {
			int steps = 8;
			for (int i = 0; i < steps; i++) {
				double angle = (i + slot * 0.37) * (Math.PI * 2.0 / steps);
				int x = baseX + Mth.floor(Math.cos(angle) * ring * 1.6);
				int z = baseZ + Mth.floor(Math.sin(angle) * ring * 1.6);
				BlockPos feet = ArrivalLanding.playerFeet(world, x, z);
				if (columnOpen(world, feet, body)) {
					return Vec3.atBottomCenterOf(feet);
				}
			}
		}
		return playerLanding.add(1.4 + slot * 0.6, 0.0, 0.4);
	}

	private static boolean columnOpen(ServerLevel world, BlockPos feet, int bodyBlocks) {
		if (feet.getY() <= world.getMinY() || feet.getY() + bodyBlocks >= world.getMaxY()) {
			return false;
		}
		for (int up = 0; up < bodyBlocks; up++) {
			BlockPos pos = feet.above(up);
			BlockState state = world.getBlockState(pos);
			if (!state.canBeReplaced() || !state.getFluidState().isEmpty()) {
				return false;
			}
		}
		BlockPos floor = feet.below();
		BlockState floorState = world.getBlockState(floor);
		return floorState.isFaceSturdy(world, floor, net.minecraft.core.Direction.UP)
			|| floorState.isCollisionShapeFullBlock(world, floor);
	}

	private static void spawnBurst(ServerLevel world, Vec3 at) {
		world.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0, at.z, 18, 0.35, 0.6, 0.35, 0.02);
		world.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 0.5, at.z, 24, 0.4, 0.5, 0.4, 0.4);
	}
}
