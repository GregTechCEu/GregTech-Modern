package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.*;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;

public interface GTCEuStartupEvents {

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    EventHandler MATERIAL_ICON_INFO = GROUP.startup("materialIconInfo", () -> MaterialIconInfoEventJS.class);
    EventHandler WORLD_GEN_LAYERS = GROUP.startup("worldGenLayers", () -> WorldGenLayerEventJS.class);

    TargetedEventHandler<ResourceKey<Registry<?>>> REGISTRY = GROUP.startup("registry", () -> GTRegistryKubeEvent.class)
            .requiredTarget(EventTargetType.REGISTRY);
    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification",
            () -> MaterialModificationEventJS.class);
    EventHandler CRAFTING_COMPONENTS = GROUP.startup("craftingComponents", () -> CraftingComponentsEventJS.class);

    EventHandler REGISTER_WOODS = GROUP.startup("registerWoods", () -> RegisterWoodsEventJS.class);
}
