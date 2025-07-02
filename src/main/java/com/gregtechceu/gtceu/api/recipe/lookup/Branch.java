package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

public class Branch {

    // Keys on this have *(should)* have unique hashcodes.
    private Map<AbstractMapIngredient, Either<GTRecipe, Branch>> nodes;
    // Keys on this have collisions, and must be differentiated by equality.
    private Map<AbstractMapIngredient, Either<GTRecipe, Branch>> specialNodes;

    public Stream<GTRecipe> getRecipes(boolean filterHidden) {
        Stream<GTRecipe> stream = null;
        if (nodes != null) {
            stream = nodes.values().stream()
                    .flatMap(either -> either.map(Stream::of, right -> right.getRecipes(filterHidden)));
        }
        if (specialNodes != null) {
            if (stream == null) {
                stream = specialNodes.values().stream()
                        .flatMap(either -> either.map(Stream::of, right -> right.getRecipes(filterHidden)));
            } else {
                stream = Stream.concat(stream, specialNodes.values().stream()
                        .flatMap(either -> either.map(Stream::of, right -> right.getRecipes(filterHidden))));
            }
        }
        if (stream == null) {
            return Stream.empty();
        }
        if (filterHidden) {
            // stream = stream.filter(t -> !t.isHidden());
        }
        return stream;
    }

    public boolean isEmptyBranch() {
        return (nodes == null || nodes.isEmpty()) && (specialNodes == null || specialNodes.isEmpty());
    }

    @NotNull
    public Map<AbstractMapIngredient, Either<GTRecipe, Branch>> getNodes() {
        if (nodes == null) {
            nodes = new Object2ObjectOpenHashMap<>(2);
        }
        return nodes;
    }

    @NotNull
    public Map<AbstractMapIngredient, Either<GTRecipe, Branch>> getSpecialNodes() {
        if (specialNodes == null) {
            specialNodes = new Object2ObjectOpenHashMap<>(2);
        }
        return specialNodes;
    }
}
