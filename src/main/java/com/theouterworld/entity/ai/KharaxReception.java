package com.theouterworld.entity.ai;

import com.theouterworld.entity.KharaxEntity;
import com.theouterworld.entity.KinKharaxEntity;
import com.theouterworld.entity.WeaverEntity;
import com.theouterworld.registry.ModEntities;
import com.theouterworld.registry.ModSounds;
import com.theouterworld.registry.ModTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A Kharax inside an Anchor's reach pulls the whole loaded colony off its work.
 * They ring it, inspect for a quarter minute, then either take a swing or adopt it.
 */
public final class KharaxReception {
	public static final int ANCHOR_RANGE = 50;
	public static final int INSPECT_TICKS = 300;
	private static final int GATHER_TIMEOUT = 400;
	private static final int SCAN_INTERVAL = 40;
	private static final int LEAVE_COOLDOWN = 400;

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	private static final Set<String> BUSY = new HashSet<>();

	private KharaxReception() {
	}

	public static void tick(KharaxEntity kharax) {
		if (!(kharax.level() instanceof ServerLevel level) || !kharax.isAlive()) {
			return;
		}
		Session session = SESSIONS.get(kharax.getUUID());
		if (session == null) {
			if (kharax.receptionScanCooldown > 0) {
				kharax.receptionScanCooldown--;
				return;
			}
			if (kharax.receptionCooldown > 0) {
				kharax.receptionCooldown--;
				return;
			}
			kharax.receptionScanCooldown = SCAN_INTERVAL;
			if (level.getNearestPlayer(kharax, 80.0) == null) {
				return;
			}
			tryStart(level, kharax);
			return;
		}
		if (!session.anchorAlive(level, kharax)) {
			cancel(kharax, LEAVE_COOLDOWN);
			return;
		}
		session.tick(level, kharax);
	}

	public static boolean isReceiving(KharaxEntity kharax) {
		return SESSIONS.containsKey(kharax.getUUID());
	}

	/** The Kharax is closing for the one swing that turns an inspection down. */
	public static boolean isLunging(KharaxEntity kharax) {
		Session session = SESSIONS.get(kharax.getUUID());
		return session != null && session.striking && !session.strikeLanded;
	}

	public static @Nullable LivingEntity lungeTarget(KharaxEntity kharax) {
		Session session = SESSIONS.get(kharax.getUUID());
		if (session == null || session.strikeWeaver == null || !(kharax.level() instanceof ServerLevel level)) {
			return null;
		}
		return level.getEntity(session.strikeWeaver) instanceof LivingEntity living && living.isAlive() ? living : null;
	}

	public static void landedStrike(ServerLevel level, KharaxEntity kharax) {
		Session session = SESSIONS.get(kharax.getUUID());
		if (session != null) {
			session.landStrike(level, kharax);
		}
	}

	public static @Nullable LivingEntity lookTarget(KharaxEntity kharax) {
		Session session = SESSIONS.get(kharax.getUUID());
		if (session == null || !(kharax.level() instanceof ServerLevel level)) {
			return null;
		}
		return session.lookTarget(level, kharax);
	}

	public static void cancel(KharaxEntity kharax) {
		cancel(kharax, 0);
	}

	private static void tryStart(ServerLevel level, KharaxEntity kharax) {
		Anchor anchor = findAnchor(level, kharax);
		if (anchor == null) {
			return;
		}
		String key = key(level, anchor.colony);
		if (BUSY.contains(key)) {
			return;
		}
		List<WeaverEntity> colony = colony(level, anchor.colony);
		if (colony.isEmpty()) {
			return;
		}
		BUSY.add(key);
		Session session = new Session(key, anchor.colony, anchor.pos);
		SESSIONS.put(kharax.getUUID(), session);
		kharax.finishRetreat();
		kharax.setWarning(false);
		kharax.stopWarningSound();
		kharax.setSpooking(false);
		kharax.setTarget(null);
		kharax.setScared(true);
		kharax.playSound(ModSounds.KHARAX_IDLE, 0.7F, 0.85F);
		session.summon(level, kharax, colony);
	}

	private static void cancel(KharaxEntity kharax, int cooldown) {
		Session session = SESSIONS.remove(kharax.getUUID());
		if (session == null) {
			return;
		}
		BUSY.remove(session.key);
		if (kharax.level() instanceof ServerLevel level) {
			session.release(level);
		}
		kharax.setScared(false);
		kharax.receptionCooldown = cooldown;
	}

	private static List<WeaverEntity> colony(ServerLevel level, long colonyId) {
		List<WeaverEntity> found = new ArrayList<>();
		for (Entity entity : level.getAllEntities()) {
			if (entity instanceof WeaverEntity weaver && weaver.isAlive() && weaver.colonyId() == colonyId) {
				found.add(weaver);
			}
		}
		return found;
	}

	private static @Nullable Anchor findAnchor(ServerLevel level, KharaxEntity kharax) {
		BlockPos origin = kharax.blockPosition();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		Anchor best = null;
		double bestDist = ANCHOR_RANGE * ANCHOR_RANGE;
		for (int dx = -ANCHOR_RANGE; dx <= ANCHOR_RANGE; dx += 5) {
			for (int dz = -ANCHOR_RANGE; dz <= ANCHOR_RANGE; dz += 5) {
				if (dx * dx + dz * dz > ANCHOR_RANGE * ANCHOR_RANGE) {
					continue;
				}
				for (int dy = -16; dy <= 48; dy += 4) {
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (!level.isLoaded(cursor) || !level.getBlockState(cursor).is(ModTags.WEAVER_ANCHOR_PARTS)) {
						continue;
					}
					long colony = WeaverColonies.colonyOwning(level, cursor);
					if (colony == 0L) {
						continue;
					}
					double dist = kharax.distanceToSqr(Vec3.atCenterOf(cursor));
					if (dist < bestDist) {
						bestDist = dist;
						best = new Anchor(colony, cursor.immutable());
					}
				}
			}
		}
		return best;
	}

	private static String key(ServerLevel level, long colony) {
		return level.dimension().identifier() + ":" + colony;
	}

	private static BlockPos standNear(Level level, KharaxEntity kharax, int index, int count) {
		double angle = (Math.PI * 2.0 * index / Math.max(1, count))
			+ (kharax.getRandom().nextDouble() - 0.5) * 0.8;
		double radius = 4.4 + kharax.getRandom().nextDouble() * 1.4;
		int x = Mth.floor(kharax.getX() + Math.cos(angle) * radius);
		int z = Mth.floor(kharax.getZ() + Math.sin(angle) * radius);
		int y = kharax.blockPosition().getY();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dy = 5; dy >= -8; dy--) {
			cursor.set(x, y + dy, z);
			if (standable(level, cursor)) {
				return cursor.immutable();
			}
		}
		return new BlockPos(x, y, z);
	}

	private static boolean standable(Level level, BlockPos feet) {
		for (int up = 0; up < 3; up++) {
			BlockPos pos = feet.above(up);
			BlockState state = level.getBlockState(pos);
			if (!state.getCollisionShape(level, pos).isEmpty() || !state.getFluidState().isEmpty()) {
				return false;
			}
		}
		BlockPos floor = feet.below();
		BlockState floorState = level.getBlockState(floor);
		return floorState.isFaceSturdy(level, floor, Direction.UP)
			|| !floorState.getCollisionShape(level, floor).isEmpty();
	}

	private record Anchor(long colony, BlockPos pos) {
	}

	private static final class Session {
		private final String key;
		private final long colony;
		private final BlockPos anchor;
		private final List<UUID> weavers = new ArrayList<>();
		private int ticks;
		private int lookIndex;
		private boolean inspecting;
		private boolean striking;
		private boolean strikeLanded;
		private int strikeTicks;
		private @Nullable UUID strikeWeaver;

		private Session(String key, long colony, BlockPos anchor) {
			this.key = key;
			this.colony = colony;
			this.anchor = anchor;
		}

		private boolean anchorAlive(ServerLevel level, KharaxEntity kharax) {
			return kharax.isAlive() && kharax.distanceToSqr(Vec3.atCenterOf(this.anchor)) <= (ANCHOR_RANGE + 8.0) * (ANCHOR_RANGE + 8.0);
		}

		private void tick(ServerLevel level, KharaxEntity kharax) {
			if (this.striking) {
				this.tickStrike(level, kharax);
				return;
			}
			this.ticks++;
			if (!this.inspecting) {
				if (this.ticks % 20 == 0) {
					this.summon(level, kharax, colony(level, this.colony));
				}
				if (this.gathered(level) || this.ticks >= GATHER_TIMEOUT) {
					this.inspecting = true;
					this.ticks = 0;
				}
				return;
			}
			if (this.ticks < INSPECT_TICKS) {
				return;
			}
			if (kharax.getRandom().nextBoolean()) {
				this.adopt(level, kharax);
			} else {
				this.reject(level, kharax);
			}
		}

		private void summon(ServerLevel level, KharaxEntity kharax, List<WeaverEntity> colony) {
			int count = Math.max(colony.size(), 1);
			int index = this.weavers.size();
			for (WeaverEntity weaver : colony) {
				if (!weaver.isAlive() || this.weavers.contains(weaver.getUUID())) {
					continue;
				}
				BlockPos stand = standNear(level, kharax, index, count);
				weaver.joinKharaxReception(kharax, stand);
				this.weavers.add(weaver.getUUID());
				index++;
			}
		}

		private boolean gathered(ServerLevel level) {
			if (this.weavers.isEmpty()) {
				return false;
			}
			for (UUID id : this.weavers) {
				if (!(level.getEntity(id) instanceof WeaverEntity weaver) || !weaver.isAlive()) {
					continue;
				}
				BlockPos stand = weaver.getReceptionStand();
				if (stand == null || weaver.distanceToSqr(Vec3.atBottomCenterOf(stand)) > 6.0) {
					return false;
				}
			}
			return true;
		}

		private @Nullable LivingEntity lookTarget(ServerLevel level, KharaxEntity kharax) {
			if (this.weavers.isEmpty()) {
				return null;
			}
			if (kharax.tickCount % 14 == 0) {
				this.lookIndex = (this.lookIndex + 1) % this.weavers.size();
			}
			for (int i = 0; i < this.weavers.size(); i++) {
				int index = (this.lookIndex + i) % this.weavers.size();
				if (level.getEntity(this.weavers.get(index)) instanceof LivingEntity living && living.isAlive()) {
					this.lookIndex = index;
					return living;
				}
			}
			return null;
		}

		private void reject(ServerLevel level, KharaxEntity kharax) {
			WeaverEntity chosen = null;
			for (UUID id : this.weavers) {
				if (level.getEntity(id) instanceof WeaverEntity weaver && weaver.isAlive()) {
					if (chosen == null || kharax.getRandom().nextBoolean()) {
						chosen = weaver;
					}
				}
			}
			if (chosen == null) {
				this.restartInspect(kharax);
				return;
			}
			this.striking = true;
			this.strikeLanded = false;
			this.strikeTicks = 0;
			this.strikeWeaver = chosen.getUUID();
			kharax.setScared(false);
			kharax.setTarget(chosen);
		}

		private void landStrike(ServerLevel level, KharaxEntity kharax) {
			if (this.strikeLanded) {
				return;
			}
			this.strikeLanded = true;
			this.strikeTicks = 0;
			kharax.setScared(true);
			kharax.finishRetreat();
			if (this.strikeWeaver != null && level.getEntity(this.strikeWeaver) instanceof WeaverEntity weaver && weaver.isAlive()) {
				weaver.beginReceptionRecoil();
			}
		}

		private void tickStrike(ServerLevel level, KharaxEntity kharax) {
			this.strikeTicks++;
			if (!this.strikeLanded) {
				if (this.strikeTicks > 80) {
					this.landStrike(level, kharax);
				}
				return;
			}
			WeaverEntity weaver = this.strikeWeaver != null && level.getEntity(this.strikeWeaver) instanceof WeaverEntity found
				? found
				: null;
			if (weaver == null || !weaver.isAlive()) {
				this.restartInspect(kharax);
				return;
			}
			if (weaver.isReceptionRecoiling() && this.strikeTicks < 200) {
				return;
			}
			BlockPos stand = weaver.getReceptionStand();
			if (stand != null
				&& weaver.distanceToSqr(Vec3.atBottomCenterOf(stand)) > 6.0
				&& this.strikeTicks < 200) {
				return;
			}
			this.restartInspect(kharax);
		}

		private void restartInspect(KharaxEntity kharax) {
			this.striking = false;
			this.strikeLanded = false;
			this.strikeTicks = 0;
			this.strikeWeaver = null;
			this.inspecting = true;
			this.ticks = 0;
			kharax.setScared(true);
			kharax.finishRetreat();
		}

		private void adopt(ServerLevel level, KharaxEntity kharax) {
			this.release(level);
			end(kharax, 0);
			KinKharaxEntity kin = ModEntities.KIN_KHARAX.create(level, EntitySpawnReason.CONVERSION);
			if (kin == null) {
				return;
			}
			kin.setPos(kharax.getX(), kharax.getY(), kharax.getZ());
			kin.setYRot(kharax.getYRot());
			kin.setXRot(kharax.getXRot());
			kin.setYBodyRot(kharax.yBodyRot);
			kin.setYHeadRot(kharax.yHeadRot);
			float scaled = kharax.getHealth() / Math.max(1.0F, kharax.getMaxHealth()) * kin.getMaxHealth();
			kin.setHealth(scaled);
			if (kharax.hasCustomName()) {
				kin.setCustomName(kharax.getCustomName());
				kin.setCustomNameVisible(kharax.isCustomNameVisible());
			}
			kin.assignColony(this.colony);
			kin.planDen(this.anchor);
			level.addFreshEntity(kin);
			Entity holder = kharax.getLeashHolder();
			if (holder != null) {
				kharax.removeLeash();
				if (holder.isAlive()) {
					kin.setLeashedTo(holder, true);
				}
			}
			level.sendParticles(ParticleTypes.HEART, kin.getX(), kin.getY() + 1.1, kin.getZ(), 10, 0.45, 0.4, 0.45, 0.02);
			level.playSound(null, kin.blockPosition(), ModSounds.WEAVER_IDLE, SoundSource.NEUTRAL, 0.9F, 1.15F);
			grant(level, kharax, holder);
			kharax.discard();
		}

		private void grant(ServerLevel level, KharaxEntity kharax, @Nullable Entity holder) {
			if (holder instanceof ServerPlayer player) {
				com.theouterworld.advancement.ModAdvancements.onKharaxAdopted(player);
				return;
			}
			for (ServerPlayer player : level.players()) {
				if (!player.isSpectator() && player.distanceToSqr(kharax) <= 12.0 * 12.0) {
					com.theouterworld.advancement.ModAdvancements.onKharaxAdopted(player);
				}
			}
		}

		private void release(ServerLevel level) {
			this.releaseExcept(level, null);
		}

		private void releaseExcept(ServerLevel level, @Nullable WeaverEntity keep) {
			for (UUID id : this.weavers) {
				if (level.getEntity(id) instanceof WeaverEntity weaver && weaver != keep) {
					weaver.clearKharaxReception();
				}
			}
		}

		private void end(KharaxEntity kharax, int cooldown) {
			SESSIONS.remove(kharax.getUUID());
			BUSY.remove(this.key);
			kharax.setScared(false);
			kharax.receptionCooldown = cooldown;
		}
	}
}
