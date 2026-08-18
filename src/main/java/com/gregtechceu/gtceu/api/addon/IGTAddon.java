package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTOreVeins;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

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
     * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g.
     *             {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerTagPrefixes() {}

    /**
     * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g.
     *             {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerElements() {}

    /**
     * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g.
     *             {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerSounds() {}

    /**
     * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g.
     *             {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerCovers() {}

    /**
     * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g.
     *             {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void registerRecipeCapabilities() {}

    /**
     * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g.
     *             {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
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

    default void removeRecipes(Consumer<ResourceLocation> consumer) {}

    /**
     * Use {@link GTOreVeins#create(ResourceLocation, Consumer)} to register the veins.
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
