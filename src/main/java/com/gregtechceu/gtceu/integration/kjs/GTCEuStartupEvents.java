package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.*;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GTCEuStartupEvents {

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    EventHandler MATERIAL_ICON_INFO = GROUP.startup("materialIconInfo", () -> MaterialIconInfoKubeEvent.class);
    EventHandler WORLD_GEN_LAYERS = GROUP.startup("worldGenLayers", () -> WorldGenLayerKubeEvent.class);

    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification",
            () -> MaterialModificationKubeEvent.class);
    EventHandler CRAFTING_COMPONENTS = GROUP.startup("craftingComponents", () -> CraftingComponentsKubeEvent.class);
}
