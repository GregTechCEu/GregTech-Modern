package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTRIC;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeLookupTest {

    private static RecipeType<?> proxyRecipes;
    private static GTRecipeType type;
    private static GTRecipeLookup lookup;
    private static Predicate<GTRecipe> truePredicate = gtRecipe -> true;
    private static Predicate<GTRecipe> falsePredicate = gtRecipe -> false;
    private static GTRecipe recipe1, recipe2, recipe3, recipe4;

    @BeforeBatch(batch = "GTRecipeLookup")
    public static void prepare(ServerLevel level) {
        GTRegistries.RECIPE_TYPES.unfreeze();
        GTRegistries.RECIPE_CATEGORIES.unfreeze();
        proxyRecipes = RecipeType.SMELTING;
        type = new GTRecipeType(GTCEu.id("test_recipes"), ELECTRIC, proxyRecipes)
                .setEUIO(IO.IN)
                .setMaxIOSize(1, 1, 0, 0);
        lookup = new GTRecipeLookup(type);

        recipe1 = type.recipeBuilder("test_recipe_1")
                .inputItems(Items.COBBLESTONE, 1)
                .outputItems(Items.STONE, 1)
                .buildRawRecipe();
        recipe2 = type.recipeBuilder("test_recipe_2")
                .inputItems(Items.ACACIA_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        recipe3 = type.recipeBuilder("test_recipe_3")
                .inputItems(Items.BIRCH_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        recipe4 = type.recipeBuilder("test_recipe_4")
                .inputItems(Items.CHERRY_WOOD, 16)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();

        for (GTRecipe recipe : List.of(recipe1, recipe2, recipe3)) {
            lookup.addRecipe(recipe);
        }

        GTRegistries.RECIPE_TYPES.freeze();
        GTRegistries.RECIPE_CATEGORIES.freeze();
    }

    private static List<List<AbstractMapIngredient>> createIngredients(ItemStack... stacks) {
        return List.of(
                Arrays.stream(stacks)
                        .map(stack -> (AbstractMapIngredient) new ItemStackMapIngredient(stack))
                        .toList());
    }

    // Simple recipe test whose lookup should succeed
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1));
        GTRecipe resultRecipe = lookup.recurseIngredientTreeFindRecipe(ingredients, lookup.getLookup(), truePredicate);
        helper.assertTrue(recipe1.equals(resultRecipe),
                "GT Recipe should be test_recipe_1, instead was " + resultRecipe);
        helper.succeed();
    }

    // Simple recipe test whose lookup should fail because we pass an ingredient
    // that does not match any of the recipes.
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = lookup.recurseIngredientTreeFindRecipe(ingredients, lookup.getLookup(), truePredicate);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was %s" + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should fail because the predicate for canHandle
    // always evaluates to false.
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupFalsePredicateFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1));
        GTRecipe resultRecipe = lookup.recurseIngredientTreeFindRecipe(ingredients, lookup.getLookup(), falsePredicate);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was %s" + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should succeed even when passed ingredients that don't have a recipe
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupMultipleIngredientsSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1),
                new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = lookup.recurseIngredientTreeFindRecipe(ingredients, lookup.getLookup(), truePredicate);
        helper.assertTrue(recipe1.equals(resultRecipe),
                "GT Recipe should be test_recipe_1, instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should fail because the amount in the recipe is not enough
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupIngredientCountFailTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.CHERRY_WOOD, 8));
        GTRecipe resultRecipe = lookup.recurseIngredientTreeFindRecipe(ingredients, lookup.getLookup(), truePredicate);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was %s" + resultRecipe);
        helper.succeed();
    }
}
