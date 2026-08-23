package com.theouterworld.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Invoker("getDefaultGravity")
	double invokeGetGravity();

	@Accessor("level")
	Level accessor$getWorld();

	@Invoker("getSharedFlag")
	boolean invokeGetFlag(int flag);
}
