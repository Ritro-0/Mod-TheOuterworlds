package com.theouterworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.weather.DustStormManager;
import com.theouterworld.weather.DustStormTicker;
import com.theouterworld.weather.InteriorFloodFiller;
import com.theouterworld.weather.InteriorShelterSavedData;
import com.theouterworld.weather.InteriorShelterTracker;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class DustStormCommand {
	public static void register(
		CommandDispatcher<CommandSourceStack> dispatcher,
		CommandBuildContext registryAccess,
		Commands.CommandSelection environment
	) {
		dispatcher.register(
			literal("duststorm")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(literal("start")
					.executes(DustStormCommand::startDustStorm)
				)
				.then(literal("stop")
					.executes(DustStormCommand::stopDustStorm)
				)
				.then(literal("status")
					.executes(DustStormCommand::getDustStormStatus)
				)
				.then(literal("interiors")
					.executes(DustStormCommand::getInteriorStatus)
					.then(literal("rescan")
						.executes(DustStormCommand::forceInteriorRescan)
					)
					.then(literal("clear")
						.executes(DustStormCommand::clearInteriors)
					)
				)
		);
	}

	private static int startDustStorm(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			source.sendFailure(Component.literal("Dust storms can only be triggered in the Outerworld dimension!"));
			return 0;
		}

		DustStormManager manager = DustStormTicker.getManager(world);
		if (manager != null) {
			manager.forceStorm(world);
		}

		source.sendSuccess(() -> Component.literal("Dust storm started!"), true);
		return 1;
	}

	private static int stopDustStorm(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			source.sendFailure(Component.literal("Dust storms can only be stopped in the Outerworld dimension!"));
			return 0;
		}

		DustStormManager manager = DustStormTicker.getManager(world);
		if (manager != null) {
			manager.endStorm(world);
			boolean stillActive = manager.isStormActive();
			boolean stillRaining = world.isRaining();
			source.sendSuccess(() -> Component.literal(String.format(
				"Dust storm stop command executed. Manager active: %s, World raining: %s",
				stillActive,
				stillRaining
			)), true);
		} else {
			source.sendFailure(Component.literal("Failed to get dust storm manager!"));
			return 0;
		}

		return 1;
	}

	private static int getDustStormStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			source.sendFailure(Component.literal("Dust storms only occur in the Outerworld dimension!"));
			return 0;
		}

		DustStormManager manager = DustStormTicker.getManager(world);
		if (manager == null) {
			source.sendFailure(Component.literal("Failed to get dust storm manager!"));
			return 0;
		}

		boolean managerActive = manager.isStormActive();
		boolean worldRaining = world.isRaining();
		boolean worldThundering = world.isThundering();

		source.sendSuccess(() -> Component.literal(String.format(
			"Dust storm status - Manager: %s, World Raining: %s, World Thundering: %s",
			managerActive,
			worldRaining,
			worldThundering
		)), false);

		return 1;
	}

	private static int getInteriorStatus(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			source.sendFailure(Component.literal("Interior shelter tracking only exists in the Outerworld dimension!"));
			return 0;
		}

		InteriorShelterSavedData data = InteriorShelterSavedData.get(world);
		boolean playerSheltered = false;
		if (source.getEntity() instanceof ServerPlayer player) {
			playerSheltered = data.isInterior(BlockPos.containing(player.getX(), player.getY(), player.getZ()));
		}

		boolean shelteredFinal = playerSheltered;
		source.sendSuccess(() -> Component.literal(String.format(
			"Interior shelter cache - regions: %d, cells: %d, you sheltered: %s",
			data.regionCount(),
			data.cellCount(),
			shelteredFinal
		)), false);
		return 1;
	}

	private static int forceInteriorRescan(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			source.sendFailure(Component.literal("Interior shelter tracking only exists in the Outerworld dimension!"));
			return 0;
		}
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command must be run by a player."));
			return 0;
		}

		InteriorFloodFiller.ScanResult scan = InteriorShelterTracker.scanFromPlayer(world, player);
		source.sendSuccess(() -> Component.literal(scan.sealed()
			? "Sealed interior stored (" + scan.visitedAir().size() + " air cells)."
			: "No sealed interior at your position."
		), true);
		return 1;
	}

	private static int clearInteriors(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			source.sendFailure(Component.literal("Interior shelter tracking only exists in the Outerworld dimension!"));
			return 0;
		}

		InteriorShelterSavedData data = InteriorShelterSavedData.get(world);
		data.clearAll();
		InteriorShelterTracker.syncToLevel(world);
		source.sendSuccess(() -> Component.literal("Cleared interior shelter cache."), true);
		return 1;
	}
}
