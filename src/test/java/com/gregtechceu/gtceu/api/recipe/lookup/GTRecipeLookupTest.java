package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeLookupTest {

    private static RecipeDB DB;
    private static final Predicate<GTRecipe> ALWAYS_TRUE = gtRecipe -> true;
    private static final Predicate<GTRecipe> ALWAYS_FALSE = gtRecipe -> false;
    private static GTRecipe SMELT_STONE;
    private static GTRecipe SMELT_BIRCH_WOOD;
    private static GTRecipe SMELT_CHERRY_WOOD;

    @BeforeBatch(batch = "GTRecipeLookup")
    public static void prepare(ServerLevel level) {
        GTRecipeType recipeType = TestUtils.createRecipeType("recipe_lookup");
        RecipeAdditionHandler handler = recipeType.getAdditionHandler();
        DB = recipeType.db();

        SMELT_STONE = recipeType.recipeBuilder("smelt_stone")
                .inputItems(Items.COBBLESTONE, 1)
                .outputItems(Items.STONE, 1)
                .buildRawRecipe();
        GTRecipe SMELT_ACACIA_WOOD = recipeType.recipeBuilder("smelt_acacia_wood")
                .inputItems(Items.ACACIA_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        SMELT_BIRCH_WOOD = recipeType.recipeBuilder("smelt_birch_wood")
                .inputItems(Items.BIRCH_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        SMELT_CHERRY_WOOD = recipeType.recipeBuilder("smelt_cherry_wood")
                .inputItems(Items.CHERRY_WOOD, 16)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();

        handler.beginStaging();
        for (GTRecipe recipe : List.of(SMELT_STONE,
                SMELT_ACACIA_WOOD,
                SMELT_BIRCH_WOOD,
                SMELT_CHERRY_WOOD)) {
            handler.addStaging(recipe);
        }
        handler.completeStaging();
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
        GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
        helper.assertTrue(SMELT_STONE.equals(resultRecipe),
                "GT Recipe should be smelt_stone, instead was " + resultRecipe);
        helper.succeed();
    }

    // Simple recipe test whose lookup should fail because we pass an ingredient
    // that does not match any of the recipes.
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should fail because the predicate for canHandle
    // always evaluates to false.
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupFalsePredicateFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1));
        GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_FALSE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should succeed even when passed ingredients that don't have a recipe
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupMultipleIngredientsSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1),
                new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
        helper.assertTrue(SMELT_STONE.equals(resultRecipe),
                "GT Recipe should be smelt_stone, instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should succeed because even though the amount in the recipe is not enough,
    // ingredients don't count items
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupIngredientCountSucceedTest(GameTestHelper helper) {
        // NOTE: RecipeLookup only checks item type, not item count, so this will still work
        var notEnoughIngredients = createIngredients(new ItemStack(Items.CHERRY_WOOD, 8));
        GTRecipe resultRecipe = DB.find(notEnoughIngredients, ALWAYS_TRUE);
        helper.assertTrue(SMELT_CHERRY_WOOD.equals(resultRecipe),
                "GT Recipe should be smelt_cherry_wood, instead was " + resultRecipe);

        var enoughIngredients = createIngredients(new ItemStack(Items.CHERRY_WOOD, 16));
        resultRecipe = DB.find(enoughIngredients, ALWAYS_TRUE);
        helper.assertTrue(SMELT_CHERRY_WOOD.equals(resultRecipe),
                "GT Recipe should be smelt_cherry_wood, instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test with a recipe-based canHandle check
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupCustomCountCanHandleTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.CHERRY_WOOD, 16));
        // Do a recipe check with a condition that requires at least 4 ingredients in the inputs
        // The recipe has 8, so this should succeed
        GTRecipe resultRecipe = DB.find(ingredients,
                recipe -> recipe.inputs
                        .getOrDefault(ItemRecipeCapability.CAP, List.of())
                        .stream()
                        .allMatch(content -> ((SizedIngredient) content.getContent()).getAmount() > 4));
        helper.assertTrue(SMELT_CHERRY_WOOD.equals(resultRecipe),
                "GT Recipe should be smelt_cherry_wood, instead was " + resultRecipe);

        // Do a recipe check with a condition that requires at least 32 ingredients in the inputs
        // The recipe has 8, so this should fail
        resultRecipe = DB.find(ingredients, recipe -> recipe.inputs
                .getOrDefault(ItemRecipeCapability.CAP, List.of())
                .stream()
                .allMatch(content -> ((SizedIngredient) content.getContent()).getAmount() > 32));
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);

        helper.succeed();
    }
}
