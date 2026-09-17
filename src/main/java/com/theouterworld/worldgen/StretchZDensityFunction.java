package com.theouterworld.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Evaluates another density function at (x, y, z / stretch) so isotropic hills
 * become observably elongated along the north–south (Z) axis.
 */
public record StretchZDensityFunction(DensityFunction argument, double stretch) implements DensityFunction {
	private static final MapCodec<StretchZDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			DensityFunction.CODEC.fieldOf("argument").forGetter(StretchZDensityFunction::argument),
			Codec.doubleRange(1.01, 16.0).fieldOf("stretch").forGetter(StretchZDensityFunction::stretch)
		).apply(instance, StretchZDensityFunction::new)
	);

	public static final KeyDispatchDataCodec<StretchZDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

	@Override
	public double compute(FunctionContext context) {
		int z = (int) Math.round(context.blockZ() / stretch);
		return argument.compute(new SinglePointContext(context.blockX(), context.blockY(), z));
	}

	@Override
	public void fillArray(double[] array, ContextProvider provider) {
		provider.fillAllDirectly(array, this);
	}

	@Override
	public DensityFunction mapChildren(Visitor visitor) {
		return new StretchZDensityFunction(visitor.apply(argument), stretch);
	}

	@Override
	public double minValue() {
		return argument.minValue();
	}

	@Override
	public double maxValue() {
		return argument.maxValue();
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		return CODEC;
	}
}
