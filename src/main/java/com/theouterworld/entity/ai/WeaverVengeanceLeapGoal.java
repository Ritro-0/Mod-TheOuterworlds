package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * One home-plate leap at the player who hurt a baby. Landing ends the leap.
 * The grudge that follows is ordinary permanent aggression, not another leap.
 */
public class WeaverVengeanceLeapGoal extends Goal {
	private static final int TIMEOUT = 400;

	private final WeaverEntity weaver;
	private int ticks;
	private boolean leftGround;

	public WeaverVengeanceLeapGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return this.weaver.isVengeanceLeaping() && this.weaver.getVengeancePlayer() != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.canUse() && this.ticks < TIMEOUT;
	}

	@Override
	public void start() {
		this.ticks = 0;
		this.leftGround = !this.weaver.onGround();
	}

	@Override
	public void stop() {
		if (this.weaver.isVengeanceLeaping()) {
			this.weaver.finishVengeanceLeap();
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.ticks++;
		Player player = this.weaver.getVengeancePlayer();
		if (player == null) {
			this.weaver.finishVengeanceLeap();
			return;
		}
		this.weaver.getLookControl().setLookAt(player, 40.0F, 40.0F);
		BlockPos ground = landing(player);
		if (ground == null) {
			this.weaver.finishVengeanceLeap();
			return;
		}
		if (!this.weaver.isHomeLeaping()) {
			this.weaver.beginVengeanceFlight(ground);
		}
		if (!this.weaver.onGround()) {
			this.leftGround = true;
			this.weaver.steerHomeLeap(ground);
			return;
		}
		if (this.leftGround) {
			this.weaver.finishVengeanceLeap();
		}
	}

	/** Dry ground near the player. Methane is never a landing. */
	private static @Nullable BlockPos landing(Player player) {
		Level level = player.level();
		BlockPos feet = player.blockPosition();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dx = -6; dx <= 6; dx++) {
			for (int dz = -6; dz <= 6; dz++) {
				for (int dy = 2; dy >= -4; dy--) {
					cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
					if (!WeaverLeapSpot.isDry(level, cursor)) {
						continue;
					}
					if (level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
						continue;
					}
					if (!level.getBlockState(cursor.above()).getCollisionShape(level, cursor.above()).isEmpty()) {
						continue;
					}
					if (!level.getBlockState(cursor.above(2)).getCollisionShape(level, cursor.above(2)).isEmpty()) {
						continue;
					}
					double dist = player.distanceToSqr(cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}
}
