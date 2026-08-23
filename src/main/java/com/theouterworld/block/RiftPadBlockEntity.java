package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RiftPadBlockEntity extends BlockEntity {
	private static final int SPIRAL_TICKS = 50;
	private static final int BEAM_TICKS = 20;
	private static final int RIFT_OFFSET = 1;

	private static final int PHASE_IDLE = 0;
	private static final int PHASE_SPIRAL = 1;
	private static final int PHASE_BEAM = 2;
	private static final int PHASE_ACTIVE = 3;
	private static final int PHASE_CLOSING_BEAM = 4;
	private static final int PHASE_CLOSING_SPIRAL = 5;

	private static final ParticleOptions[] RITUAL_PARTICLES = {
		ParticleTypes.PORTAL,
		ParticleTypes.REVERSE_PORTAL,
		ParticleTypes.ENCHANT,
		ParticleTypes.WITCH,
		ParticleTypes.END_ROD,
		ParticleTypes.SOUL,
		ParticleTypes.SOUL_FIRE_FLAME,
		ParticleTypes.ELECTRIC_SPARK,
		ParticleTypes.GLOW,
		ParticleTypes.FIREWORK,
		ParticleTypes.CRIT,
		ParticleTypes.ENCHANTED_HIT,
		ParticleTypes.NAUTILUS,
		ParticleTypes.TOTEM_OF_UNDYING,
		ParticleTypes.WARPED_SPORE,
		ParticleTypes.CHERRY_LEAVES
	};

	private static final ParticleOptions[] BEAM_PARTICLES = {
		ParticleTypes.END_ROD,
		ParticleTypes.FIREWORK,
		ParticleTypes.TOTEM_OF_UNDYING,
		ParticleTypes.GLOW,
		ParticleTypes.ELECTRIC_SPARK,
		ParticleTypes.CRIT,
		ParticleTypes.ENCHANTED_HIT,
		ParticleTypes.SOUL_FIRE_FLAME,
		ParticleTypes.REVERSE_PORTAL,
		ParticleTypes.PORTAL
	};

	private int phase = PHASE_IDLE;
	private int phaseTicks = 0;

	public RiftPadBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RIFT_PAD, pos, state);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, RiftPadBlockEntity entity) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}

		boolean powered = serverWorld.hasNeighborSignal(pos);
		boolean openSky = serverWorld.canSeeSky(pos.above());
		boolean canRun = powered && openSky;

		if (state.getValue(RiftPadBlock.POWERED) != powered) {
			serverWorld.setBlock(pos, state.setValue(RiftPadBlock.POWERED, powered), Block.UPDATE_CLIENTS);
		}

		if (canRun) {
			if (entity.isClosing()) {
				entity.reverseClosingToOpening();
			}
			entity.tickOpening(serverWorld, pos);
			return;
		}

		if (!powered) {
			entity.tickClosing(serverWorld, pos);
			return;
		}

		if (entity.phase == PHASE_SPIRAL || entity.phase == PHASE_BEAM) {
			entity.resetRitual();
		}
	}

	private void tickOpening(ServerLevel world, BlockPos pos) {
		BlockPos existingRift = findRiftAbove(world, pos);
		if (existingRift != null && phase != PHASE_ACTIVE) {
			phase = PHASE_ACTIVE;
			phaseTicks = 0;
			setChanged();
		}

		switch (phase) {
			case PHASE_IDLE -> {
				phase = PHASE_SPIRAL;
				phaseTicks = 0;
				setChanged();
				world.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.85F, 0.7F);
			}
			case PHASE_SPIRAL -> tickSpiral(world, pos, false);
			case PHASE_BEAM -> tickBeam(world, pos, false);
			case PHASE_ACTIVE -> tickActive(world, pos, existingRift);
		}
	}

	private void tickClosing(ServerLevel world, BlockPos pos) {
		switch (phase) {
			case PHASE_ACTIVE -> beginClose(world, pos);
			case PHASE_SPIRAL -> {
				phase = PHASE_CLOSING_SPIRAL;
				phaseTicks = SPIRAL_TICKS - phaseTicks;
				setChanged();
				tickSpiral(world, pos, true);
			}
			case PHASE_BEAM -> {
				phase = PHASE_CLOSING_BEAM;
				phaseTicks = BEAM_TICKS - phaseTicks;
				setChanged();
				tickBeam(world, pos, true);
			}
			case PHASE_CLOSING_BEAM -> tickBeam(world, pos, true);
			case PHASE_CLOSING_SPIRAL -> tickSpiral(world, pos, true);
			default -> {
			}
		}
	}

	private void tickSpiral(ServerLevel world, BlockPos pos, boolean closing) {
		phaseTicks++;
		RandomSource random = world.getRandom();
		double cx = pos.getX() + 0.5;
		double cz = pos.getZ() + 0.5;
		float progress = phaseTicks / (float) SPIRAL_TICKS;
		if (closing) {
			progress = 1.0F - progress;
		}
		double radius = 1.15 * (1.0 - progress * 0.72);
		double height = 0.22 + progress * 1.85;
		double angle = (closing ? -phaseTicks : phaseTicks) * 0.55;

		for (int arm = 0; arm < 4; arm++) {
			double a = angle + arm * (Math.PI * 0.5);
			double px = cx + Math.cos(a) * radius;
			double pz = cz + Math.sin(a) * radius;
			double py = pos.getY() + height + Math.sin(angle * 2.0 + arm) * 0.08;
			ParticleOptions particle = RITUAL_PARTICLES[random.nextInt(RITUAL_PARTICLES.length)];
			double inward = closing ? 0.08 : -0.08;
			world.sendParticles(particle, px, py, pz, 0, Math.sin(a) * inward, closing ? -0.04 : 0.04, -Math.cos(a) * inward, 1.0);
		}

		if (phaseTicks % 8 == 0) {
			world.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.35F, 0.6F + (closing ? 1.0F - progress : progress));
		}

		if (phaseTicks >= SPIRAL_TICKS) {
			if (closing) {
				phase = PHASE_IDLE;
				phaseTicks = 0;
				setChanged();
				world.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.85F, 0.85F);
			} else {
				phase = PHASE_BEAM;
				phaseTicks = 0;
				setChanged();
				world.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 0.5F);
			}
		}
	}

	private void tickBeam(ServerLevel world, BlockPos pos, boolean closing) {
		phaseTicks++;
		RandomSource random = world.getRandom();
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 0.35;
		double cz = pos.getZ() + 0.5;

		for (int i = 0; i < 18; i++) {
			ParticleOptions particle = BEAM_PARTICLES[random.nextInt(BEAM_PARTICLES.length)];
			double ox = (random.nextDouble() - 0.5) * 0.18;
			double oz = (random.nextDouble() - 0.5) * 0.18;
			double speed = 1.85 + random.nextDouble() * 1.1;
			double spawnY = closing ? cy + 14.0 : cy;
			world.sendParticles(particle, cx + ox, spawnY, cz + oz, 0, 0.0, closing ? -speed : speed, 0.0, 1.0);
		}

		for (int column = 0; column < 10; column++) {
			ParticleOptions particle = BEAM_PARTICLES[random.nextInt(BEAM_PARTICLES.length)];
			int columnIndex = closing ? 9 - column : column;
			double y = cy + columnIndex * 1.4 + random.nextDouble() * 0.4;
			world.sendParticles(particle, cx, y, cz, 0, 0.0, closing ? -2.4 : 2.4, 0.0, 1.0);
		}

		if (phaseTicks >= BEAM_TICKS) {
			if (closing) {
				phase = PHASE_CLOSING_SPIRAL;
				phaseTicks = 0;
				setChanged();
				world.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.35F);
			} else {
				strikeAndOpenRift(world, pos);
			}
		}
	}

	private void tickActive(ServerLevel world, BlockPos pos, BlockPos riftPos) {
		if (riftPos == null) {
			phase = PHASE_IDLE;
			phaseTicks = 0;
			setChanged();
			return;
		}

		phaseTicks++;
		if (phaseTicks % 10 != 0) {
			return;
		}

		RandomSource random = world.getRandom();
		Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5);
		Vec3 end = new Vec3(riftPos.getX() + 0.5, riftPos.getY() + 0.35, riftPos.getZ() + 0.5);
		for (int i = 0; i < 2; i++) {
			Vec3 point = start.lerp(end, random.nextDouble());
			world.sendParticles(
				ParticleTypes.END_ROD,
				point.x,
				point.y,
				point.z,
				0,
				(random.nextDouble() - 0.5) * 0.02,
				0.01,
				(random.nextDouble() - 0.5) * 0.02,
				1.0
			);
		}

		if (phaseTicks % 40 == 0) {
			world.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.2F, 1.4F);
		}
	}

	private void strikeAndOpenRift(ServerLevel world, BlockPos pos) {
		BlockPos riftLower = findOpenRiftSpot(world, pos);
		strikeLightning(world, pos, riftLower);

		if (riftLower != null) {
			Direction facing = getPadFacing();
			BlockState rift = ModBlocks.RIFT.defaultBlockState()
				.setValue(RiftBlock.FACING, facing)
				.setValue(RiftBlock.HALF, DoubleBlockHalf.LOWER);
			world.setBlock(riftLower, rift, Block.UPDATE_ALL);
			world.setBlock(riftLower.above(), rift.setValue(RiftBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
			world.playSound(null, riftLower, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.85F, 1.15F);
		}

		phase = PHASE_ACTIVE;
		phaseTicks = 0;
		setChanged();
	}

	private void beginClose(ServerLevel world, BlockPos pos) {
		BlockPos riftLower = findRiftAbove(world, pos);
		strikeLightning(world, pos, riftLower);
		if (riftLower != null) {
			removeRift(world, riftLower);
			world.playSound(null, riftLower, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7F, 0.65F);
		} else {
			world.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.7F, 0.8F);
		}
		phase = PHASE_CLOSING_BEAM;
		phaseTicks = 0;
		setChanged();
	}

	private static void strikeLightning(ServerLevel world, BlockPos padPos, @Nullable BlockPos riftLower) {
		double strikeX = padPos.getX() + 0.5;
		double strikeZ = padPos.getZ() + 0.5;
		double strikeY = riftLower != null ? riftLower.getY() : padPos.getY() + RIFT_OFFSET;

		LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
		if (lightning != null) {
			lightning.snapTo(strikeX, strikeY, strikeZ);
			lightning.setVisualOnly(true);
			world.addFreshEntity(lightning);
		}
	}

	private static void removeRift(ServerLevel world, BlockPos riftLower) {
		BlockPos riftUpper = riftLower.above();
		if (world.getBlockState(riftUpper).is(ModBlocks.RIFT)) {
			world.setBlock(riftUpper, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		if (world.getBlockState(riftLower).is(ModBlocks.RIFT)) {
			world.setBlock(riftLower, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	private Direction getPadFacing() {
		BlockState state = getBlockState();
		if (state.hasProperty(RiftPadBlock.FACING)) {
			return state.getValue(RiftPadBlock.FACING);
		}
		return Direction.NORTH;
	}

	private boolean isClosing() {
		return phase == PHASE_CLOSING_BEAM || phase == PHASE_CLOSING_SPIRAL;
	}

	private void reverseClosingToOpening() {
		if (phase == PHASE_CLOSING_SPIRAL) {
			phase = PHASE_SPIRAL;
			phaseTicks = Math.max(0, SPIRAL_TICKS - phaseTicks);
		} else if (phase == PHASE_CLOSING_BEAM) {
			phase = PHASE_BEAM;
			phaseTicks = Math.max(0, BEAM_TICKS - phaseTicks);
		}
		setChanged();
	}

	private static BlockPos findOpenRiftSpot(ServerLevel world, BlockPos padPos) {
		BlockPos existing = findRiftAbove(world, padPos);
		if (existing != null) {
			return existing;
		}
		int maxY = Math.min(padPos.getY() + 8, world.getMaxY() - 1);
		for (int y = padPos.getY() + RIFT_OFFSET; y <= maxY; y++) {
			BlockPos lower = new BlockPos(padPos.getX(), y, padPos.getZ());
			BlockPos upper = lower.above();
			if (world.getBlockState(lower).canBeReplaced() && world.getBlockState(upper).canBeReplaced()) {
				return lower;
			}
		}
		return null;
	}

	private static BlockPos findRiftAbove(ServerLevel world, BlockPos padPos) {
		int maxY = Math.min(padPos.getY() + 10, world.getMaxY());
		for (int y = padPos.getY() + 1; y <= maxY; y++) {
			BlockPos check = new BlockPos(padPos.getX(), y, padPos.getZ());
			BlockState state = world.getBlockState(check);
			if (state.is(ModBlocks.RIFT) && state.getValue(RiftBlock.HALF) == DoubleBlockHalf.LOWER) {
				return check;
			}
		}
		return null;
	}

	private void resetRitual() {
		phase = PHASE_IDLE;
		phaseTicks = 0;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		output.putInt("Phase", phase);
		output.putInt("PhaseTicks", phaseTicks);
		super.saveAdditional(output);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.phase = Mth.clamp(input.getIntOr("Phase", PHASE_IDLE), PHASE_IDLE, PHASE_CLOSING_SPIRAL);
		this.phaseTicks = Math.max(0, input.getIntOr("PhaseTicks", 0));
	}
}
