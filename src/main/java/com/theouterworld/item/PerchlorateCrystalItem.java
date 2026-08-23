package com.theouterworld.item;

import com.theouterworld.block.Corrosion;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PerchlorateCrystalItem extends Item {
	public PerchlorateCrystalItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockState state = world.getBlockState(context.getClickedPos());
		if (Corrosion.tryApply(world, context.getClickedPos(), state, context.getPlayer(), context.getItemInHand())) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
