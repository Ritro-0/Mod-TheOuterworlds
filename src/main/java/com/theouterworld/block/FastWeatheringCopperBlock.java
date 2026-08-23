package com.theouterworld.block;

import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;

public class FastWeatheringCopperBlock extends WeatheringCopperFullBlock {
	public FastWeatheringCopperBlock(WeatheringCopper.WeatherState weatherState, Properties properties) {
		super(weatherState, properties);
	}

	@Override
	public float getChanceModifier() {
		return (this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75f : 1.0f) * 4.0f;
	}
}
