package com.theouterworld.network;

import com.theouterworld.OuterWorldMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DustStormSyncPacket(boolean active) implements CustomPacketPayload {
	public static final Type<DustStormSyncPacket> ID = new Type<>(OuterWorldMod.id("dust_storm_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DustStormSyncPacket> CODEC = StreamCodec.ofMember(
		DustStormSyncPacket::write,
		DustStormSyncPacket::new
	);

	public DustStormSyncPacket(RegistryFriendlyByteBuf buf) {
		this(buf.readBoolean());
	}

	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeBoolean(active);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
