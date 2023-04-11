package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import net.minecraft.resources.ResourceLocation;

public interface GTCEuStartupEvents {
    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    Extra REGISTRY_EXTRA = Extra.REQUIRES_ID.validator(GTCEuStartupEvents::validateRegistry);
    private static boolean validateRegistry(Object o) {
        try {
            return GTRegistries.REGISTRIES.containKey(GTCEu.appendId(o.toString()));
        } catch (Exception ex) {
            return false;
        }
    }
    EventHandler REGISTRY = GROUP.startup("registry", () -> GTRegistryEventJS.class).extra(REGISTRY_EXTRA);

}