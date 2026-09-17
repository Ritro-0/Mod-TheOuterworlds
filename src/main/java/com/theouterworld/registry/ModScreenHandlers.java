package com.theouterworld.registry;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.screen.AstralTelescopeMenu;
import com.theouterworld.screen.ForgePlateMenu;
import com.theouterworld.screen.KnappingTableScreenHandler;
import com.theouterworld.screen.ProcessorScreenHandler;
import com.theouterworld.screen.RiftPadMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    public static final MenuType<ProcessorScreenHandler> PROCESSOR_SCREEN_HANDLER = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "processor"),
        new MenuType<>(ProcessorScreenHandler::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<KnappingTableScreenHandler> KNAPPING_TABLE_SCREEN_HANDLER = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "knapping_table"),
        new MenuType<>(KnappingTableScreenHandler::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<ForgePlateMenu> FORGE_PLATE_SCREEN_HANDLER = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "forge_plate"),
        new MenuType<>(ForgePlateMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<AstralTelescopeMenu> ASTRAL_TELESCOPE_SCREEN_HANDLER = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "astral_telescope"),
        new MenuType<>(AstralTelescopeMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<RiftPadMenu> RIFT_PAD_SCREEN_HANDLER = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "rift_pad"),
        new MenuType<>(RiftPadMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<RiftPadMenu> QUANTUM_POD_SCREEN_HANDLER = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "quantum_pod"),
        new MenuType<>((id, inv) -> RiftPadMenu.quantumPod(id, inv), FeatureFlags.VANILLA_SET)
    );

    public static void registerModScreenHandlers() {
        OuterWorldMod.LOGGER.info("Registering screen handlers for " + OuterWorldMod.MOD_ID);
    }
}

