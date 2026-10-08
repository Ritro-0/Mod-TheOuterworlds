package com.theouterworld.mixin.client;

import com.theouterworld.client.DoggyGlassCarrier;
import com.theouterworld.client.DoggyGlassWolfLayer;
import com.theouterworld.item.DoggyGlassItem;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.entity.WolfRenderer.class)
public abstract class WolfRendererMixin extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel> {
	protected WolfRendererMixin(EntityRendererProvider.Context context, WolfModel adultModel, WolfModel babyModel, float shadow) {
		super(context, adultModel, babyModel, shadow);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void theouterworlds$doggyGlassLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
		this.addLayer(new DoggyGlassWolfLayer(this, context.getEquipmentRenderer()));
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void theouterworlds$copyDoggyGlass(Wolf wolf, WolfRenderState state, float partialTicks, CallbackInfo ci) {
		((DoggyGlassCarrier) state).theouterworlds$doggyGlass(DoggyGlassItem.isDoggyGlass(wolf.getItemBySlot(EquipmentSlot.HEAD))
			? wolf.getItemBySlot(EquipmentSlot.HEAD).copy()
			: ItemStack.EMPTY);
	}
}
