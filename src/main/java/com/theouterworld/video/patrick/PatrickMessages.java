package com.theouterworld.video.patrick;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** TEMP VIDEO FEATURE — styled Patrick chat lines. */
public final class PatrickMessages {
	public static final int PATRICK_CRIMSON = 0x8B0000;
	public static final String PATRICK_NAME = "P̷a̷t̷r̷i̷c̷k̷";

	private PatrickMessages() {
	}

	public static MutableComponent patrickName() {
		return Component.literal(PATRICK_NAME).withStyle(
			Style.EMPTY
				.withColor(TextColor.fromRgb(PATRICK_CRIMSON))
				.withBold(true)
				.withUnderlined(true)
		);
	}

	public static void broadcastBanishment(ServerLevel world, List<ServerPlayer> victims) {
		MutableComponent message = Component.empty()
			.append(Component.literal("<"))
			.append(patrickName())
			.append(Component.literal("> Away with you, "))
			.append(Component.literal(formatNames(victims)))
			.append(Component.literal(", for you have angered the Great "))
			.append(patrickName())
			.append(Component.literal(
				" and have therefore been banished to the Outerworld. Feel blessed to have received this glass helmet, for you would not have been able to breathe without it. Consider this a life sentence, rather than an execution. Enjoy your new homes. The great "
			))
			.append(patrickName())
			.append(Component.literal(" has spoken, and He has been merciful!"));
		world.getServer().getPlayerList().broadcastSystemMessage(message, false);
	}

	public static void broadcastReturn(ServerLevel world) {
		MutableComponent message = Component.empty()
			.append(Component.literal("<"))
			.append(patrickName())
			.append(Component.literal("> How did you make it back? This is unacceptable. The end of days is imminent. "))
			.append(patrickName())
			.append(Component.literal(" does not forgive. "))
			.append(patrickName())
			.append(Component.literal(" does not forget."));
		world.getServer().getPlayerList().broadcastSystemMessage(message, false);
	}

	public static String formatNames(List<ServerPlayer> victims) {
		List<String> names = victims.stream().map(ServerPlayer::getScoreboardName).toList();
		if (names.size() == 1) {
			return names.get(0);
		}
		if (names.size() == 2) {
			return names.get(0) + " & " + names.get(1);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.size(); i++) {
			if (i > 0 && i == names.size() - 1) {
				sb.append(", and ");
			} else if (i > 0) {
				sb.append(", ");
			}
			sb.append(names.get(i));
		}
		return sb.toString();
	}
}
