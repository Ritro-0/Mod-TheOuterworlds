package com.theouterworld.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class OxidizableLanternAging {
	public enum Kind {
		IRON,
		COPPER
	}

	private OxidizableLanternAging() {
	}

	public static void randomTick(
		WeatheringCopper weathering,
		Kind kind,
		boolean outerworldOnly,
		BlockState state,
		ServerLevel world,
		BlockPos pos,
		RandomSource random
	) {
		if (outerworldOnly && !OxidizableIronBehavior.shouldOxidize(world, pos)) {
			return;
		}
		if (kind == Kind.COPPER) {
			weathering.changeOverTime(state, world, pos, random);
			return;
		}
		Optional<BlockState> nextState = weathering.getNext(state);
		if (nextState.isEmpty()) {
			return;
		}
		float chance = OxidizableIronBehavior.getOxidationChance(weathering.getAge(), 0, state.getBlock());
		if (random.nextFloat() < chance) {
			world.setBlockAndUpdate(pos, nextState.get());
		}
	}

	public static float copperChanceModifier(WeatheringCopper weathering) {
		float base = weathering.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75f : 1.0f;
		if (weathering instanceof Block block && Corrosion.isCorroded(block)) {
			return base * 4.0f;
		}
		return base;
	}
}
