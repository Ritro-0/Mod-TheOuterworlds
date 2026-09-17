package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizableLanternBlock extends LanternBlock implements WeatheringCopper {
	public static final MapCodec<OxidizableLanternBlock> CODEC = simpleCodec(OxidizableLanternBlock::new);

	private final WeatheringCopper.WeatherState degradationLevel;
	private final OxidizableLanternAging.Kind kind;
	private final boolean outerworldOnly;

	public OxidizableLanternBlock(Properties properties) {
		this(WeatheringCopper.WeatherState.UNAFFECTED, OxidizableLanternAging.Kind.IRON, true, properties);
	}

	public OxidizableLanternBlock(
		WeatheringCopper.WeatherState degradationLevel,
		OxidizableLanternAging.Kind kind,
		boolean outerworldOnly,
		Properties properties
	) {
		super(properties);
		this.degradationLevel = degradationLevel;
		this.kind = kind;
		this.outerworldOnly = outerworldOnly;
	}

	@Override
	public MapCodec<? extends LanternBlock> codec() {
		return CODEC;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		OxidizableLanternAging.randomTick(this, this.kind, this.outerworldOnly, state, world, pos, random);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return WeatheringCopper.getNext(state.getBlock()).isPresent();
	}

	@Override
	public WeatheringCopper.WeatherState getAge() {
		return this.degradationLevel;
	}

	@Override
	public Optional<BlockState> getNext(BlockState state) {
		return WeatheringCopper.getNext(state.getBlock()).map(block -> block.withPropertiesOf(state));
	}

	@Override
	public float getChanceModifier() {
		return this.kind == OxidizableLanternAging.Kind.COPPER
			? OxidizableLanternAging.copperChanceModifier(this)
			: WeatheringCopper.super.getChanceModifier();
	}
}
