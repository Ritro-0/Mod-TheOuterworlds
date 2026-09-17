package com.theouterworld.fluid;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.item.ModItems;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Glowing metallic hydrogen: does not mix with liquid hydrogen, not bucketable.
 */
public abstract class MetallicHydrogenFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return ModFluids.FLOWING_METALLIC_HYDROGEN;
	}

	@Override
	public Fluid getSource() {
		return ModFluids.METALLIC_HYDROGEN;
	}

	@Override
	public Item getBucket() {
		return ModItems.METALLIC_HYDROGEN;
	}

	@Override
	public void animateTick(Level level, BlockPos pos, FluidState fluidState, RandomSource random) {
		if (fluidState.isSource() && random.nextInt(40) == 0) {
			level.addParticle(
				ParticleTypes.GLOW,
				pos.getX() + random.nextDouble(),
				pos.getY() + random.nextDouble(),
				pos.getZ() + random.nextDouble(),
				0.0,
				0.0,
				0.0
			);
		}
		if (fluidState.isSource() && random.nextInt(120) == 0) {
			level.playLocalSound(
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				SoundEvents.GLOW_SQUID_AMBIENT,
				SoundSource.BLOCKS,
				0.15F + random.nextFloat() * 0.1F,
				0.8F + random.nextFloat() * 0.4F,
				false
			);
		}
	}

	@Override
	public @Nullable ParticleOptions getDripParticle() {
		return ParticleTypes.GLOW;
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
		// Deposits use a normal block now; keep this for any leftover fluid references.
		return ModBlocks.METALLIC_HYDROGEN.defaultBlockState();
	}

	@Override
	public boolean isSame(Fluid other) {
		return other == ModFluids.METALLIC_HYDROGEN || other == ModFluids.FLOWING_METALLIC_HYDROGEN;
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 2;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 10;
	}

	@Override
	protected boolean isRandomlyTicking() {
		return false;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
		// Never mix with liquid hydrogen; otherwise only downward replacement by foreign fluids.
		if (other == ModFluids.LIQUID_HYDROGEN || other == ModFluids.FLOWING_LIQUID_HYDROGEN) {
			return false;
		}
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

	public static class Flowing extends MetallicHydrogenFluid {
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

	public static class Source extends MetallicHydrogenFluid {
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
