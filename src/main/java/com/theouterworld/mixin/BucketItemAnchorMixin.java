package com.theouterworld.mixin;

import com.theouterworld.world.WeaverColonyHarm;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemAnchorMixin {
	@Inject(method = "emptyContents", at = @At("RETURN"))
	private void theouterworlds$anchorFluid(
		@Nullable LivingEntity user,
		Level level,
		BlockPos pos,
		@Nullable BlockHitResult hitResult,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!Boolean.TRUE.equals(cir.getReturnValue()) || !(user instanceof Player player) || !(level instanceof ServerLevel server)) {
			return;
		}
		WeaverColonyHarm.onPlayerFluid(player, server, pos, ((BucketItem) (Object) this).getContent());
	}
}
