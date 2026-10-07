package com.theouterworld.network;

import com.theouterworld.OuterWorldMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client → server: the player backed out of a Sun visit warning. */
public record SunVisitCancelledPacket() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SunVisitCancelledPacket> ID =
		new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "sun_visit_cancelled"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SunVisitCancelledPacket> CODEC =
		StreamCodec.unit(new SunVisitCancelledPacket());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
