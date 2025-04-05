package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface IGTAddon {

    /**
     * @return this addon's GTRegistrate instance.
     *         remember to call{@link GTRegistrate#registerRegistrate} in your mod class!
     */
    GTRegistrate getRegistrate();

    /**
     * This runs after GTCEu has setup it's content.
     */
    void initializeAddon();

    /**
     * this addon's Mod id.
     * 
     * @return the Mod ID this addon uses for content.
     */
    String addonModId();

    /**
     * Call init on your custom TagPrefix class(es) here
     */
    default void registerTagPrefixes() {}

    /**
     * Call init on your custom Element class(es) here
     */
    default void registerElements() {}

    /**
     * Call init on your custom Material class(es) here
     * 
     * @deprecated use {@link com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent} and
     *             {@link com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent} instead.
     */
    @Deprecated(forRemoval = true, since = "1.0.21")
    default void registerMaterials() {}

    /**
     * Call init on your custom Sound class(es) here
     */
    default void registerSounds() {}

    /**
     * Call init on your custom Cover class(es) here
     */
    default void registerCovers() {}

    /**
     * Call init on your custom Recipe Capabilities here
     */
    default void registerRecipeCapabilities() {}

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

    default void addRecipes(Consumer<FinishedRecipe> provider) {}

    default void removeRecipes(Consumer<ResourceLocation> consumer) {}

    /**
     * Use {@link GTOres#create(ResourceLocation, Consumer)} to register the veins.
     */
    default void registerOreVeins() {}

    /**
     * Use {@link BedrockFluidDefinition#builder(ResourceLocation)} to register the veins.
     */
    default void registerFluidVeins() {}

    /**
     * Use {@link com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition#builder(ResourceLocation)} to
     * register the veins.
     */
    default void registerBedrockOreVeins() {}

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
