package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import dev.latvian.mods.kubejs.event.EventJS;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote MaterialEventJS
 */
public class MaterialEventJS extends EventJS {

    public Material.Builder create(String name) {
        return new Material.Builder(name);
    }

    public boolean remove(String name) {
        return GTRegistries.MATERIALS.remove(name);
    }

    @Nullable
    public Material get(String name) {
        return GTRegistries.MATERIALS.get(name);
    }

    public void post() {
        GTCEuStartupEvents.MATERIAL.post(this);
    }
}
