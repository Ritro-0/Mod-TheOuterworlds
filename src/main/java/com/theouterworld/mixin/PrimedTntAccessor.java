package com.theouterworld.mixin;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PrimedTnt.class)
public interface PrimedTntAccessor {
	@Accessor("explosionPower")
	void theouterworlds$setExplosionPower(float explosionPower);

	@Accessor("owner")
	void theouterworlds$setOwner(EntityReference<LivingEntity> owner);
}
