package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Test cases:
 * We have 3 chance logics (OR, AND, XOR). FIRST isn't real.
 * Chance logics are applied to all ingredients within a Handler and IO side. AND and XOR roll in those groupings.
 * Singleblock rolls are % chances. Batch/Parallel rolls multiply for an expected "guaranteed" amount.
 * Need to special test Assembly Line and Distillation Tower because they use nonstandard recipe logic
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ChancedIngredientORTest {

    private static GTRecipeType CR_RECIPE_TYPE;
    private static GTRecipeType ASSM_RECIPE_TYPE;
    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CENTRIFUGE_RECIPE_TYPE;

    // items used in recipes. Up top here for quick replacements.
    private static final ItemStack IN_SINGLE = new ItemStack(Items.STRIPPED_SPRUCE_WOOD);
    private static final ItemStack OUT_SINGLE = new ItemStack(Items.SPRUCE_SLAB);
    private static final ItemStack IN_OR_1 = new ItemStack(Items.STRIPPED_BIRCH_WOOD);
    private static final ItemStack IN_OR_2 = new ItemStack(Items.BIRCH_SLAB);
    private static final ItemStack IN_OR_3 = new ItemStack(Items.STRIPPED_JUNGLE_WOOD);
    private static final ItemStack IN_OR_4 = new ItemStack(Items.JUNGLE_SLAB);
    private static final ItemStack IN_TICK_1 = new ItemStack(Items.STRIPPED_OAK_WOOD);
    private static final ItemStack IN_TICK_2 = new ItemStack(Items.OAK_SLAB);
    private static final ItemStack IN_TICK_3 = new ItemStack(Items.STRIPPED_ACACIA_WOOD);
    private static final ItemStack IN_TICK_4 = new ItemStack(Items.ACACIA_SLAB);
    private static final ItemStack COBBLE = new ItemStack(Items.COBBLESTONE);
    private static final ItemStack STONE = new ItemStack(Items.STONE);

    @BeforeBatch(batch = "ChancedIngredientsOR")
    public static void prepare(ServerLevel level) {
        CR_RECIPE_TYPE = TestUtils.createRecipeType("chanced_ingredient_or_cr_tests", GTRecipeTypes.CHEMICAL_RECIPES);
        ASSM_RECIPE_TYPE = TestUtils.createRecipeType("chanced_ingredient_or_assm_tests", GTRecipeTypes.ASSEMBLER_RECIPES);
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("chanced_ingredient_or_lcr_tests",
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES);
        CENTRIFUGE_RECIPE_TYPE = TestUtils.createRecipeType("chanced_ingredient_or_centrifuge_tests",
                GTRecipeTypes.CENTRIFUGE_RECIPES);

        var CRHandler = CR_RECIPE_TYPE.getAdditionHandler();
        CRHandler.beginStaging();
        var ASSMHandler = ASSM_RECIPE_TYPE.getAdditionHandler();
        ASSMHandler.beginStaging();
        var LCRHandler = LCR_RECIPE_TYPE.getAdditionHandler();
        LCRHandler.beginStaging();
        var CENTHandler = CENTRIFUGE_RECIPE_TYPE.getAdditionHandler();
        CENTHandler.beginStaging();

        CRHandler.addStaging(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_single_chanced_input_cr"))
                .chancedInput(IN_SINGLE.copyWithCount(3), 5000, 0)
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CRHandler.addStaging(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_single_chanced_output_cr"))
                .inputItems(IN_OR_3)
                .chancedOutput(STONE, 5000, 0)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CRHandler.addStaging(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_double_or_chanced_output_assm"))
                .inputItems(OUT_SINGLE)
                .chance(4000)
                .outputItems(IN_OR_1)
                .chance(6000)
                .outputItems(IN_OR_2)
                .chance(10000)
                .chancedItemOutputLogic(ChanceLogic.OR)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCRHandler.addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_double_or_chanced_input_lcr"))
                .chance(4000)
                .inputItems(IN_OR_1.copyWithCount(3))
                .chance(6000)
                .inputItems(IN_OR_2.copyWithCount(3))
                .chance(10000)
                .chancedItemInputLogic(ChanceLogic.OR)
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        ASSMHandler.addStaging(ASSM_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_double_or_chanced_input_assm"))
                .chance(4000)
                .inputItems(IN_OR_1.copyWithCount(3))
                .chance(6000)
                .inputItems(IN_OR_2.copyWithCount(3))
                .chance(10000)
                .chancedItemInputLogic(ChanceLogic.OR)
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        ASSMHandler.addStaging(ASSM_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_quad_or_chanced_input_assm"))
                .chance(3000)
                .inputItems(STONE.copyWithCount(3))
                .chance(4000)
                .inputItems(IN_OR_2.copyWithCount(3))
                .chance(5000)
                .inputItems(IN_OR_3.copyWithCount(3))
                .chance(6000)
                .inputItems(IN_OR_4.copyWithCount(3))
                .chance(10000)
                .chancedItemInputLogic(ChanceLogic.OR)
                .inputItems(COBBLE)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CENTHandler.addStaging(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_double_or_chanced_output_cent"))
                .inputItems(COBBLE)
                .chance(4000)
                .outputItems(IN_OR_1.copyWithCount(1))
                .chance(6000)
                .outputItems(IN_OR_2.copyWithCount(1))
                .chance(10000)
                .chancedItemOutputLogic(ChanceLogic.OR)
                .EUt(GTValues.V[GTValues.HV])
                .duration(16)
                .buildRawRecipe());

        // will subtick overclock once
        CENTHandler.addStaging(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_quad_or_chanced_output_cent"))
                .inputItems(STONE)
                .chance(3000)
                .outputItems(IN_OR_1.copyWithCount(1))
                .chance(4000)
                .outputItems(IN_OR_2.copyWithCount(1))
                .chance(5000)
                .outputItems(IN_OR_3.copyWithCount(1))
                .chance(6000)
                .outputItems(IN_OR_4.copyWithCount(1))
                .chance(10000)
                .chancedItemOutputLogic(ChanceLogic.OR)
                .EUt(GTValues.V[GTValues.HV])
                .duration(8)
                .buildRawRecipe());

        // per tick 2 output
        // per tick 4 output
        CRHandler.addStaging(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_single_chanced_tick_input_cr"))
                .perTick(true)
                .chancedInput(STONE, 5000, 0)
                .perTick(false)
                .inputItems(IN_TICK_1)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(64)
                .buildRawRecipe());

        ASSMHandler.addStaging(ASSM_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_double_or_tick_chanced_input_assm"))
                .perTick(true)
                .chance(4000)
                .inputItems(IN_TICK_3)
                .chance(6000)
                .inputItems(COBBLE)
                .chance(10000)
                .perTick(false)
                .chancedItemInputLogic(ChanceLogic.OR)
                .inputItems(IN_TICK_4)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(16)
                .buildRawRecipe());

        ASSMHandler.addStaging(ASSM_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_quad_or_tick_chanced_input_assm"))
                .perTick(true)
                .chance(3000)
                .inputItems(STONE)
                .chance(4000)
                .inputItems(IN_OR_2)
                .chance(5000)
                .inputItems(IN_TICK_3)
                .chance(6000)
                .inputItems(IN_TICK_4)
                .chance(10000)
                .perTick(false)
                .chancedItemInputLogic(ChanceLogic.OR)
                .inputItems(IN_OR_3)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(16)
                .buildRawRecipe());

        LCRHandler.addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_double_or_tick_chanced_input_lcr"))
                .perTick(true)
                .chance(4000)
                .inputItems(STONE)
                .chance(6000)
                .inputItems(COBBLE)
                .chance(10000)
                .perTick(false)
                .chancedItemInputLogic(ChanceLogic.OR)
                .inputItems(IN_TICK_2)
                .outputItems(STONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(16)
                .buildRawRecipe());

        CENTHandler.addStaging(CENTRIFUGE_RECIPE_TYPE
            .recipeBuilder(GTCEu.id("test_double_or_tick_chanced_output_cent"))
            .inputItems(IN_TICK_1)
            .perTick(true)
            .chance(4000)
            .outputItems(IN_TICK_1.copyWithCount(1))
            .chance(6000)
            .outputItems(IN_TICK_2.copyWithCount(1))
            .chance(10000)
            .perTick(false)
            .chancedItemOutputLogic(ChanceLogic.OR)
            .EUt(GTValues.V[GTValues.HV])
            .duration(400)
            .buildRawRecipe());

    // will subtick overclock once
        CENTHandler.addStaging(CENTRIFUGE_RECIPE_TYPE
            .recipeBuilder(GTCEu.id("test_quad_or_tick_chanced_output_cent"))
            .inputItems(IN_TICK_2)
            .perTick(true)
            .chance(3000)
            .outputItems(IN_TICK_1.copyWithCount(1))
            .chance(4000)
            .outputItems(IN_TICK_2.copyWithCount(1))
            .chance(5000)
            .outputItems(IN_TICK_3.copyWithCount(1))
            .chance(6000)
            .outputItems(IN_TICK_4.copyWithCount(1))
            .chance(10000)
            .perTick(false)
            .chancedItemOutputLogic(ChanceLogic.OR)
            .EUt(GTValues.V[GTValues.HV])
            .duration(8)
                .buildRawRecipe());
    }

    private static MetaMachine getMetaMachine(BlockEntity entity) {
        return (MetaMachine) entity;
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

    // Failure Test for singleblock machine with chanced item input
    // Provides too few input items, should not run recipes.
    @GameTest(template = "singleblock_charged_cr", batch = "ChancedIngredientsOR")
    public static void singleblockSingleChancedItemInputFailure(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 10;
        itemIn.setStackInSlot(0, IN_SINGLE.copyWithCount(2));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per non- recipe run
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(0);

            helper.assertTrue(itemOut.isEmpty(),
                    "Singleblock CR should not have run, ran [" +
                            itemOut.getStackInSlot(0).getCount() + "] times");
            helper.assertTrue(TestUtils.isItemStackEqual(results, IN_SINGLE.copyWithCount(2)),
                    "Singleblock CR should not have consumed items, consumed [" +
                            (2 - results.getCount()) + "]");

            helper.succeed();
        });
    }

    // Test for singleblock machine with single chanced item input
    @GameTest(template = "singleblock_charged_cr", batch = "ChancedIngredientsOR")
    public static void singleblockSingleChancedItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 16;
        itemIn.setStackInSlot(0, IN_SINGLE.copyWithCount(runs * 3));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(0);
            int upperLimit = 64 - (runs * 0);
            int lowerLimit = 64 - (runs * 3);
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Singleblock CR didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");
            helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                    "Singleblock CR didn't consume correct number of items, consumed [" +
                            (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getCount() == lowerLimit),
                    "Singleblock CR succeeded every chance roll!");
            helper.assertFalse((results.getCount() == upperLimit),
                    "Singleblock CR failed every chance roll!");

            helper.succeed();
        });
    }

    // Failure Test for singleblock machine with two OR chanced item inputs
    // Provides too few input items, should not run recipes.
    @GameTest(template = "singleblock_charged_assembler", batch = "ChancedIngredientsOR")
    public static void singleblockDoubleORChancedItemInputFailure(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(ASSM_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 10;
        itemIn.setStackInSlot(0, IN_OR_1.copyWithCount(63));
        itemIn.setStackInSlot(1, IN_OR_2.copyWithCount(2));
        itemIn.setStackInSlot(2, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per non- recipe run
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(1);

            helper.assertTrue(itemOut.isEmpty(),
                    "Singleblock Assembler (OR) should not have run, ran [" +
                            itemOut.getStackInSlot(0).getCount() + "] times");
            helper.assertTrue(TestUtils.isItemStackEqual(results, IN_OR_2.copyWithCount(2)),
                    "Singleblock Assembler (OR) should not have consumed items, consumed [" +
                            (2 - results.getCount()) + "] + [ " + (63 - itemIn.getStackInSlot(0).getCount()) + "]");

            helper.succeed();
        });
    }

    // Test for singleblock machine with two OR chanced item inputs
    @GameTest(template = "singleblock_charged_assembler", batch = "ChancedIngredientsOR")
    public static void singleblockDoubleORChancedItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(ASSM_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 16;
        itemIn.setStackInSlot(0, IN_OR_1.copyWithCount(runs * 3));
        itemIn.setStackInSlot(1, IN_OR_2.copyWithCount(runs * 3));
        itemIn.setStackInSlot(2, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            int upperLimit = 48 - (runs * 0);
            int lowerLimit = 48 - (runs * 3);
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Singleblock Assembler (OR) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemIn.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Singleblock Assembler (OR) didn't consume correct number of item " + i + ", consumed [" +
                                (48 - result.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Singleblock Assembler (OR) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Singleblock Assembler (OR) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // Test for singleblock machine with four OR chanced item inputs
    @GameTest(template = "singleblock_charged_assembler", batch = "ChancedIngredientsOR")
    public static void singleblockQuadORChancedItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(ASSM_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 16;
        itemIn.setStackInSlot(0, STONE.copyWithCount(runs * 3));
        itemIn.setStackInSlot(1, IN_OR_2.copyWithCount(runs * 3));
        itemIn.setStackInSlot(2, IN_OR_3.copyWithCount(runs * 3));
        itemIn.setStackInSlot(3, IN_OR_4.copyWithCount(runs * 3));
        itemIn.setStackInSlot(4, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            int upperLimit = runs * 3;
            int lowerLimit = 0;
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Singleblock Assembler (OR) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");

            for (int i = 0; i < 4; i++) {
                ItemStack result = itemIn.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Singleblock Assembler (OR) didn't consume correct number of item " + i + ", consumed [" +
                                (48 - result.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Singleblock Assembler (OR) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Singleblock Assembler (OR) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // Test for singleblock machine with single chanced item output
    @GameTest(template = "singleblock_charged_cr", batch = "ChancedIngredientsOR")
    public static void singleblockSingleChancedItemOutput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 16;
        itemIn.setStackInSlot(0, IN_OR_3.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemOut.getStackInSlot(0);
            int upperLimit = runs * 1;
            int lowerLimit = runs * 0;
            helper.assertTrue(itemIn.isEmpty(),
                    "Singleblock CR didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getStackInSlot(0).getCount()) + "] not [" + runs + "]");
            helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                    "Singleblock CR didn't produce correct number of items, produced [" +
                            results.getCount() + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getCount() == lowerLimit),
                    "Singleblock CR succeeded every chance roll!");
            helper.assertFalse((results.getCount() == upperLimit),
                    "Singleblock CR failed every chance roll!");

            helper.succeed();
        });
    }

    // Test for singleblock machine with two OR chanced item outputs
    @GameTest(template = "singleblock_charged_cr", batch = "ChancedIngredientsOR")
    public static void singleblockDoubleORChancedItemOutput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 16;
        itemIn.setStackInSlot(0, OUT_SINGLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            int upperLimit = runs;
            int lowerLimit = 0;
            helper.assertTrue(itemIn.isEmpty(),
                    "Singleblock CR (OR) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getStackInSlot(0).getCount()) + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Singleblock CR (OR) didn't consume produce number of item " + i + ", produce [" +
                                result.getCount() + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Singleblock CR (OR) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Singleblock CR (OR) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // Failure Test for multiblock machine with two OR chanced item inputs
    // Provides too few input items, should not run recipes.
    @GameTest(template = "lcr_ranged_ingredients", batch = "ChancedIngredientsOR")
    public static void multiblockLCRDoubleORChancedItemInputFailure(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 10;
        itemIn.setStackInSlot(0, IN_OR_1.copyWithCount(63));
        itemIn.setStackInSlot(1, IN_OR_2.copyWithCount(2));
        itemIn.setStackInSlot(2, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per non- recipe run
        helper.runAfterDelay(runs * 2 + 1, () -> {
            ItemStack results = itemIn.getStackInSlot(1);

            helper.assertTrue(itemOut.isEmpty(),
                    "Multiblock LCR (OR) should not have run, ran [" +
                            itemOut.getStackInSlot(0).getCount() + "] times");
            helper.assertTrue(TestUtils.isItemStackEqual(results, IN_OR_2.copyWithCount(2)),
                    "Multiblock LCR (OR) should not have consumed items, consumed [" +
                            (2 - results.getCount()) + "] + [ " + (63 - itemIn.getStackInSlot(0).getCount()) + "]");

            helper.succeed();
        });
    }

    // Test for multiblock machine with two OR chanced item inputs
    @GameTest(template = "lcr_ranged_ingredients", batch = "ChancedIngredientsOR")
    public static void multiblockLCRDoubleORChancedItemInput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 16;
        itemIn.setStackInSlot(0, IN_OR_1.copyWithCount(runs * 3));
        itemIn.setStackInSlot(1, IN_OR_2.copyWithCount(runs * 3));
        itemIn.setStackInSlot(2, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            int upperLimit = 48 - (runs * 0);
            int lowerLimit = 48 - (runs * 3);
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Multiblock LCR (OR) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemIn.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Multiblock LCR (OR) didn't consume correct number of item " + i + ", consumed [" +
                                (48 - result.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Multiblock LCR (OR) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Multiblock LCR (OR) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // test for multiblock machine with two OR chanced item outputs
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "ChancedIngredientsOR",
              timeoutTicks = 500)
    public static void multiblockLCentDoubleORChancedItemOutput(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1;
        int parallels = 1;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 64;
        itemIn.setStackInSlot(0, COBBLE.copyWithCount(runs));

        helper.runAfterDelay(runs + 1, () -> {
            int lowerLimit = 0;
            int upperLimit = runs;

            helper.assertTrue(itemIn.isEmpty(),
                    "LCent (double OR) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "LCent (double OR) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "LCent (double OR) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "LCent (double OR) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // test for multiblock machine with four OR chanced item outputs
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "ChancedIngredientsOR",
              timeoutTicks = 500)
    public static void multiblockLCentQuadORChancedItemOutput(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1;
        int parallels = 1;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 64;
        itemIn.setStackInSlot(0, STONE.copyWithCount(runs));

        // 1t per recipe but recipe subtick overclocks once
        helper.runAfterDelay(runs / 2 + 1, () -> {
            int lowerLimit = 0;
            int upperLimit = runs;

            helper.assertTrue(itemIn.isEmpty(),
                    "LCent (quad OR) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            for (int i = 0; i < 4; i++) {
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "LCent (quad OR) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "LCent (quad OR) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "LCent (quad OR) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // test for multiblock machine with two OR chanced item outputs and batched/parallel recipes
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "ChancedIngredientsOR",
              timeoutTicks = 500)
    public static void multiblockLCentDoubleORChancedItemOutputBatchParallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 10;
        int parallels = 10;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 100;
        for (int j = 0; j < batches; j++) {
            itemIn.setStackInSlot(j, COBBLE.copyWithCount(parallels));
        }

        // 16t -> 1t OC
        // 1t -> 4t parallel
        // 4t -> 40t batch
        helper.runAfterDelay(41, () -> {
            helper.assertTrue(itemIn.isEmpty(),
                    "Batched LCent (double OR) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            // result counts are guaranteed for batch/parallel runs
            for (int i = 0; i < 2; i++) {
                int base = 40 + (20 * i);
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(result.getCount() == base,
                        "Batched LCent (double OR) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + base + "]");
            }
            helper.succeed();
        });
    }

    // test for multiblock machine with four OR chanced item outputs and batched/parallel recipes
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "ChancedIngredientsOR",
              timeoutTicks = 500)
    public static void multiblockLCentQuadORChancedItemOutputBatchParallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 10;
        int parallels = 10;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 100;
        for (int j = 0; j < batches; j++) {
            itemIn.setStackInSlot(j, STONE.copyWithCount(parallels));
        }

        // 8t -> 1t OC + 1 subtick OC
        // 1t -> 4t parallel
        // 4t -> 40t batch
        helper.runAfterDelay(21, () -> {
            helper.assertTrue(itemIn.isEmpty(),
                    "Batched LCent (quad OR) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            // result counts are guaranteed for batch/parallel runs
            for (int i = 0; i < 4; i++) {
                int base = 30 + (10 * i);
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(result.getCount() == base,
                        "Batched LCent (quad OR) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + base + "]");
            }
            helper.succeed();
        });
    }

    // Failure Test for singleblock machine with chanced per-tick item input
    // Provides too few input items, should not run recipes.
    @GameTest(template = "singleblock_charged_cr", batch = "ChancedIngredientsOR")
    public static void singleblockSingleChancedTickItemInputFailure(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, IN_TICK_1.copyWithCount(2));
        itemIn.setStackInSlot(1, STONE.copyWithCount(2));
        // 1t to turn on, 2t per non- recipe run
        helper.runAfterDelay(65, () -> {
            SimpleTieredMachine m = machine;
            helper.assertTrue(machine.recipeLogic.getStatus() == RecipeLogic.Status.WAITING,
                    "Singleblock CR (per-tick) should be WAITING but is not!");
            helper.assertTrue(itemOut.isEmpty(),
                    "Singleblock CR (per-tick) should not have finished recipe!");
            helper.assertFalse(TestUtils.isItemStackEqual(itemIn.getStackInSlot(1), COBBLE.copyWithCount(2)),
                    "Singleblock CR (per-tick) should have consumed per-tick inputs but did not!");

            helper.succeed();
        });
    }

    // Test for singleblock machine with single per-tick chanced item input
    @GameTest(template = "singleblock_charged_cr", batch = "ChancedIngredientsOR")
    public static void singleblockSingleChancedTickItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);


        itemIn.setStackInSlot(0, IN_TICK_1.copyWithCount(1));
        itemIn.setStackInSlot(1, STONE.copyWithCount(64));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(65, () -> {
            ItemStack results = itemIn.getStackInSlot(1);
            int upperLimit = 64;
            int lowerLimit = 0;
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(1)),
                    "Singleblock CR (per-tick) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + 1 + "]");
            helper.assertTrue(TestUtils.isItemWithinRange(results, lowerLimit, upperLimit),
                    "Singleblock CR (per-tick) didn't consume correct number of items, consumed [" +
                            (64 - results.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getCount() == lowerLimit),
                    "Singleblock CR (per-tick) succeeded every chance roll!");
            helper.assertFalse((results.getCount() == upperLimit),
                    "Singleblock CR (per-tick) failed every chance roll!");

            helper.succeed();
        });
    }

    // Failure Test for singleblock machine with two per-tick OR chanced item inputs
    // Provides too few input items, should not run recipes.
    @GameTest(template = "singleblock_charged_assembler", batch = "ChancedIngredientsOR")
    public static void singleblockDoubleORTickChancedItemInputFailure(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(ASSM_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 3;
        itemIn.setStackInSlot(0, IN_TICK_3.copyWithCount(64));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(2));
        itemIn.setStackInSlot(2, IN_TICK_4.copyWithCount(runs));
        // 1t to turn on, 16t per non- recipe run
        helper.runAfterDelay(runs * 16 + 1, () -> {
            helper.assertTrue(machine.recipeLogic.getStatus() == RecipeLogic.Status.WAITING,
                    "Singleblock Assembler (OR) (per-tick) should be WAITING but is not!");
            helper.assertTrue(itemOut.isEmpty(),
                    "Singleblock Assembler (OR) (per-tick) should not have finished recipe!");
            helper.assertFalse(TestUtils.isItemStackEqual(itemIn.getStackInSlot(1), COBBLE.copyWithCount(2)),
                    "Singleblock Assembler (OR) (per-tick) should have consumed per-tick inputs but did not!");

            helper.succeed();
        });
    }

    // Test for singleblock machine with two per-tick OR chanced item inputs
    @GameTest(template = "singleblock_charged_assembler", batch = "ChancedIngredientsOR")
    public static void singleblockDoubleORTickChancedItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(ASSM_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 4;
        itemIn.setStackInSlot(0, IN_TICK_3.copyWithCount(runs * 16));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs * 16));
        itemIn.setStackInSlot(2, IN_TICK_4.copyWithCount(runs));
        // 1t to turn on, 16t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 16 + 1, () -> {
            int upperLimit = 64;
            int lowerLimit = 0;
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Singleblock Assembler (OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemIn.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Singleblock Assembler (OR) (per-tick) didn't consume correct number of item " + i + ", consumed [" +
                                (48 - result.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Singleblock Assembler (OR) (per-tick) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Singleblock Assembler (OR) (per-tick) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // Test for singleblock machine with four per-tick OR chanced item inputs
    @GameTest(template = "singleblock_charged_assembler", batch = "ChancedIngredientsOR")
    public static void singleblockQuadORTickChancedItemInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(ASSM_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        int runs = 4;
        itemIn.setStackInSlot(0, STONE.copyWithCount(runs * 16));
        itemIn.setStackInSlot(1, IN_OR_2.copyWithCount(runs * 16));
        itemIn.setStackInSlot(2, IN_TICK_3.copyWithCount(runs * 16));
        itemIn.setStackInSlot(3, IN_TICK_4.copyWithCount(runs * 16));
        itemIn.setStackInSlot(4, IN_OR_3.copyWithCount(runs));

        // 1t to turn on, 16t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 16 + 1, () -> {
            int upperLimit = 64;
            int lowerLimit = 0;
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Singleblock Assembler (OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");

            for (int i = 0; i < 4; i++) {
                ItemStack result = itemIn.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Singleblock Assembler (OR) (per-tick) didn't consume correct number of item " + i + ", consumed [" +
                                (48 - result.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Singleblock Assembler (OR) (per-tick) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Singleblock Assembler (OR) (per-tick) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // Failure Test for multiblock machine with two per-tick OR chanced item inputs
    // Provides too few input items, should not run recipes.
    @GameTest(template = "lcr_ranged_ingredients", batch = "ChancedIngredientsOR")
    public static void multiblockLCRDoubleORChancedTickItemInputFailure(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);


        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 5;
        itemIn.setStackInSlot(0, STONE.copyWithCount(63));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(2));
        itemIn.setStackInSlot(2, IN_TICK_2.copyWithCount(runs));
        // 1t to turn on, 2t per non- recipe run
        helper.runAfterDelay(runs * 16 + 1, () -> {
            helper.assertTrue(busHolder.controller.recipeLogic.getStatus() == RecipeLogic.Status.WAITING,
                    "Multiblock LCR (OR) (per-tick) should be WAITING but is not!");
            helper.assertTrue(itemOut.isEmpty(),
                    "Multiblock LCR (OR) (per-tick) should not have finished recipe!");
            helper.assertFalse(TestUtils.isItemStackEqual(itemIn.getStackInSlot(1), COBBLE.copyWithCount(2)),
                    "Multiblock LCR (OR) (per-tick) should have consumed per-tick inputs but did not!");

            helper.succeed();
        });
    }

    // Test for multiblock machine with two per-tick OR chanced item inputs
    @GameTest(template = "lcr_ranged_ingredients", batch = "ChancedIngredientsOR", timeoutTicks = 300)
    public static void multiblockLCRDoubleORChancedTickItemInput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int runs = 4;
        itemIn.setStackInSlot(0, STONE.copyWithCount(runs * 16));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs * 16));
        itemIn.setStackInSlot(2, IN_TICK_2.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // check the results of all rolls together
        helper.runAfterDelay(runs * 16 + 1, () -> {
            int upperLimit = 64;
            int lowerLimit = 0;
            helper.assertTrue(TestUtils.isItemStackEqual(itemOut.getStackInSlot(0), STONE.copyWithCount(runs)),
                    "Multiblock LCR (OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemIn.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "Multiblock LCR (OR) (per-tick) didn't consume correct number of item " + i + ", consumed [" +
                                (64 - result.getCount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "Multiblock LCR (OR) (per-tick) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "Multiblock LCR (OR) (per-tick) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // test for multiblock machine with two OR chanced item outputs
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
            batch = "ChancedIngredientsOR",
            timeoutTicks = 500)
    public static void multiblockLCentDoubleORChancedTickItemOutput(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1;
        int parallels = 1;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 4;
        itemIn.setStackInSlot(0, IN_TICK_1.copyWithCount(runs));

        helper.runAfterDelay(runs * 25 + 1, () -> {
            int lowerLimit = 0;
            int upperLimit = runs * 16;

            helper.assertTrue(itemIn.isEmpty(),
                    "LCent (double OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            for (int i = 0; i < 2; i++) {
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "LCent (double OR) (per-tick) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "LCent (double OR) (per-tick) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "LCent (double OR) (per-tick) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }

    // test for multiblock machine with four OR chanced item outputs
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
            batch = "ChancedIngredientsOR")
    public static void multiblockLCentQuadORChancedTickItemOutput(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 1;
        int parallels = 1;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 32;
        itemIn.setStackInSlot(0, IN_TICK_2.copyWithCount(runs));

        // 1t per recipe but recipe subtick overclocks once
        helper.runAfterDelay(runs / 2 + 1, () -> {
            int lowerLimit = 0;
            int upperLimit = runs*2;

            helper.assertTrue(itemIn.isEmpty(),
                    "LCent (quad OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            for (int i = 0; i < 4; i++) {
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(TestUtils.isItemWithinRange(result, lowerLimit, upperLimit),
                        "LCent (quad OR) (per-tick) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + lowerLimit + "-" + upperLimit + "]");
                helper.assertFalse((result.getCount() == lowerLimit),
                        "LCent (quad OR) (per-tick) item " + i + " failed every chance roll!");
                helper.assertFalse((result.getCount() == upperLimit),
                        "LCent (quad OR) (per-tick) item " + i + " succeeded every chance roll!");
            }

            helper.succeed();
        });
    }
/*
    // test for multiblock machine with two OR chanced item outputs and batched/parallel recipes
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
            batch = "ChancedIngredientsOR",
            timeoutTicks = 150)
    public static void multiblockLCentDoubleORChancedTickItemOutputBatchParallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 2;
        int parallels = 4;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 8;
        for (int j = 0; j < batches; j++) {
            itemIn.setStackInSlot(j, IN_TICK_2.copyWithCount(parallels));
        }

        // 400t -> 25t OC
        // 25t -> 50t parallel
        // 50t -> 100t batch
        helper.runAfterDelay(101, () -> {
            helper.assertTrue(itemIn.isEmpty(),
                    "Batched LCent (double OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            // result counts are guaranteed for batch/parallel runs
            for (int i = 0; i < 2; i++) {
                int base = 40 + (20 * i);
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(result.getCount() == base,
                        "Batched LCent (double OR) (per-tick) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + base + "]");
            }
            helper.succeed();
        });
    }

    // test for multiblock machine with four OR chanced item outputs and batched/parallel recipes
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
            batch = "ChancedIngredientsOR",
    timeoutTicks = 150)
    public static void multiblockLCentQuadORChancedTickItemOutputBatchParallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler itemOut = busHolder.outputBus1.getInventory();

        int batches = 2;
        int parallels = 4;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int runs = 8;
        for (int j = 0; j < batches; j++) {
            itemIn.setStackInSlot(j, IN_TICK_2.copyWithCount(parallels));
        }

        // 400t -> 25t OC
        // 25t -> 50t parallel
        // 50t -> 100t batch
        helper.runAfterDelay(101, () -> {
            helper.assertTrue(itemIn.isEmpty(),
                    "Batched LCent (quad OR) (per-tick) didn't complete correct number of recipes, completed [" +
                            (runs - itemIn.getTotalContentAmount()) + "] not [" + runs + "]");

            // result counts are guaranteed for batch/parallel runs
            for (int i = 0; i < 4; i++) {
                int base = 30 + (10 * i);
                ItemStack result = itemOut.getStackInSlot(i);
                helper.assertTrue(result.getCount() == base,
                        "Batched LCent (quad OR) (per-tick) didn't produce correct number of item " + i + ", produced [" +
                                result.getCount() + "] not [" + base + "]");
            }
            helper.succeed();
        });
    }
*/
}
