package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.world.WeaverColonySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * A suspicious Weaver keeps its distance. It still walks the home-plate path, and housekeeping
 * outranks this goal so an inspection is never abandoned to get away.
 */
public class WeaverAvoidPlayerGoal extends Goal {
	private static final double NEAR = 10.0;
	private static final double NEAR_SQR = NEAR * NEAR;
	private static final double CLEAR_SQR = 12.0 * 12.0;
	/** Player standing on the commute counts as "in the path" and is not fled from. */
	private static final double PATH_SQR = 16.0;

	private final WeaverEntity weaver;
	private @Nullable Vec3 away;
	private int giveUpTicks;

	public WeaverAvoidPlayerGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return pickThreat() != null;
	}

	@Override
	public boolean canContinueToUse() {
		Player threat = pickThreat();
		return threat != null && giveUpTicks < 80 && weaver.distanceToSqr(threat) < CLEAR_SQR;
	}

	@Override
	public void start() {
		giveUpTicks = 0;
		Player threat = pickThreat();
		away = threat == null ? null : pickAway(threat);
		if (away != null) {
			weaver.getNavigation().moveTo(away.x, away.y, away.z, 1.15);
		}
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
		away = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		giveUpTicks++;
		Player threat = pickThreat();
		if (threat == null || away == null) {
			return;
		}
		if (weaver.distanceToSqr(away) < 4.0) {
			away = pickAway(threat);
			if (away == null) {
				return;
			}
		}
		if (weaver.getNavigation().isDone()) {
			weaver.getNavigation().moveTo(away.x, away.y, away.z, 1.15);
		}
	}

	private @Nullable Player pickThreat() {
		if (!(weaver.level() instanceof ServerLevel level)) {
			return null;
		}
		if (weaver.isSleeping() || weaver.isAggressive() || weaver.isRetreating() || weaver.isInspecting() || weaver.isOfferingGift()) {
			return null;
		}
		if (!weaver.getCarriedItem().isEmpty()) {
			return null;
		}
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		long id = weaver.colonyId();
		Player nearest = null;
		double nearestSqr = NEAR_SQR;
		for (Player player : level.players()) {
			if (!player.isAlive() || player.isSpectator() || weaver.personallyTrusts(player.getUUID())) {
				continue;
			}
			if (!data.isWary(id, player.getUUID())) {
				continue;
			}
			if (onCommute(player)) {
				continue;
			}
			double distance = weaver.distanceToSqr(player);
			if (distance <= nearestSqr) {
				nearest = player;
				nearestSqr = distance;
			}
		}
		return nearest;
	}

	private boolean onCommute(Player player) {
		BlockPos plate = weaver.getHomePlate();
		if (plate == null || !weaver.hasHome()) {
			return false;
		}
		Vec3 start = Vec3.atCenterOf(plate);
		Vec3 end = Vec3.atCenterOf(weaver.getHomePosition());
		Vec3 point = player.position();
		Vec3 span = end.subtract(start);
		double length = span.lengthSqr();
		double t = length < 1.0E-4 ? 0.0 : Mth.clamp(point.subtract(start).dot(span) / length, 0.0, 1.0);
		return point.distanceToSqr(start.add(span.scale(t))) <= PATH_SQR;
	}

	private @Nullable Vec3 pickAway(Player threat) {
		Vec3 awayFrom = threat.position();
		Vec3 offset = weaver.position().subtract(awayFrom);
		if (offset.lengthSqr() < 1.0E-3) {
			float yaw = weaver.getYRot() * Mth.DEG_TO_RAD;
			offset = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
		}
		offset = offset.normalize().scale(14.0);
		Vec3 target = weaver.position().add(offset);
		Vec3 random = DefaultRandomPos.getPosTowards(weaver, 14, 7, target, (float) Math.PI / 2.0F);
		return random != null ? random : target;
	}
}
