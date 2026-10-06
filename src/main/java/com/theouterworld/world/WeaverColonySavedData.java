package com.theouterworld.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.entity.ai.WeaverColonies;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Per-Anchor memory: what the colony has already judged, how much it has been given,
 * and whether it will still offer a gift. Loaded Weavers read this; nothing here
 * walks unloaded entities.
 */
public class WeaverColonySavedData extends SavedData {
	private static final int FIRST_THRESHOLD = 32;
	/** Fifteen minutes between gifts, and each one demands a higher score than the last. */
	private static final int GIFT_COOLDOWN = 18000;
	private static final int HARM_PAUSE = 200;
	/** Lifetime Anchor blocks one player can break before that colony stops trusting them. */
	public static final int STRUCTURE_RUIN = 100;
	/** Placed blocks remembered so an unforgiven player's clutter can still be thrown out after a relog. */
	private static final int MAX_PLACEMENTS = 256;

	public static final Codec<WeaverColonySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Colony.CODEC.listOf().optionalFieldOf("colonies", List.of()).forGetter(WeaverColonySavedData::colonyList)
	).apply(instance, WeaverColonySavedData::new));

	public static final SavedDataType<WeaverColonySavedData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "weaver_colonies"),
		WeaverColonySavedData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final Map<Long, Colony> colonies = new HashMap<>();

	public WeaverColonySavedData() {
		this(List.of());
	}

	public WeaverColonySavedData(List<Colony> colonies) {
		for (Colony colony : colonies) {
			this.colonies.put(colony.id, colony);
		}
	}

	public static WeaverColonySavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	private List<Colony> colonyList() {
		return new ArrayList<>(colonies.values());
	}

	public @Nullable Boolean recall(long id, String block) {
		Colony colony = colonies.get(id);
		if (colony == null) {
			return null;
		}
		return colony.memory.get(block);
	}

	public void remember(long id, String block, boolean keep) {
		colony(id).memory.put(block, keep);
		setDirty();
	}

	public void addContribution(ServerLevel level, long id, int amount) {
		if (amount <= 0 || id == 0L) {
			return;
		}
		Colony colony = colony(id);
		colony.contribution += amount;
		long now = level.getGameTime();
		boolean babyHere = now - colony.babySeenAt < 200L;
		if (!colony.hostile
			&& colony.pendingGift.isEmpty()
			&& (babyHere || now >= colony.giftReadyAt)
			&& now >= colony.lastHarm + HARM_PAUSE
			&& colony.contribution >= colony.nextThreshold) {
			ItemStack gift = WeaverColonyGifts.roll(level);
			if (!gift.isEmpty()) {
				colony.pendingGift = gift;
				int threshold = colony.nextThreshold;
				while (threshold <= colony.contribution) {
					threshold += Math.max(FIRST_THRESHOLD, threshold / 2);
				}
				colony.nextThreshold = threshold;
				colony.giftReadyAt = now + GIFT_COOLDOWN;
			}
		}
		setDirty();
	}

	public void noteBaby(long id, long gameTime) {
		if (id == 0L) {
			return;
		}
		Colony colony = colony(id);
		if (gameTime <= colony.babySeenAt) {
			return;
		}
		colony.babySeenAt = gameTime;
		setDirty();
	}

	public boolean hasPendingGift(long id) {
		Colony colony = colonies.get(id);
		return colony != null && !colony.hostile && !colony.pendingGift.isEmpty();
	}

	public ItemStack claimGift(long id, UUID weaver) {
		Colony colony = colonies.get(id);
		if (colony == null || colony.hostile || colony.pendingGift.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (colony.bearer != null && !colony.bearer.equals(weaver)) {
			return ItemStack.EMPTY;
		}
		ItemStack gift = colony.pendingGift;
		colony.pendingGift = ItemStack.EMPTY;
		colony.bearer = null;
		setDirty();
		return gift;
	}

	public boolean isHostile(long id) {
		Colony colony = colonies.get(id);
		return colony != null && colony.hostile;
	}

	/** Colony-wide hostility, or this player personally ruined the Anchor. Other players are unaffected. */
	public boolean isUntrusted(long id, UUID player) {
		if (player == null) {
			return false;
		}
		Colony colony = colonies.get(id);
		if (colony == null) {
			return false;
		}
		if (colony.hostile) {
			return true;
		}
		Grudge grudge = colony.grudges.get(player);
		return grudge != null && grudge.hostile;
	}

	/**
	 * Unforgiven, or merely suspicious after being hit. Suspicion keeps a Weaver away.
	 * It does not make the player's blocks unwelcome, and it never becomes permanent on its own.
	 */
	public boolean isWary(long id, UUID player) {
		if (player == null) {
			return false;
		}
		if (isUntrusted(id, player)) {
			return true;
		}
		Colony colony = colonies.get(id);
		if (colony == null) {
			return false;
		}
		Grudge grudge = colony.grudges.get(player);
		return grudge != null && grudge.suspicion > 0;
	}

	public void noteSuspicion(long id, UUID player) {
		if (id == 0L || player == null) {
			return;
		}
		Grudge grudge = grudge(id, player);
		if (grudge.hostile || grudge.suspicion >= 8) {
			return;
		}
		grudge.suspicion++;
		setDirty();
	}

	/** @return the player's lifetime break count after this block, or 0 when it was not recorded */
	public int noteStructureBreak(long id, UUID player) {
		if (id == 0L || player == null) {
			return 0;
		}
		Grudge grudge = grudge(id, player);
		grudge.broken++;
		if (grudge.broken >= STRUCTURE_RUIN) {
			grudge.hostile = true;
		}
		setDirty();
		return grudge.broken;
	}

	/** @return true the first time this player is marked hostile to the colony */
	public boolean markPlayerHostile(long id, UUID player) {
		if (id == 0L || player == null) {
			return false;
		}
		Grudge grudge = grudge(id, player);
		if (grudge.hostile) {
			return false;
		}
		grudge.hostile = true;
		setDirty();
		return true;
	}

	/** @return true the first time this blast makes the player hostile to the colony */
	public boolean noteExplosion(long id, UUID player, int blocks) {
		if (id == 0L || player == null || blocks <= 0) {
			return false;
		}
		Grudge grudge = grudge(id, player);
		boolean fresh = !grudge.hostile;
		grudge.broken += blocks;
		grudge.hostile = true;
		setDirty();
		return fresh;
	}

	private Grudge grudge(long id, UUID player) {
		Colony colony = colony(id);
		Grudge grudge = colony.grudges.get(player);
		if (grudge == null) {
			grudge = new Grudge(player, 0, false, 0);
			colony.grudges.put(player, grudge);
		}
		return grudge;
	}

	public long lastHarm(long id) {
		Colony colony = colonies.get(id);
		return colony == null ? 0L : colony.lastHarm;
	}

	/** A kill still turns the colony against this player. Any lesser hit only makes them suspicious. */
	public void notePlayerDamage(long id, UUID player, long gameTime, boolean killed) {
		if (id == 0L || player == null) {
			return;
		}
		noteHarm(id, gameTime, killed);
		if (killed) {
			markPlayerHostile(id, player);
		} else {
			noteSuspicion(id, player);
		}
	}

	public @Nullable UUID placementAt(long id, BlockPos pos) {
		Colony colony = colonies.get(id);
		if (colony == null || pos == null) {
			return null;
		}
		return colony.placements.get(pos.asLong());
	}

	/** The colony already set this block down inside a pod, and it is still there. */
	public boolean furnitureRemains(ServerLevel level, long id, String block) {
		Colony colony = colonies.get(id);
		if (colony == null) {
			return false;
		}
		BlockPos at = colony.furniture.get(block);
		if (at == null) {
			return false;
		}
		if (!level.hasChunkAt(at)) {
			return true;
		}
		BlockState state = level.getBlockState(at);
		if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(block)) {
			return true;
		}
		colony.furniture.remove(block);
		queueBreak(colony, block);
		setDirty();
		return false;
	}

	public void noteFurniture(long id, String block, BlockPos pos) {
		if (id == 0L || block == null || pos == null) {
			return;
		}
		colony(id).furniture.put(block, pos.immutable());
		setDirty();
	}

	/** True when a Weaver set this exact block down and it is still that block. */
	public boolean isWeaverPlaced(BlockPos pos, BlockState state) {
		if (pos == null || state == null) {
			return false;
		}
		String now = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		long at = pos.asLong();
		for (Colony colony : colonies.values()) {
			BlockPos placed = colony.furniture.get(now);
			if (placed != null && placed.asLong() == at) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The block at a Weaver's placement changed. Loaded Weavers hear it on their next tick;
	 * the notice stays queued for any Weaver that is not in the world yet.
	 */
	public void onBlockChanged(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
		if (pos == null || oldState == null || newState == null || oldState.getBlock() == newState.getBlock()) {
			return;
		}
		long at = pos.asLong();
		String now = BuiltInRegistries.BLOCK.getKey(newState.getBlock()).toString();
		boolean changed = false;
		for (Colony colony : colonies.values()) {
			if (colony.furniture.isEmpty()) {
				continue;
			}
			String removed = null;
			for (Map.Entry<String, BlockPos> entry : colony.furniture.entrySet()) {
				if (entry.getValue().asLong() == at) {
					removed = entry.getKey();
					break;
				}
			}
			if (removed == null || removed.equals(now)) {
				continue;
			}
			colony.furniture.remove(removed);
			queueBreak(colony, removed);
			changed = true;
		}
		if (changed) {
			setDirty();
			for (Entity entity : level.getAllEntities()) {
				if (entity instanceof WeaverEntity weaver && weaver.colonyId() != 0L) {
					deliverFurnitureNotices(weaver.colonyId(), weaver);
				}
			}
		}
	}

	private static void queueBreak(Colony colony, String block) {
		colony.noticeSeq++;
		colony.notices.add(new FurnitureNotice(colony.noticeSeq, block));
		while (colony.notices.size() > 32) {
			colony.notices.remove(0);
		}
	}

	public void deliverFurnitureNotices(long id, WeaverEntity weaver) {
		Colony colony = colonies.get(id);
		if (colony == null || weaver == null) {
			return;
		}
		long cursor = weaver.furnitureCursor();
		long start = cursor;
		if (!colony.notices.isEmpty() && cursor < colony.notices.get(0).id() - 1) {
			weaver.retainFurnished(colony.furniture.keySet());
			cursor = colony.notices.get(0).id() - 1;
		}
		for (FurnitureNotice notice : colony.notices) {
			if (notice.id() <= cursor) {
				continue;
			}
			weaver.forgetFurnished(notice.block());
			cursor = notice.id();
		}
		if (cursor != start) {
			weaver.setFurnitureCursor(cursor);
		}
	}

	public boolean holdsSpecimen(long id, String block) {
		Colony colony = colonies.get(id);
		return colony != null && colony.specimens.contains(block);
	}

	public void noteSpecimen(long id, String block) {
		if (id == 0L || block == null) {
			return;
		}
		if (colony(id).specimens.add(block)) {
			setDirty();
		}
	}

	public void forgetSpecimen(long id, String block) {
		Colony colony = colonies.get(id);
		if (colony != null && colony.specimens.remove(block)) {
			setDirty();
		}
	}

	public void notePlacement(long id, BlockPos pos, UUID player) {
		if (id == 0L || pos == null || player == null) {
			return;
		}
		Colony colony = colony(id);
		long key = pos.asLong();
		colony.placements.remove(key);
		colony.placements.put(key, player);
		while (colony.placements.size() > MAX_PLACEMENTS) {
			colony.placements.remove(colony.placements.keySet().iterator().next());
		}
		setDirty();
	}

	public @Nullable UUID takePlacement(long id, BlockPos pos) {
		Colony colony = colonies.get(id);
		if (colony == null || pos == null) {
			return null;
		}
		UUID player = colony.placements.remove(pos.asLong());
		if (player != null) {
			setDirty();
		}
		return player;
	}

	public void noteHarm(long id, long gameTime, boolean severe) {
		if (id == 0L) {
			return;
		}
		Colony colony = colony(id);
		colony.lastHarm = gameTime;
		if (severe) {
			colony.hostile = true;
			colony.pendingGift = ItemStack.EMPTY;
			colony.bearer = null;
		}
		setDirty();
	}

	public boolean canExpand(long id, long gameTime, int visibleNets) {
		if (visibleNets >= WeaverColonies.MAX_NETS) {
			return false;
		}
		Colony colony = colonies.get(id);
		if (colony == null) {
			return true;
		}
		return colony.expansions < WeaverColonies.MAX_EXPANSIONS && gameTime >= colony.expandReadyAt;
	}

	public void markExpanded(long id, long gameTime) {
		Colony colony = colony(id);
		colony.expansions++;
		colony.expandReadyAt = gameTime + WeaverColonies.EXPANSION_COOLDOWN;
		setDirty();
	}

	private Colony colony(long id) {
		Colony colony = colonies.get(id);
		if (colony == null) {
			colony = new Colony(id);
			colonies.put(id, colony);
		}
		return colony;
	}

	public static final class Colony {
		public static final Codec<Memory> MEMORY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("block").forGetter(Memory::block),
			Codec.BOOL.fieldOf("keep").forGetter(Memory::keep)
		).apply(instance, Memory::new));

		public static final Codec<Colony> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("id").forGetter(colony -> colony.id),
			MEMORY_CODEC.listOf().optionalFieldOf("memory", List.of()).forGetter(Colony::memoryList),
			Codec.INT.optionalFieldOf("contribution", 0).forGetter(colony -> colony.contribution),
			Codec.INT.optionalFieldOf("next_threshold", FIRST_THRESHOLD).forGetter(colony -> colony.nextThreshold),
			Codec.LONG.optionalFieldOf("gift_ready_at", 0L).forGetter(colony -> colony.giftReadyAt),
			Codec.LONG.optionalFieldOf("expand_ready_at", 0L).forGetter(colony -> colony.expandReadyAt),
			Codec.INT.optionalFieldOf("expansions", 0).forGetter(colony -> colony.expansions),
			Codec.BOOL.optionalFieldOf("hostile", false).forGetter(colony -> colony.hostile),
			Codec.LONG.optionalFieldOf("last_harm", 0L).forGetter(colony -> colony.lastHarm),
			Codec.LONG.optionalFieldOf("baby_seen_at", 0L).forGetter(colony -> colony.babySeenAt),
			ItemStack.CODEC.optionalFieldOf("pending_gift").forGetter(Colony::pendingGift),
			Grudge.CODEC.listOf().optionalFieldOf("grudges", List.of()).forGetter(Colony::grudgeList),
			Placement.CODEC.listOf().optionalFieldOf("placements", List.of()).forGetter(Colony::placementList),
			Furnishing.CODEC.listOf().optionalFieldOf("furniture", List.of()).forGetter(Colony::furnitureList),
			Codec.STRING.listOf().optionalFieldOf("specimens", List.of()).forGetter(Colony::specimenList),
			FurnitureMail.CODEC.optionalFieldOf("furniture_mail", FurnitureMail.EMPTY).forGetter(Colony::furnitureMail)
		).apply(instance, Colony::new));

		private final long id;
		private final Map<String, Boolean> memory = new HashMap<>();
		private final Map<UUID, Grudge> grudges = new HashMap<>();
		private final Map<Long, UUID> placements = new LinkedHashMap<>();
		private final Map<String, BlockPos> furniture = new HashMap<>();
		private final Set<String> specimens = new HashSet<>();
		private final List<FurnitureNotice> notices = new ArrayList<>();
		private long noticeSeq;
		private int contribution;
		private int nextThreshold = FIRST_THRESHOLD;
		private long giftReadyAt;
		private long expandReadyAt;
		private int expansions;
		private boolean hostile;
		private long lastHarm;
		private long babySeenAt;
		private ItemStack pendingGift = ItemStack.EMPTY;
		private @Nullable UUID bearer;

		private Colony(long id) {
			this.id = id;
		}

		private Colony(
			long id,
			List<Memory> memory,
			int contribution,
			int nextThreshold,
			long giftReadyAt,
			long expandReadyAt,
			int expansions,
			boolean hostile,
			long lastHarm,
			long babySeenAt,
			Optional<ItemStack> pendingGift,
			List<Grudge> grudges,
			List<Placement> placements,
			List<Furnishing> furniture,
			List<String> specimens,
			FurnitureMail furnitureMail
		) {
			this.id = id;
			for (Memory entry : memory) {
				this.memory.put(entry.block, entry.keep);
			}
			this.contribution = contribution;
			this.nextThreshold = nextThreshold;
			this.giftReadyAt = giftReadyAt;
			this.expandReadyAt = expandReadyAt;
			this.expansions = expansions;
			this.hostile = hostile;
			this.lastHarm = lastHarm;
			this.babySeenAt = babySeenAt;
			this.pendingGift = pendingGift.orElse(ItemStack.EMPTY);
			for (Grudge grudge : grudges) {
				this.grudges.put(grudge.player, grudge);
			}
			for (Placement placement : placements) {
				this.placements.put(placement.pos.asLong(), placement.player);
			}
			for (Furnishing furnishing : furniture) {
				this.furniture.put(furnishing.block, furnishing.pos);
			}
			this.specimens.addAll(specimens);
			this.noticeSeq = furnitureMail.seq();
			this.notices.addAll(furnitureMail.notices());
		}

		private List<Memory> memoryList() {
			List<Memory> list = new ArrayList<>(memory.size());
			memory.forEach((block, keep) -> list.add(new Memory(block, keep)));
			return list;
		}

		private Optional<ItemStack> pendingGift() {
			return pendingGift.isEmpty() ? Optional.empty() : Optional.of(pendingGift);
		}

		private List<Grudge> grudgeList() {
			return new ArrayList<>(grudges.values());
		}

		private List<Placement> placementList() {
			List<Placement> list = new ArrayList<>(placements.size());
			placements.forEach((pos, player) -> list.add(new Placement(BlockPos.of(pos), player)));
			return list;
		}

		private List<Furnishing> furnitureList() {
			List<Furnishing> list = new ArrayList<>(furniture.size());
			furniture.forEach((block, pos) -> list.add(new Furnishing(block, pos)));
			return list;
		}

		private List<String> specimenList() {
			return new ArrayList<>(specimens);
		}

		private FurnitureMail furnitureMail() {
			return new FurnitureMail(noticeSeq, List.copyOf(notices));
		}
	}

	/** A block a Weaver set down, later broken, waiting for every Weaver in the colony to hear about it. */
	public record FurnitureNotice(long id, String block) {
		public static final Codec<FurnitureNotice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("id").forGetter(FurnitureNotice::id),
			Codec.STRING.fieldOf("block").forGetter(FurnitureNotice::block)
		).apply(instance, FurnitureNotice::new));
	}

	public record FurnitureMail(long seq, List<FurnitureNotice> notices) {
		public static final FurnitureMail EMPTY = new FurnitureMail(0L, List.of());
		public static final Codec<FurnitureMail> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.optionalFieldOf("seq", 0L).forGetter(FurnitureMail::seq),
			FurnitureNotice.CODEC.listOf().optionalFieldOf("notices", List.of()).forGetter(FurnitureMail::notices)
		).apply(instance, FurnitureMail::new));
	}

	public record Furnishing(String block, BlockPos pos) {
		public static final Codec<Furnishing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("block").forGetter(Furnishing::block),
			BlockPos.CODEC.fieldOf("pos").forGetter(Furnishing::pos)
		).apply(instance, Furnishing::new));
	}

	public record Placement(BlockPos pos, UUID player) {
		public static final Codec<Placement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("pos").forGetter(Placement::pos),
			UUIDUtil.CODEC.fieldOf("player").forGetter(Placement::player)
		).apply(instance, Placement::new));
	}

	public static final class Grudge {
		public static final Codec<Grudge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("player").forGetter(grudge -> grudge.player),
			Codec.INT.optionalFieldOf("broken", 0).forGetter(grudge -> grudge.broken),
			Codec.BOOL.optionalFieldOf("hostile", false).forGetter(grudge -> grudge.hostile),
			Codec.INT.optionalFieldOf("suspicion", 0).forGetter(grudge -> grudge.suspicion)
		).apply(instance, Grudge::new));

		private final UUID player;
		private int broken;
		private boolean hostile;
		private int suspicion;

		private Grudge(UUID player, int broken, boolean hostile, int suspicion) {
			this.player = player;
			this.broken = broken;
			this.hostile = hostile;
			this.suspicion = suspicion;
		}
	}

	public record Memory(String block, boolean keep) {
	}
}
