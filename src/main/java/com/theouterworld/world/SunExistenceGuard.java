package com.theouterworld.world;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.registry.ModFluids;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Nothing may exist on The Sun except terrain (bedrock / lava / plasma) and the
 * orange stained-glass spawn pane. Illegal blocks and entities are erased with smoke.
 */
public final class SunExistenceGuard {
	private SunExistenceGuard() {
	}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (ModDimensions.isSun(world.dimension())) {
				rejectEntity(world, entity);
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(world -> {
			if (!ModDimensions.isSun(world.dimension())) {
				return;
			}
			for (Entity entity : world.getAllEntities()) {
				rejectEntity(world, entity);
			}
			for (Player player : world.players()) {
				BlockPos origin = player.blockPosition();
				scrubAround(world, origin, 6);
				SolarPlasmaPulse.wakeSurfaceAround(world, origin, 8);
			}
		});
	}

	public static boolean isAllowedBlock(BlockState state) {
		if (state.isAir()) {
			return true;
		}
		if (state.is(Blocks.BEDROCK) || state.is(Blocks.LAVA)
			|| state.is(Blocks.STAINED_GLASS_PANE.pick(DyeColor.ORANGE))) {
			return true;
		}
		if (state.is(ModBlocks.SOLAR_PLASMA)) {
			return true;
		}
		FluidState fluid = state.getFluidState();
		return fluid.getType() == ModFluids.SOLAR_PLASMA
			|| fluid.getType() == ModFluids.FLOWING_SOLAR_PLASMA
			|| fluid.getType() == net.minecraft.world.level.material.Fluids.LAVA
			|| fluid.getType() == net.minecraft.world.level.material.Fluids.FLOWING_LAVA;
	}

	private static void rejectEntity(ServerLevel world, Entity entity) {
		if (entity instanceof Player || entity.isRemoved()) {
			return;
		}
		spawnSmoke(world, entity.blockPosition());
		entity.discard();
	}

	private static void scrubAround(ServerLevel world, BlockPos center, int radius) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -2; dy <= 4; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					BlockState state = world.getBlockState(cursor);
					if (!isAllowedBlock(state)) {
						spawnSmoke(world, cursor);
						world.setBlockAndUpdate(cursor, Blocks.AIR.defaultBlockState());
					}
				}
			}
		}
	}

	private static void spawnSmoke(ServerLevel world, BlockPos pos) {
		world.sendParticles(
			ParticleTypes.LARGE_SMOKE,
			pos.getX() + 0.5,
			pos.getY() + 0.5,
			pos.getZ() + 0.5,
			8,
			0.25,
			0.35,
			0.25,
			0.02
		);
		world.sendParticles(
			ParticleTypes.SMOKE,
			pos.getX() + 0.5,
			pos.getY() + 0.5,
			pos.getZ() + 0.5,
			12,
			0.3,
			0.4,
			0.3,
			0.01
		);
	}
}
