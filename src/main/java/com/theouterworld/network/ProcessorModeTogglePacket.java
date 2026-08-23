package com.theouterworld.network;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ProcessorModeTogglePacket(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ProcessorModeTogglePacket> ID = 
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "processor_mode_toggle"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorModeTogglePacket> CODEC = 
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, ProcessorModeTogglePacket::pos,
            ProcessorModeTogglePacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

