package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;

import net.minecraft.resources.ResourceLocation;

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
    public MaterialIconSet register() {
        return value = new MaterialIconSet(this.id.getPath(), parent);
    }
}
