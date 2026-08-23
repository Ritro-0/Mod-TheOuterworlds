package com.theouterworld.mixin;

import com.theouterworld.registry.ModDecoratedPotPatterns;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DecoratedPotPatterns.class)
public class DecoratedPotPatternsMixin {
	@Inject(method = "itemToPatternMappings", at = @At("TAIL"))
	private static void theouterworlds$addRoverSherd(
		BiConsumer<ResourceKey<Item>, ResourceKey<DecoratedPotPattern>> itemToPattern,
		CallbackInfo ci
	) {
		itemToPattern.accept(ModDecoratedPotPatterns.ROVER_POTTERY_SHERD, ModDecoratedPotPatterns.ROVER);
	}
}
