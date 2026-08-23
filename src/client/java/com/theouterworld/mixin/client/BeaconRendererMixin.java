package com.theouterworld.mixin.client;

import com.theouterworld.block.BeaconConcentratorBlock;
import com.theouterworld.block.ModBlocks;
import java.util.List;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconRenderer.class)
public class BeaconRendererMixin {
	@Inject(
		method = "extract(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;FLnet/minecraft/world/phys/Vec3;)V",
		at = @At("TAIL")
	)
	private static void theouterworlds$hideBeamWhenConcentrated(
		BlockEntity blockEntity,
		BeaconRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		CallbackInfo ci
	) {
		Level level = blockEntity.getLevel();
		if (level == null) {
			return;
		}
		BlockState above = level.getBlockState(blockEntity.getBlockPos().above());
		if (above.is(ModBlocks.BEACON_CONCENTRATOR)
			&& above.hasProperty(BeaconConcentratorBlock.ACTIVE)
			&& above.getValue(BeaconConcentratorBlock.ACTIVE)) {
			state.sections = List.of();
		}
	}
}
