package com.theouterworld.block;

import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.entity.ai.WeaverColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Weaver-spun collection net. Snags whatever walks through it like a cobweb, but the
 * weave is loose enough to struggle out of, and it doubles as the Weavers' storage.
 */
public class WeaverNetBlock extends Block implements EntityBlock {
	/** Cobweb clamps movement to 0.25/0.05/0.25; the nets are woven far looser than that. */
	private static final Vec3 DRAG = new Vec3(0.7, 0.5, 0.7);

	public WeaverNetBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
		return neighborState.is(this) || super.skipRendering(state, neighborState, direction);
	}

	@Override
	protected void entityInside(
		BlockState state,
		Level level,
		BlockPos pos,
		Entity entity,
		InsideBlockEffectApplier effectApplier,
		boolean isPrecise
	) {
		if (entity instanceof WeaverEntity) {
			return;
		}
		entity.makeStuckInBlock(state, DRAG);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockState result = super.playerWillDestroy(level, pos, state, player);
		WeaverColonies.noteBroken(level, pos, player, true);
		return result;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WeaverNetBlockEntity(pos, state);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
		if (!moved && level.getBlockEntity(pos) instanceof WeaverNetBlockEntity net) {
			Containers.dropContents(level, pos, net);
		}
		super.affectNeighborsAfterRemoval(state, level, pos, moved);
	}
}
