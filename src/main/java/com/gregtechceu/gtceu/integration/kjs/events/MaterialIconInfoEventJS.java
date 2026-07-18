package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.typings.Info;

@SuppressWarnings("unused")
public class MaterialIconInfoEventJS implements KubeStartupEvent {

    @Info("Create a new material icon set with the default parent.")
    public MaterialIconSet createIconSet(GTResourceLocation name) {
        return new MaterialIconSet(name.wrapped());
    }

    @Info("Create a new material icon set with a specific parent.")
    public MaterialIconSet createIconSet(GTResourceLocation name, MaterialIconSet parent) {
        return new MaterialIconSet(name.wrapped(), parent);
    }

    @Info("Create a new material icon type.")
    public MaterialIconType createIconType(String name) {
        return new MaterialIconType(name);
    }
}
