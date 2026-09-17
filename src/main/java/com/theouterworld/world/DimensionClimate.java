package com.theouterworld.world;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Instant climate conversions for low-gravity dimensions.
 * Cold climates (Outerworld, Moon): water freezes, portals freeze, fire snuffs.
 * Hot climate (Innerworld / Mercury, Highworld / Jupiter): ice, snow, and water flash-boil to smoke; fire snuffs
 * without spawning frozen portals.
 * Scorching climate (Nearworld / Venus, Emberworld / Io): ice, snow, and water flash-boil; fire and lava stay active.
 */
public final class DimensionClimate {
	public static final int MERCURY_LIQUID_MAX_Y = 12;
	public static final int MERCURY_MELT_LIGHT = 8;

	private DimensionClimate() {
	}

	public static boolean shouldSolidifyMercury(Level level, BlockPos pos) {
		if (level == null || pos == null) {
			return false;
		}
		if (ModDimensions.isMoon(level.dimension())
			|| ModDimensions.isWanderlands(level.dimension())
			|| ModDimensions.isBeyondlands(level.dimension())
			|| ModDimensions.isBeyondlandsIi(level.dimension())
			|| ModDimensions.isSpinlands(level.dimension())
			|| ModDimensions.isScarletlands(level.dimension())
			|| ModDimensions.isLonelands(level.dimension())) {
			return true;
		}
		return ModDimensions.isOuterworld(level.dimension()) && pos.getY() > MERCURY_LIQUID_MAX_Y;
	}

	public static boolean isNearMercuryMeltLight(Level level, BlockPos pos) {
		if (level.getBrightness(LightLayer.BLOCK, pos) >= MERCURY_MELT_LIGHT) {
			return true;
		}
		for (Direction direction : Direction.values()) {
			if (level.getBrightness(LightLayer.BLOCK, pos.relative(direction)) >= MERCURY_MELT_LIGHT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Freezes mercury back into a block when it is in a solidify zone and no longer lit.
	 * Flowing mercury in that zone disappears instead of becoming extra blocks.
	 * @return true if the fluid was replaced
	 */
	public static boolean tryFreezeMercury(ServerLevel level, BlockPos pos, FluidState fluidState) {
		if (!shouldSolidifyMercury(level, pos) || isNearMercuryMeltLight(level, pos)) {
			return false;
		}
		if (fluidState.isSource()) {
			level.setBlock(pos, ModBlocks.MERCURY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		} else {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
		return true;
	}

	/**
	 * The mercury cell touching sulfur (or a sulfur variant) becomes cinnabar.
	 * Same idea as lava turning into cobblestone/obsidian when it meets water.
	 */
	public static boolean tryConvertMercuryToCinnabar(LevelAccessor level, BlockPos pos) {
		if (level instanceof Level world && world.isClientSide()) {
			return false;
		}
		if (!touchesSulfur(level, pos)) {
			return false;
		}
		level.setBlock(pos, Blocks.CINNABAR.defaultBlockState(), Block.UPDATE_ALL);
		level.levelEvent(1501, pos, 0);
		return true;
	}

	public static boolean touchesSulfur(LevelAccessor level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (level.getBlockState(pos.relative(direction)).is(ModTags.CONVERTS_MERCURY_TO_CINNABAR)) {
				return true;
			}
		}
		return false;
	}

	public static BlockState convert(Level level, BlockPos pos, BlockState state) {
		if (level == null || !ModDimensions.isLowGravity(level.dimension())) {
			return state;
		}

		if (ModDimensions.isScorchingClimate(level.dimension())) {
			return convertScorching(level, pos, state);
		}
		if (ModDimensions.isHotClimate(level.dimension())) {
			return convertHot(level, pos, state);
		}
		if (ModDimensions.isColdClimate(level.dimension())) {
			return convertCold(level, pos, state);
		}
		if (ModDimensions.isFrostworld(level.dimension()) || ModDimensions.isSpongeworld(level.dimension())) {
			return convertIcyVacuum(level, pos, state);
		}
		return state;
	}

	/** Nearworld: boil water/ice/snow, keep fire lit, never form portals.
	 * Emberworld also snuffs torches/lanterns in the vacuum. */
	private static BlockState convertScorching(Level level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (block instanceof LiquidBlock && state.getFluidState().is(Fluids.WATER)) {
			return flashMelt(level, pos);
		}
		if (isHotClimateVolatile(block)) {
			return flashMelt(level, pos);
		}
		if (block == Blocks.NETHER_PORTAL) {
			return Blocks.AIR.defaultBlockState();
		}
		if (ModDimensions.isEmberworld(level.dimension())) {
			if (block instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
				extinguishSound(level, pos);
				return state.setValue(CampfireBlock.LIT, false);
			}
			if (block instanceof AbstractCandleBlock && state.hasProperty(AbstractCandleBlock.LIT) && state.getValue(AbstractCandleBlock.LIT)) {
				extinguishSound(level, pos);
				return state.setValue(AbstractCandleBlock.LIT, false);
			}
			return ColdDimensionLights.convert(state);
		}
		return state;
	}

	private static BlockState convertHot(Level level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (block instanceof LiquidBlock && state.getFluidState().is(Fluids.WATER)) {
			return flashMelt(level, pos);
		}
		if (isHotClimateVolatile(block)) {
			return flashMelt(level, pos);
		}
		if (block instanceof BaseFireBlock) {
			extinguishSound(level, pos);
			return Blocks.AIR.defaultBlockState();
		}
		if (block instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
			extinguishSound(level, pos);
			return state.setValue(CampfireBlock.LIT, false);
		}
		if (block instanceof AbstractCandleBlock && state.hasProperty(AbstractCandleBlock.LIT) && state.getValue(AbstractCandleBlock.LIT)) {
			extinguishSound(level, pos);
			return state.setValue(AbstractCandleBlock.LIT, false);
		}
		return ColdDimensionLights.convert(state);
	}

	private static BlockState convertCold(Level level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (block instanceof LiquidBlock && state.getFluidState().is(Fluids.WATER)) {
			return ModBlocks.ICE.defaultBlockState();
		}
		if (block instanceof LiquidBlock && state.getFluidState().is(Fluids.LAVA)) {
			return Blocks.OBSIDIAN.defaultBlockState();
		}
		if (block == Blocks.ICE) {
			return ModBlocks.ICE.defaultBlockState();
		}
		if (block == Blocks.PACKED_ICE) {
			return ModBlocks.PACKED_ICE.defaultBlockState();
		}
		if (block == Blocks.BLUE_ICE) {
			return ModBlocks.BLUE_ICE.defaultBlockState();
		}
		if (block == Blocks.NETHER_PORTAL) {
			return ModBlocks.FROZEN_NETHER_PORTAL.withPropertiesOf(state);
		}
		if (block == Blocks.MAGMA_BLOCK) {
			return ModBlocks.FROZEN_MAGMA.defaultBlockState();
		}
		if (block instanceof BaseFireBlock) {
			return extinguishFire(level, pos);
		}
		if (block instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
			extinguishSound(level, pos);
			return state.setValue(CampfireBlock.LIT, false);
		}
		if (block instanceof AbstractCandleBlock && state.hasProperty(AbstractCandleBlock.LIT) && state.getValue(AbstractCandleBlock.LIT)) {
			extinguishSound(level, pos);
			return state.setValue(AbstractCandleBlock.LIT, false);
		}
		if (block == Blocks.FIREFLY_BUSH) {
			return ModBlocks.FROZEN_FIREFLY_BUSH.defaultBlockState();
		}
		return ColdDimensionLights.convert(state);
	}

	/**
	 * Frostworld (Europa): vacuum cold effects without freezing the subsurface ocean.
	 * Torches/lanterns fail, candles/campfires snuff, lava→obsidian, magma freezes, portals freeze.
	 */
	private static BlockState convertIcyVacuum(Level level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (block instanceof LiquidBlock && state.getFluidState().is(Fluids.LAVA)) {
			return Blocks.OBSIDIAN.defaultBlockState();
		}
		if (block == Blocks.NETHER_PORTAL) {
			return ModBlocks.FROZEN_NETHER_PORTAL.withPropertiesOf(state);
		}
		if (block == Blocks.MAGMA_BLOCK) {
			return ModBlocks.FROZEN_MAGMA.defaultBlockState();
		}
		if (block instanceof BaseFireBlock) {
			return extinguishFire(level, pos);
		}
		if (block instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
			extinguishSound(level, pos);
			return state.setValue(CampfireBlock.LIT, false);
		}
		if (block instanceof AbstractCandleBlock && state.hasProperty(AbstractCandleBlock.LIT) && state.getValue(AbstractCandleBlock.LIT)) {
			extinguishSound(level, pos);
			return state.setValue(AbstractCandleBlock.LIT, false);
		}
		return ColdDimensionLights.convert(state);
	}

	/** Ice and snow that flash-boil to smoke in Innerworld / Nearworld. */
	private static boolean isHotClimateVolatile(Block block) {
		return block == Blocks.ICE
			|| block == Blocks.PACKED_ICE
			|| block == Blocks.BLUE_ICE
			|| block == Blocks.FROSTED_ICE
			|| block == Blocks.SNOW
			|| block == Blocks.SNOW_BLOCK
			|| block == Blocks.POWDER_SNOW
			|| block == ModBlocks.ICE
			|| block == ModBlocks.PACKED_ICE
			|| block == ModBlocks.BLUE_ICE
			|| block == ModBlocks.DRY_ICE
			|| block == ModBlocks.CARBONIC_ICE
			|| block == ModBlocks.NITROGEN_ICE
			|| block == ModBlocks.METHANE_ICE;
	}

	private static BlockState flashMelt(Level level, BlockPos pos) {
		if (!level.isClientSide() && level instanceof ServerLevel server) {
			server.sendParticles(
				ParticleTypes.SMOKE,
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				8,
				0.25,
				0.25,
				0.25,
				0.02
			);
			server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.6F);
		}
		return Blocks.AIR.defaultBlockState();
	}

	private static BlockState extinguishFire(Level level, BlockPos pos) {
		if (trySpawnFrozenPortal(level, pos)) {
			BlockState filled = level.getBlockState(pos);
			if (filled.is(ModBlocks.FROZEN_NETHER_PORTAL)) {
				return filled;
			}
		}
		extinguishSound(level, pos);
		return Blocks.AIR.defaultBlockState();
	}

	/**
	 * Fills a valid frame with frozen portal blocks.
	 * Uses Vanilla Tweaks-style detection (any enclosed 2D shape, crying obsidian allowed)
	 * because vanilla only lights rectangular Overworld/Nether portals.
	 */
	public static boolean trySpawnFrozenPortal(Level level, BlockPos pos) {
		if (ModDimensions.isHotClimate(level.dimension()) || ModDimensions.isScorchingClimate(level.dimension())) {
			return false;
		}
		return CustomPortalFrame.tryFill(level, pos);
	}

	private static void extinguishSound(Level level, BlockPos pos) {
		if (!level.isClientSide()) {
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F);
		}
	}
}
