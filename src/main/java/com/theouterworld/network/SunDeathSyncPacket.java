package com.theouterworld.network;

import com.theouterworld.OuterWorldMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server → client: sun death sequence tick progress (-1 clears). */
public record SunDeathSyncPacket(int ticks) implements CustomPacketPayload {
	public static final Type<SunDeathSyncPacket> ID = new Type<>(OuterWorldMod.id("sun_death_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SunDeathSyncPacket> CODEC =
		StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SunDeathSyncPacket::ticks,
			SunDeathSyncPacket::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
