package com.theouterworld.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * Evaluates another density function at (x, y, z / stretch) so isotropic hills
 * become observably elongated along the north–south (Z) axis.
 */
public record StretchZDensityFunction(DensityFunction argument, double stretch) implements DensityFunction {
	public static final MapCodec<StretchZDensityFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			DensityFunction.CODEC.fieldOf("argument").forGetter(StretchZDensityFunction::argument),
			Codec.doubleRange(1.01, 16.0).fieldOf("stretch").forGetter(StretchZDensityFunction::stretch)
		).apply(instance, StretchZDensityFunction::new)
	);

	@Override
	public DensitySampler compileSampler(CompileContext context) {
		DensitySampler input = argument.compileSampler(context);
		double stretchFactor = stretch;
		return new DensitySampler() {
			@Override
			public void sampleVolume(SamplerContext samplerContext, DensityBuffer outputBuffer, DensityVolume volume) {
				DensitySampler.sampleVolumeNaive(samplerContext, outputBuffer, volume, this);
			}

			@Override
			public float sampleValue(SamplerContext samplerContext, int blockX, int blockY, int blockZ) {
				int z = (int) Math.round(blockZ / stretchFactor);
				return input.sampleValue(samplerContext, blockX, blockY, z);
			}
		};
	}

	@Override
	public DensityFunction rewriteChildren(DfRewriteRule rule) {
		DensityFunction rewritten = rule.rewrite(argument);
		return rewritten == argument ? this : new StretchZDensityFunction(rewritten, stretch);
	}

	@Override
	public Interval range() {
		return argument.range();
	}

	@Override
	public @Axes int domainAxes() {
		return argument.domainAxes();
	}

	@Override
	public MapCodec<StretchZDensityFunction> codec() {
		return CODEC;
	}
}
