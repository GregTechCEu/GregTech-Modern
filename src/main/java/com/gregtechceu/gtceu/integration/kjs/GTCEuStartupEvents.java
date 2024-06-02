package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.event.SpecializedEventHandler;

public interface GTCEuStartupEvents {

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    Extra<ResourceLocation> REGISTRY_EXTRA = Extra.create(ResourceLocation.class)
            .validator(GTCEuStartupEvents::validateRegistry);

    private static boolean validateRegistry(Object o) {
        try {
            var id = GTCEu.appendId(o.toString());
            return GTRegistry.REGISTERED.containsKey(id) || GTRegistryInfo.EXTRA_IDS.contains(id);
        } catch (Exception ex) {
            return false;
        }
    }

    SpecializedEventHandler<ResourceLocation> REGISTRY = GROUP.startup("registry", REGISTRY_EXTRA,
            () -> GTRegistryEventJS.class);
    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification", () -> MaterialModificationEventJS.class);
}
