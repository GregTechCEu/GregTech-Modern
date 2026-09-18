package com.gregtechceu.gtceu.api.registry.registrate;

import net.minecraft.resources.Identifier;

import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class BuilderBase<T> implements Supplier<T> {

    public Identifier id;
    protected T value = null;

    public BuilderBase(Identifier id) {
        this.id = id;
    }

    public void generateDataJsons(DataJsonGenerator generator) {}

    public void generateAssetJsons(@Nullable AssetJsonGenerator generator) {}

    public void generateLang(LangEventJS lang) {}

    public abstract T register();

    @Override
    public T get() {
        return value;
    }
}
