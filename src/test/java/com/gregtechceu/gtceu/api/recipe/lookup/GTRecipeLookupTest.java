package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeLookupTest {

    private static GTRecipeLookup LOOKUP;
    private static final Predicate<GTRecipe> ALWAYS_TRUE = gtRecipe -> true;
    private static final Predicate<GTRecipe> ALWAYS_FALSE = gtRecipe -> false;
    private static GTRecipeType RECIPE_TYPE;
    private static GTRecipe SMELT_STONE, SMELT_ACACIA_WOOD, SMELT_BIRCH_WOOD, SMELT_CHERRY_WOOD;
    private static GTRecipe RANGED_INPUT_ITEM, RANGED_INPUT_FLUID, RANGED_INPUT_BOTH;

    @BeforeBatch(batch = "GTRecipeLookup")
    public static void prepare(ServerLevel level) {
        RECIPE_TYPE = TestUtils.createRecipeType("recipe_lookup");
        LOOKUP = RECIPE_TYPE.getLookup();

        SMELT_STONE = RECIPE_TYPE.recipeBuilder("smelt_stone")
                .inputItems(Items.COBBLESTONE, 1)
                .outputItems(Items.STONE, 1)
                .buildRawRecipe();
        SMELT_ACACIA_WOOD = RECIPE_TYPE.recipeBuilder("smelt_acacia_wood")
                .inputItems(Items.ACACIA_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        SMELT_BIRCH_WOOD = RECIPE_TYPE.recipeBuilder("smelt_birch_wood")
                .inputItems(Items.BIRCH_WOOD, 1)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        SMELT_CHERRY_WOOD = RECIPE_TYPE.recipeBuilder("smelt_cherry_wood")
                .inputItems(Items.CHERRY_WOOD, 16)
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        RANGED_INPUT_ITEM = RECIPE_TYPE.recipeBuilder("ranged_input_item")
                .inputItemsRanged(Items.RED_WOOL, UniformInt.of(0, 4))
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        RANGED_INPUT_FLUID = RECIPE_TYPE.recipeBuilder("ranged_input_fluid")
                .inputFluidsRanged(GTMaterials.Helium.getFluid(1), UniformInt.of(0, 4))
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();
        RANGED_INPUT_BOTH = RECIPE_TYPE.recipeBuilder("ranged_input_both")
                .inputItemsRanged(Items.BLUE_WOOL, UniformInt.of(0, 4))
                .inputFluidsRanged(GTMaterials.Iron.getFluid(1), UniformInt.of(0, 4))
                .outputItems(Items.CHARCOAL, 1)
                .buildRawRecipe();

        for (GTRecipe recipe : List.of(SMELT_STONE,
                SMELT_ACACIA_WOOD,
                SMELT_BIRCH_WOOD,
                SMELT_CHERRY_WOOD,
                RANGED_INPUT_ITEM,
                RANGED_INPUT_FLUID,
                RANGED_INPUT_BOTH)) {
            LOOKUP.addRecipe(recipe);
        }
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
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1));
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(SMELT_STONE.equals(resultRecipe),
                "GT Recipe should be smelt_stone, instead was " + resultRecipe);
        helper.succeed();
    }

    // Simple recipe test whose lookup should fail because we pass an ingredient
    // that does not match any of the recipes.
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should fail because the predicate for canHandle
    // always evaluates to false.
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupFalsePredicateFailureTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1));
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(), ALWAYS_FALSE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);
        helper.succeed();
    }

    // Recipe test whose lookup should succeed even when passed ingredients that don't have a recipe
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupMultipleIngredientsSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.COBBLESTONE, 1),
                new ItemStack(Items.REDSTONE_TORCH, 1));
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(), ALWAYS_TRUE);
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
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(notEnoughIngredients, LOOKUP.getLookup(),
                ALWAYS_TRUE);
        helper.assertTrue(SMELT_CHERRY_WOOD.equals(resultRecipe),
                "GT Recipe should be smelt_cherry_wood, instead was " + resultRecipe);

        var enoughIngredients = createIngredients(new ItemStack(Items.CHERRY_WOOD, 16));
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(enoughIngredients, LOOKUP.getLookup(), ALWAYS_TRUE);
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
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(),
                recipe -> recipe.inputs
                        .getOrDefault(ItemRecipeCapability.CAP, List.of())
                        .stream()
                        .allMatch(content -> ((SizedIngredient) content.getContent()).getAmount() > 4));
        helper.assertTrue(SMELT_CHERRY_WOOD.equals(resultRecipe),
                "GT Recipe should be smelt_cherry_wood, instead was " + resultRecipe);

        // Do a recipe check with a condition that requires at least 32 ingredients in the inputs
        // The recipe has 8, so this should fail
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(), recipe -> recipe.inputs
                .getOrDefault(ItemRecipeCapability.CAP, List.of())
                .stream()
                .allMatch(content -> ((SizedIngredient) content.getContent()).getAmount() > 32));
        helper.assertTrue(resultRecipe == null, "GT Recipe should be empty (null), instead was " + resultRecipe);

        helper.succeed();
    }

    // Simple recipe test with ranged item input, whose lookup should succeed
    // Repeats 100 times to make sure there's no random roll interference
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleRangedItemSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(new ItemStack(Items.RED_WOOL, 4));
        for (int i = 0; i < 100; i++) {
            GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(),
                    ALWAYS_TRUE);
            helper.assertTrue(RANGED_INPUT_ITEM.equals(resultRecipe),
                    "GT Recipe should be ranged_input_item, instead was " + resultRecipe + ". Failed on check " + i);
        }
        helper.succeed();
    }

    // Simple recipe test with ranged fluid input, whose lookup should succeed
    // Repeats 100 times to make sure there's no random roll interference
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleRangedFluidSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(GTMaterials.Helium.getFluid(4));
        for (int i = 0; i < 100; i++) {
            GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(),
                    ALWAYS_TRUE);
            helper.assertTrue(RANGED_INPUT_FLUID.equals(resultRecipe),
                    "GT Recipe should be ranged_input_fluid, instead was " + resultRecipe + ". Failed on check " + i);
        }
        helper.succeed();
    }

    // Simple recipe test with ranged item and fluid inputs, whose lookup should succeed
    // Repeats 100 times to make sure there's no random roll interference
    @GameTest(template = "empty", batch = "GTRecipeLookup")
    public static void recipeLookupSimpleRangedItemFluidSuccessTest(GameTestHelper helper) {
        var ingredients = createIngredients(
                createIngredients(new ItemStack(Items.BLUE_WOOL, 4)),
                createIngredients(GTMaterials.Iron.getFluid(4)));
        for (int i = 0; i < 100; i++) {
            GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(ingredients, LOOKUP.getLookup(),
                    ALWAYS_TRUE);
            helper.assertTrue(RANGED_INPUT_BOTH.equals(resultRecipe),
                    "GT Recipe should be raged_input_both, instead was " + resultRecipe + ". Failed on check " + i);
        }
        helper.succeed();
    }
}
