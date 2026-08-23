package com.theouterworld.fluid;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.item.ModItems;
import com.theouterworld.registry.ModFluids;
import com.theouterworld.world.DimensionClimate;
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
 * Dense metallic liquid: travels as far as Overworld lava, at water speed.
 */
public abstract class MercuryFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return ModFluids.FLOWING_MERCURY;
	}

	@Override
	public Fluid getSource() {
		return ModFluids.MERCURY;
	}

	@Override
	public Item getBucket() {
		return ModItems.MERCURY_BUCKET;
	}

	@Override
	public void animateTick(Level level, BlockPos pos, FluidState fluidState, RandomSource random) {
		if (!fluidState.isSource() && !fluidState.getValue(FALLING) && random.nextInt(64) == 0) {
			level.playLocalSound(
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				SoundEvents.HONEY_BLOCK_SLIDE,
				SoundSource.AMBIENT,
				random.nextFloat() * 0.2F + 0.15F,
				0.6F + random.nextFloat() * 0.3F,
				false
			);
		}
	}

	@Override
	public @Nullable ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_HONEY;
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
		return 2;
	}

	@Override
	public BlockState createLegacyBlock(FluidState fluidState) {
		return ModBlocks.MERCURY.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
	}

	@Override
	public boolean isSame(Fluid other) {
		return other == ModFluids.MERCURY || other == ModFluids.FLOWING_MERCURY;
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 2;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}

	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}

	@Override
	public void randomTick(ServerLevel level, BlockPos pos, FluidState fluidState, RandomSource random) {
		if (DimensionClimate.tryConvertMercuryToCinnabar(level, pos)) {
			return;
		}
		if (!DimensionClimate.tryFreezeMercury(level, pos, fluidState)
			&& DimensionClimate.shouldSolidifyMercury(level, pos)) {
			level.scheduleTick(pos, this, 20);
		}
	}

	@Override
	public void tick(ServerLevel level, BlockPos pos, BlockState blockState, FluidState fluidState) {
		if (DimensionClimate.tryConvertMercuryToCinnabar(level, pos)) {
			return;
		}
		if (DimensionClimate.tryFreezeMercury(level, pos, fluidState)) {
			return;
		}
		if (DimensionClimate.shouldSolidifyMercury(level, pos)) {
			level.scheduleTick(pos, this, 20);
		}
		super.tick(level, pos, blockState, fluidState);
	}

	@Override
	protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState state, Direction direction, FluidState target) {
		if (DimensionClimate.touchesSulfur(level, pos)) {
			if (!state.isAir() && state.getFluidState().isEmpty()) {
				this.beforeDestroyingBlock(level, pos, state);
			}
			DimensionClimate.tryConvertMercuryToCinnabar(level, pos);
			return;
		}
		super.spreadTo(level, pos, state, direction, target);
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
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	public static class Flowing extends MercuryFluid {
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

	public static class Source extends MercuryFluid {
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
