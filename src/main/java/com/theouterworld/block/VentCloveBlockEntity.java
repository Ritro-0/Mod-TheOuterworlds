package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds no data; it only exists so the clove's planes can be animated. */
public class VentCloveBlockEntity extends BlockEntity {
	public VentCloveBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.VENT_CLOVE, pos, state);
	}
}
