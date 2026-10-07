package com.theouterworld.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Fires when a player is in, or arrives in, one charted body. */
public class VisitBodyTrigger extends SimpleCriterionTrigger<VisitBodyTrigger.TriggerInstance> {
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, ResourceKey<Level> dimension) {
		this.trigger(player, instance -> instance.dimension.equals(dimension));
	}

	public record TriggerInstance(Optional<Holder<LootItemCondition>> player, ResourceKey<Level> dimension)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(TriggerInstance::dimension)
			).apply(instance, TriggerInstance::new)
		);
	}
}
