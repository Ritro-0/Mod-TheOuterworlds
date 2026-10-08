package com.theouterworld.mixin.client;

import com.theouterworld.client.DoggyGlassCarrier;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WolfRenderState.class)
public class WolfRenderStateMixin implements DoggyGlassCarrier {
	@Unique
	private ItemStack theouterworlds$doggyGlass = ItemStack.EMPTY;

	@Override
	public ItemStack theouterworlds$doggyGlass() {
		return this.theouterworlds$doggyGlass == null ? ItemStack.EMPTY : this.theouterworlds$doggyGlass;
	}

	@Override
	public void theouterworlds$doggyGlass(ItemStack stack) {
		this.theouterworlds$doggyGlass = stack;
	}
}
