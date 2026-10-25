package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class MaterialIconSetBuilderJS extends BuilderBase<MaterialIconSet> {

    @Getter
    public @Nullable Holder<MaterialIconSet> parentIconset = MaterialIconSet.DULL;

    public MaterialIconSetBuilderJS(ResourceLocation id) {
        super(id);
    }

    public MaterialIconSetBuilderJS parentIconset(@Nullable ResourceLocation parent) {
        this.parentIconset = parent == null ? null :
                DeferredHolder.create(GTRegistries.Keys.MATERIAL_ICON_SET, parent);
        return this;
    }

    @Override
    public MaterialIconSet createObject() {
        return new MaterialIconSet(id, parentIconset, parentIconset == null);
    }
}
