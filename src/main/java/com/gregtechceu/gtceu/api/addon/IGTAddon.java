package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTBedrockFluids;
import com.gregtechceu.gtceu.common.data.GTOreVeins;

import com.gregtechceu.gtceu.data.DataGenerators;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockFluidVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinEventJS;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.worldgen.BootstapContext;
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
     * @deprecated Your ore veins should now be registered as datapack content.<br>
     *             To do this via JSON, see {@link GTOreDefinition#DIRECT_CODEC}.<br>
     *             To do this via Java, see {@link GTOreVeins#bootstrap(BootstapContext)} and {@link DataGenerators} for a datagen example.<br>
     *             To do this via KubeJS, see {@link GTOreVeinEventJS}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    default void registerOreVeins() {}

    /**
     * @deprecated Your bedrock fluids should now be registered as datapack content.<br>
     *             To do this via JSON, see {@link BedrockFluidDefinition#DIRECT_CODEC}.<br>
     *             To do this via Java, see {@link GTBedrockFluids#bootstrap(BootstapContext)} and {@link DataGenerators} for a datagen example.<br>
     *             To do this via KubeJS, see {@link GTBedrockFluidVeinEventJS}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    default void registerFluidVeins() {}

    /**
     * @deprecated Your bedrock ore veins should now be registered as datapack content.<br>
     *             To do this via JSON, see {@link BedrockOreDefinition#DIRECT_CODEC}.<br>
     *             To do this via Java, see {@link GTOreVeins#bootstrap(BootstapContext)} and {@link DataGenerators} for a datagen example.<br>
     *             To do this via KubeJS, see {@link GTBedrockOreVeinEventJS}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
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
