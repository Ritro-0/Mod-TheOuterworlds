package com.theouterworld.block;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MangroveRootsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Woven Titan fibre the Weavers build their Anchors out of. Behaves like mangrove roots. */
public class TholinFiberBlock extends MangroveRootsBlock {
	public TholinFiberBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
		return neighborState.is(this) && direction.getAxis() == Direction.Axis.Y;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext entities
			&& entities.getEntity() instanceof WeaverEntity weaver
			&& (weaver.phasesThroughFiber() || blocksTheBody(weaver, level, pos))) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	/**
	 * Fibre from the feet upward that is in the body, not a floor and not a step
	 * they can climb. The block under the feet stays solid.
	 */
	private static boolean blocksTheBody(WeaverEntity weaver, BlockGetter level, BlockPos pos) {
		int feet = Mth.floor(weaver.getY());
		if (pos.getY() < feet) {
			return false;
		}
		double rise = pos.getY() + 1.0 - weaver.getY();
		if (rise <= weaver.maxUpStep() + 0.05 && clearAbove(level, pos)) {
			return false;
		}
		return true;
	}

	private static boolean clearAbove(BlockGetter level, BlockPos pos) {
		for (int up = 1; up <= 3; up++) {
			BlockPos above = pos.above(up);
			BlockState state = level.getBlockState(above);
			if (state.getBlock() instanceof TholinFiberBlock || !state.getCollisionShape(level, above).isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
