package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeLookupTest {

    private static RecipeDB DB;
    private static final Predicate<GTRecipe> ALWAYS_TRUE = gtRecipe -> true;
    private static final Predicate<GTRecipe> ALWAYS_FALSE = gtRecipe -> false;
    private static GTRecipe SMELT_STONE, SMELT_ACACIA_WOOD, SMELT_BIRCH_WOOD, SMELT_CHERRY_WOOD;
    private static GTRecipe RANGED_INPUT_ITEM, RANGED_INPUT_FLUID, RANGED_INPUT_BOTH;

    @BeforeBatch(batch = "GTRecipeLookup")
    public static void prepare(ServerLevel level) {
        ((MappedRegistry<GTRecipeCategory>) GTRegistries.RECIPE_CATEGORIES).unfreeze();
        ((MappedRegistry<RecipeType<?>>) BuiltInRegistries.RECIPE_TYPE).unfreeze();
        RecipeType<?> proxyRecipes = RecipeType.SMELTING;

        GTRecipeType recipeType = TestUtils.createRecipeType("recipe_lookup");
        RecipeAdditionHandler handler = recipeType.getAdditionHandler();
        DB = recipeType.db();

        SMELT_STONE = recipeType.recipeBuilder("smelt_stone")
                .inputItems(Items.COBBLESTONE, 1)
                .outputItems(Items.STONE, 1)
                .build();
        SMELT_ACACIA_WOOD = recipeType.recipeBuilder("smelt_acacia_wood")
                .inputItems(Items.ACACIA_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .build();
        SMELT_BIRCH_WOOD = recipeType.recipeBuilder("smelt_birch_wood")
                .inputItems(Items.BIRCH_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .build();
        SMELT_CHERRY_WOOD = recipeType.recipeBuilder("smelt_cherry_wood")
                .inputItems(Items.CHERRY_WOOD, 16)
                .outputItems(Items.CHARCOAL, 1)
                .build();
        RANGED_INPUT_ITEM = recipeType.recipeBuilder("ranged_input_item")
                .inputItemsRanged(Items.RED_WOOL, UniformInt.of(0, 4))
                .outputItems(Items.CHARCOAL, 1)
                .build();
        RANGED_INPUT_FLUID = recipeType.recipeBuilder("ranged_input_fluid")
                .inputFluidsRanged(GTMaterials.Helium.getFluid(1), UniformInt.of(0, 4))
                .outputItems(Items.CHARCOAL, 1)
                .build();
        RANGED_INPUT_BOTH = recipeType.recipeBuilder("ranged_input_both")
                .inputItemsRanged(Items.BLUE_WOOL, UniformInt.of(0, 4))
                .inputFluidsRanged(GTMaterials.Iron.getFluid(1), UniformInt.of(0, 4))
                .outputItems(Items.CHARCOAL, 1)
                .build();

        handler.beginStaging();
        for (GTRecipe recipe : List.of(SMELT_STONE,
                SMELT_ACACIA_WOOD,
                SMELT_BIRCH_WOOD,
                SMELT_CHERRY_WOOD,
                RANGED_INPUT_ITEM,
                RANGED_INPUT_FLUID,
                RANGED_INPUT_BOTH)) {
            handler.addStaging(recipe);
        }
        handler.completeStaging();

        GTRegistries.RECIPE_CATEGORIES.freeze();
        BuiltInRegistries.RECIPE_TYPE.freeze();
    }

    private static List<List<AbstractMapIngredient>> createIngredients(ItemStack... stacks) {
        return List.of(
                Arrays.stream(stacks)
                        .map(stack -> (AbstractMapIngredient) new ItemStackMapIngredient(stack))
                        .toList());
    }

    private static List<List<AbstractMapIngredient>> createIngredients(FluidStack... stacks) {
        return List.of(
                Arrays.stream(stacks)
                        .map(stack -> (AbstractMapIngredient) new FluidStackMapIngredient(stack))
                        .toList());
    }

    private static List<List<AbstractMapIngredient>> createIngredients(List<List<AbstractMapIngredient>>... stacks) {
        return Arrays.stream(stacks).flatMap(Collection::stream).toList();
    }

    // Simple recipe test whose lookup should succeed
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
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
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should fail because the predicate for canHandle
    // always evaluates to false.
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupFalsePredicateFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1));
        GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_FALSE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should succeed even when passed ingredients that don't have a recipe
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
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
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
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
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupCustomCountCanHandleTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.CHERRY_WOOD, 16));
        // Do a recipe check with a condition that requires at least 4 ingredients in the inputs
        // The recipe has 8, so this should succeed
        GTRecipe resultRecipe = DB.find(ingredients,
                recipe -> recipe.inputs
                        .getOrDefault(ItemRecipeCapability.CAP, List.of())
                        .stream()
                        .allMatch(content -> ((SizedIngredient) content.getContent()).count() > 4));
        helper.assertTrue(SMELT_CHERRY_WOOD.equals(resultRecipe),
                "GT Recipe should be smelt_cherry_wood, instead was " + resultRecipe);

        // Do a recipe check with a condition that requires at least 32 ingredients in the inputs
        // The recipe has 8, so this should fail
        resultRecipe = DB.find(ingredients, recipe -> recipe.inputs
                .getOrDefault(ItemRecipeCapability.CAP, List.of())
                .stream()
                .allMatch(content -> ((SizedIngredient) content.getContent()).count() > 32));
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);

        helper.succeed();
    }

    // Simple recipe test with ranged item input, whose lookup should succeed
    // Repeats 100 times to make sure there's no random roll interference
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleRangedItemSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.RED_WOOL, 4));
        for (int i = 0; i < 100; i++) {
            GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
            helper.assertTrue(RANGED_INPUT_ITEM.equals(resultRecipe),
                    "GT Recipe should be ranged_input_item, instead was " + resultRecipe + ". Failed on check " + i);
        }
        helper.succeed();
    }

    // Simple recipe test with ranged fluid input, whose lookup should succeed
    // Repeats 100 times to make sure there's no random roll interference
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleRangedFluidSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(GTMaterials.Helium.getFluid(4));
        for (int i = 0; i < 100; i++) {
            GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
            helper.assertTrue(RANGED_INPUT_FLUID.equals(resultRecipe),
                    "GT Recipe should be ranged_input_fluid, instead was " + resultRecipe + ". Failed on check " + i);
        }
        helper.succeed();
    }

    // Simple recipe test with ranged item and fluid inputs, whose lookup should succeed
    // Repeats 100 times to make sure there's no random roll interference
    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleRangedItemFluidSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(
                createIngredients(new ItemStack(Items.BLUE_WOOL, 4)),
                createIngredients(GTMaterials.Iron.getFluid(4)));
        for (int i = 0; i < 100; i++) {
            GTRecipe resultRecipe = DB.find(ingredients, ALWAYS_TRUE);
            helper.assertTrue(RANGED_INPUT_BOTH.equals(resultRecipe),
                    "GT Recipe should be raged_input_both, instead was " + resultRecipe + ". Failed on check " + i);
        }
        helper.succeed();
    }
}
