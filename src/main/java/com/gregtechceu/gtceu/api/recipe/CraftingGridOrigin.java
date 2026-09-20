package com.gregtechceu.gtceu.api.recipe;

/** Original grid dimensions and the location of the trimmed input inside it. */
public record CraftingGridOrigin(int width, int height, int left, int top) {
    public interface Access {
        CraftingGridOrigin gtceu$getGridOrigin();

        void gtceu$setGridOrigin(CraftingGridOrigin origin);
    }
}
