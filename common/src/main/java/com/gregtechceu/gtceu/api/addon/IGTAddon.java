package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;

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

    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {

    }
}
