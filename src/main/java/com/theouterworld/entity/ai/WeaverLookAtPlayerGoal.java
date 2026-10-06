package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.world.WeaverColonySavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

/** Looks at a nearby player, and looks away again when that player is someone the colony distrusts. */
public class WeaverLookAtPlayerGoal extends LookAtPlayerGoal {
	private final WeaverEntity weaver;

	public WeaverLookAtPlayerGoal(WeaverEntity weaver) {
		super(weaver, Player.class, 8.0F);
		this.weaver = weaver;
	}

	@Override
	public boolean canUse() {
		return !waryNearby() && super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return !waryNearby() && super.canContinueToUse();
	}

	private boolean waryNearby() {
		if (!(weaver.level() instanceof ServerLevel level)) {
			return false;
		}
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		long id = weaver.colonyId();
		for (Player player : level.players()) {
			if (!player.isAlive() || player.isSpectator() || weaver.personallyTrusts(player.getUUID())) {
				continue;
			}
			if (!data.isWary(id, player.getUUID())) {
				continue;
			}
			if (weaver.distanceToSqr(player) <= 64.0) {
				return true;
			}
		}
		return false;
	}
}
