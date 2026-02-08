package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ContentModifier(double multiplier, double addition) {

    public static final ContentModifier IDENTITY = new ContentModifier(1, 0);

    public static ContentModifier multiplier(double multiplier) {
        return multiplier == 1 ? IDENTITY : new ContentModifier(multiplier, 0);
    }

    public static ContentModifier addition(double addition) {
        return addition == 0 ? IDENTITY : new ContentModifier(1, addition);
    }

    public int apply(int number) {
        return (int) (number * multiplier + addition);
    }

    public long apply(long number) {
        return (long) (number * multiplier + addition);
    }

    public float apply(float number) {
        return (float) (number * multiplier + addition);
    }

    public double apply(double number) {
        return number * multiplier + addition;
    }

    /**
     * Applies this ContentModifier to all entries in the given Content map
     *
     * @param contents the content map to apply to
     * @return A new Content map that is the modified version of the argument
     */
    public Map<RecipeCapability<?>, List<Content>> applyContents(Map<RecipeCapability<?>, List<Content>> contents) {
        if (this == IDENTITY) return new HashMap<>(contents);
        Map<RecipeCapability<?>, List<Content>> copyContents = new HashMap<>();
        for (var entry : contents.entrySet()) {
            var contentList = entry.getValue();
            var cap = entry.getKey();
            if (contentList != null && !contentList.isEmpty()) {
                List<Content> contentsCopy = new ArrayList<>();
                for (Content content : contentList) {
                    contentsCopy.add(content.copy(cap, this));
                }
                copyContents.put(entry.getKey(), contentsCopy);
            }
        }
        return copyContents;
    }
}
