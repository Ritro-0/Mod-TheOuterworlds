package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class ModTrimMaterials {
	public static final ResourceKey<TrimMaterial> OLIVINE = key("olivine");
	public static final ResourceKey<TrimMaterial> OPAL = key("opal");
	public static final ResourceKey<TrimMaterial> JAROSITE = key("jarosite");
	public static final ResourceKey<TrimMaterial> GRAPHITE = key("graphite");

	private static ResourceKey<TrimMaterial> key(String name) {
		return ResourceKey.create(Registries.TRIM_MATERIAL, OuterWorldMod.id(name));
	}
}
