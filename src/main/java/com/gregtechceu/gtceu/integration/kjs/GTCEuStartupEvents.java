package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.*;
import com.gregtechceu.gtceu.integration.kjs.events.CraftingComponentsEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialIconTypeEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface GTCEuStartupEvents {

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    EventHandler REGISTRY = GROUP.startup("registry", () -> GTRegistryEventJS.class).extra(Extra.REQUIRES_REGISTRY);
    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification", () -> MaterialModificationEventJS.class);
    EventHandler CRAFTING_COMPONENTS = GROUP.startup("craftingComponents", () -> CraftingComponentsEventJS.class);
    EventHandler MATERIAL_ICON_TYPE = GROUP.startup("materialIconType", () -> MaterialIconTypeEventJS.class);
    EventHandler MACHINE_MODIFICATION = GROUP.startup("machineModification", () -> ModifyMachineEventJS.class);
    EventHandler REGISTER_SPOILABLES = GROUP.startup("registerSpoilables", () -> RegisterSpoilablesEventJS.class);
}
