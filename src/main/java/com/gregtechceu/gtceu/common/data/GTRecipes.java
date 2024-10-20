package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.data.recipe.MaterialInfoLoader;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeAddition;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.gregtechceu.gtceu.data.recipe.generated.*;
import com.gregtechceu.gtceu.data.recipe.misc.*;
import com.gregtechceu.gtceu.data.recipe.serialized.chemistry.ChemistryRecipes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ComposterBlock;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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
    public static void recipeAddition(Consumer<FinishedRecipe> originalConsumer) {
        Consumer<FinishedRecipe> consumer = recipe -> {
            if (!RECIPE_FILTERS.contains(recipe.getId())) {
                originalConsumer.accept(recipe);
            }
        };

        ComposterRecipes.addComposterRecipes(ComposterBlock.COMPOSTABLES::put);

        // Decomposition info loading
        MaterialInfoLoader.init();

        // com.gregtechceu.gtceu.data.recipe.generated.*
        DecompositionRecipeHandler.init(consumer);
        MaterialRecipeHandler.init(consumer);
        OreRecipeHandler.init(consumer);
        PartsRecipeHandler.init(consumer);
        PipeRecipeHandler.init(consumer);
        PolarizingRecipeHandler.init(consumer);
        RecyclingRecipeHandler.init(consumer);
        ToolRecipeHandler.init(consumer);
        WireCombiningHandler.init(consumer);
        WireRecipeHandler.init(consumer);

        AirScrubberRecipes.init(consumer);
        ChemistryRecipes.init(consumer);
        MetaTileEntityMachineRecipeLoader.init(consumer);
        MiscRecipeLoader.init(consumer);
        VanillaStandardRecipes.init(consumer);
        WoodMachineRecipes.init(consumer);
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
        // Must run recycling recipes very last
        RecyclingRecipes.init(consumer);

        // Kinetic Machines
        if (GTCEu.isCreateLoaded()) {
            CreateRecipeLoader.init(consumer);
        }

        AddonFinder.getAddons().forEach(addon -> addon.addRecipes(consumer));
    }

    /*
     * Called on resource reload in-game, just before the above method.
     *
     * This is also where any recipe removals should happen.
     */
    public static void recipeRemoval() {
        RECIPE_FILTERS.clear();

        RecipeRemoval.init(RECIPE_FILTERS::add);
        AddonFinder.getAddons().forEach(addon -> addon.removeRecipes(RECIPE_FILTERS::add));
    }
}
