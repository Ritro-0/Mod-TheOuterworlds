package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** Remembers which trim pattern was forged onto this glass, so breaking it keeps the tooltip. */
public class TrimmedGlassBlockEntity extends BlockEntity {
	@Nullable
	private Identifier patternId;

	public TrimmedGlassBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRIMMED_GLASS, pos, state);
	}

	@Nullable
	public Identifier patternId() {
		return this.patternId;
	}

	public void setPatternId(@Nullable Identifier patternId) {
		this.patternId = patternId;
		this.setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.patternId != null) {
			output.putString("Pattern", this.patternId.toString());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String pattern = input.getStringOr("Pattern", "");
		this.patternId = pattern.isEmpty() ? null : Identifier.tryParse(pattern);
	}
}
