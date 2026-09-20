package com.gregtechceu.gtceu.api.recipe.condition;

import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import com.mojang.serialization.MapCodec;

public class RecipeConditionType<T extends RecipeCondition<T>> {

    public final ConditionFactory<T> factory;
    public final MapCodec<T> codec;

    public RecipeConditionType(ConditionFactory<T> factory, MapCodec<T> codec) {
        this.factory = factory;
        this.codec = codec;
    }

    public MapCodec<T> getCodec() {
        return codec;
    }

    @FunctionalInterface
    public interface ConditionFactory<T extends RecipeCondition<T>> {

        T createDefault();
    }
}
