package com.theouterworld.mixin;

import com.theouterworld.world.WeaverColonyHarm;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerExplosion.class)
public class ServerExplosionAnchorMixin {
	@Inject(method = "explode", at = @At("HEAD"))
	private void theouterworlds$anchorBlast(CallbackInfoReturnable<Integer> cir) {
		ServerExplosion explosion = (ServerExplosion) (Object) this;
		if (!destructive(explosion)) {
			return;
		}
		Player player = igniter(explosion);
		if (player != null) {
			WeaverColonyHarm.onDetonation(explosion.level(), player, List.of(), explosion.center());
		}
	}

	@Inject(method = "interactWithBlocks", at = @At("HEAD"))
	private void theouterworlds$anchorBlocks(List<BlockPos> targetBlocks, CallbackInfo ci) {
		ServerExplosion explosion = (ServerExplosion) (Object) this;
		if (!destructive(explosion)) {
			return;
		}
		Player player = igniter(explosion);
		if (player != null) {
			WeaverColonyHarm.onDetonation(explosion.level(), player, targetBlocks, null);
		}
	}

	private static boolean destructive(ServerExplosion explosion) {
		Explosion.BlockInteraction interaction = explosion.getBlockInteraction();
		return interaction == Explosion.BlockInteraction.DESTROY || interaction == Explosion.BlockInteraction.DESTROY_WITH_DECAY;
	}

	/** TNT and perchlorate charges remember who lit them. Anything else is not this grudge. */
	private static @Nullable Player igniter(ServerExplosion explosion) {
		if (explosion.getDirectSourceEntity() instanceof PrimedTnt tnt && tnt.getOwner() instanceof Player player) {
			return player;
		}
		return null;
	}
}
