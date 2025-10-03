package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import lombok.Getter;

/**
 * Test cases:
 * do many passes of most tests as a safeguard against bad rolls
 * Same output more than once
 * Out of bounds
 * Output a multiple of batchparallels
 * Rolls of 0
 * Forced rolls of 0 breaking recipes
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class IntProviderIngredientTest {

    private static GTRecipeType CR_RECIPE_TYPE;
    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CENTRIFUGE_RECIPE_TYPE;

    // items used in recipes. Up top here for quick replacements.
    private static final ItemStack CR_IN = new ItemStack(Items.GREEN_STAINED_GLASS);
    private static final ItemStack CR_OUT = new ItemStack(Items.BRICK_SLAB);
    private static final ItemStack LCR_IN = new ItemStack(Items.BLACK_STAINED_GLASS);
    private static final ItemStack LCR_OUT = new ItemStack(Items.BRICK_STAIRS);
    private static final ItemStack LCENT_IN = new ItemStack(Items.LIME_STAINED_GLASS);
    private static final ItemStack LCENT_OUT = new ItemStack(Items.BRICK_WALL);
    private static final ItemStack COBBLE = new ItemStack(Items.COBBLESTONE);
    private static final ItemStack STONE = new ItemStack(Items.STONE);

    /**
     * How many times to repeat the Batch and Parallel random roll tests to avoid false positives
     * Currently set to 7, with singleblock recipes processing up to 9 items, allowing for stacks of up to 63 items.
     */
    @Getter
    private static final int REPLICAS = 7;

    @BeforeBatch(batch = "RangedIngredients")
    public static void prepare(ServerLevel level) {
        CR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_cr_tests", 2, 2, 3, 2);
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_lcr_tests", 3, 3, 5, 4);
        CENTRIFUGE_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_centrifuge_tests", 2, 6, 1, 6);

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_cr"))
                .inputItemsRanged(CR_IN, UniformInt.of(0, 9))
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_cr"))
                .inputItems(CR_OUT)
                .outputItemsRanged(STONE, UniformInt.of(0, 9))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_lcr"))
                .inputItemsRanged(LCR_IN, UniformInt.of(0, 9))
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_lcr"))
                .inputItems(LCR_OUT)
                .outputItemsRanged(STONE, UniformInt.of(0, 9))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_cent"))
                .inputItemsRanged(LCENT_IN, UniformInt.of(0, 4))
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.IV])
                .duration(4)
                .buildRawRecipe());

        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_cent"))
                .inputItems(LCENT_OUT)
                .outputItemsRanged(STONE, UniformInt.of(0, 4))
                .EUt(GTValues.V[GTValues.IV])
                .duration(4)
                .buildRawRecipe());
    }

    private static MetaMachine getMetaMachine(BlockEntity entity) {
        return ((MetaMachineBlockEntity) entity).getMetaMachine();
    }

    private record BusHolder(ItemBusPartMachine inputBus1, FluidHatchPartMachine inputHatch1,
                             ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, WorkableMultiblockMachine controller) {}

    private record BusHolderBatchParallel(ItemBusPartMachine inputBus1, FluidHatchPartMachine inputHatch1,
                                          ItemBusPartMachine outputBus1,
                                          FluidHatchPartMachine outputHatch1,
                                          WorkableElectricMultiblockMachine controller,
                                          ParallelHatchPartMachine parallelHatch) {}

    /**
     * Retrieves the busses for this LCR template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndFormLCR(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        FluidHatchPartMachine inputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 2, 0)));
        return new BusHolder(inputBus1, inputHatch1, outputBus1, outputHatch1, controller);
    }

    /**
     * Retrieves the busses for this Large Centrifuge template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolderBatchParallel getBussesAndFormLCENT(GameTestHelper helper) {
        WorkableElectricMultiblockMachine controller = (WorkableElectricMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(CENTRIFUGE_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        FluidHatchPartMachine inputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 0)));
        ParallelHatchPartMachine parallelHatch = (ParallelHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(3, 3, 0)));
        return new BusHolderBatchParallel(inputBus1, inputHatch1, outputBus1, outputHatch1, controller, parallelHatch);
    }

    // test for IntProviderIngredient.test()
    @GameTest(template = "empty", batch = "RangedIngredients")
    public static void rangedIngredientTestEqualTest(GameTestHelper helper) {
        var ingredient = IntProviderIngredient.of(new ItemStack(Items.BRICK, 1), UniformInt.of(1, 5));
        helper.assertTrue(ingredient.test(new ItemStack(Items.BRICK, 3)),
                "IntProviderIngredient.test doesn't match when it should have");
        // This should work since test only tries the item type.
        helper.assertTrue(ingredient.test(new ItemStack(Items.BRICK, 64)),
                "IntProviderIngredient.test doesn't match when it should have with value outside bounds");
        helper.assertFalse(ingredient.test(new ItemStack(Items.COBBLESTONE, 3)),
                "IntProviderIngredient.test shouldn't match with different items");
        helper.succeed();
    }

    // test for IntProviderIngredient.getStacks()
    @GameTest(template = "empty", batch = "RangedIngredients")
    public static void rangedIngredientGetStacksTest(GameTestHelper helper) {
        var ingredient = IntProviderIngredient.of(new ItemStack(Items.BRICK, 1), UniformInt.of(1, 5000));
        var stacks = ingredient.getItems();
        helper.assertTrue(stacks.length == 1, "IntProviderIngredient should only return 1 item when made with 1 item");
        helper.assertTrue(stacks[0].is(new ItemStack(Items.BRICK, 1).getItem()),
                "IntProviderIngredient should have item equal to what it was made with");
        helper.assertTrue(TestUtils.areItemStacksEqual(stacks, ingredient.getItems()),
                "IntProviderIngredient.getItems shouldn't change between getStacks calls");
        ingredient.reroll();
        helper.assertFalse(TestUtils.areItemStacksEqual(stacks, ingredient.getItems()),
                "IntProviderIngredient.getItems should have changed after rerolling");
        helper.succeed();
    }

    // test for IntProviderIngredient.toJson()
    @GameTest(template = "empty", batch = "RangedIngredients")
    public static void rangedIngredientJsonTest(GameTestHelper helper) {
        var ingredient = IntProviderIngredient.of(new ItemStack(Items.BRICK, 1), UniformInt.of(1, 5000));

        // serialize/deserialize before rolling count
        var jsonPreRoll = ingredient.toJson();
        var ingredientDeserializedPreRoll = IntProviderIngredient.fromJson(jsonPreRoll);

        var stacks = ingredient.getItems();
        var stacksDeserializedPreRoll = ingredientDeserializedPreRoll.getItems();

        // serialize/deserialize after rolling count
        var jsonPostRoll = ingredient.toJson();
        var ingredientDeserializedPostRoll = IntProviderIngredient.fromJson(jsonPostRoll);
        var stacksDeserializedPostRoll = ingredientDeserializedPostRoll.getItems();

        helper.assertTrue(
                stacks.length == stacksDeserializedPreRoll.length && stacks.length == stacksDeserializedPostRoll.length,
                "IntProviderIngredient should only return 1 item when made with 1 item, even after serializing");
        helper.assertTrue(stacksDeserializedPreRoll[0].is(new ItemStack(Items.BRICK, 1).getItem()),
                "IntProviderIngredient should have item equal to what it was made with after serializing");
        helper.assertTrue(stacksDeserializedPostRoll[0].is(new ItemStack(Items.BRICK, 1).getItem()),
                "IntProviderIngredient should have item equal to what it was made with after serializing");
        helper.assertFalse(TestUtils.areItemStacksEqual(stacksDeserializedPreRoll, ingredient.getItems()),
                "IntProviderIngredient.getItems should be different if it wasn't rolled before serializing");
        helper.assertTrue(TestUtils.areItemStacksEqual(stacksDeserializedPostRoll, ingredient.getItems()),
                "IntProviderIngredient.getItems shouldn't change between getItems calls if it was rolled before serializing");
        helper.succeed();
    }

    // Test for singleblock machine with ranged item input.
    // Forcibly sabotages the first recipe run, setting its output amount to 0 to ensure that doesn't break the recipe.
    // This is specifically a test for #3593 / #3594
    @GameTest(template = "singleblock_charged_cr", batch = "RangedIngredients")
    public static void singleblockRangedItemOutputSabotaged(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 7;
        itemIn.setStackInSlot(0, CR_OUT.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];

        helper.runAfterDelay(4, () -> {
            if (machine.getRecipeLogic().getLastRecipe().getOutputContents(ItemRecipeCapability.CAP).get(0)
                    .getContent() instanceof IntProviderIngredient ingredient) {
                ingredient.setSampledCount(0);

                if (ingredient.getSampledCount() != 0) {
                    helper.fail("Singleblock Ranged Item Output sabotage failed! " +
                            "Output count not was altered!");
                }
            } else {
                helper.fail("Singleblock Ranged Item Output sabotage failed! " +
                        "Recipe logic did not contain a Ranged Output!");
            }
        });
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 3, () -> {
                addedRolls[finalI] = itemOut.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemOut.getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemWithinRange(results, runs, runs * 9),
                    "Sabotaged Singleblock CR didn't produce correct number of items, produced [" +
                            results.getCount() + "] not [" + runs + "-" + (runs * 9) + "]");
            helper.assertFalse((results.getCount() == runs * 9),
                    "Sabotaged Singleblock CR rolled max value on every roll (how??)");
            helper.assertFalse((results.getCount() == runs * 0),
                    "Sabotaged Singleblock CR rolled min value on every roll! " +
                            "This is the failure this sabotage was intended to induce.");

            // check if all the rolls were equal, but not min/max
            int[] rolls = new int[runs];
            rolls[0] = addedRolls[0];
            boolean allEqual = false;
            for (int i = 1; i < runs; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (rolls[i] == rolls[i - 1]) {
                    allEqual = true;
                } else {
                    allEqual = false;
                    break;
                }
            }
            helper.assertFalse(allEqual,
                    "Sabotaged Singleblock CR rolled the same value on every input roll (rolled " + rolls[0] + ")");
            helper.succeed();
        });
    }

    // Failure Test for singleblock machine with ranged item input
    // Provides too few input items, should not run recipes.
    @GameTest(template = "singleblock_charged_cr", batch = "RangedIngredients")
    public static void singleblockRangedItemInputFailure(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 10;
        itemIn.setStackInSlot(0, CR_IN.copyWithCount(8));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per non- recipe run
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(0);

            helper.assertTrue(itemOut.isEmpty(),
                    "Singleblock CR should not have run, ran [" +
                            itemOut.getStackInSlot(0).getCount() + "] times");
            helper.assertTrue(TestUtils.isItemStackEqual(results, CR_IN.copyWithCount(8)),
                    "Singleblock CR should not have consumed items, consumed [" +
                            (8 - results.getCount()) + "]");

            helper.succeed();
        });
    }

    // Test for singleblock machine with ranged item input
    @GameTest(template = "singleblock_charged_cr", batch = "RangedIngredients")
    public static void singleblockRangedItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 7;
        itemIn.setStackInSlot(0, CR_IN.copyWithCount(64));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 1, () -> {
                addedRolls[finalI] = itemIn.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(0);
            int upperLimit = 64 - (runs * 0);
            int lowerLimit = 64 - (runs * 9);
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Singleblock CR didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");
            helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                    "Singleblock CR didn't consume correct number of items, consumed [" +
                            (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getCount() == lowerLimit),
                    "Singleblock CR rolled max value on every roll");
            helper.assertFalse((results.getCount() == upperLimit),
                    "Singleblock CR rolled min value on every roll");

            // check if all the rolls were equal, but not min/max
            int[] rolls = new int[runs];
            rolls[0] = 64 - addedRolls[0];
            boolean allEqual = false;
            for (int i = 1; i < runs; i++) {
                rolls[i] = addedRolls[i - 1] - addedRolls[i];
                if (rolls[i] == rolls[i - 1]) {
                    allEqual = true;
                } else {
                    allEqual = false;
                    break;
                }
            }
            helper.assertFalse(allEqual,
                    "Singleblock CR rolled the same value on every input roll (rolled " + rolls[0] + ")");
            helper.succeed();
        });
    }

    // Test for singleblock machine with ranged item output
    @GameTest(template = "singleblock_charged_cr", batch = "RangedIngredients")
    public static void singleblockRangedItemOutput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 7;
        itemIn.setStackInSlot(0, CR_OUT.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 3, () -> {
                addedRolls[finalI] = itemOut.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            helper.assertTrue(itemIn.getStackInSlot(0).isEmpty(),
                    "Singleblock CR didn't complete correct number of recipes, completed [" +
                            itemIn.getStackInSlot(0).getCount() + "] not [" + runs + "]");
            ItemStack results = itemOut.getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemWithinRange(results, runs, runs * 9),
                    "Singleblock CR didn't produce correct number of items, produced [" +
                            results.getCount() + "] not [" + runs + "-" + (runs * 9) + "]");
            helper.assertFalse((results.getCount() == runs * 9),
                    "Singleblock CR rolled max value on every roll");
            helper.assertFalse((results.getCount() == runs * 0),
                    "Singleblock CR rolled min value on every roll");

            // check if all the rolls were equal, but not min/max
            int[] rolls = new int[runs];
            rolls[0] = addedRolls[0];
            boolean allEqual = false;
            for (int i = 1; i < runs; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (rolls[i] == rolls[i - 1]) {
                    allEqual = true;
                } else {
                    allEqual = false;
                    break;
                }
            }
            helper.assertFalse(allEqual,
                    "Singleblock CR rolled the same value on every input roll (rolled " + rolls[0] + ")");
            helper.succeed();
        });
    }

    // test for multiblock machine with ranged item input
    @GameTest(template = "lcr_ranged_ingredients", batch = "RangedIngredients")
    public static void multiblockLCRRangedItemInput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 7;
        itemIn.setStackInSlot(0, LCR_IN.copyWithCount(64));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 1, () -> {
                addedRolls[finalI] = itemIn.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(0);
            int upperLimit = 64 - (runs * 0);
            int lowerLimit = 64 - (runs * 9);
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "LCR didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");
            helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                    "LCR didn't consume correct number of items, consumed [" +
                            (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getCount() == lowerLimit),
                    "LCR rolled max value on every roll");
            helper.assertFalse((results.getCount() == upperLimit),
                    "LCR rolled min value on every roll");

            // check if all the rolls were equal, but not min/max
            int[] rolls = new int[runs];
            rolls[0] = 64 - addedRolls[0];
            boolean allEqual = false;
            for (int i = 1; i < runs; i++) {
                rolls[i] = addedRolls[i - 1] - addedRolls[i];
                if (rolls[i] == rolls[i - 1]) {
                    allEqual = true;
                } else {
                    allEqual = false;
                    break;
                }
            }
            helper.assertFalse(allEqual,
                    "LCR rolled the same value on every input roll (rolled " + rolls[0] + ")");
            helper.succeed();
        });
    }

    // test for multiblock machine with ranged item input
    @GameTest(template = "lcr_ranged_ingredients", batch = "RangedIngredients")
    public static void multiblockLCRRangedItemOutput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 7;
        itemIn.setStackInSlot(0, LCR_OUT.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 3, () -> {
                addedRolls[finalI] = itemOut.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            helper.assertTrue(itemIn.getStackInSlot(0).isEmpty(),
                    "LCR didn't complete correct number of recipes, completed [" +
                            itemIn.getStackInSlot(0).getCount() + "] not [" + runs + "]");
            ItemStack results = itemOut.getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemWithinRange(results, runs, runs * 9),
                    "LCR didn't produce correct number of items, produced [" +
                            results.getCount() + "] not [" + runs + "-" + (runs * 9) + "]");
            helper.assertFalse((results.getCount() == runs * 9),
                    "LCR rolled max value on every roll");
            helper.assertFalse((results.getCount() == runs * 0),
                    "LCR rolled min value on every roll");

            // check if all the rolls were equal, but not min/max
            int[] rolls = new int[runs];
            rolls[0] = addedRolls[0];
            boolean allEqual = false;
            for (int i = 1; i < runs; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (rolls[i] == rolls[i - 1]) {
                    allEqual = true;
                } else {
                    allEqual = false;
                    break;
                }
            }
            helper.assertFalse(allEqual,
                    "LCR rolled the same value on every input roll (rolled " + rolls[0] + ")");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged item input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemInput16Parallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1; // unused on this test
        int parallels = 16;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        itemIn.setStackInSlot(0, LCENT_IN.copyWithCount(64));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(parallels));

        // 1t to turn on, 4t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(17 * finalI, () -> {
                ItemStack results = itemIn.getStackInSlot(0);
                int upperLimit = 64 - (batches * parallels * 0);
                int lowerLimit = 64 - (batches * parallels * 4);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isItemStackEqual(itemOut.getStackInSlot(0)
                                .copyWithCount((int) Math.round(itemOut.getTotalContentAmount())),
                                STONE.copyWithCount(completed)),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(itemOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                        "Parallel LCent didn't consume correct number of items, consumed " +
                                (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getCount();

                // reset for a rerun
                itemIn.setStackInSlot(0, LCENT_IN.copyWithCount(64));
                itemIn.setStackInSlot(1, COBBLE.copyWithCount(parallels));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            for (int i = 0; i < REPLICAS; i++) {
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Parallel LCent ranged item input test iteration " + i + " consumed [" +
                            rolls[i] + "] items, a multiple of its Batch * Parallel count (" + (parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Parallel LCent ranged item input test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged item output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemOutput16Parallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1; // unused on this test
        int parallels = 16;
        itemIn.setStackInSlot(0, LCENT_OUT.copyWithCount(16));

        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(17 * finalI, () -> {
                int runs = finalI * batches * parallels;
                helper.assertTrue(itemIn.getStackInSlot(0).isEmpty(),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                itemIn.getStackInSlot(0).getCount() + "] not [" + runs + "]");
                int resultCount = (int) Math.round(itemOut.getTotalContentAmount());
                int lowerLimit = runs * 0;
                int upperLimit = runs * 4;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Parallel LCent didn't produce correct number of items, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                itemIn.setStackInSlot(0, LCENT_OUT.copyWithCount(16));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            int[] rolls = new int[REPLICAS];

            rolls[0] = addedRolls[0];
            if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[0], batches, parallels, 1)) {
                sus = true;
                GTCEu.LOGGER.warn("Parallel LCent ranged item output test iteration " + 1 + " produced [" +
                        rolls[0] + "] items, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                        "). If this message only appears once, this is likely a false positive.");
            }
            for (int i = 1; i < REPLICAS; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Parallel LCent ranged item output test iteration " + (i + 1) + " produced [" +
                            rolls[i] + "] items, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Parallel LCent ranged item output test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged item input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemInputBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 1; // unused on this test
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        itemIn.setStackInSlot(0, LCENT_IN.copyWithCount(64));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(batches));

        // 1t to turn on, 1t per recipe run
        // 16 batches
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(17 * finalI, () -> {
                ItemStack results = itemIn.getStackInSlot(0);
                int upperLimit = 64 - (batches * parallels * 0);
                int lowerLimit = 64 - (batches * parallels * 4);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isItemStackEqual(itemOut.getStackInSlot(0)
                                .copyWithCount((int) Math.round(itemOut.getTotalContentAmount())),
                                STONE.copyWithCount(completed)),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(itemOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                        "Parallel LCent didn't consume correct number of items, consumed " +
                                (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getCount();

                // reset for a rerun
                itemIn.setStackInSlot(0, LCENT_IN.copyWithCount(64));
                itemIn.setStackInSlot(1, COBBLE.copyWithCount(batches));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            for (int i = 0; i < REPLICAS; i++) {
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Parallel LCent ranged item input test iteration " + i + " consumed [" +
                            rolls[i] + "] items, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Parallel LCent ranged item input test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged item output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemOutputBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 1; // unused on this test
        itemIn.setStackInSlot(0, LCENT_OUT.copyWithCount(16));

        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(17 * finalI, () -> {
                int runs = finalI * batches * parallels;
                helper.assertTrue(itemIn.getStackInSlot(0).isEmpty(),
                        "Batched LCent didn't complete correct number of recipes, completed [" +
                                itemIn.getStackInSlot(0).getCount() + "] not [" + runs + "]");
                int resultCount = (int) Math.round(itemOut.getTotalContentAmount());
                int lowerLimit = runs * 0;
                int upperLimit = runs * 4;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Batched LCent didn't produce correct number of items, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                itemIn.setStackInSlot(0, LCENT_OUT.copyWithCount(16));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            int[] rolls = new int[REPLICAS];

            rolls[0] = addedRolls[0];
            if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[0], batches, parallels, 1)) {
                sus = true;
                GTCEu.LOGGER.warn("Batched LCent ranged item output test iteration " + 1 + " produced [" +
                        rolls[0] + "] items, a multiple of its Batch * Parallel count (" + batches +
                        "). If this message only appears once, this is likely a false positive.");
            }
            for (int i = 1; i < REPLICAS; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched LCent ranged item output test iteration " + (i + 1) + " produced [" +
                            rolls[i] + "] items, a multiple of its Batch * Parallel count (" + batches +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched LCent ranged item output test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged item input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              timeoutTicks = 500)
    public static void multiblockLCentRangedItemInput16ParallelBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 16;
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int j;
        int stacks = batches * parallels / 64;

        for (j = 0; j < stacks; j++) {
            itemIn.setStackInSlot(j, COBBLE.copyWithCount((batches * parallels / stacks)));
        }
        for (int k = j; k < stacks + batches; k++) {
            itemIn.setStackInSlot(k, LCENT_IN.copyWithCount(64));
        }

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(65 * finalI, () -> {
                ItemStack results = itemIn.getStackInSlot(0);
                int upperLimit = 64 - (batches * parallels * 0);
                int lowerLimit = 64 - (batches * parallels * 4);
                int completed = batches * parallels * finalI;
                helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0)
                        .copyWithCount((int) Math.round(itemOut.getTotalContentAmount())),
                        STONE.copyWithCount(completed)),
                        "Batched Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(itemOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                        "Batched Parallel LCent didn't consume correct number of items, consumed " +
                                (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getCount();

                // reset for a rerun
                int l;
                for (l = 0; l < stacks; l++) {
                    itemIn.setStackInSlot(l,
                            COBBLE.copyWithCount((batches * parallels / stacks)));
                }
                for (int k = l; k < stacks + batches; k++) {
                    itemIn.setStackInSlot(k, LCENT_IN.copyWithCount(64));
                }
            });
        }

        helper.runAfterDelay(1 + 65 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            for (int i = 0; i < REPLICAS; i++) {
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched Parallel LCent ranged item input test iteration " + i + " consumed [" +
                            rolls[i] + "] items, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched Parallel LCent ranged item input test rolled exactly even to" +
                    " Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged item output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              timeoutTicks = 500)
    public static void multiblockLCentRangedItemOutput16ParallelBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 16;
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        for (int j = 0; j < batches; j++) {
            itemIn.setStackInSlot(j, LCENT_OUT.copyWithCount(16));
        }

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(65 * finalI, () -> {
                int runs = finalI * batches * parallels;
                helper.assertTrue(itemIn.isEmpty(),
                        "Batched Parallel LCent didn't complete correct number of recipes, completed [" +
                                itemIn.getTotalContentAmount() + "] not [" + runs + "]");
                int resultCount = (int) Math.round(itemOut.getTotalContentAmount());
                int lowerLimit = runs * 0;
                int upperLimit = runs * 4;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Batched Parallel LCent didn't produce correct number of items, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                for (int j = 0; j < batches; j++) {
                    itemIn.setStackInSlot(j, LCENT_OUT.copyWithCount(16));
                }
            });
        }

        helper.runAfterDelay(1 + 65 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            int[] rolls = new int[REPLICAS];

            rolls[0] = addedRolls[0];
            if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[0], batches, parallels, 1)) {
                sus = true;
                GTCEu.LOGGER.warn("Batched Parallel LCent ranged item output test iteration " + 1 + " produced [" +
                        rolls[0] + "] items, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                        "). If this message only appears once, this is likely a false positive.");
            }
            for (int i = 1; i < REPLICAS; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched Parallel LCent ranged item output test iteration " + (i + 1) +
                            " produced [" + rolls[i] + "] items, a multiple of its Batch * Parallel count (" +
                            (batches * parallels) + "). If this message only appears once, this is likely" +
                            " a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched Parallel LCent ranged item output test rolled exactly even to" +
                    " Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }
}
