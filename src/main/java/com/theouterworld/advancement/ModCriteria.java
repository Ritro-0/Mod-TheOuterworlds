package com.theouterworld.advancement;

import com.theouterworld.OuterWorldMod;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModCriteria {
	public static final ModPlayerTrigger POWER_BEACON_CONCENTRATOR = register("power_beacon_concentrator", new ModPlayerTrigger());
	public static final ModPlayerTrigger START_PROCESSOR = register("start_processor", new ModPlayerTrigger());
	public static final ModPlayerTrigger EXTRACT_PROCESSOR_IRON = register("extract_processor_iron", new ModPlayerTrigger());
	public static final ModPlayerTrigger BRUSH_SUSPICIOUS_REGOLITH = register("brush_suspicious_regolith", new ModPlayerTrigger());
	public static final ModPlayerTrigger EMERGENCY_RETURN = register("emergency_return", new ModPlayerTrigger());
	public static final ModPlayerTrigger USE_ASTRAL_TELESCOPE = register("use_astral_telescope", new ModPlayerTrigger());
	public static final ModPlayerTrigger WITNESS_RIFT_CHARGE = register("witness_rift_charge", new ModPlayerTrigger());
	public static final ModPlayerTrigger ENTER_OUTERWORLD = register("enter_outerworld", new ModPlayerTrigger());
	public static final ModPlayerTrigger DUST_STORM_EXPOSURE = register("dust_storm_exposure", new ModPlayerTrigger());
	public static final ModPlayerTrigger DUST_STORM_NEGATED = register("dust_storm_negated", new ModPlayerTrigger());
	public static final ModPlayerTrigger AEROSTAT_ASCENT = register("aerostat_ascent", new ModPlayerTrigger());
	public static final ModPlayerTrigger QUANTUM_TRAVEL = register("quantum_travel", new ModPlayerTrigger());
	public static final ModPlayerTrigger CANCEL_SUN_VISIT = register("cancel_sun_visit", new ModPlayerTrigger());
	public static final ModPlayerTrigger WEAVER_GIFT = register("weaver_gift", new ModPlayerTrigger());
	public static final ModPlayerTrigger KHARAX_ADOPTED = register("kharax_adopted", new ModPlayerTrigger());
	public static final ModPlayerTrigger MOON_VOID_FALL = register("moon_void_fall", new ModPlayerTrigger());
	public static final VisitBodyTrigger VISIT_BODY = register("visit_body", new VisitBodyTrigger());

	private ModCriteria() {
	}

	public static void register() {
		// Class initialization registers the triggers.
	}

	private static <T extends CriterionTrigger<?>> T register(String path, T trigger) {
		return Registry.register(BuiltInRegistries.TRIGGER_TYPES, OuterWorldMod.id(path), trigger);
	}
}
