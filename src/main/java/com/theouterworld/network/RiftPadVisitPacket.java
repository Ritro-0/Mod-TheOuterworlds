package com.theouterworld.network;

import com.theouterworld.OuterWorldMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client → server: request a rift-pad visit to a supported destination world. */
public record RiftPadVisitPacket(String destination) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RiftPadVisitPacket> ID =
		new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "rift_pad_visit"));

	public static final StreamCodec<RegistryFriendlyByteBuf, RiftPadVisitPacket> CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, RiftPadVisitPacket::destination,
			RiftPadVisitPacket::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
