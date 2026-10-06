package com.theouterworld.mixin;

import com.theouterworld.weather.InteriorShelterTracker;
import com.theouterworld.world.WeaverColonyHarm;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class InteriorShelterPlaceMixin {
	@Inject(
		method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At("TAIL")
	)
	private void theouterworlds$notifyInteriorShelter(
		Level world,
		BlockPos pos,
		BlockState state,
		LivingEntity placer,
		ItemStack itemStack,
		CallbackInfo ci
	) {
		if (!world.isClientSide()) {
			InteriorShelterTracker.onBlockPlaced(world, pos, placer);
			if (world instanceof ServerLevel server && placer instanceof Player player) {
				WeaverColonyHarm.notePlaced(server, pos, player);
			}
		}
	}
}
