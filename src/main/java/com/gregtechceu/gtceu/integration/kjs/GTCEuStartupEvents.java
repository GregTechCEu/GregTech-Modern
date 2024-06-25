package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;

public interface GTCEuStartupEvents {

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    EventTargetType<ResourceLocation> REGISTRY_EXTRA = EventTargetType.create(ResourceLocation.class)
            .validator(GTCEuStartupEvents::validateRegistry);

    private static boolean validateRegistry(Object o) {
        try {
            var id = GTCEu.appendId(o.toString());
            return GTRegistry.REGISTERED.containsKey(id) || GTRegistryInfo.EXTRA_IDS.contains(id);
        } catch (Exception ex) {
            return false;
        }
    }

    TargetedEventHandler<ResourceLocation> REGISTRY = GROUP.startup("registry", () -> GTRegistryEventJS.class)
            .requiredTarget(REGISTRY_EXTRA);
    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification", () -> MaterialModificationEventJS.class);
}
