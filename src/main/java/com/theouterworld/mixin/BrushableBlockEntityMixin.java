package com.theouterworld.mixin;

import com.theouterworld.advancement.ModAdvancements;
import com.theouterworld.block.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityMixin {
	@Inject(
		method = "dropContent",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
		)
	)
	private void theouterworlds$onRegolithItemDropped(ServerLevel level, LivingEntity user, ItemStack brush, CallbackInfo ci) {
		if (user instanceof ServerPlayer player
			&& level.getBlockState(((BlockEntity) (Object) this).getBlockPos()).is(ModBlocks.SUSPICIOUS_REGOLITH)) {
			ModAdvancements.onSuspiciousRegolithBrushed(player);
		}
	}
}
