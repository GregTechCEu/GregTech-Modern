package com.gregtechceu.gtceu.data.recipes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addons.AddonFinder;
import com.gregtechceu.gtceu.data.recipes.configurable.RecipeAddition;
import com.gregtechceu.gtceu.data.recipes.configurable.RecipeRemoval;
import com.gregtechceu.gtceu.data.recipes.generated.*;
import com.gregtechceu.gtceu.data.recipes.misc.*;
import com.gregtechceu.gtceu.data.recipes.serialized.chemistry.ChemistryRecipes;
import net.minecraft.data.recipes.RecipeOutput;
import com.gregtechceu.gtceu.utils.ResearchManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.function.Consumer;

public class GTRecipes {

    /*
     * Called on resource reload in-game.
     *
     * These methods are meant for recipes that cannot be reasonably changed by a Datapack,
     * such as "X Ingot -> 2 X Rods" types of recipes, that follow a pattern for many recipes.
     *
     * This should also be used for recipes that need
     * to respond to a config option in ConfigHolder.
     */
    public static void recipeAddition(RecipeOutput consumer) {
        ComposterRecipes.addComposterRecipes(ComposterBlock.COMPOSTABLES::put);
        ResearchManager.registerScannerLogic();

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

        CircuitRecipes.init(consumer);
        ComponentRecipes.init(consumer);
        MetaTileEntityLoader.init(consumer);

        //GCyM
        GCyMRecipes.init(consumer);

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
    public static void recipeRemoval(Consumer<ResourceLocation> consumer) {
        RecipeRemoval.init(consumer);

        AddonFinder.getAddons().forEach(addon -> addon.removeRecipes(consumer));
    }
}
