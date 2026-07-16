package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import dev.latvian.mods.kubejs.registry.BuilderBase;
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
    public MaterialIconSet createObject() {
        return new MaterialIconSet(this.id, parent);
    }
}
