package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.AstralTelescopeBlockEntity;
import com.theouterworld.block.BeaconConcentratorBlockEntity;
import com.theouterworld.block.IronGolemStatueBlockEntity;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.ProcessorBlockEntity;
import com.theouterworld.block.RiftBlockEntity;
import com.theouterworld.block.RiftChargeBlockEntity;
import com.theouterworld.block.RiftPadBlockEntity;
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

	public static final BlockEntityType<AstralTelescopeBlockEntity> ASTRAL_TELESCOPE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		OuterWorldMod.id("astral_telescope"),
		net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.<AstralTelescopeBlockEntity>create(
			AstralTelescopeBlockEntity::new,
			ModBlocks.ASTRAL_TELESCOPE
		).build()
	);

	public static void registerModBlockEntities() {
		OuterWorldMod.LOGGER.info("Registering block entities for {}", OuterWorldMod.MOD_ID);
		((FabricBlockEntityType) (Object) BlockEntityTypes.BRUSHABLE_BLOCK)
			.addValidBlock(ModBlocks.SUSPICIOUS_REGOLITH);
	}
}
