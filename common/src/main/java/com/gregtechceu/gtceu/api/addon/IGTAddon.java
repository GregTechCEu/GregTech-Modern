package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public interface IGTAddon {

    /**
     * You can freely initialize blocks/items/etc. here, this runs after GTCEu has setup it's content
     */
    void initializeAddon();

    /**
     * this addon's Mod id.
     * @return the Mod ID this addon uses for content.
     */
    String addonModId();

    /**
     * Call init on your custom TagPrefix class(es) here
     */
    default void registerTagPrefixes() {

    }

    /**
     * Call init on your custom Element class(es) here
     */
    default void registerElements() {

    }

    /**
     * Call init on your custom Material class(es) here
     */
    default void registerMaterials() {

    }

    /**
     * Call init on your custom Sound class(es) here
     */
    default void registerSounds() {

    }

    /**
     * Call init on your custom Cover class(es) here
     */
    default void registerCovers() {

    }

    /**
     * Call init on your custom RecipeType class(es) here
     */
    default void registerRecipeTypes() {

    }

    /**
     * Call init on your custom Machine class(es) here
     */
    default void registerMachines() {

    }

    /**
     * Call init on your custom IWorldGenLayer class(es) here
     */
    default void registerWorldgenLayers() {

    }

    /**
     * Call init on your custom VeinGenerator class(es) here
     */
    default void registerVeinGenerators() {

    }

    default void initializeRecipes(Consumer<FinishedRecipe> provider) {

    }

    /**
     * Register Material -> Casing block mappings here
     */
    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {

    }

    default void registerRecipeKeys(KJSRecipeKeyEvent event) {

    }
}
