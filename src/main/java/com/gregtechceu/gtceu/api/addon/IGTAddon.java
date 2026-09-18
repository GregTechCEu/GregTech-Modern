package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.Identifier;

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
     * 
     * @deprecated Subscribe to the {@code GTCEuAPI.RegisterEvent<Identifier, TagPrefix>} register event instead
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerTagPrefixes() {}

    /**
     * Call init on your custom Element class(es) here
     * 
     * @deprecated Subscribe to the {@code GTCEuAPI.RegisterEvent<Identifier, Element>} register event instead
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerElements() {}

    /**
     * Call init on your custom Sound class(es) here
     * 
     * @deprecated Subscribe to the {@code GTCEuAPI.RegisterEvent<Identifier, SoundEntry>} register event instead
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerSounds() {}

    /**
     * Call init on your custom Cover class(es) here
     * 
     * @deprecated Subscribe to the {@code GTCEuAPI.RegisterEvent<Identifier, CoverDefinition>} register event
     *             instead
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerCovers() {}

    /**
     * Call init on your custom Recipe Capabilities here
     * 
     * @deprecated Subscribe to the {@code GTCEuAPI.RegisterEvent<Identifier, RecipeCapability>} register event
     *             instead
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerRecipeCapabilities() {}

    /**
     * Call init on your custom IWorldGenLayer class(es) here
     *
     * @deprecated Subscribe to the {@code GTCEuAPI.RegisterEvent<Identifier, IWorldGenLayer>} register event
     *             instead
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
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

    default void removeRecipes(Consumer<Identifier> consumer) {}

    /**
     * Use {@link GTOres#create(Identifier, Consumer)} to register the veins.
     */
    default void registerOreVeins() {}

    /**
     * Use {@link BedrockFluidDefinition#builder(Identifier)} to register the veins.
     */
    default void registerFluidVeins() {}

    /**
     * Use {@link com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition#builder(Identifier)} to
     * register the veins.
     */
    default void registerBedrockOreVeins() {}

    /**
     * Register Material -> Casing block mappings here
     * 
     * @deprecated Subscribe to the {@link MaterialCasingCollectionEvent} directly.
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {}

    /**
     * @deprecated Subscribe to the {@link KJSRecipeKeyEvent} directly.
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
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
