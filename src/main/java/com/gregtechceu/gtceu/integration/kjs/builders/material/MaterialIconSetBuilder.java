package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.BuilderBase;

public class MaterialIconSetBuilder extends BuilderBase<MaterialIconSet> {

    private transient MaterialIconSet parent;

    public MaterialIconSetBuilder(ResourceLocation id) {
        super(id);
        parent = MaterialIconSet.DULL;
    }

    public MaterialIconSetBuilder parent(MaterialIconSet parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public MaterialIconSet createObject() {
        return new MaterialIconSet(this.id, parent);
    }
}
