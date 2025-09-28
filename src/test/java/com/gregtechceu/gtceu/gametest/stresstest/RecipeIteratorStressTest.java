package com.gregtechceu.gtceu.gametest.stresstest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RecipeIteratorStressTest {

    private static final boolean DO_RUN_RECIPE_ITERATOR_STRESSTEST = true;

    @GameTest(template = "empty")
    public static void iteratorStressTest(GameTestHelper helper) {
        if (!DO_RUN_RECIPE_ITERATOR_STRESSTEST) helper.succeed();
        List<List<AbstractMapIngredient>> list = new ArrayList();
        for (var item : BuiltInRegistries.ITEM) {
            list.add(MapIngredientTypeManager.getFrom(Ingredient.of(item), ItemRecipeCapability.CAP));
        }
        for (var block : BuiltInRegistries.BLOCK) {
            list.add(MapIngredientTypeManager.getFrom(Ingredient.of(block), ItemRecipeCapability.CAP));
        }

        long start = System.nanoTime();

        long currentIterator = 0;
        for (int i = 0; i < 100; i++) {
            RecipeDB.RecipeIterator iterator = new RecipeDB.RecipeIterator(GTRecipeTypes.ASSEMBLER_RECIPES.db(), list,
                    (ignored) -> true);
            while (iterator.hasNext()) {
                var recipe = iterator.next();
                currentIterator++;
            }
        }
        long end = System.nanoTime();
        GTCEu.LOGGER.info("current iterator recipes: " + currentIterator / 100);
        GTCEu.LOGGER.info("Took " + (end - start) / 1_000_000.0 + " ms");

        start = System.nanoTime();
        long dfsIteratorRecipes = 0;
        for (int i = 0; i < 100; i++) {
            RecipeDB.DFSRecipeIterator dfsiterator = new RecipeDB.DFSRecipeIterator(
                    GTRecipeTypes.ASSEMBLER_RECIPES.db(), list, (ignored) -> true);
            while (dfsiterator.hasNext()) {
                var recipe = dfsiterator.next();
                dfsIteratorRecipes++;
            }
        }
        end = System.nanoTime();
        GTCEu.LOGGER.info("DFS iterator recipes: " + dfsIteratorRecipes / 100);
        GTCEu.LOGGER.info("Took " + (end - start) / 1_000_000.0 + " ms");
        helper.succeed();
    }
}
