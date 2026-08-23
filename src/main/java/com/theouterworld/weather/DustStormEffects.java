package com.theouterworld.weather;

import com.theouterworld.util.GlassHelmetUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Server-side handler that applies dust storm effects (slowness, mining fatigue)
 * to players when they're exposed to an active dust storm outside marked interiors.
 */
public class DustStormEffects {
	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				ServerLevel serverWorld = (ServerLevel) player.level();
				DustStormManager manager = DustStormTicker.getManager(serverWorld);

				boolean exposed = manager != null
					&& manager.affectsEntity(serverWorld, player)
					&& !player.isSpectator()
					&& !player.isCreative()
					&& !GlassHelmetUtil.isWearingDustStormProtection(player);

				if (exposed) {
					DustStormExposure.enterDuststorm(player);

					float intensity = DustStormExposure.getIntensity(player, 0f);
					if (intensity > 0.1f) {
						player.addEffect(new MobEffectInstance(
							MobEffects.SLOWNESS,
							25,
							4,
							false,
							false,
							false
						));

						player.addEffect(new MobEffectInstance(
							MobEffects.MINING_FATIGUE,
							25,
							2,
							false,
							false,
							false
						));
					}
				} else {
					DustStormExposure.exitDuststorm(player);
				}

				DustStormExposure.serverTick(player);
			}
		});
	}
}
