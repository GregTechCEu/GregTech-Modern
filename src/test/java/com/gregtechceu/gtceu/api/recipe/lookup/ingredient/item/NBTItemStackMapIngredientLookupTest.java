package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

// spotless:off

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.minecraft.gametest.framework.GameTestHelper;

import com.simibubi.create.foundation.recipe.trie.AbstractIngredient;

import java.util.List;
import java.util.function.Predicate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class NBTItemStackMapIngredientLookupTest {

    /*
    private static GTRecipeLookup LOOKUP;
    private static final Predicate<GTRecipe> ALWAYS_TRUE = gtRecipe -> true;
    private static final Predicate<GTRecipe> ALWAYS_FALSE = gtRecipe -> false;
    private static GTRecipeType RECIPE_TYPE;
    private static GTRecipe PARTIAL_TAG_1, PARTIAL_TAG_2, STRICT_TAG_1, STRICT_TAG_2;

    private static CompoundTag tag1, tag2;

    @BeforeBatch(batch = "NBTItemStackMapIngredientLookup")
    public static void prepare(ServerLevel level) {
        RECIPE_TYPE = TestUtils.createRecipeType("NBT_item_stack_map_ingredient_lookup");
        LOOKUP = RECIPE_TYPE.getLookup();

        tag1 = new CompoundTag();
        tag1.putString("tag1", "tag1");

        tag2 = new CompoundTag();
        tag2.putString("tag1", "tag1");
        var otherStuff = new CompoundTag();
        otherStuff.putBoolean("a", true);
        otherStuff.putLong("b", 4);
        tag2.put("testTag", otherStuff);

// Partial tag 2 matches a recipe looking for partial tag 1
// apart from these, all ingredients should only match themselves
//
// Recipe looking for ingredient X : Does it match recipe Y
// pt1: pt1 pt2 x x
// pt2: x pt2 x x
// st1: x x st1 x
// st2: x x x st2

        AbstractIngredient partialComponentIngredient1 = PartialNBTIngredient.of(Items.RED_BED, tag1);
        AbstractIngredient partialComponentIngredient2 = PartialNBTIngredient.of(Items.BROWN_BED, tag2);
        Ingredient strictComponentIngredient1 = createStrictTaggedIngredient(Items.GREEN_BED, tag1);
        AbstractIngredient strictComponentIngredient2 = createStrictTaggedIngredient(Items.BLUE_BED, tag2);

        PARTIAL_TAG_1 = RECIPE_TYPE.recipeBuilder("partial_match_NBT_1")
                .inputItems(partialComponentIngredient1)
                .outputItems(Items.RED_BED, 1)
                .build();

        PARTIAL_TAG_2 = RECIPE_TYPE.recipeBuilder("partial_match_NBT_2")
                .inputItems(partialComponentIngredient2)
                .outputItems(Items.BROWN_BED, 1)
                .build();

        STRICT_TAG_1 = RECIPE_TYPE.recipeBuilder("strict_match_NBT_1")
                .inputItems(strictComponentIngredient1)
                .outputItems(Items.GREEN_BED, 1)
                .build();

        STRICT_TAG_2 = RECIPE_TYPE.recipeBuilder("strict_match_NBT_2")
                .inputItems(strictComponentIngredient2)
                .outputItems(Items.BLUE_BED, 1)
                .build();

        for (GTRecipe recipe : List.of(PARTIAL_TAG_1,
                PARTIAL_TAG_2,
                STRICT_TAG_1,
                STRICT_TAG_2)) {
            LOOKUP.addRecipe(recipe);
        }
    }

    private static Ingredient createStrictTaggedIngredient(Item item, CompoundTag tag) {
        ItemStack stack = new ItemStack(item);
        for (var tagKey : tag.getAllKeys()) {
            stack.getOrCreateTag().put(tagKey, tag.get(tagKey));
        }
        return StrictNBTIngredient.of(stack);
    }

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "NBTItemStackMapIngredientLookup")
    public static void NBTItemStackMapIngredientMatchingPartialTag1Test(GameTestHelper helper) {
// Partial tag 1 fits in Partial tag 1
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.RED_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == PARTIAL_TAG_1,
                "GT Recipe should be PARTIAL_TAG_1, instead was " + resultRecipe);

// Partial tag 2 fits in Partial tag 1
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.RED_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == PARTIAL_TAG_1,
                "GT Recipe should be PARTIAL_TAG_1, instead was " + resultRecipe);

// Strict tag 1 and 2 should never fit in partial tag 1
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.RED_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.RED_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);
        helper.succeed();
    }

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "NBTItemStackMapIngredientLookup")
    public static void NBTItemStackMapIngredientMatchingPartialTag2Test(GameTestHelper helper) {
// Partial tag 1 should not fit in partial tag 2
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.BROWN_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

// Partial tag 2 fits in Partial tag 2
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.BROWN_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == PARTIAL_TAG_2,
                "GT Recipe should be PARTIAL_TAG_2, instead was " + resultRecipe);

// Strict tag 1 and 2 should never fit in partial tag 2
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.BROWN_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.BROWN_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);
        helper.succeed();
    }

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "NBTItemStackMapIngredientLookup")
    public static void NBTItemStackMapIngredientMatchingStrictTag1Test(GameTestHelper helper) {
// Partial tag 1 and 2 should not fit in strict tag 1
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.GREEN_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.GREEN_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

// Strict tag 1 should fit in strict tag 1
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.GREEN_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == STRICT_TAG_1,
                "GT Recipe should be STRICT_TAG_1, instead was " + resultRecipe);

// Strict tag 2 should not fit in strict tag 1
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.GREEN_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);
        helper.succeed();
    }

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate
    @GameTest(template = "empty", batch = "NBTItemStackMapIngredientLookup")
    public static void NBTItemStackMapIngredientMatchingStrictTag2Test(GameTestHelper helper) {
// Partial tag 1 and 2 should not fit in strict tag 2
        GTRecipe resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.BLUE_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        PartialNBTItemStackMapIngredient.from(PartialNBTIngredient.of(Items.BLUE_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

// Strict tag 1 should not fit in strict tag 2
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.BLUE_BED, tag1))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == null, "GT Recipe should be null, instead was " + resultRecipe);

// Strict tag 2 should fit in strict tag 2
        resultRecipe = LOOKUP.recurseIngredientTreeFindRecipe(
                List.of(
                        StrictNBTItemStackMapIngredient.from(createStrictTaggedIngredient(Items.BLUE_BED, tag2))),
                LOOKUP.getLookup(), ALWAYS_TRUE);
        helper.assertTrue(resultRecipe == STRICT_TAG_2,
                "GT Recipe should be STRICT_TAG_2, instead was " + resultRecipe);
        helper.succeed();
    }
    */
    // spotless:on
}
