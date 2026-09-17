package com.theouterworld.video.patrick;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.commands.Commands.literal;

/** TEMP VIDEO FEATURE — {@code /patrick spawn|awaken|complete|dismiss}. */
public final class PatrickCommand {
	private PatrickCommand() {
	}

	public static void register(
		CommandDispatcher<CommandSourceStack> dispatcher,
		CommandBuildContext registryAccess,
		Commands.CommandSelection environment
	) {
		dispatcher.register(
			literal("patrick")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(literal("spawn").executes(PatrickCommand::spawn))
				.then(literal("awaken").executes(PatrickCommand::awaken))
				.then(literal("complete").executes(PatrickCommand::complete))
				.then(literal("dismiss").executes(PatrickCommand::dismiss))
		);
	}

	private static int spawn(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("Run /patrick spawn as a player."));
			return 0;
		}
		ServerLevel world = source.getLevel();
		Direction facing = player.getDirection();
		Rotation rotation = PatrickActivator.rotationFromFacing(facing);
		BlockPos origin = PatrickActivator.originInFrontOf(player.blockPosition(), facing);
		PatrickActivator.spawnStructure(world, origin, rotation);
		PatrickVideo.rememberSpawn(origin, rotation);
		source.sendSuccess(
			() -> Component.literal(
				"Spawned Patrick facing you at " + origin.toShortString()
					+ " (" + rotation + "). Use /patrick awaken nearby."
			),
			true
		);
		return 1;
	}

	private static int awaken(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();
		BlockPos center = BlockPos.containing(source.getPosition());
		PatrickMatcher.Match match = PatrickMatcher.findNear(world, center);
		if (match == null) {
			source.sendFailure(Component.literal(
				"No matching Patrick structure within " + PatrickTemplate.SEARCH_RADIUS
					+ " blocks (exact N/E/S/W build + glow item frame eyes)."
			));
			return 0;
		}

		PatrickActivator.Result result = PatrickActivator.awaken(world, match);
		source.sendSuccess(
			() -> Component.literal(
				"Patrick awakened at " + match.origin().toShortString()
					+ " [" + match.rotation() + "] with " + result.displayCount()
					+ " displays / " + result.eyeCount() + " eyes."
			),
			true
		);
		return 1;
	}

	private static int complete(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();
		Vec3 center = source.getPosition();

		List<BlockPos> controllers = PatrickVideo.findControllers(world, center, PatrickTemplate.SEARCH_RADIUS);
		PatrickControllerBlockEntity nearest = null;
		int alreadyRunning = 0;
		for (BlockPos pos : controllers) {
			if (!(world.getBlockEntity(pos) instanceof PatrickControllerBlockEntity controller)) {
				continue;
			}
			if (controller.canComplete()) {
				nearest = controller;
				break;
			}
			alreadyRunning++;
		}

		if (nearest == null) {
			source.sendFailure(Component.literal(
				alreadyRunning > 0
					? "Patrick is already performing his return sequence."
					: "No awakened Patrick within " + PatrickTemplate.SEARCH_RADIUS + " blocks of "
						+ BlockPos.containing(center).toShortString() + " in "
						+ world.dimension().identifier() + " (run /patrick awaken first)."
			));
			return 0;
		}

		ServerPlayer focus = source.getEntity() instanceof ServerPlayer player ? player : null;
		if (!nearest.beginComplete(world, focus)) {
			source.sendFailure(Component.literal("Patrick could not begin his return sequence (no players nearby?)."));
			return 0;
		}

		return 1;
	}

	private static int dismiss(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel world = source.getLevel();
		Vec3 center = source.getPosition();
		List<BlockPos> nearby = PatrickVideo.findControllers(world, center, PatrickTemplate.SEARCH_RADIUS);
		int removed = 0;
		for (BlockPos pos : nearby) {
			if (world.getBlockEntity(pos) instanceof PatrickControllerBlockEntity controller) {
				controller.dismiss(world);
				removed++;
			} else {
				PatrickVideo.untrack(pos);
			}
		}
		if (removed == 0) {
			source.sendFailure(Component.literal("No awakened Patrick controllers nearby."));
			return 0;
		}
		int finalRemoved = removed;
		source.sendSuccess(() -> Component.literal("Dismissed " + finalRemoved + " Patrick controller(s)."), true);
		return removed;
	}
}
