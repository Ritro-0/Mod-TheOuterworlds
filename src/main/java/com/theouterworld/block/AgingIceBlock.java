package com.theouterworld.block;

import com.theouterworld.registry.ModDimensions;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Ice that ages on a short timer in the Outerworld and Innerworld:
 * ice → packed ice → blue ice, about 3–8 seconds per stage.
 */
public class AgingIceBlock extends Block {
	private static final int MIN_AGE_TICKS = 60;
	private static final int MAX_AGE_TICKS = 160;

	@Nullable
	private Supplier<Block> nextStage;

	public AgingIceBlock(Properties properties) {
		super(properties);
	}

	public void setNextStage(Block nextStage) {
		this.nextStage = () -> nextStage;
	}

	private boolean canAge(Level level) {
		return this.nextStage != null && this.nextStage.get() != null && ModDimensions.isColdClimate(level.dimension());
	}

	private static int agingDelay(RandomSource random) {
		return MIN_AGE_TICKS + random.nextInt(MAX_AGE_TICKS - MIN_AGE_TICKS + 1);
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.is(state.getBlock()) && world instanceof ServerLevel serverWorld && canAge(serverWorld)) {
			serverWorld.scheduleTick(pos, this, agingDelay(serverWorld.getRandom()));
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (!canAge(world) || !world.getBlockState(pos).is(this)) {
			return;
		}
		world.setBlock(pos, this.nextStage.get().defaultBlockState(), Block.UPDATE_ALL);
	}
}
