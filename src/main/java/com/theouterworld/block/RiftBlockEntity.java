package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RiftBlockEntity extends BlockEntity {
	public RiftBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RIFT, pos, state);
	}
}
