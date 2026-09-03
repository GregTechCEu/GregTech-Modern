package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class MaterialIconSetBuilder extends BuilderBase<MaterialIconSet> {

    @Getter
    @Setter
    public @Nullable MaterialIconSet parentIconset;

    public MaterialIconSetBuilder(ResourceLocation id) {
        super(id);
        parentIconset = MaterialIconSet.DULL.value();
    }

    @Override
    public MaterialIconSet createObject() {
        return new MaterialIconSet(id,
                parentIconset == null ? null : GTRegistries.MATERIAL_ICON_SETS.wrapAsHolder(parentIconset),
                parentIconset == null);
    }
}
