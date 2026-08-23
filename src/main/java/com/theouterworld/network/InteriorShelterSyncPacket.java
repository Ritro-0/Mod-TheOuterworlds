package com.theouterworld.network;

import com.theouterworld.OuterWorldMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Full replace of the client interior shelter cache.
 */
public record InteriorShelterSyncPacket(long[] cells) implements CustomPacketPayload {
	public static final Type<InteriorShelterSyncPacket> ID = new Type<>(OuterWorldMod.id("interior_shelter_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, InteriorShelterSyncPacket> CODEC = StreamCodec.ofMember(
		InteriorShelterSyncPacket::write,
		InteriorShelterSyncPacket::new
	);

	public InteriorShelterSyncPacket(RegistryFriendlyByteBuf buf) {
		this(readCells(buf));
	}

	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(cells.length);
		for (long cell : cells) {
			buf.writeLong(cell);
		}
	}

	private static long[] readCells(RegistryFriendlyByteBuf buf) {
		int size = buf.readVarInt();
		long[] cells = new long[size];
		for (int i = 0; i < size; i++) {
			cells[i] = buf.readLong();
		}
		return cells;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
