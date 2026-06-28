package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.typings.Info;

@SuppressWarnings("unused")
public class MaterialIconTypeEventJS extends StartupEventJS {

    @Info("Create a new material icon type.")
    public MaterialIconType create(String name) {
        return new MaterialIconType(name);
    }
}
