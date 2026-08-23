package com.theouterworld.mixin;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace vanilla iron doors with oxidizable versions when placed in the Outerworld.
 * 
 * DoorBlock.onPlaced has custom logic to place the upper half, so we need a separate mixin.
 * This fires AFTER the door is fully placed (both halves).
 */
@Mixin(DoorBlock.class)
public abstract class VanillaIronDoorOnPlacedMixin {

    @Inject(method = "setPlacedBy", at = @At("TAIL"))
    private void theouterworlds$replaceIronDoor(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        // Only run on server side
        if (world.isClientSide() || !(world instanceof ServerLevel serverWorld)) {
            return;
        }

        // Only replace in Outerworld dimension
        if (!serverWorld.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
            return;
        }

        // Check if this is the vanilla iron door using state.isOf() for reliable comparison
        if (!state.is(Blocks.IRON_DOOR)) {
            return;
        }

        // Get current state and determine which half we're dealing with
        BlockState currentState = world.getBlockState(pos);
        
        // Replace lower half
        BlockState newLowerState = ModBlocks.UNAFFECTED_IRON_DOOR.withPropertiesOf(currentState);
        serverWorld.setBlock(pos, newLowerState, Block.UPDATE_ALL);
        
        // Replace upper half (door places upper half at pos.up())
        BlockPos upperPos = pos.above();
        BlockState upperState = world.getBlockState(upperPos);
        if (upperState.is(Blocks.IRON_DOOR) && upperState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            BlockState newUpperState = ModBlocks.UNAFFECTED_IRON_DOOR.withPropertiesOf(upperState);
            serverWorld.setBlock(upperPos, newUpperState, Block.UPDATE_ALL);
        }
    }
}

