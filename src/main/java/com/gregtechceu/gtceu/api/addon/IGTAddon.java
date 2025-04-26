package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface IGTAddon {

    /**
     * @return this addon's GTRegistrate instance.
     */
    GTRegistrate getRegistrate();

    /**
     * This runs after GTCEu has set up it's content. Set up GT loading-dependent (but NOT ones dependent on
     * 
     * @apiNote DO NOT REGISTER ANY OF YOUR OWN CONTENT HERE, AS IF YOU DO, IT'LL REGISTER AS IF GTCEu REGISTERED IT
     *          AND YOUR DATAGEN AND EVENTS WILL <b><i>NOT</i></b> WORK AS EXPECTED, IF AT ALL.
     */
    void gtInitComplete();

    /**
     * Call init on your custom IWorldGenLayer class(es) here
     */
    default void registerWorldgenLayers() {}

    /**
     * Call init on your custom VeinGenerator class(es) here
     */
    default void registerVeinGenerators() {}

    /**
     * Call init on your custom IndicatorGenerator class(es) here
     */
    default void registerIndicatorGenerators() {}

    default void addRecipes(RecipeOutput provider) {}

    default void removeRecipes(Consumer<ResourceLocation> consumer) {}

    /**
     * Register Material -> Casing block mappings here
     */
    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {}

    default void registerRecipeKeys(KJSRecipeKeyEvent event) {}

    /**
     * Does this addon require high-tier content to be enabled?
     * 
     * @return if this addon requires highTier.
     */
    default boolean requiresHighTier() {
        return false;
    }
}
