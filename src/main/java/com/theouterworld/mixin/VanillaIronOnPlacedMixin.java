package com.theouterworld.mixin;

import com.theouterworld.event.VanillaIronReplacementListener;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace vanilla iron blocks, trapdoors, and chains with oxidizable versions when placed in the Outerworld.
 * 
 * Uses a deferred replacement queue to let placement finalize (AXIS property for chains, etc.) before swapping.
 * The actual replacement happens in the next server tick via VanillaIronReplacementListener.
 * 
 * Note: Doors have their own mixin (VanillaIronDoorOnPlacedMixin) because DoorBlock has complex two-block placement.
 */
@Mixin(Block.class)
public abstract class VanillaIronOnPlacedMixin {

    @Inject(method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void theouterworlds$queueReplacement(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (world.isClientSide() || !(world instanceof ServerLevel serverWorld)) return;

        if (!serverWorld.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) return;

        Block placedBlock = state.getBlock();

        // Skip if already oxidizable (prevents loops)
        if (placedBlock instanceof WeatheringCopper) return;

        Block replacement = VanillaIronReplacementListener.getReplacement(placedBlock);

        if (replacement != null) {
            // Queue replacement for next tick - lets placement finalize (AXIS for chains, etc.)
            VanillaIronReplacementListener.PENDING_REPLACEMENTS.add(
                new VanillaIronReplacementListener.PendingReplacement(serverWorld, pos.immutable(), replacement)
            );
        }
    }
}
