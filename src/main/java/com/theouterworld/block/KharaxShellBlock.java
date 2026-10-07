package com.theouterworld.block;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Kharax shed and spore. Solid to everyone else. A Weaver, including an adopted
 * Kharax, falls through it on the night return and can slip out if already inside.
 */
public class KharaxShellBlock extends Block {
	public KharaxShellBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static boolean isShell(BlockState state) {
		return state.getBlock() instanceof KharaxShellBlock;
	}

	/** Feet or waist are already in shed or spore, so pathing may leave the shell. */
	public static boolean embedded(WeaverEntity weaver) {
		BlockPos feet = BlockPos.containing(weaver.getX(), weaver.getY() + 0.2, weaver.getZ());
		BlockPos waist = BlockPos.containing(weaver.getX(), weaver.getY() + weaver.getBbHeight() * 0.5, weaver.getZ());
		return isShell(weaver.level().getBlockState(feet)) || isShell(weaver.level().getBlockState(waist));
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext entities
			&& entities.getEntity() instanceof WeaverEntity weaver
			&& passes(weaver, pos)) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	/**
	 * The night return falls through the whole shell, roof included, until the
	 * ground inside the hut catches them. A body already in the shell can walk on through it.
	 */
	private static boolean passes(WeaverEntity weaver, BlockPos pos) {
		if (weaver.isHomePlateLeap()) {
			// The shed floor is the landing. Everything above it, roof included, stays open.
			BlockPos plate = weaver.getHomePlate();
			return plate == null || pos.getY() > plate.getY();
		}
		if (pos.getY() < Mth.floor(weaver.getY())) {
			return false;
		}
		return weaver.phasesThroughFiber()
			|| weaver.isDeckBound()
			|| embedded(weaver)
			|| bodyInside(weaver, pos);
	}

	private static boolean bodyInside(WeaverEntity weaver, BlockPos pos) {
		AABB body = weaver.getBoundingBox().inflate(-0.08);
		return body.intersects(new AABB(pos));
	}
}
