package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import net.minecraft.network.FriendlyByteBuf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class RecipeSpoilageData {

    public static final Codec<RecipeSpoilageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RecipeCapability.INGREDIENT_CODEC.optionalFieldOf("consumedInputs", new HashMap<>())
                    .forGetter(RecipeSpoilageData::getConsumedInputs),
            Codec.BOOL.fieldOf("keepSpoilingProgress").forGetter(RecipeSpoilageData::keepSpoilingProgress))
            .apply(instance, RecipeSpoilageData::new));

    /**
     * Populated after the inputs are already consumed, but the recipe didn't start yet.
     * For use in {@link GTRecipe#outputModifier}
     */
    @Getter(AccessLevel.PRIVATE)
    private Map<RecipeCapability<?>, List<?>> consumedInputs;
    @Accessors(fluent = true)
    @Getter
    private boolean keepSpoilingProgress;

    public RecipeSpoilageData(boolean keepSpoilingProgress) {
        this(new HashMap<>(), keepSpoilingProgress);
    }

    public RecipeSpoilageData copy() {
        return new RecipeSpoilageData(new HashMap<>(consumedInputs), keepSpoilingProgress);
    }

    public <T> void addConsumedInput(RecipeCapability<T> recipeCapability, T t) {
        // noinspection unchecked why can't I just add whatever I want to a List<?>
        ((List<T>) consumedInputs.computeIfAbsent(recipeCapability, cap -> new ArrayList<>())).add(t);
    }

    @Unmodifiable
    public <T> List<T> getConsumedInputs(RecipeCapability<T> recipeCapability) {
        // noinspection unchecked
        return (List<T>) consumedInputs.getOrDefault(recipeCapability, List.of());
    }

    public static RecipeSpoilageData readFromNetwork(FriendlyByteBuf buf) {
        return new RecipeSpoilageData(new HashMap<>(), buf.readBoolean());
    }
}
