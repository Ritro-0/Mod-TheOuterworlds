package com.theouterworld.fluid;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModFluids;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Solar plasma — looks and registers as a fluid, but vanilla spread is disabled.
 * Surface pulsing is handled by {@link com.theouterworld.block.SolarPlasmaLiquidBlock}.
 */
public abstract class SolarPlasmaFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return ModFluids.FLOWING_SOLAR_PLASMA;
	}

	@Override
	public Fluid getSource() {
		return ModFluids.SOLAR_PLASMA;
	}

	@Override
	public Item getBucket() {
		return Items.AIR;
	}

	@Override
	public void animateTick(Level level, BlockPos pos, FluidState fluidState, RandomSource random) {
		boolean surface = level.getBlockState(pos.above()).getFluidState().isEmpty();
		if (surface && random.nextInt(40) == 0) {
			level.playLocalSound(
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				SoundEvents.LAVA_AMBIENT,
				SoundSource.BLOCKS,
				0.35F + random.nextFloat() * 0.2F,
				0.4F + random.nextFloat() * 0.3F,
				false
			);
		}
		if (surface && random.nextInt(12) == 0) {
			level.addParticle(
				ParticleTypes.FLAME,
				pos.getX() + random.nextDouble(),
				pos.getY() + 0.95,
				pos.getZ() + random.nextDouble(),
				0.0,
				0.04,
				0.0
			);
		}
	}

	@Override
	public @Nullable ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_LAVA;
	}

	@Override
	protected boolean canConvertToSource(ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		Block.dropResources(state, level, pos, blockEntity);
	}

	@Override
	public int getSlopeFindDistance(LevelReader level) {
		return 0;
	}

	@Override
	public BlockState createLegacyBlock(FluidState fluidState) {
		return ModBlocks.SOLAR_PLASMA.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
	}

	@Override
	public boolean isSame(Fluid other) {
		return other == ModFluids.SOLAR_PLASMA || other == ModFluids.FLOWING_SOLAR_PLASMA;
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 8;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}

	@Override
	protected boolean isRandomlyTicking() {
		return false;
	}

	/** Disable vanilla fluid spread — pulse animation owns placement. */
	@Override
	protected void spread(ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
		return direction == Direction.DOWN && !this.isSame(other);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.empty();
	}

	public static class Flowing extends SolarPlasmaFluid {
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState fluidState) {
			return fluidState.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState fluidState) {
			return false;
		}
	}

	public static class Source extends SolarPlasmaFluid {
		@Override
		public int getAmount(FluidState fluidState) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState fluidState) {
			return true;
		}
	}
}
