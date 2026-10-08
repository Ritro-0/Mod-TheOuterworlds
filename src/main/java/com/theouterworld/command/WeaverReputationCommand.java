package com.theouterworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.theouterworld.entity.ai.WeaverColonies;
import com.theouterworld.world.WeaverColonySavedData;
import com.theouterworld.world.WeaverReputationDebug;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class WeaverReputationCommand {
	public static void register(
		CommandDispatcher<CommandSourceStack> dispatcher,
		CommandBuildContext registryAccess,
		Commands.CommandSelection environment
	) {
		dispatcher.register(
			literal("weaverrep")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.executes(WeaverReputationCommand::toggle)
		);
	}

	private static int toggle(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("A player has to run this."));
			return 0;
		}
		boolean on = WeaverReputationDebug.toggle(player.getUUID());
		if (!on) {
			source.sendSuccess(() -> Component.literal("Weaver reputation chat is off."), false);
			return 1;
		}
		ServerLevel level = source.getLevel();
		BlockPos pos = player.blockPosition();
		long id = WeaverColonies.colonyOwning(level, pos);
		String standing = id == 0L
			? "You are not inside an Anchor right now."
			: WeaverReputationDebug.label(level, id, pos) + ": " + WeaverColonySavedData.get(level).describePlayer(id, player.getUUID());
		source.sendSuccess(() -> Component.literal("Weaver reputation chat is on. " + standing), false);
		return 1;
	}
}
