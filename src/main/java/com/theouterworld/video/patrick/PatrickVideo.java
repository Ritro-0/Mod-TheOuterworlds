package com.theouterworld.video.patrick;

import com.theouterworld.OuterWorldMod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

/**
 * TEMP VIDEO FEATURE bootstrap.
 * <p>
 * To remove later: delete {@code com.theouterworld.video.patrick}, its assets under
 * {@code patrick_controller}, and the {@code PatrickVideo.register()} call in {@link OuterWorldMod}.
 */
public final class PatrickVideo {
	public static final Block CONTROLLER = Registry.register(
		BuiltInRegistries.BLOCK,
		OuterWorldMod.id("patrick_controller"),
		new PatrickControllerBlock(
			BlockBehaviour.Properties.of()
				.setId(ResourceKey.create(Registries.BLOCK, OuterWorldMod.id("patrick_controller")))
				.mapColor(MapColor.COLOR_RED)
				.strength(-1.0F, 3600000.0F)
				.noLootTable()
				.noOcclusion()
				.noCollision()
				.sound(SoundType.EMPTY)
				.pushReaction(PushReaction.BLOCK)
		)
	);

	public static final BlockEntityType<PatrickControllerBlockEntity> CONTROLLER_BE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("patrick_controller"),
		FabricBlockEntityTypeBuilder.<PatrickControllerBlockEntity>create(
			PatrickControllerBlockEntity::new,
			CONTROLLER
		).build()
	);

	private static final Set<BlockPos> ACTIVE = ConcurrentHashMap.newKeySet();
	private static final List<SpawnRecord> RECENT_SPAWNS = Collections.synchronizedList(new ArrayList<>());

	private PatrickVideo() {
	}

	public static void register() {
		OuterWorldMod.LOGGER.info("Registering TEMP video Patrick feature (easy to remove)");
		CommandRegistrationCallback.EVENT.register(PatrickCommand::register);
	}

	public static void track(BlockPos pos) {
		ACTIVE.add(pos.immutable());
	}

	public static void untrack(BlockPos pos) {
		ACTIVE.remove(pos.immutable());
	}

	public static Set<BlockPos> activeControllers() {
		return ACTIVE;
	}

	/**
	 * Locate controller blocks near {@code center}, nearest first.
	 * <p>
	 * The in-memory {@link #ACTIVE} set is only a hint: it is lost on server restart and never
	 * repopulated until a chunk happens to reload. The loaded-chunk sweep is the authoritative
	 * source, so an awakened Patrick stays findable across restarts and unload cycles.
	 */
	public static List<BlockPos> findControllers(ServerLevel world, Vec3 center, double radius) {
		double radiusSq = radius * radius;
		Set<BlockPos> found = new LinkedHashSet<>();

		for (BlockPos pos : ACTIVE) {
			if (pos.distToCenterSqr(center) <= radiusSq && world.getBlockState(pos).is(CONTROLLER)) {
				found.add(pos.immutable());
			}
		}

		int centerChunkX = Mth.floor(center.x) >> 4;
		int centerChunkZ = Mth.floor(center.z) >> 4;
		int chunkRadius = Mth.ceil(radius / 16.0);
		for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
			for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
				LevelChunk chunk = world.getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
				if (chunk == null) {
					continue;
				}
				for (BlockPos pos : chunk.getBlockEntitiesPos()) {
					if (pos.distToCenterSqr(center) <= radiusSq
						&& chunk.getBlockEntity(pos) instanceof PatrickControllerBlockEntity) {
						found.add(pos.immutable());
						ACTIVE.add(pos.immutable());
					}
				}
			}
		}

		List<BlockPos> sorted = new ArrayList<>(found);
		sorted.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(center)));
		return sorted;
	}

	public static void rememberSpawn(BlockPos origin, Rotation rotation) {
		RECENT_SPAWNS.add(0, new SpawnRecord(origin.immutable(), rotation));
		while (RECENT_SPAWNS.size() > 8) {
			RECENT_SPAWNS.remove(RECENT_SPAWNS.size() - 1);
		}
	}

	public static List<SpawnRecord> recentSpawns() {
		return List.copyOf(RECENT_SPAWNS);
	}

	public record SpawnRecord(BlockPos origin, Rotation rotation) {
	}
}
