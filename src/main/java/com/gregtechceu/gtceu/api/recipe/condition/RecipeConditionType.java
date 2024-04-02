package com.gregtechceu.gtceu.api.recipe.condition;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RecipeConditionType<T extends RecipeCondition> {
    public ConditionFactory<T> factory;
    @Getter
    public Codec<T> codec;

    @FunctionalInterface
    public interface ConditionFactory<T extends RecipeCondition> {
        public T createDefault();
    }
}
