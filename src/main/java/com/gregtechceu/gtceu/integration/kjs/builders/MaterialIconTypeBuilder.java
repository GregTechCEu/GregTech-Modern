package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;

import net.minecraft.resources.ResourceLocation;

public class MaterialIconTypeBuilder extends BuilderBase<MaterialIconType> {

    public MaterialIconTypeBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public MaterialIconType register() {
        return new MaterialIconType(this.id.getPath());
    }
}
