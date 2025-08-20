package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
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

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;

/*
 * TODO:
 * do 3 passes of most tests as a safeguard against bad rolls
 * Same output more than once
 * Out of bounds
 * Output a multiple of batchparallels
 * 
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class IntProviderIngredientTest {

    private static GTRecipeType CR_RECIPE_TYPE;
    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CENTRIFUGE_RECIPE_TYPE;

    @BeforeBatch(batch = "RangedIngredients")
    public static void prepare(ServerLevel level) {
        CR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_cr_tests", 2,2,3,2);
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_lcr_tests", 3,3,3,3);
        CENTRIFUGE_RECIPE_TYPE = TestUtils.createRecipeType("ranged_inputs_centrifuge_tests", 1,1,6,6);

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_item_cr"))
                .inputItemsRanged(new ItemStack(Items.BLUE_BED), UniformInt.of(1, 9))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_item_cr"))
                .inputItems(new ItemStack(Blocks.BRICK_SLAB))
                .outputItemsRanged(new ItemStack(Blocks.STONE), UniformInt.of(5, 5))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

//        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
//                .recipeBuilder(GTCEu.id("test_ranged_input_item_lcr"))
//                .inputItemsRanged(new ItemStack(Items.GREEN_BED), UniformInt.of(1, 8))
//                .inputItems(new ItemStack(Blocks.COBBLESTONE))
//                .outputItems(new ItemStack(Blocks.STONE))
//                .EUt(GTValues.V[GTValues.HV])
//                .duration(4)
//                .buildRawRecipe());
//
//        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
//                .recipeBuilder(GTCEu.id("test_ranged_input_item_cent"))
//                .inputItemsRanged(new ItemStack(Items.RED_BED), UniformInt.of(1, 8))
//                .inputItems(new ItemStack(Blocks.COBBLESTONE))
//                .outputItems(new ItemStack(Blocks.STONE))
//                .EUt(GTValues.V[GTValues.HV])
//                .duration(16)
//                .buildRawRecipe());
    }

    private static MetaMachine getMetaMachine(BlockEntity entity) {
        return ((MetaMachineBlockEntity) entity).getMetaMachine();
    }

//    private record BusHolder(ItemBusPartMachine inputBus1, FluidHatchPartMachine inputHatch1,
//                             ItemBusPartMachine outputBus1,
//                             FluidHatchPartMachine outputHatch1, WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this LCR template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
/*    private static BusHolder getBussesAndFormLCR(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
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
/*    private static BusHolder getBussesAndFormLCENT(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(CENTRIFUGE_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 0, 0)));
        FluidHatchPartMachine inputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 0, 0)));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        return new BusHolder(inputBus1, inputHatch1, outputBus1, outputHatch1, controller);
    }

    /*
     * // Test for running HV recipe at HV
     * 
     * @GameTest(template = "lcr_input_separation", batch = "RangedIngredients", setupTicks = 40, timeoutTicks = 200)
     * public static void overclockLogicOnTierNothingChanges(GameTestHelper helper) {
     * BusHolder busHolder = getBussesAndFormLCR(helper);
     * busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.RED_BED));
     * // One tick to start, 20 for the recipe to run
     * helper.succeedOnTickWhen(21, () -> {
     * helper.assertTrue(
     * TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
     * new ItemStack(Blocks.STONE)),
     * "Item didn't craft at the right tick with an on-tier recipe" +
     * busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
     * });
     * }
     * 
     * // Test for running LV 1t recipe at HV
     * 
     * @GameTest(template = "lcr_input_separation", batch = "RangedIngredients", setupTicks = 40, timeoutTicks = 200)
     * public static void overclockLogicTwoTiersAbove16Parallels(GameTestHelper helper) {
     * BusHolder busHolder = getBussesAndFormLCR(helper);
     * busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.STICK, 64));
     * // One tick to start, 4 for the recipe to run (16/t from ULV recipe to HV)
     * helper.succeedOnTickWhen(5, () -> {
     * helper.assertTrue(
     * TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
     * new ItemStack(Blocks.STONE, 64)),
     * "Item didn't craft at the right tick with an on-tier recipe" +
     * busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
     * });
     * }
     */
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
        itemIn.setStackInSlot(0, new ItemStack(Items.BLUE_BED, 64));
        itemIn.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++){
            final int finalI = i; //lambda preserve you
            helper.runAfterDelay(2*i + 1, () -> {
                addedRolls[finalI] = itemIn.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(0);
            int upperLimit = 64 - (runs * 1);
            int lowerLimit = 64 - (runs * 9);
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), new ItemStack(Blocks.STONE, runs)),
                    "Singleblock CR didn't complete correct number of recipes, completed " +
                            itemOut.getStackInSlot(0).getCount());
            helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                    "Singleblock CR didn't consume correct number of items, consumed " +
                            (64 - results.getCount()) + "items");
            helper.assertFalse((results.getCount() == lowerLimit),
                    "Singleblock CR rolled max value on every roll");
            helper.assertFalse((results.getCount() == upperLimit),
                    "Singleblock CR rolled min value on every roll");

            // check if all the rolls were equal, but not min/max
            int[] rolls = new int[runs];
            rolls[0] = addedRolls[0];
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
                    "Singleblock CR rolled the same value on every input roll");
            helper.succeed();
        });
    }

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
        for (int i = 0; i < runs; i++){
            final int finalI = i; //lambda preserve you
            helper.runAfterDelay(2*i + 1, () -> {
                addedRolls[finalI] = itemIn.getStackInSlot(0).getCount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemOut.getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemWithinRange(results, runs, runs * 9),
                    "Singleblock CR didn't produce correct number of items, produced " +
                            results.getCount() + "items");
            helper.assertFalse((results.getCount() == runs * 9),
                    "Singleblock CR rolled max value on every roll");
            helper.assertFalse((results.getCount() == runs),
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
                    "Singleblock CR rolled the same value on every input roll");
            helper.succeed();
        });
    }
}
