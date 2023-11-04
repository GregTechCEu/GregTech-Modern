package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface IGTAddon {

    /**
     * This runs after GTCEu has setup it's content.
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
     * Call init on your custom IWorldGenLayer class(es) here
     */
    default void registerWorldgenLayers() {

    }

    /**
     * Call init on your custom VeinGenerator class(es) here
     */
    default void registerVeinGenerators() {

    }

    default void addRecipes(Consumer<FinishedRecipe> provider) {

    }

    default void removeRecipes(Consumer<ResourceLocation> consumer) {

    }

    /**
     * Register Material -> Casing block mappings here
     */
    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {

    }

    default void registerRecipeKeys(KJSRecipeKeyEvent event) {

    }

    /**
     * Does this addon require high-tier content to be enabled?
     * @return if this addon requires highTier.
     */
    default boolean requiresHighTier() {
        return false;
    }
}
