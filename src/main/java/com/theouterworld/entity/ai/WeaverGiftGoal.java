package com.theouterworld.entity.ai;

import com.theouterworld.block.WeaverNetBlockEntity;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.world.WeaverColonySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * When a colony has earned a gift and a player is nearby, one Weaver carries it over,
 * leaps, and lands beside them the same way it leaps onto its home plate. The wait to
 * hand the gift over starts only once that landing is close enough.
 */
public class WeaverGiftGoal extends Goal {
	public static final double PLAYER_RANGE = 100.0;
	private static final int OFFER_TIMEOUT = 600;
	private static final int ABSENT_TIMEOUT = 40;
	private static final double HANDOFF_DISTANCE = 3.0;
	private static final double APPROACH_SQR = (double) WeaverHomeLeap.APPROACH_XZ * WeaverHomeLeap.APPROACH_XZ;
	private static final double WALK_SPEED = 1.0;

	private final WeaverEntity weaver;
	private int offerTicks;
	private int absentTicks;
	private int leapCooldown;
	private long startedAt = Long.MAX_VALUE;
	private boolean finished;
	private @Nullable Player player;
	private @Nullable BlockPos landing;

	public WeaverGiftGoal(WeaverEntity weaver) {
		this.weaver = weaver;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!(weaver.level() instanceof ServerLevel level)) {
			return false;
		}
		if (weaver.isOfferingGift() && !weaver.getCarriedItem().isEmpty()) {
			return !weaver.isAggressive() && !weaver.isRetreating();
		}
		if (weaver.isBreeding() || weaver.isAggressive() || weaver.isRetreating() || weaver.isSleeping() || !weaver.getCarriedItem().isEmpty()) {
			return false;
		}
		if (!weaver.isBaby() && weaver.colonyHasBaby(level)) {
			return false;
		}
		long id = weaver.colonyId();
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		if (data.isHostile(id) || !data.hasPendingGift(id)) {
			return false;
		}
		if (level.getGameTime() < data.lastHarm(id) + 200) {
			return false;
		}
		return nearbyPlayer(level) != null;
	}

	@Override
	public boolean canContinueToUse() {
		return !finished && weaver.isOfferingGift() && !weaver.getCarriedItem().isEmpty();
	}

	@Override
	public void start() {
		finished = false;
		offerTicks = 0;
		absentTicks = 0;
		leapCooldown = 0;
		landing = null;
		if (!(weaver.level() instanceof ServerLevel level)) {
			finished = true;
			return;
		}
		startedAt = level.getGameTime();
		player = nearbyPlayer(level);
		if (!weaver.isOfferingGift()) {
			ItemStack gift = WeaverColonySavedData.get(level).claimGift(weaver.colonyId(), weaver.getUUID());
			if (gift.isEmpty()) {
				finished = true;
				return;
			}
			weaver.setCarriedItem(gift);
			weaver.setOfferingGift(true);
			level.playSound(
				null,
				weaver.getX(),
				weaver.getY(),
				weaver.getZ(),
				SoundEvents.EXPERIENCE_ORB_PICKUP,
				SoundSource.NEUTRAL,
				0.5F,
				0.8F
			);
		}
	}

	@Override
	public void stop() {
		weaver.getNavigation().stop();
		if (weaver.isHomeLeaping()) {
			weaver.endHomeLeap();
		}
		if (!weaver.isOfferingGift()) {
			return;
		}
		if (harmed() && weaver.level() instanceof ServerLevel level) {
			withdraw(level);
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!(weaver.level() instanceof ServerLevel level)) {
			finished = true;
			return;
		}
		if (harmed()) {
			withdraw(level);
			finished = true;
			return;
		}
		if (weaver.getCarriedItem().isEmpty()) {
			weaver.setOfferingGift(false);
			finished = true;
			return;
		}
		player = nearbyPlayer(level);
		if (player == null) {
			if (++absentTicks > ABSENT_TIMEOUT) {
				cancelOffer(level);
				finished = true;
			}
			return;
		}
		absentTicks = 0;
		weaver.getLookControl().setLookAt(player, 30.0F, 30.0F);
		if (weaver.isHomeLeaping()) {
			if (!weaver.onGround()) {
				BlockPos beside = this.landingBeside(level, player);
				if (beside != null) {
					this.landing = beside;
					weaver.steerHomeLeap(beside);
				}
				return;
			}
			weaver.endHomeLeap();
			this.leapCooldown = 8;
			return;
		}
		if (this.leapCooldown > 0) {
			this.leapCooldown--;
			return;
		}
		double horizontal = this.horizontalDistanceSqr(player);
		boolean inReach = horizontal <= HANDOFF_DISTANCE * HANDOFF_DISTANCE && weaver.onGround();
		if (inReach) {
			offerTicks++;
			weaver.getNavigation().stop();
			if (offerTicks > OFFER_TIMEOUT) {
				cancelOffer(level);
				finished = true;
			}
			return;
		}
		if (horizontal <= APPROACH_SQR && weaver.onGround()) {
			BlockPos beside = this.landingBeside(level, player);
			if (beside != null) {
				this.landing = beside;
				weaver.beginGiftLeap(beside);
			}
			return;
		}
		if (weaver.getNavigation().isDone()) {
			weaver.getNavigation().moveTo(player, WALK_SPEED);
		}
	}

	private double horizontalDistanceSqr(Player target) {
		double dx = target.getX() - weaver.getX();
		double dz = target.getZ() - weaver.getZ();
		return dx * dx + dz * dz;
	}

	/** Ground beside the player, the block the leap falls onto. Never methane. */
	private @Nullable BlockPos landingBeside(Level level, Player target) {
		BlockPos feet = target.blockPosition();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}
				for (int dy = 1; dy >= -2; dy--) {
					BlockPos ground = new BlockPos(feet.getX() + dx, feet.getY() + dy - 1, feet.getZ() + dz);
					BlockPos stand = ground.above();
					if (!WeaverLeapSpot.isDry(level, ground)) {
						continue;
					}
					if (level.getBlockState(ground).getCollisionShape(level, ground).isEmpty()) {
						continue;
					}
					if (!level.getBlockState(stand).getCollisionShape(level, stand).isEmpty()) {
						continue;
					}
					if (!level.getBlockState(stand.above()).getCollisionShape(level, stand.above()).isEmpty()) {
						continue;
					}
					double dxp = stand.getX() + 0.5 - target.getX();
					double dzp = stand.getZ() + 0.5 - target.getZ();
					double score = dxp * dxp + dzp * dzp;
					if (score < bestScore) {
						best = ground;
						bestScore = score;
					}
				}
			}
		}
		return best;
	}

	private boolean harmed() {
		if (weaver.isAggressive() || weaver.isRetreating()) {
			return true;
		}
		if (!(weaver.level() instanceof ServerLevel level)) {
			return false;
		}
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		long id = weaver.colonyId();
		return data.isHostile(id) || data.lastHarm(id) >= startedAt;
	}

	/** The offer is over. Another Weaver does not bring the same gift. */
	private void withdraw(ServerLevel level) {
		cancelOffer(level);
	}

	private void cancelOffer(ServerLevel level) {
		ItemStack carried = weaver.getCarriedItem().copy();
		weaver.setCarriedItem(ItemStack.EMPTY);
		weaver.setOfferingGift(false);
		if (carried.isEmpty()) {
			return;
		}
		BlockPos net = WeaverColonies.nearestOpenNet(
			level,
			weaver.position(),
			weaver.blockPosition(),
			weaver.colonyId(),
			carried
		);
		if (net != null && level.getBlockEntity(net) instanceof WeaverNetBlockEntity block) {
			carried = block.stow(carried);
		}
		if (!carried.isEmpty()) {
			weaver.spawnAtLocation(level, carried);
		}
	}

	private @Nullable Player nearbyPlayer(ServerLevel level) {
		WeaverColonySavedData data = WeaverColonySavedData.get(level);
		long id = weaver.colonyId();
		Player nearest = null;
		double nearestSqr = PLAYER_RANGE * PLAYER_RANGE;
		for (Player player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) {
				continue;
			}
			if (data.isUntrusted(id, player.getUUID())) {
				continue;
			}
			if (data.isWary(id, player.getUUID()) && !weaver.personallyTrusts(player.getUUID())) {
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
}
