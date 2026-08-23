package com.theouterworld.client;

import com.theouterworld.OuterWorldClient;
import com.theouterworld.registry.ModDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DustStormHandler {
	private static int distantLayerTickCounter = 0;
	private static int intermediateLayerTickCounter = 0;

	private static final int DISTANT_LAYER_TICK_INTERVAL = 3;
	private static final int INTERMEDIATE_LAYER_TICK_INTERVAL = 2;

	public static void register() {
		ClientTickEvents.END_LEVEL_TICK.register(DustStormHandler::onWorldTick);
	}

	private static void onWorldTick(ClientLevel world) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}

		if (!world.dimension().equals(ModDimensions.OUTERWORLD_WORLD_KEY)) {
			return;
		}

		if (!OuterWorldClient.isDustStormActive) {
			return;
		}

		float particleMultiplier = getParticleMultiplier(client);

		RandomSource random = world.getRandom();
		Vec3 eyePos = client.player.getEyePosition();

		distantLayerTickCounter++;
		intermediateLayerTickCounter++;

		if (distantLayerTickCounter >= DISTANT_LAYER_TICK_INTERVAL) {
			distantLayerTickCounter = 0;
			int particleCount = (int) (22 * particleMultiplier);
			for (int i = 0; i < particleCount; i++) {
				spawnParticle(world, eyePos, 55.0, random);
			}
		}

		if (intermediateLayerTickCounter >= INTERMEDIATE_LAYER_TICK_INTERVAL) {
			intermediateLayerTickCounter = 0;
			int particleCount = (int) (28 * particleMultiplier);
			for (int i = 0; i < particleCount; i++) {
				spawnParticle(world, eyePos, 18.0, random);
			}
		}

		int particleCount = (int) (38 * particleMultiplier);
		for (int i = 0; i < particleCount; i++) {
			spawnParticle(world, eyePos, 5.5, random);
		}
	}

	private static float getParticleMultiplier(Minecraft client) {
		Object particleValue = client.options.particles().get();
		String particleName = particleValue.toString();

		if (particleName.equalsIgnoreCase("ALL") || particleName.contains("ALL")) {
			return 1.0f;
		} else if (particleName.equalsIgnoreCase("DECREASED") || particleName.contains("DECREASED")) {
			return 0.5f;
		} else if (particleName.equalsIgnoreCase("MINIMAL") || particleName.contains("MINIMAL")) {
			return 0.2f;
		}

		return 1.0f;
	}

	private static void spawnParticle(ClientLevel world, Vec3 center, double radius, RandomSource random) {
		double theta = random.nextDouble() * Math.PI * 2.0;
		double phi = random.nextDouble() * Math.PI;
		double r = radius * Math.cbrt(random.nextDouble());

		double x = center.x + r * Math.sin(phi) * Math.cos(theta);
		double y = center.y + r * Math.cos(phi);
		double z = center.z + r * Math.sin(phi) * Math.sin(theta);

		if (InteriorShelterClient.isInterior(x, y, z)) {
			return;
		}

		BlockParticleOption particle = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.RED_SAND.defaultBlockState());

		world.addParticle(
			particle,
			true,
			false,
			x, y, z,
			0.0, 0.0, 0.0
		);
	}
}
