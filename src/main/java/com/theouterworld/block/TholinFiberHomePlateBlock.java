package com.theouterworld.block;

import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.entity.ai.WeaverColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Landing plate at a pod door. Breaking it is harm against the colony that owns it. */
public class TholinFiberHomePlateBlock extends Block {
	public TholinFiberHomePlateBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext entities
			&& entities.getEntity() instanceof WeaverEntity weaver
			&& weaver.isHomeLeaping()
			&& weaver.getY() < pos.getY() + 0.95) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockState result = super.playerWillDestroy(level, pos, state, player);
		WeaverColonies.noteBroken(level, pos, player, true);
		return result;
	}
}
