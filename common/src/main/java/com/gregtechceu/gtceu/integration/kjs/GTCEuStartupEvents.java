package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.events.GTModificationEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public interface GTCEuStartupEvents {
    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    Extra REGISTRY_EXTRA = Extra.REQUIRES_STRING.copy().validator(GTCEuStartupEvents::validateRegistry);
    private static boolean validateRegistry(Object o) {
        try {
            var id = GTCEu.appendId(o.toString());
            return GTRegistries.REGISTRIES.containKey(id) || GTRegistryObjectBuilderTypes.EXTRA_IDS.contains(id);
        } catch (Exception ex) {
            return false;
        }
    }

    EventHandler REGISTRY = GROUP.startup("registry", () -> GTRegistryEventJS.class).extra(REGISTRY_EXTRA);
    EventHandler MODIFICATION = GROUP.startup("modification", () -> GTModificationEventJS.class).extra(REGISTRY_EXTRA);

}