package com.theouterworld.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class OceanPredation {
	private OceanPredation() {
	}

	public static void consume(LivingEntity eater, Entity prey) {
		eater.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 0.85F + eater.getRandom().nextFloat() * 0.3F);
		if (prey instanceof Player player && player.level() instanceof ServerLevel server) {
			player.kill(server);
		} else {
			prey.discard();
		}
	}
}
