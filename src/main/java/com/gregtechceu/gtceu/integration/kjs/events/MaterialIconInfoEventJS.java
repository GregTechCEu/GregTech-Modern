package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.typings.Info;

@SuppressWarnings("unused")
public class MaterialIconInfoEventJS implements KubeStartupEvent {

    @Info("Create a new material icon set with the default parent.")
    public MaterialIconSet createIconSet(String name) {
        return new MaterialIconSet(name);
    }

    @Info("Create a new material icon set with a specific parent.")
    public MaterialIconSet createIconSet(String name, MaterialIconSet parent) {
        return new MaterialIconSet(name, parent);
    }

    @Info("Create a new material icon type.")
    public MaterialIconType createIconType(String name) {
        return new MaterialIconType(name);
    }
}
