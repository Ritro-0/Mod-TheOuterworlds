package com.theouterworld.block;

import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FastWeatheringCopperStairsBlock extends WeatheringCopperStairBlock {
	public FastWeatheringCopperStairsBlock(WeatheringCopper.WeatherState weatherState, BlockState baseState, Properties properties) {
		super(weatherState, baseState, properties);
	}

	@Override
	public float getChanceModifier() {
		return (this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75f : 1.0f) * 4.0f;
	}
}
