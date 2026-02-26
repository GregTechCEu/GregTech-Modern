package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;
import static com.gregtechceu.gtceu.data.recipe.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.LARGE_CHEMICAL_RECIPES;
import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class OverclockLogicTest {

    static GTRecipeType LCR_RECIPE_TYPE;
    static GTRecipeType CR_RECIPE_TYPE;

    @BeforeBatch(batch = "OverclockLogic")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("overclock_logic_lcr_tests", GTRecipeTypes.LARGE_CHEMICAL_RECIPES);
        CR_RECIPE_TYPE = TestUtils.createRecipeType("overclock_logic_cr_tests", GTRecipeTypes.CHEMICAL_RECIPES);

        LCR_RECIPE_TYPE.getAdditionHandler().beginStaging();
        CR_RECIPE_TYPE.getAdditionHandler().beginStaging();
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic"))
                .inputItems(new ItemStack(Items.RED_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV])
                .duration(20)
                // NBT has a schematic in it with an HV energy input hatch
                .build());
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_2"))
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.LV])
                .duration(1)
                // NBT has a schematic in it with an HV energy input hatch
                .build());
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_3"))
                .inputItems(new ItemStack(Items.BROWN_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.EV])
                .duration(1)
                // NBT has a schematic in it with an HV energy input hatch
                .build());
        CR_RECIPE_TYPE.getAdditionHandler().addStaging(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_4"))
                .inputItems(new ItemStack(Items.RED_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(16)
                // NBT has a schematic in it with an HV charged singleblock CR in it
                .build());
        CR_RECIPE_TYPE.getAdditionHandler().addStaging(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_5"))
                .inputItems(new ItemStack(Items.BROWN_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.MV])
                .duration(16)
                // NBT has a schematic in it with an HV charged singleblock CR in it
                .build());
        LCR_RECIPE_TYPE.getAdditionHandler().completeStaging();
        CR_RECIPE_TYPE.getAdditionHandler().completeStaging();
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        controller.setRecipeType(LCR_RECIPE_TYPE);
        TestUtils.formMultiblock(controller);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        return new BusHolder(inputBus1, inputBus2, outputBus1, controller);
    }

    // Test for running HV recipe at HV
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicOnTierNothingChanges(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.RED_BED));
        // One tick to start, 20 for the recipe to run
        helper.succeedOnTickWhen(21, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    // Test for running LV 1t recipe at HV
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicTwoTiersAbove16Parallels(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.STICK, 64));
        // One tick to start, 4 for the recipe to run (16/t from ULV recipe to HV)
        helper.succeedOnTickWhen(5, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE, 64)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    // Test for running EV recipe at HV
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicOverTierNothingHappens(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    busHolder.outputBus1.getInventory().getStackInSlot(0).getItem().equals(Blocks.STONE.asItem()),
                    "Item crafted at one tier over when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper, 200);
    }

    // Test for code wise calculating perfect OC
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyPerfectOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-input-separation"))
                .id(GTCEu.id("test-multiblock-input-separation"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.ACACIA_WOOD))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(100)
                .build();

        GTRecipe newRecipe = OC_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);
        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.duration == (recipeBeforeModifiers.duration / PERFECT_DURATION_FACTOR_INV),
                "Perfect perfect overclock didn't cut recipe time by 4");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Non perfect overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating non-perfect OC
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyNonPerfectOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-npo"))
                .id(GTCEu.id("test-multiblock-overclock-test-npo"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.ACACIA_WOOD))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(100)
                .build();

        GTRecipe newRecipe = OC_NON_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);
        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.duration == (recipeBeforeModifiers.duration / STD_DURATION_FACTOR_INV),
                "Non perfect overclock didn't cut recipe time by 2");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Non perfect overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating subtick perfect OC
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyPerfectParallelOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-psto"))
                .id(GTCEu.id("test-multiblock-overclock-test-psto"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(1)
                .build();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_PERFECT_SUBTICK.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.subtickParallels == PERFECT_DURATION_FACTOR_INV,
                "Perfect subtick overclock didn't multiply parallels by 4");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Perfect subtick overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating subtick non-perfect OC
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyNonPerfectParallelOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .id(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(1)
                .build();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_NON_PERFECT_SUBTICK.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.subtickParallels == STD_DURATION_FACTOR_INV,
                "Non-Perfect subtick overclock didn't multiply parallels by 2");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Non-Perfect subtick overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating non-subtick non-perfect OC on a 1t recipe
    @TestHolder()
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyNonPerfectNonParallel1tOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .id(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(1)
                .build();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_NON_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.subtickParallels == 1,
                "Non-Perfect Non-subtick overclock overclocked when it shouldn't have");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() == recipeBeforeModifiers.getInputEUt().getTotalEU(),
                "Non-Perfect Non-subtick overclock at 1t changed EU");
        helper.succeed();
    }

    // Test for code wise calculating an overclock on a recipe that can't be run
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicEVRecipeHVMachineTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-ev-hv"))
                .id(GTCEu.id("test-multiblock-overclock-test-ev-hv"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.EV]).duration(1)
                .build();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_NON_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe == null, "Applied EV overclock to HV recipe when it shouldn't have");

        helper.succeed();
    }
}
