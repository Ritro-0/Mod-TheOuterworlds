package com.theouterworld.world;

import com.theouterworld.item.ModItems;
import com.theouterworld.registry.ModDamageTypes;
import com.theouterworld.registry.ModDimensions;
import com.theouterworld.util.GraphiteProtection;
import com.theouterworld.util.IridiumProtection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Scorching solar exposure in the Innerworld when the player has sky access during
 * the stretched daytime, and ambient heat in the Nearworld / Emberworld. Redsteel chest/legs/boots
 * each cut heat by one third; a full set (helmet slot reserved for glass/Opal Lens)
 * blocks all heat damage. Graphite trim (any 1 piece) or Iridium armor (any 1 piece)
 * also fully blocks heat.
 */
public final class SolarIrradiation {
	/** Seconds of open sky to reach full intensity. */
	private static final float RAMP_SECONDS = 8.0F;
	private static final float NEARWORLD_RAMP_SECONDS = 5.0F;
	private static final float MAX_DAMAGE = 1.5F;
	private static final float NEARWORLD_MAX_DAMAGE = 2.0F;
	private static final int DAMAGE_INTERVAL_TICKS = 20;
	private static final int MAX_EXPOSURE_TICKS = (int) (RAMP_SECONDS * 20.0F);
	private static final int NEARWORLD_MAX_EXPOSURE_TICKS = (int) (NEARWORLD_RAMP_SECONDS * 20.0F);

	private static final Map<UUID, Integer> EXPOSURE_TICKS = new HashMap<>();

	private SolarIrradiation() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
			Iterator<Map.Entry<UUID, Integer>> it = EXPOSURE_TICKS.entrySet().iterator();
			while (it.hasNext()) {
				UUID id = it.next().getKey();
				if (server.getPlayerList().getPlayer(id) == null) {
					it.remove();
				}
			}
		});
	}

	public static boolean isExposed(LivingEntity entity) {
		Level level = entity.level();
		if (level == null) {
			return false;
		}
		if (ModDimensions.isNearworld(level.dimension()) || ModDimensions.isEmberworld(level.dimension())) {
			return true;
		}
		if (!ModDimensions.isInnerworld(level.dimension())) {
			return false;
		}
		if (!InnerworldDayCycle.isSunUp(level)) {
			return false;
		}
		BlockPos eye = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
		return level.canSeeSky(eye);
	}

	/** 0–3 pieces that count toward heat shielding (chest, legs, boots). */
	public static int countRedsteelPieces(LivingEntity entity) {
		int count = 0;
		if (isRedsteel(entity.getItemBySlot(EquipmentSlot.CHEST), ModItems.REDSTEEL_CHESTPLATE)) {
			count++;
		}
		if (isRedsteel(entity.getItemBySlot(EquipmentSlot.LEGS), ModItems.REDSTEEL_LEGGINGS)) {
			count++;
		}
		if (isRedsteel(entity.getItemBySlot(EquipmentSlot.FEET), ModItems.REDSTEEL_BOOTS)) {
			count++;
		}
		return count;
	}

	/** Remaining heat fraction after armor: iridium (any 1 piece) or graphite trim (any 1 piece)
	 * fully blocks heat; otherwise redsteel chest/legs/boots each cut heat by one third. */
	public static float heatMultiplier(LivingEntity entity) {
		if (IridiumProtection.hasIridiumArmor(entity)
			|| GraphiteProtection.hasHeatShield(entity)) {
			return 0.0F;
		}
		return 1.0F - countRedsteelPieces(entity) / 3.0F;
	}

	public static float getIntensity(Player player, float partialTick) {
		Integer ticks = EXPOSURE_TICKS.get(player.getUUID());
		if (ticks == null) {
			return 0.0F;
		}
		int maxTicks = player.level() != null
			&& (ModDimensions.isNearworld(player.level().dimension()) || ModDimensions.isEmberworld(player.level().dimension()))
			? NEARWORLD_MAX_EXPOSURE_TICKS
			: MAX_EXPOSURE_TICKS;
		return Mth.clamp((ticks + partialTick) / (float) maxTicks, 0.0F, 1.0F);
	}

	private static boolean isRedsteel(ItemStack stack, Item item) {
		return !stack.isEmpty() && stack.is(item);
	}

	private static void tickPlayer(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		boolean inner = ModDimensions.isInnerworld(level.dimension());
		boolean near = ModDimensions.isNearworld(level.dimension()) || ModDimensions.isEmberworld(level.dimension());
		if (!inner && !near) {
			EXPOSURE_TICKS.remove(player.getUUID());
			return;
		}

		GameType mode = player.gameMode();
		if (mode == null || !mode.isSurvival() || player.getAbilities().invulnerable || !player.isAlive()) {
			EXPOSURE_TICKS.remove(player.getUUID());
			return;
		}

		int maxTicks = near ? NEARWORLD_MAX_EXPOSURE_TICKS : MAX_EXPOSURE_TICKS;
		float maxDamage = near ? NEARWORLD_MAX_DAMAGE : MAX_DAMAGE;

		float heatMult = heatMultiplier(player);
		boolean exposed = heatMult > 0.0F && isExposed(player);
		int ticks = EXPOSURE_TICKS.getOrDefault(player.getUUID(), 0);
		if (exposed) {
			ticks = Math.min(maxTicks, ticks + 1);
		} else {
			ticks = Math.max(0, ticks - 2);
		}
		if (ticks <= 0) {
			EXPOSURE_TICKS.remove(player.getUUID());
		} else {
			EXPOSURE_TICKS.put(player.getUUID(), ticks);
		}

		if (!exposed || ticks < 8 || heatMult <= 0.0F) {
			return;
		}
		if (player.tickCount % DAMAGE_INTERVAL_TICKS != 0) {
			return;
		}

		float intensity = ticks / (float) maxTicks;
		float damage = maxDamage * intensity * intensity * heatMult;
		if (damage <= 0.01F) {
			return;
		}
		player.hurtServer(level, player.damageSources().source(ModDamageTypes.SOLAR_IRRADIATION), damage);
	}
}
