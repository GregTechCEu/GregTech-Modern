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
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import lombok.Getter;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;

// spotless, stop breaking my todo comments!
// spotless:off
/*
 * TODO: (done, but retained for paper trail)
 *  do many passes of most tests as a safeguard against bad rolls
 *  Same output more than once
 *  Out of bounds
 *  Output a multiple of batchparallels
 *  Rolls of 0
 *  Forced rolls of 0 breaking recipes
 */
// spotless:on
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class IntProviderIngredientTest {

    private static GTRecipeType CR_RECIPE_TYPE;
    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CENTRIFUGE_RECIPE_TYPE;

    /**
     * How many times to repeat the Batch and Parallel random roll tests to avoid false positives
     * Currently set to 7, with singleblock recipes processing up to 9 items, allowing for stacks of up to 63 items.
     */
    @Getter
    private static final int REPLICAS = 7;

    @BeforeBatch(batch = "RangedIngredients")
    public static void prepare(ServerLevel level) {
        CR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_cr_tests", 2, 2, 3, 2);
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_lcr_tests", 3, 3, 3, 3);
        CENTRIFUGE_RECIPE_TYPE = TestUtils.createRecipeType("ranged_inputs_centrifuge_tests", 2, 6, 1, 6);

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_cr"))
                .inputItemsRanged(new ItemStack(Items.GREEN_STAINED_GLASS), UniformInt.of(0, 9))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_cr"))
                .inputItems(new ItemStack(Blocks.BRICK_SLAB))
                .outputItemsRanged(new ItemStack(Blocks.STONE), UniformInt.of(0, 9))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_lcr"))
                .inputItemsRanged(new ItemStack(Items.BLACK_STAINED_GLASS), UniformInt.of(0, 9))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_lcr"))
                .inputItems(new ItemStack(Blocks.BRICK_STAIRS))
                .outputItemsRanged(new ItemStack(Blocks.STONE), UniformInt.of(0, 9))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_cent"))
                .inputItemsRanged(new ItemStack(Items.LIME_STAINED_GLASS), UniformInt.of(0, 4))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.IV])
                .duration(1)
                .buildRawRecipe());

        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_cent"))
                .inputItems(new ItemStack(Blocks.BRICK_WALL))
                .outputItemsRanged(new ItemStack(Blocks.STONE), UniformInt.of(0, 4))
                .EUt(GTValues.V[GTValues.IV])
                .duration(1)
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

    // TODO: SABOTAGED OUT
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
        itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_SLAB, runs));
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

    // TODO: IN
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
        itemIn.setStackInSlot(0, new ItemStack(Items.GREEN_STAINED_GLASS, 64));
        itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, runs));
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
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), new ItemStack(Blocks.STONE, runs)),
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
                rolls[i] = 64 - (addedRolls[i] - addedRolls[i - 1]);
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

    // TODO: OUT
    // Test for singleblock machine with ranged item input
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
        itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_SLAB, runs));
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

    // TODO: IN
    // test for multiblock machine with ranged item input
    @GameTest(template = "lcr_ranged_ingredients", batch = "RangedIngredients", setupTicks = 40, timeoutTicks = 200)
    public static void multiblockLCRRangedItemInput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 7;
        itemIn.setStackInSlot(0, new ItemStack(Items.BLACK_STAINED_GLASS, 64));
        itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, runs));
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
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), new ItemStack(Blocks.STONE, runs)),
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
                rolls[i] = 64 - (addedRolls[i] - addedRolls[i - 1]);
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

    // TODO: OUT
    // test for multiblock machine with ranged item input
    @GameTest(template = "lcr_ranged_ingredients", batch = "RangedIngredients", setupTicks = 40, timeoutTicks = 200)
    public static void multiblockLCRRangedItemOutput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 7;
        itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_STAIRS, runs));
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

    // TODO: IN
    // test for multiblock machine with 16x Parallels with ranged item input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemInput16Parallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1; // unused on this test
        int parallels = 16;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        itemIn.setStackInSlot(0, new ItemStack(Items.LIME_STAINED_GLASS, 64));
        itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, parallels));



        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * finalI, () -> {

                GTRecipe check = busHolder.controller.getRecipeLogic().getLastRecipe();

                ItemStack results = itemIn.getStackInSlot(0);
                int upperLimit = 64 - (batches * parallels * 0);
                int lowerLimit = 64 - (batches * parallels * 4);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isItemStackEqual(itemOut.getStackInSlot(0)
                                .copyWithCount((int) Math.round(itemOut.getTotalContentAmount())),
                                new ItemStack(Blocks.STONE, completed)),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(itemOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                        "Parallel LCent didn't consume correct number of items, consumed " +
                                (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getCount();

                // reset for a rerun
                itemIn.setStackInSlot(0, new ItemStack(Items.LIME_STAINED_GLASS, 64));
                itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, parallels));
            });
        }

        helper.runAfterDelay(1 + 2 * REPLICAS, () -> {
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

    // TODO: OUT
    // test for multiblock machine with 16x Parallels with ranged item output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemOutput16Parallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1; // unused on this test
        int parallels = 16;
        itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_WALL, 16));

        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * finalI, () -> {
                int resultCount = (int) Math.round(itemOut.getTotalContentAmount());
                int lowerLimit = finalI * batches * parallels * 0;
                int upperLimit = finalI * batches * parallels * 4;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Parallel LCent didn't produce correct number of items, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_WALL, 16));
            });
        }

        helper.runAfterDelay(1 + 2 * REPLICAS, () -> {
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

    // TODO: IN
    // TODO 2: fix the run count and time
    // test for multiblock machine with 16x Parallels with ranged item input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemInputBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 1; // unused on this test
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        itemIn.setStackInSlot(0, new ItemStack(Items.LIME_STAINED_GLASS, 64));
        itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, batches));

        // 1t to turn on, 1t per recipe run
        // 16 batches
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(5 * finalI, () -> {

                GTRecipe check = busHolder.controller.getRecipeLogic().getLastRecipe();

                ItemStack results = itemIn.getStackInSlot(0);
                int upperLimit = 64 - (batches * parallels * 0);
                int lowerLimit = 64 - (batches * parallels * 4);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isItemStackEqual(itemOut.getStackInSlot(0)
                                .copyWithCount((int) Math.round(itemOut.getTotalContentAmount())),
                                new ItemStack(Blocks.STONE, completed)),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(itemOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                        "Parallel LCent didn't consume correct number of items, consumed " +
                                (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getCount();

                // reset for a rerun
                itemIn.setStackInSlot(0, new ItemStack(Items.LIME_STAINED_GLASS, 64));
                itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, batches));
            });
        }

        helper.runAfterDelay(1 + 5 * REPLICAS, () -> {
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

    // TODO: OUT
    // TODO 2: fix the run count and time
    // test for multiblock machine with 16x Parallels with ranged item output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemOutputBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 1; // unused on this test
        itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_WALL, 16));

        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * finalI, () -> {
                int resultCount = (int) Math.round(itemOut.getTotalContentAmount());
                int lowerLimit = finalI * batches * parallels * 0;
                int upperLimit = finalI * batches * parallels * 4;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Parallel LCent didn't produce correct number of items, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                itemIn.setStackInSlot(0, new ItemStack(Items.BRICK_WALL, 16));
            });
        }

        helper.runAfterDelay(1 + 2 * REPLICAS, () -> {
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

    // TODO: IN
    // TODO 2: fix the all of it
    // test for multiblock machine with 16x Parallels with ranged item input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              setupTicks = 40,
              timeoutTicks = 200)
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

        for (j = 0; j < stacks; j++){
            itemIn.setStackInSlot(j, new ItemStack(Items.COBBLESTONE, (batches * parallels / stacks)));
        }
        for (int k=j; k < stacks+batches; k++){
            itemIn.setStackInSlot(k, new ItemStack(Items.LIME_STAINED_GLASS, 64));
        }

        // 1t to turn on, 1t per recipe run
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
                                new ItemStack(Blocks.STONE, completed)),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(itemOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                        "Parallel LCent didn't consume correct number of items, consumed " +
                                (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getCount();

                // reset for a rerun
                int l;
                for (l = 0; l < stacks; l++){
                    itemIn.setStackInSlot(l, new ItemStack(Items.COBBLESTONE, (batches * parallels / stacks)));
                }
                for (int k=l; k < stacks+batches; k++){
                    itemIn.setStackInSlot(k, new ItemStack(Items.LIME_STAINED_GLASS, 64));
                }
            });
        }

        helper.runAfterDelay(1 + 2 * REPLICAS, () -> {
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

    // TODO: OUT
    // TODO 2: fix the all of it
    // test for multiblock machine with 16x Parallels with ranged item output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedIngredients",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void multiblockLCentRangedItemOutput16ParallelBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 16;
        int parallels = 16;
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        for (int j=0; j<batches; j++){
            itemIn.setStackInSlot(j, new ItemStack(Items.BRICK_WALL, 16));
        }

        // 1t to turn on, 1t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * finalI, () -> {
                int resultCount = (int) Math.round(itemOut.getTotalContentAmount());
                int lowerLimit = finalI * batches * parallels * 0;
                int upperLimit = finalI * batches * parallels * 4;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Parallel LCent didn't produce correct number of items, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                for (int j=0; j<batches; j++){
                    itemIn.setStackInSlot(j, new ItemStack(Items.BRICK_WALL, 16));
                }
            });
        }

        helper.runAfterDelay(1 + 5 * REPLICAS, () -> {
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
}
