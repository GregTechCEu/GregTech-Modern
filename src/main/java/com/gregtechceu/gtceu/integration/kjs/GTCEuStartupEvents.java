package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.integration.kjs.events.CraftingComponentsEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface GTCEuStartupEvents {

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    Extra REGISTRY_EXTRA = Extra.REQUIRES_STRING.copy().validator(GTCEuStartupEvents::validateRegistry);

    private static boolean validateRegistry(Object o) {
        try {
            var id = GTCEu.id(o.toString());
            return GTRegistry.REGISTERED.containsKey(id) || GTRegistryInfo.EXTRA_IDS.contains(id);
        } catch (Exception ex) {
            return false;
        }
    }

    EventHandler REGISTRY = GROUP.startup("registry", () -> GTRegistryEventJS.class).extra(REGISTRY_EXTRA);
    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification", () -> MaterialModificationEventJS.class);
    EventHandler CRAFTING_COMPONENTS = GROUP.startup("craftingComponents", () -> CraftingComponentsEventJS.class);
}
