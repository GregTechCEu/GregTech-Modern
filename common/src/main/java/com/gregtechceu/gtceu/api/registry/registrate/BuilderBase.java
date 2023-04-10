package com.gregtechceu.gtceu.api.registry.registrate;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public abstract class BuilderBase<T> implements Supplier<T> {

    public ResourceLocation id;

    public BuilderBase(ResourceLocation id) {
        this.id = id;
    }

    public abstract T register();
}
