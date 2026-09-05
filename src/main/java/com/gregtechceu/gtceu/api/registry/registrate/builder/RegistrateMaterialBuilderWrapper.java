package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.MaterialBuilder;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;

import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;

import java.util.function.UnaryOperator;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class RegistrateMaterialBuilderWrapper extends
                                              AbstractBuilder<Material, Material, GTRegistrate, RegistrateMaterialBuilderWrapper> {

    private final UnaryOperator<MaterialBuilder> materialBuilderCallback;

    public RegistrateMaterialBuilderWrapper(GTRegistrate owner, String name, BuilderCallback callback,
                                            UnaryOperator<MaterialBuilder> materialBuilderCallback) {
        super(owner, owner, name, callback, GTRegistries.Keys.MATERIAL);
        this.materialBuilderCallback = materialBuilderCallback;
    }

    @Override
    public GTRegistrate getOwner() {
        return (GTRegistrate) super.getOwner();
    }

    @Override
    protected MaterialRegistryEntry createEntryWrapper(DeferredHolder<Material, Material> delegate) {
        return new MaterialRegistryEntry(getOwner(), delegate);
    }

    public MaterialRegistryEntry register() {
        return (MaterialRegistryEntry) super.register();
    }

    @Override
    protected Material createEntry() {
        return materialBuilderCallback.apply(new MaterialBuilder(getOwner().makeResourceLocation(getName())))
                .createMaterial();
    }
}
