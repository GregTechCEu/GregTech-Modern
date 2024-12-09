package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentModifier {

    public static final ContentModifier IDENTITY = new ContentModifier(1, 0);
    @Getter
    private double multiplier;
    @Getter
    private double addition;

    public static ContentModifier multiplier(double multiplier) {
        return new ContentModifier(multiplier, 0);
    }

    public static ContentModifier addition(double addition) {
        return new ContentModifier(1, addition);
    }

    public ContentModifier() {
        this(1, 0);
    }

    public ContentModifier(double multiplier, double addition) {
        this.multiplier = multiplier;
        this.addition = addition;
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

    public ContentModifier compose(ContentModifier that) {
        return new ContentModifier(this.multiplier * that.multiplier,
                this.multiplier * that.addition + this.addition);
    }

    public Map<RecipeCapability<?>, List<Content>> applyContents(Map<RecipeCapability<?>, List<Content>> contents) {
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

    public Map<RecipeCapability<?>, List<Content>> applyAllButEU(Map<RecipeCapability<?>, List<Content>> contents) {
        Map<RecipeCapability<?>, List<Content>> copyContents = new HashMap<>();
        for (var entry : contents.entrySet()) {
            var cap = entry.getKey();
            var contentList = entry.getValue();
            if (contentList != null && !contentList.isEmpty()) {
                if(cap == EURecipeCapability.CAP) {
                    copyContents.put(cap, contentList);
                    continue;
                }
                List<Content> contentsCopy = new ArrayList<>();
                for (Content content : contentList) {
                    contentsCopy.add(content.copy(cap, this));
                }
                copyContents.put(cap, contentsCopy);
            }
        }
        return copyContents;
    }
}
