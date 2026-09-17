package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AstralTelescopeBlockEntity extends BlockEntity {
	public AstralTelescopeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ASTRAL_TELESCOPE, pos, state);
	}
}
