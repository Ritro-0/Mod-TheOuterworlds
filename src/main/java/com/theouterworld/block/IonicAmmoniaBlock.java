package com.theouterworld.block;

import com.theouterworld.item.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mineable ionic-ammonia lens. Looks / glows like a liquid blob but is a
 * normal block so the fluid engine cannot restore it after breaking.
 */
public class IonicAmmoniaBlock extends Block {
	public IonicAmmoniaBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		return List.of(new ItemStack(ModItems.IONIC_AMMONIA));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(40) == 0) {
			level.addParticle(
				ParticleTypes.GLOW,
				pos.getX() + random.nextDouble(),
				pos.getY() + random.nextDouble(),
				pos.getZ() + random.nextDouble(),
				0.0,
				0.0,
				0.0
			);
		}
	}
}
