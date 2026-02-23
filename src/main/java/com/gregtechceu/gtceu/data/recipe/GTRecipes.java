package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.material.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialFlags;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeAddition;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.gregtechceu.gtceu.data.recipe.generated.*;
import com.gregtechceu.gtceu.data.recipe.misc.*;
import com.gregtechceu.gtceu.data.recipe.serialized.chemistry.ChemistryRecipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

public class GTRecipes {

    public static final Set<ResourceLocation> RECIPE_FILTERS = new ObjectOpenHashSet<>();

    /*
     * Called on resource reload in-game.
     *
     * These methods are meant for recipes that cannot be reasonably changed by a Datapack,
     * such as "X Ingot -> 2 X Rods" types of recipes, that follow a pattern for many recipes.
     *
     * This should also be used for recipes that need
     * to respond to a config option in ConfigHolder.
     */
    public static void recipeAddition(RecipeOutput originalConsumer) {
        RecipeOutput consumer = new RecipeOutput() {

            @Override
            public Advancement.@NotNull Builder advancement() {
                return originalConsumer.advancement();
            }

            @Override
            public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement, ICondition @NotNull... conditions) {
                if (!RECIPE_FILTERS.contains(id)) {
                    originalConsumer.accept(id, recipe, advancement, conditions);
                }
            }
        };

        // Decomposition info loading
        ItemMaterialData.reinitializeMaterialData();
        MaterialInfoLoader.init();

        // com.gregtechceu.gtceu.data.recipe.generated.*
        for (Material material : GTCEuAPI.materialManager) {
            if (material.hasFlag(MaterialFlags.DISABLE_MATERIAL_RECIPES)) {
                continue;
            }

            DecompositionRecipeHandler.run(consumer, material);
            MaterialRecipeHandler.run(consumer, material);
            OreRecipeHandler.run(consumer, material);
            PartsRecipeHandler.run(consumer, material);
            PipeRecipeHandler.run(consumer, material);
            PolarizingRecipeHandler.run(consumer, material);
            RecyclingRecipeHandler.run(consumer, material);
            ToolRecipeHandler.run(consumer, material);
            WireCombiningHandler.run(consumer, material);
            WireRecipeHandler.run(consumer, material);
        }

        CustomToolRecipes.init(consumer);
        AirScrubberRecipes.init(consumer);
        ChemistryRecipes.init(consumer);
        MetaTileEntityMachineRecipeLoader.init(consumer);
        MiscRecipeLoader.init(consumer);
        VanillaStandardRecipes.init(consumer);
        WoodMachineRecipes.init(consumer);
        StoneMachineRecipes.init(consumer);
        CraftingRecipeLoader.init(consumer);
        FuelRecipes.init(consumer);
        FusionLoader.init(consumer);
        MachineRecipeLoader.init(consumer);
        AssemblerRecipeLoader.init(consumer);
        AssemblyLineLoader.init(consumer);
        BatteryRecipes.init(consumer);
        DecorationRecipes.init(consumer);

        CircuitRecipes.init(consumer);
        ComponentRecipes.init(consumer);
        MetaTileEntityLoader.init(consumer);

        // GCYM
        GCYMRecipes.init(consumer);

        // Config-dependent recipes
        RecipeAddition.init(consumer);

        AddonFinder.getAddonList().forEach(addon -> addon.addRecipes(consumer));

        // Must run recycling recipes very last
        if (!(GTCEu.Mods.isKubeJSLoaded() && KJSCallWrapper.recipeEventHasListeners())) {
            RecyclingRecipes.init(consumer);
            ItemMaterialData.resolveItemMaterialInfos(consumer);
        }
    }

    /*
     * Called on resource reload in-game, just before the above method.
     *
     * This is also where any recipe removals should happen.
     */
    public static void recipeRemoval(Consumer<ResourceLocation> registry) {
        final Consumer<ResourceLocation> actualConsumer = registry.andThen(RECIPE_FILTERS::add);
        RECIPE_FILTERS.clear();

        RecipeRemoval.init(actualConsumer);
        AddonFinder.getAddonList().forEach(addon -> addon.removeRecipes(actualConsumer));
    }

    private static class KJSCallWrapper {

        private static boolean recipeEventHasListeners() {
            return ServerEvents.RECIPES.hasListeners();
        }
    }
}
