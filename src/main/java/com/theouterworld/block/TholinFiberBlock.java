package com.theouterworld.block;

import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.entity.ai.WeaverColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MangroveRootsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockState result = super.playerWillDestroy(level, pos, state, player);
		WeaverColonies.noteBroken(level, pos, player, false);
		return result;
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
		return neighborState.is(this) && direction.getAxis() == Direction.Axis.Y;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext entities
			&& entities.getEntity() instanceof WeaverEntity weaver
			&& passesFiber(weaver, pos)) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	/**
	 * The night return falls through fibre, including fibre above the home plate,
	 * until the plate itself catches them. Any other leap, and the walk to the pad,
	 * still stands on the fibre under their feet.
	 */
	private static boolean passesFiber(WeaverEntity weaver, BlockPos pos) {
		if (weaver.isHomePlateLeap()) {
			return true;
		}
		if (pos.getY() < Mth.floor(weaver.getY())) {
			return false;
		}
		// Same rule the pathfinder uses: a Weaver already inside fibre may walk on through it.
		return weaver.phasesThroughFiber()
			|| weaver.isDeckBound()
			|| weaver.isEmbeddedInFiber()
			|| bodyInside(weaver, pos);
	}

	private static boolean bodyInside(WeaverEntity weaver, BlockPos pos) {
		AABB body = weaver.getBoundingBox().inflate(-0.08);
		return body.intersects(new AABB(pos));
	}
}
