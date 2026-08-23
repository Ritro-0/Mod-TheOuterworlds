package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	private ModSounds() {}

	public static final SoundEvent KHARAX_IDLE = register("entity.kharax.idle");
	public static final SoundEvent KHARAX_HURT = register("entity.kharax.hurt");
	public static final SoundEvent KHARAX_DEATH = register("entity.kharax.death");
	public static final SoundEvent KHARAX_CLICKS = register("entity.kharax.clicks");

	private static SoundEvent register(String name) {
		Identifier id = OuterWorldMod.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void registerModSounds() {
		OuterWorldMod.LOGGER.info("Registering sounds for {}", OuterWorldMod.MOD_ID);
	}
}
