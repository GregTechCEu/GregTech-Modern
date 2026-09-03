package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MaterialRegistryEntry extends RegistryEntry<Material, Material> {

    public MaterialRegistryEntry(GTRegistrate owner, DeferredHolder<Material, Material> key) {
        super(owner, key);
    }

}
