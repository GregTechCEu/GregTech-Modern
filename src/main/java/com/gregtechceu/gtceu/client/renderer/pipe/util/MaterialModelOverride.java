package com.gregtechceu.gtceu.client.renderer.pipe.util;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;

import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface MaterialModelOverride<T extends AbstractPipeModel<?>> {

    @Nullable
    T getModel(Material material, int i);

    record StandardOverride<T extends AbstractPipeModel<?>>(@NotNull T[] models,
                                                            @NotNull Predicate<Material> predicate)
            implements MaterialModelOverride<T> {

        @Override
        public @Nullable T getModel(Material material, int i) {
            if (material == null || !predicate.test(material)) return null;
            else return models[i];
        }
    }

    record PerMaterialOverride<T extends AbstractPipeModel<?>>(@NotNull Table<Material, Integer, T> models,
                                                               @NotNull BiFunction<Material, Integer, @NotNull T> createFunction,
                                                               @NotNull Predicate<Material> predicate)
            implements MaterialModelOverride<T> {

        @Override
        public @Nullable T getModel(Material material, int i) {
            if (material == null || !predicate.test(material)) return null;
            if (!models.contains(material, i)) {
                T model = createFunction.apply(material, i);
                models.put(material, i, model);
                return model;
            }
            return models.get(material, i);
        }
    }
}
