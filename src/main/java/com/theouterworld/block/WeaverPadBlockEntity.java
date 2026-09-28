package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** Remembers which Weaver this pad belongs to, even while they are away. */
public class WeaverPadBlockEntity extends BlockEntity {
	private @Nullable UUID owner;

	public WeaverPadBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WEAVER_PAD, pos, state);
	}

	public @Nullable UUID getOwner() {
		return this.owner;
	}

	public boolean allows(UUID weaverId) {
		return this.owner == null || this.owner.equals(weaverId);
	}

	public boolean claim(UUID weaverId) {
		if (!this.allows(weaverId)) {
			return false;
		}
		if (this.owner == null) {
			this.owner = weaverId;
			this.setChanged();
		}
		return true;
	}

	public void release(UUID weaverId) {
		if (this.owner != null && this.owner.equals(weaverId)) {
			this.owner = null;
			this.setChanged();
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.owner != null) {
			output.store("Owner", UUIDUtil.CODEC, this.owner);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
	}
}
