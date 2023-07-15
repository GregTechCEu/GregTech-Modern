package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public interface IGTAddon {

    void initializeAddon();

    default void registerTagPrefixes() {

    }

    default void registerElements() {

    }

    default void registerMaterials() {

    }

    default void registerSounds() {

    }

    default void registerCovers() {

    }

    default void registerRecipeTypes() {

    }

    default void registerMachines() {

    }

    default void registerWorldgenLayers() {

    }

    default void registerVeinGenerators() {

    }

    default void initializeRecipes(Consumer<FinishedRecipe> provider) {

    }

    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {

    }
}
