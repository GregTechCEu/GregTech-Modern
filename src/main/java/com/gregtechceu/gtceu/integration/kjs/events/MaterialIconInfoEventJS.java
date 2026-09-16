package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.typings.Info;

@SuppressWarnings("unused")
public class MaterialIconInfoEventJS implements KubeStartupEvent {

    @Info("Create a new material icon type.")
    public MaterialIconType createIconType(String name) {
        return new MaterialIconType(name);
    }
}
