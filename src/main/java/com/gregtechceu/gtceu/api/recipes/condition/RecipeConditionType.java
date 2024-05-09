package com.gregtechceu.gtceu.api.recipes.condition;

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
        public T createDefault();
    }
}
