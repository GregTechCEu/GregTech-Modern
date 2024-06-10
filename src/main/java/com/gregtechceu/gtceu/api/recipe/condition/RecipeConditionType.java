package com.gregtechceu.gtceu.api.recipe.condition;

import com.mojang.serialization.MapCodec;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RecipeConditionType<T extends RecipeCondition> {

    public final ConditionFactory<T> factory;
    @Getter
    public final MapCodec<T> codec;

    @FunctionalInterface
    public interface ConditionFactory<T extends RecipeCondition> {

        T createDefault();
    }
}
