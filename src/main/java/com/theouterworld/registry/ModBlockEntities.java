package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.AstralTelescopeBlockEntity;
import com.theouterworld.block.TrimmedGlassBlock;
import com.theouterworld.block.TrimmedGlassBlockEntity;
import com.theouterworld.block.BeaconConcentratorBlockEntity;
import com.theouterworld.block.IronGolemStatueBlockEntity;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.ProcessorBlockEntity;
import com.theouterworld.block.RiftBlockEntity;
import com.theouterworld.block.RiftChargeBlockEntity;
import com.theouterworld.block.RiftPadBlockEntity;
import com.theouterworld.block.VentCloveBlockEntity;
import com.theouterworld.block.WeaverNetBlockEntity;
import com.theouterworld.block.WeaverPadBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class ModBlockEntities {
	public static final BlockEntityType<RiftChargeBlockEntity> RIFT_CHARGE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("rift_charge"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<RiftChargeBlockEntity>create(
			RiftChargeBlockEntity::new,
			ModBlocks.RIFT_CHARGE
		).build()
	);

	public static final BlockEntityType<RiftPadBlockEntity> RIFT_PAD = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("rift_pad"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<RiftPadBlockEntity>create(
			RiftPadBlockEntity::new,
			ModBlocks.RIFT_PAD
		).build()
	);

	public static final BlockEntityType<RiftBlockEntity> RIFT = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("rift"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<RiftBlockEntity>create(
			RiftBlockEntity::new,
			ModBlocks.RIFT
		).build()
	);

	public static final BlockEntityType<IronGolemStatueBlockEntity> IRON_GOLEM_STATUE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("iron_golem_statue"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<IronGolemStatueBlockEntity>create(
			IronGolemStatueBlockEntity::new,
			ModBlocks.IRON_GOLEM_STATUE,
			ModBlocks.EXPOSED_IRON_GOLEM_STATUE,
			ModBlocks.WEATHERED_IRON_GOLEM_STATUE,
			ModBlocks.OXIDIZED_IRON_GOLEM_STATUE,
			ModBlocks.WAXED_IRON_GOLEM_STATUE,
			ModBlocks.WAXED_EXPOSED_IRON_GOLEM_STATUE,
			ModBlocks.WAXED_WEATHERED_IRON_GOLEM_STATUE,
			ModBlocks.WAXED_OXIDIZED_IRON_GOLEM_STATUE
		).build()
	);

	public static final BlockEntityType<BeaconConcentratorBlockEntity> BEACON_CONCENTRATOR = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("beacon_concentrator"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<BeaconConcentratorBlockEntity>create(
			BeaconConcentratorBlockEntity::new,
			ModBlocks.BEACON_CONCENTRATOR,
			ModBlocks.CONCENTRATED_BEACON_BEAM
		).build()
	);

	public static final BlockEntityType<ProcessorBlockEntity> PROCESSOR = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("processor"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<ProcessorBlockEntity>create(
			ProcessorBlockEntity::new,
			ModBlocks.PROCESSOR
		).build()
	);

	public static final BlockEntityType<WeaverPadBlockEntity> WEAVER_PAD = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("weaver_pad"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<WeaverPadBlockEntity>create(
			WeaverPadBlockEntity::new,
			ModBlocks.WEAVER_PAD
		).build()
	);

	public static final BlockEntityType<WeaverNetBlockEntity> WEAVER_NET = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("weaver_net"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<WeaverNetBlockEntity>create(
			WeaverNetBlockEntity::new,
			ModBlocks.WEAVER_NET
		).build()
	);

	public static final BlockEntityType<VentCloveBlockEntity> VENT_CLOVE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("vent_clove"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<VentCloveBlockEntity>create(
			VentCloveBlockEntity::new,
			ModBlocks.VENT_CLOVE
		).build()
	);

	public static final BlockEntityType<AstralTelescopeBlockEntity> ASTRAL_TELESCOPE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("astral_telescope"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<AstralTelescopeBlockEntity>create(
			AstralTelescopeBlockEntity::new,
			ModBlocks.ASTRAL_TELESCOPE
		).build()
	);

	public static final BlockEntityType<TrimmedGlassBlockEntity> TRIMMED_GLASS = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("trimmed_glass"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<TrimmedGlassBlockEntity>create(
			TrimmedGlassBlockEntity::new,
			TrimmedGlassBlock.BLOCK
		).build()
	);

	public static void registerModBlockEntities() {
		OuterWorldMod.LOGGER.info("Registering block entities for {}", OuterWorldMod.MOD_ID);
		((FabricBlockEntityType) (Object) BlockEntityTypes.BRUSHABLE_BLOCK)
			.addValidBlock(ModBlocks.SUSPICIOUS_REGOLITH);
	}
}
