package com.theouterworld.entity;

import java.util.List;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Death lines for a killing blow from a Weaver or a Kharax.
 * A second insect in the same fight replaces the usual pool.
 */
public final class WeaverKharaxDeathMessages {
	private static final String[] WEAVER = {
		"death.attack.theouterworlds.weaver.concept",
		"death.attack.theouterworlds.weaver.subscription",
		"death.attack.theouterworlds.weaver.atoms",
		"death.attack.theouterworlds.weaver.ceased",
		"death.attack.theouterworlds.weaver.grave"
	};
	private static final String[] KHARAX = {
		"death.attack.theouterworlds.kharax.chomped",
		"death.attack.theouterworlds.kharax.apologies",
		"death.attack.theouterworlds.kharax.waste",
		"death.attack.theouterworlds.kharax.meal"
	};
	private static final String BOTH = "death.attack.theouterworlds.alien_insects";

	private WeaverKharaxDeathMessages() {
	}

	public static @Nullable Component forKill(LivingEntity victim, List<CombatEntry> entries) {
		if (entries.isEmpty()) {
			return null;
		}
		Kind killer = kind(attacker(entries.get(entries.size() - 1)));
		if (killer == null) {
			return null;
		}
		for (CombatEntry entry : entries) {
			Kind other = kind(attacker(entry));
			if (other != null && other != killer) {
				return Component.translatable(BOTH, victim.getDisplayName());
			}
		}
		String[] lines = killer == Kind.WEAVER ? WEAVER : KHARAX;
		int index = new Random(victim.getId() * 31L + victim.tickCount).nextInt(lines.length);
		return Component.translatable(lines[index], victim.getDisplayName());
	}

	private static @Nullable Entity attacker(CombatEntry entry) {
		Entity entity = entry.source().getEntity();
		return entity != null ? entity : entry.source().getDirectEntity();
	}

	private static @Nullable Kind kind(@Nullable Entity entity) {
		if (entity instanceof KinKharaxEntity || entity instanceof KharaxEntity) {
			return Kind.KHARAX;
		}
		if (entity instanceof WeaverEntity) {
			return Kind.WEAVER;
		}
		return null;
	}

	private enum Kind {
		WEAVER,
		KHARAX
	}
}
