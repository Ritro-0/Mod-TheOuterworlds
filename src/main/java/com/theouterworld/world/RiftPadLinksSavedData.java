package com.theouterworld.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Remembers linked rift pads that should be deleted the next time generation
 * would recreate or touch them — so breaking one side avoids duplicates when
 * the pad is moved and used again elsewhere.
 */
public class RiftPadLinksSavedData extends SavedData {
	public static final Codec<RiftPadLinksSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		GlobalPos.CODEC.listOf().fieldOf("pending_deletion").forGetter(RiftPadLinksSavedData::pendingList)
	).apply(instance, RiftPadLinksSavedData::new));

	public static final SavedDataType<RiftPadLinksSavedData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "rift_pad_links"),
		RiftPadLinksSavedData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final List<GlobalPos> pendingDeletion = new ArrayList<>();

	public RiftPadLinksSavedData() {
		this(List.of());
	}

	public RiftPadLinksSavedData(List<GlobalPos> pending) {
		this.pendingDeletion.addAll(pending);
	}

	private List<GlobalPos> pendingList() {
		return List.copyOf(pendingDeletion);
	}

	@Nullable
	public static RiftPadLinksSavedData get(MinecraftServer server) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return null;
		}
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	public void queueDeletion(GlobalPos pos) {
		if (!pendingDeletion.contains(pos)) {
			pendingDeletion.add(pos);
			setDirty();
		}
	}

	public void processPending(MinecraftServer server) {
		if (pendingDeletion.isEmpty()) {
			return;
		}
		List<GlobalPos> remaining = new ArrayList<>();
		boolean changed = false;
		for (GlobalPos global : pendingDeletion) {
			ServerLevel level = server.getLevel(global.dimension());
			if (level == null) {
				remaining.add(global);
				continue;
			}
			BlockPos pos = global.pos();
			if (!level.hasChunkAt(pos)) {
				remaining.add(global);
				continue;
			}
			if (level.getBlockState(pos).is(ModBlocks.RIFT_PAD)) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			changed = true;
		}
		if (changed || remaining.size() != pendingDeletion.size()) {
			pendingDeletion.clear();
			pendingDeletion.addAll(remaining);
			setDirty();
		}
	}
}
