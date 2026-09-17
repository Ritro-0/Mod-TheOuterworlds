package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.world.BeaconConcentratorPortals;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BeaconConcentratorBlockEntity extends BlockEntity {
	private static final double BEAM_RADIUS = 1.0 / 16.0;
	private static final double LAUNCH_SPEED = 1.6;
	private static final double DEFAULT_TARGET_Y = 400.0;
	private static final double HIGH_ENTRY_EXTRA_Y = 50.0;
	private static final int TELEPORT_COOLDOWN_TICKS = 100;
	private static final int CHARGE_TICKS = 60;
	private static final Map<Player, BeamRide> RIDES = new WeakHashMap<>();
	private static final Map<Entity, Long> COOLDOWNS = new WeakHashMap<>();

	private static final ParticleOptions[] CHARGE_PARTICLES = {
		ParticleTypes.END_ROD,
		ParticleTypes.ENCHANT,
		ParticleTypes.WITCH,
		ParticleTypes.ELECTRIC_SPARK,
		ParticleTypes.GLOW,
		ParticleTypes.REVERSE_PORTAL,
		ParticleTypes.FIREWORK
	};

	private int chargeTicks;

	public BeaconConcentratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BEACON_CONCENTRATOR, pos, state);
	}

	public boolean isActive() {
		BlockState state = getBlockState();
		if (state.is(ModBlocks.CONCENTRATED_BEACON_BEAM)) {
			return true;
		}
		return state.hasProperty(BeaconConcentratorBlock.ACTIVE) && state.getValue(BeaconConcentratorBlock.ACTIVE);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, BeaconConcentratorBlockEntity entity) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}

		boolean returnBeam = state.is(ModBlocks.CONCENTRATED_BEACON_BEAM);
		if (returnBeam) {
			if (!ModDimensions.isMoon(serverWorld.dimension())) {
				return;
			}
			launchAndTeleport(serverWorld, pos, true);
			return;
		}

		boolean wasActive = state.getValue(BeaconConcentratorBlock.ACTIVE);
		boolean canActivate = BeaconConcentratorBlock.canActivate(serverWorld, pos);
		if (!canActivate) {
			entity.chargeTicks = 0;
			if (wasActive) {
				serverWorld.setBlock(pos, state.setValue(BeaconConcentratorBlock.ACTIVE, false), Block.UPDATE_CLIENTS);
				BeaconConcentratorPortals.deactivate(serverWorld, pos);
			}
			return;
		}

		if (!wasActive) {
			entity.tickCharging(serverWorld, pos);
			if (entity.chargeTicks < CHARGE_TICKS) {
				return;
			}
			serverWorld.setBlock(pos, state.setValue(BeaconConcentratorBlock.ACTIVE, true), Block.UPDATE_CLIENTS);
			BeaconConcentratorPortals.activate(serverWorld, pos);
			serverWorld.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.15F);
		}

		launchAndTeleport(serverWorld, pos, false);
	}

	private void tickCharging(ServerLevel world, BlockPos pos) {
		if (chargeTicks == 0) {
			world.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.7F, 0.65F);
		}
		chargeTicks++;
		setChanged();

		RandomSource random = world.getRandom();
		double cx = pos.getX() + 0.5;
		double cz = pos.getZ() + 0.5;
		float progress = chargeTicks / (float) CHARGE_TICKS;
		double radius = 1.25 * (1.0 - progress * 0.85);
		double height = 0.35 + progress * 2.4;
		double angle = chargeTicks * 0.62;

		for (int arm = 0; arm < 4; arm++) {
			double a = angle + arm * (Math.PI * 0.5);
			double px = cx + Math.cos(a) * radius;
			double pz = cz + Math.sin(a) * radius;
			double py = pos.getY() + height + Math.sin(angle * 2.0 + arm) * 0.1;
			ParticleOptions particle = CHARGE_PARTICLES[random.nextInt(CHARGE_PARTICLES.length)];
			world.sendParticles(particle, px, py, pz, 0, -Math.sin(a) * 0.08, 0.05, Math.cos(a) * 0.08, 1.0);
		}

		int rising = 4 + Mth.floor(progress * 8);
		for (int i = 0; i < rising; i++) {
			ParticleOptions particle = CHARGE_PARTICLES[random.nextInt(CHARGE_PARTICLES.length)];
			double ox = (random.nextDouble() - 0.5) * 0.22;
			double oz = (random.nextDouble() - 0.5) * 0.22;
			world.sendParticles(particle, cx + ox, pos.getY() + 0.2, cz + oz, 0, 0.0, 0.55 + progress * 1.4, 0.0, 1.0);
		}

		if (chargeTicks % 8 == 0) {
			world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.45F, 0.7F + progress * 0.6F);
		}
		if (chargeTicks % 12 == 0) {
			world.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.25F, 0.8F + progress * 0.5F);
		}
	}

	private static void launchAndTeleport(ServerLevel world, BlockPos pos, boolean returnBeam) {
		AABB beam = beamBox(pos, world);
		long portalKey = pos.asLong();
		RIDES.entrySet().removeIf(entry ->
			entry.getValue().portalKey == portalKey && !entry.getKey().getBoundingBox().intersects(beam)
		);
		List<Player> riders = new ArrayList<>(world.getEntitiesOfClass(Player.class, beam, p -> !p.isSpectator()));
		for (Player player : riders) {
			if (!player.isAlive() || player.isRemoved() || !player.getBoundingBox().intersects(beam)) {
				continue;
			}
			if (isOnCooldown(player, world)) {
				continue;
			}

			BeamRide ride = RIDES.get(player);
			if (ride == null || ride.portalKey != portalKey) {
				double entryY = player.getY();
				boolean highEntry = entryY >= world.getMaxY();
				ride = new BeamRide(portalKey, entryY, highEntry);
				RIDES.put(player, ride);
			}

			Vec3 velocity = player.getDeltaMovement();
			player.setDeltaMovement(velocity.x * 0.55, Math.max(velocity.y, LAUNCH_SPEED), velocity.z * 0.55);
			player.resetFallDistance();
			player.hurtMarked = true;

			double targetY = ride.highEntry ? ride.entryY + HIGH_ENTRY_EXTRA_Y : DEFAULT_TARGET_Y;
			if (player.getY() >= targetY) {
				RIDES.remove(player);
				setCooldown(player, world);
				BlockPos portal = pos.immutable();
				world.getServer().execute(() -> teleportPlayer(player, world, portal, returnBeam));
			}
		}
	}

	private static AABB beamBox(BlockPos pos, ServerLevel world) {
		double minY = pos.getY();
		double maxY = Math.max(world.getMaxY() + HIGH_ENTRY_EXTRA_Y + 16.0, DEFAULT_TARGET_Y + 16.0);
		return new AABB(
			pos.getX() + 0.5 - BEAM_RADIUS,
			minY,
			pos.getZ() + 0.5 - BEAM_RADIUS,
			pos.getX() + 0.5 + BEAM_RADIUS,
			maxY,
			pos.getZ() + 0.5 + BEAM_RADIUS
		);
	}

	private static void teleportPlayer(Player player, ServerLevel source, BlockPos portalPos, boolean returnBeam) {
		ResourceKey<Level> destinationKey = returnBeam ? Level.OVERWORLD : ModDimensions.MOON_WORLD_KEY;
		if (!returnBeam && !source.dimension().equals(Level.OVERWORLD)) {
			return;
		}
		if (returnBeam && !ModDimensions.isMoon(source.dimension())) {
			return;
		}

		if (!player.isAlive() || player.isRemoved()) {
			return;
		}

		var server = source.getServer();
		ServerLevel target = server.getLevel(destinationKey);
		if (target == null) {
			return;
		}

		Vec3 destination = BeaconConcentratorPortals.prepareArrival(
			server,
			portalPos.getX(),
			portalPos.getZ(),
			!returnBeam
		);
		double minY = target.getMinY() + 1.0;
		double maxY = target.getMaxY() - 1.0;
		if (destination.y < minY || destination.y > maxY) {
			destination = new Vec3(destination.x, Mth.clamp(destination.y, minY, maxY), destination.z);
		}

		setCooldown(player, target);
		Entity teleported = player.teleport(new TeleportTransition(
			target,
			destination,
			Vec3.ZERO,
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		if (teleported != null) {
			teleported.resetFallDistance();
			setCooldown(teleported, target);
			RIDES.remove(player);
			if (teleported instanceof Player teleportedPlayer) {
				RIDES.remove(teleportedPlayer);
			}
		}
		target.playSound(null, BlockPos.containing(destination), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.4F, 1.0F);
	}

	private static boolean isOnCooldown(Entity entity, ServerLevel world) {
		Long readyAt = COOLDOWNS.get(entity);
		return readyAt != null && world.getGameTime() < readyAt;
	}

	private static void setCooldown(Entity entity, ServerLevel world) {
		COOLDOWNS.put(entity, world.getGameTime() + TELEPORT_COOLDOWN_TICKS);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		output.putInt("ChargeTicks", chargeTicks);
		super.saveAdditional(output);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.chargeTicks = Math.max(0, input.getIntOr("ChargeTicks", 0));
	}

	private record BeamRide(long portalKey, double entryY, boolean highEntry) {
	}
}
