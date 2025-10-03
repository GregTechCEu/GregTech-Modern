package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
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
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import lombok.Getter;

/**
 * Test cases:
 * Do many passes of most tests as a safeguard against bad rolls
 * Same output more than once
 * Out of bounds
 * Output a multiple of batchparallels
 * Rolls of 0
 * Forced rolls of 0 breaking recipes
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class IntProviderFluidIngredientTest {

    private static GTRecipeType CR_RECIPE_TYPE;
    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CENTRIFUGE_RECIPE_TYPE;

    // fluids used in recipes. Up top here for quick replacements.
    private static final FluidStack CR_IN = GTMaterials.Hydrogen.getFluid(1);
    private static final FluidStack CR_OUT = GTMaterials.Iron.getFluid(1);
    private static final FluidStack LCR_IN = GTMaterials.Oxygen.getFluid(1);
    private static final FluidStack LCR_OUT = GTMaterials.Copper.getFluid(1);
    private static final FluidStack LCENT_IN = GTMaterials.Nitrogen.getFluid(1);
    private static final FluidStack LCENT_OUT = GTMaterials.Gold.getFluid(1);
    private static final FluidStack RUBBER = GTMaterials.Rubber.getFluid(1);
    private static final FluidStack REDSTONE = GTMaterials.Redstone.getFluid(1);
    private static final ItemStack COBBLE = new ItemStack(Items.COBBLESTONE);

    /**
     * How many times to repeat the Batch and Parallel random roll tests to avoid false positives
     * Currently set to 7, with singleblock recipes processing up to 9 fluids, allowing for stacks of up to 63 fluids.
     */
    @Getter
    private static final int REPLICAS = 7;

    @BeforeBatch(batch = "RangedFluidIngredients")
    public static void prepare(ServerLevel level) {
        CR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_fluid_ingredient_cr_tests", GTRecipeTypes.CHEMICAL_RECIPES);
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_fluid_ingredient_lcr_tests",
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES);
        CENTRIFUGE_RECIPE_TYPE = TestUtils.createRecipeType("ranged_fluid_ingredient_centrifuge_tests",
                GTRecipeTypes.CENTRIFUGE_RECIPES);

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_fluid_cr"))
                .inputFluidsRanged(CR_IN, UniformInt.of(0, 9))
                .inputItems(COBBLE)
                .outputFluids(REDSTONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_fluid_cr"))
                .inputFluids(CR_OUT)
                .outputFluidsRanged(REDSTONE, UniformInt.of(0, 9))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_fluid_lcr"))
                .inputFluidsRanged(LCR_IN, UniformInt.of(0, 9))
                .inputFluids(RUBBER)
                .outputFluids(REDSTONE)
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_fluid_lcr"))
                .inputFluids(LCR_OUT)
                .outputFluidsRanged(REDSTONE, UniformInt.of(0, 9))
                .EUt(GTValues.V[GTValues.HV])
                .duration(2)
                .buildRawRecipe());

        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_input_fluid_cent"))
                .inputFluidsRanged(LCENT_IN, UniformInt.of(0, 40))
                .inputItems(COBBLE)
                .outputFluids(REDSTONE)
                .EUt(GTValues.V[GTValues.IV])
                .duration(4)
                .buildRawRecipe());

        CENTRIFUGE_RECIPE_TYPE.getLookup().addRecipe(CENTRIFUGE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_ranged_output_fluid_cent"))
                .inputFluids(LCENT_OUT)
                .outputFluidsRanged(REDSTONE, UniformInt.of(0, 40))
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

    // test for IntProviderFluidIngredient.test()
    @GameTest(template = "empty", batch = "RangedFluidIngredients")
    public static void rangedFluidIngredientTestEqualTest(GameTestHelper helper) {
        var ingredient = IntProviderFluidIngredient.of(GTMaterials.Water.getFluid(1), 1, 5);
        helper.assertTrue(ingredient.test(GTMaterials.Water.getFluid(3)),
                "IntProviderFluidIngredient.test doesn't match when it should have");
        // This should work since test only tries the fluid type.
        helper.assertTrue(ingredient.test(GTMaterials.Water.getFluid(64)),
                "IntProviderFluidIngredient.test doesn't match when it should have with value outside bounds");
        helper.assertFalse(ingredient.test(GTMaterials.Lava.getFluid(3)),
                "IntProviderFluidIngredient.test shouldn't match with different fluids");
        helper.succeed();
    }

    // test for IntProviderFluidIngredient.getStacks()
    @GameTest(template = "empty", batch = "RangedFluidIngredients")
    public static void rangedFluidIngredientGetStacksTest(GameTestHelper helper) {
        var ingredient = IntProviderFluidIngredient.of(GTMaterials.Water.getFluid(1), 1, 500000);
        var stacks = ingredient.getStacks();
        helper.assertTrue(stacks.length == 1,
                "IntProviderFluidIngredient should only return 1 fluid when made with 1 fluid");
        helper.assertTrue(stacks[0].isFluidEqual(GTMaterials.Water.getFluid(1)),
                "IntProviderFluidIngredient should have fluid equal to what it was made with");
        helper.assertTrue(stacks[0].isFluidStackIdentical(ingredient.getStacks()[0]),
                "IntProviderFluidIngredient.getStacks shouldn't change between getStacks calls");
        ingredient.reroll();
        helper.assertFalse(stacks[0].isFluidStackIdentical(ingredient.getStacks()[0]),
                "IntProviderFluidIngredient.getStacks should have changed after rerolling");
        helper.succeed();
    }

    // test for IntProviderFluidIngredient.toJson()
    @GameTest(template = "empty", batch = "RangedFluidIngredients")
    public static void rangedIngredientJsonTest(GameTestHelper helper) {
        var ingredient = IntProviderFluidIngredient.of(GTMaterials.Water.getFluid(1), 1, 500000);

        // serialize/deserialize before rolling count
        var jsonPreRoll = ingredient.toJson();
        var ingredientDeserializedPreRoll = IntProviderFluidIngredient.fromJson(jsonPreRoll);

        var stacks = ingredient.getStacks();
        var stacksDeserializedPreRoll = ingredientDeserializedPreRoll.getStacks();

        // serialize/deserialize after rolling count
        var jsonPostRoll = ingredient.toJson();
        var ingredientDeserializedPostRoll = IntProviderFluidIngredient.fromJson(jsonPostRoll);
        var stacksDeserializedPostRoll = ingredientDeserializedPostRoll.getStacks();

        helper.assertTrue(
                stacks.length == stacksDeserializedPreRoll.length && stacks.length == stacksDeserializedPostRoll.length,
                "IntProviderFluidIngredient should only return 1 fluid when made with 1 fluid, even after serializing");
        helper.assertTrue(stacksDeserializedPreRoll[0].isFluidEqual(GTMaterials.Water.getFluid(1)),
                "IntProviderFluidIngredient should have fluid equal to what it was made with after serializing");
        helper.assertTrue(stacksDeserializedPostRoll[0].isFluidEqual(GTMaterials.Water.getFluid(1)),
                "IntProviderFluidIngredient should have fluid equal to what it was made with after serializing");
        helper.assertFalse(TestUtils.areFluidStacksEqual(stacksDeserializedPreRoll, ingredient.getStacks()),
                "IntProviderFluidIngredient.getStacks should be different if it wasn't rolled before serializing");
        helper.assertTrue(TestUtils.areFluidStacksEqual(stacksDeserializedPostRoll, ingredient.getStacks()),
                "IntProviderFluidIngredient.getStacks shouldn't change between getStacks calls if it was rolled before serializing");
        helper.succeed();
    }

    // Test for singleblock machine with ranged fluid input.
    // Forcibly sabotages the first recipe run, setting its output amount to 0 to ensure that doesn't break the recipe.
    // This is specifically a test for #3593 / #3594
    @GameTest(template = "singleblock_charged_cr", batch = "RangedFluidIngredients")
    public static void singleblockRangedFluidOutputSabotaged(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableFluidTank fluidIn = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).get(0);
        NotifiableFluidTank fluidOut = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP).get(0);

        int runs = 7;
        fluidIn.setFluidInTank(0, new FluidStack(CR_OUT, runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];

        helper.runAfterDelay(4, () -> {
            if (machine.getRecipeLogic().getLastRecipe().getOutputContents(FluidRecipeCapability.CAP).get(0)
                    .getContent() instanceof IntProviderFluidIngredient ingredient) {
                ingredient.setSampledCount(0);

                if (ingredient.getSampledCount() != 0) {
                    helper.fail("Singleblock Ranged Fluid Output sabotage failed! " +
                            "Output count not was altered!");
                }
            } else {
                helper.fail("Singleblock Ranged Fluid Output sabotage failed! " +
                        "Recipe logic did not contain a Ranged Output!");
            }
        });
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 3, () -> {
                addedRolls[finalI] = (int) fluidOut.getTotalContentAmount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            FluidStack results = fluidOut.getFluidInTank(0);
            helper.assertTrue(TestUtils.isFluidWithinRange(results, runs, runs * 9),
                    "Sabotaged Singleblock CR didn't produce correct number of fluids, produced [" +
                            results.getAmount() + "] not [" + runs + "-" + (runs * 9) + "]");
            helper.assertFalse((results.getAmount() == runs * 9),
                    "Sabotaged Singleblock CR rolled max value on every roll (how??)");
            helper.assertFalse((results.getAmount() == runs * 0),
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

    // Failure Test for singleblock machine with ranged fluid input
    // Provides too little input fluid, should not run recipes.
    @GameTest(template = "singleblock_charged_cr", batch = "RangedFluidIngredients")
    public static void singleblockRangedFluidInputFailure(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableFluidTank fluidIn = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).get(0);
        NotifiableFluidTank fluidOut = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP).get(0);

        int runs = 10;
        fluidIn.setFluidInTank(0, new FluidStack(CR_IN, 8));
        itemIn.setStackInSlot(1, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per non-recipe run
        helper.runAfterDelay(runs * 2 + 1, () -> {
            FluidStack results = fluidIn.getFluidInTank(0);

            helper.assertTrue(fluidOut.isEmpty(),
                    "Singleblock CR should not have run, ran [" +
                            fluidOut.getFluidInTank(0).getAmount() + "] times");
            helper.assertTrue(TestUtils.isFluidStackEqual(results, new FluidStack(CR_IN, 8)),
                    "Singleblock CR should not have consumed items, consumed [" +
                            (8 - results.getAmount()) + "]");

            helper.succeed();
        });
    }

    // Test for singleblock machine with ranged fluid input
    @GameTest(template = "singleblock_charged_cr", batch = "RangedFluidIngredients")
    public static void singleblockRangedFluidInput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableFluidTank fluidIn = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).get(0);
        NotifiableFluidTank fluidOut = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP).get(0);

        int runs = 7;
        fluidIn.setFluidInTank(0, new FluidStack(CR_IN, 64));
        itemIn.setStackInSlot(0, COBBLE.copyWithCount(runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 1, () -> {
                addedRolls[finalI] = fluidIn.getFluidInTank(0).getAmount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 2, () -> {
            FluidStack results = fluidIn.getFluidInTank(0);
            int upperLimit = 64 - (runs * 0);
            int lowerLimit = 64 - (runs * 9);
            helper.assertTrue(TestUtils.isFluidStackEqual(fluidOut.getFluidInTank(0), new FluidStack(REDSTONE, runs)),
                    "Singleblock CR didn't complete correct number of recipes, completed [" +
                            fluidOut.getFluidInTank(0).getAmount() + "] not [" + runs + "]");
            helper.assertTrue(TestUtils.isFluidWithinRange(results, lowerLimit, upperLimit),
                    "Singleblock CR didn't consume correct number of fluids, consumed [" +
                            (64 - results.getAmount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getAmount() == lowerLimit),
                    "Singleblock CR rolled max value on every roll");
            helper.assertFalse((results.getAmount() == upperLimit),
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

    // Test for singleblock machine with ranged fluid input
    @GameTest(template = "singleblock_charged_cr", batch = "RangedFluidIngredients")
    public static void singleblockRangedFluidOutput(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableFluidTank fluidIn = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).get(0);
        NotifiableFluidTank fluidOut = (NotifiableFluidTank) machine
                .getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP).get(0);

        int runs = 7;
        fluidIn.setFluidInTank(0, new FluidStack(CR_OUT, runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 3, () -> {
                addedRolls[finalI] = fluidOut.getFluidInTank(0).getAmount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            helper.assertTrue(fluidIn.getFluidInTank(0).isEmpty(),
                    "Singleblock CR didn't complete correct number of recipes, completed [" +
                            fluidIn.getFluidInTank(0).getAmount() + "] not [" + runs + "]");
            FluidStack results = fluidOut.getFluidInTank(0);
            helper.assertTrue(TestUtils.isFluidWithinRange(results, runs, runs * 9),
                    "Singleblock CR didn't produce correct number of fluids, produced [" +
                            results.getAmount() + "] not [" + runs + "-" + (runs * 9) + "]");
            helper.assertFalse((results.getAmount() == runs * 9),
                    "Singleblock CR rolled max value on every roll");
            helper.assertFalse((results.getAmount() == runs * 0),
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

    // test for multiblock machine with ranged fluid input
    @GameTest(template = "lcr_ranged_ingredients",
              batch = "RangedFluidIngredients")
    public static void multiblockLCRRangedFluidInput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int runs = 7;
        fluidIn.setFluidInTank(0, new FluidStack(LCR_IN, 64));
        fluidIn.setFluidInTank(1, new FluidStack(RUBBER, runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 1, () -> {
                addedRolls[finalI] = fluidIn.getFluidInTank(0).getAmount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 2, () -> {
            FluidStack results = fluidIn.getFluidInTank(0);
            int upperLimit = 64 - (runs * 0);
            int lowerLimit = 64 - (runs * 9);
            helper.assertTrue(TestUtils.isFluidStackEqual(fluidOut.getFluidInTank(0), new FluidStack(REDSTONE, runs)),
                    "LCR didn't complete correct number of recipes, completed [" +
                            fluidOut.getFluidInTank(0).getAmount() + "] not [" + runs + "]");
            helper.assertTrue(TestUtils.isFluidWithinRange(results, lowerLimit, upperLimit),
                    "LCR didn't consume correct number of fluids, consumed [" +
                            (64 - results.getAmount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");
            helper.assertFalse((results.getAmount() == lowerLimit),
                    "LCR rolled max value on every roll");
            helper.assertFalse((results.getAmount() == upperLimit),
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

    // test for multiblock machine with ranged fluid input
    @GameTest(template = "lcr_ranged_ingredients",
              batch = "RangedFluidIngredients")
    public static void multiblockLCRRangedFluidOutput(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndFormLCR(helper);

        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int runs = 7;
        fluidIn.setFluidInTank(0, new FluidStack(LCR_OUT, runs));
        // 1t to turn on, 2t per recipe run
        // get the result of each roll independently
        int[] addedRolls = new int[runs];
        for (int i = 0; i < runs; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(2 * i + 3, () -> {
                addedRolls[finalI] = fluidOut.getFluidInTank(0).getAmount();
            });
        }
        // check the results of all rolls together
        helper.runAfterDelay(runs * 2 + 1, () -> {
            helper.assertTrue(fluidIn.getFluidInTank(0).isEmpty(),
                    "LCR didn't complete correct number of recipes, completed [" +
                            fluidIn.getFluidInTank(0).getAmount() + "] not [" + runs + "]");
            FluidStack results = fluidOut.getFluidInTank(0);
            helper.assertTrue(TestUtils.isFluidWithinRange(results, runs, runs * 9),
                    "LCR didn't produce correct number of fluids, produced [" +
                            results.getAmount() + "] not [" + runs + "-" + (runs * 9) + "]");
            helper.assertFalse((results.getAmount() == runs * 9),
                    "LCR rolled max value on every roll");
            helper.assertFalse((results.getAmount() == runs * 0),
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

    // test for multiblock machine with 16x Parallels with ranged fluid input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedFluidIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedFluidInput16Parallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int batches = 1; // unused on this test
        int parallels = 16;
        final int amount = 40 * batches * parallels;
        busHolder.controller.setBatchEnabled(false);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        itemIn.setStackInSlot(0, COBBLE.copyWithCount(batches * parallels));
        fluidIn.setFluidInTank(0, new FluidStack(LCENT_IN, amount));

        // 1t to turn on, 4t per recipe run
        // 16 parallels
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(17 * finalI, () -> {
                FluidStack results = fluidIn.getFluidInTank(0);
                int upperLimit = amount - (batches * parallels * 0);
                int lowerLimit = amount - (batches * parallels * 40);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isFluidStackEqual(new FluidStack(fluidOut.getFluidInTank(0),
                                ((int) Math.round(fluidOut.getTotalContentAmount()))),
                                new FluidStack(REDSTONE, completed)),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(fluidOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isFluidWithinRange(results, lowerLimit, upperLimit),
                        "Parallel LCent didn't consume correct number of fluids, consumed " +
                                (amount - results.getAmount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = amount - results.getAmount();

                // reset for a rerun
                itemIn.setStackInSlot(0, COBBLE.copyWithCount(batches * parallels));
                fluidIn.setFluidInTank(0, new FluidStack(LCENT_IN, 40 * batches * parallels));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            for (int i = 0; i < REPLICAS; i++) {
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Parallel LCent ranged fluid input test iteration " + i + " consumed [" +
                            rolls[i] + "] fluids, a multiple of its Batch * Parallel count (" + (parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Parallel LCent ranged fluid input test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged fluid output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedFluidIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedFluidOutput16Parallel(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int batches = 1; // unused on this test
        int parallels = 16;
        fluidIn.setFluidInTank(0, new FluidStack(LCENT_OUT, 16));

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
                helper.assertTrue(fluidIn.getFluidInTank(0).isEmpty(),
                        "Parallel LCent didn't complete correct number of recipes, completed [" +
                                fluidIn.getFluidInTank(0).getAmount() + "] not [" + runs + "]");
                int resultCount = (int) Math.round(fluidOut.getTotalContentAmount());
                int lowerLimit = runs * 0;
                int upperLimit = runs * 40;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Parallel LCent didn't produce correct number of fluids, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                fluidIn.setFluidInTank(0, new FluidStack(LCENT_OUT, 16));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            int[] rolls = new int[REPLICAS];

            rolls[0] = addedRolls[0];
            if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[0], batches, parallels, 1)) {
                sus = true;
                GTCEu.LOGGER.warn("Parallel LCent ranged fluid output test iteration " + 1 + " produced [" +
                        rolls[0] + "] fluids, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                        "). If this message only appears once, this is likely a false positive.");
            }
            for (int i = 1; i < REPLICAS; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Parallel LCent ranged fluid output test iteration " + (i + 1) + " produced [" +
                            rolls[i] + "] fluids, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Parallel LCent ranged fluid output test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged fluid input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedFluidIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedFluidInputBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int batches = 16;
        int parallels = 1;
        final int amount = 40 * batches * parallels;
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        itemIn.setStackInSlot(0, COBBLE.copyWithCount(batches * parallels));
        fluidIn.setFluidInTank(0, new FluidStack(LCENT_IN, amount));

        // 1t to turn on, 1t per recipe run
        // 16 batches
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(17 * finalI, () -> {
                FluidStack results = fluidIn.getFluidInTank(0);
                int upperLimit = amount - (batches * parallels * 0);
                int lowerLimit = amount - (batches * parallels * 40);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isFluidStackEqual(new FluidStack(fluidOut.getFluidInTank(0),
                                ((int) Math.round(fluidOut.getTotalContentAmount()))),
                                new FluidStack(REDSTONE, completed)),
                        "Batched LCent didn't complete correct number of recipes, completed [" +
                                ((int) Math.round(fluidOut.getTotalContentAmount())) + "] not [" +
                                completed + "]");
                helper.assertTrue(TestUtils.isFluidWithinRange(results, lowerLimit, upperLimit),
                        "Batched LCent didn't consume correct number of fluids, consumed " +
                                (amount - results.getAmount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = amount - results.getAmount();

                // reset for a rerun
                itemIn.setStackInSlot(0, COBBLE.copyWithCount(batches * parallels));
                fluidIn.setFluidInTank(0, new FluidStack(LCENT_IN, amount));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            for (int i = 0; i < REPLICAS; i++) {
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched LCent ranged fluid input test iteration " + i + " consumed [" +
                            rolls[i] + "] fluids, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched LCent ranged fluid input test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged fluid output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedFluidIngredients",
              timeoutTicks = 200)
    public static void multiblockLCentRangedFluidOutputBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int batches = 16;
        int parallels = 1; // unused on this test
        fluidIn.setFluidInTank(0, new FluidStack(LCENT_OUT, 16));

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
                helper.assertTrue(fluidIn.getFluidInTank(0).isEmpty(),
                        "Batched LCent didn't complete correct number of recipes, completed [" +
                                fluidIn.getFluidInTank(0).getAmount() + "] not [" + runs + "]");
                int resultCount = (int) Math.round(fluidOut.getTotalContentAmount());
                int lowerLimit = runs * 0;
                int upperLimit = runs * 40;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Batched LCent didn't produce correct number of fluids, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                fluidIn.setFluidInTank(0, new FluidStack(LCENT_OUT, 16));
            });
        }

        helper.runAfterDelay(1 + 17 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            int[] rolls = new int[REPLICAS];

            rolls[0] = addedRolls[0];
            if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[0], batches, parallels, 1)) {
                sus = true;
                GTCEu.LOGGER.warn("Batched LCent ranged fluid output test iteration " + 1 + " produced [" +
                        rolls[0] + "] fluids, a multiple of its Batch * Parallel count (" + batches +
                        "). If this message only appears once, this is likely a false positive.");
            }
            for (int i = 1; i < REPLICAS; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched LCent ranged fluid output test iteration " + (i + 1) + " produced [" +
                            rolls[i] + "] fluids, a multiple of its Batch * Parallel count (" + batches +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched LCent ranged fluid output test rolled exactly even to " +
                    "Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged fluid input
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedFluidIngredients",
              timeoutTicks = 500)
    public static void multiblockLCentRangedFluidInput16ParallelBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        NotifiableItemStackHandler itemIn = busHolder.inputBus1.getInventory();
        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int batches = 16;
        int parallels = 16;
        final int amount = batches * parallels * 40;
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        int stacks = batches * parallels / 64;

        for (int j = 0; j < stacks; j++) {
            itemIn.setStackInSlot(j, COBBLE.copyWithCount((batches * parallels / stacks)));
        }
        fluidIn.setFluidInTank(0, new FluidStack(LCENT_IN, amount));

        // 1t to turn on, 64t per recipe run
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] rolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(65 * finalI, () -> {
                FluidStack results = fluidIn.getFluidInTank(0);
                int completed = batches * parallels * finalI;
                helper.assertTrue(
                        TestUtils.isFluidStackEqual(
                                new FluidStack(fluidOut.getFluidInTank(0), fluidOut.getFluidInTank(0).getAmount()),
                                new FluidStack(REDSTONE, completed)),
                        "Batched Parallel LCent didn't complete correct number of recipes, completed [" +
                                (fluidOut.getFluidInTank(0).getAmount()) + "] not [" + completed + "]");
                int upperLimit = amount - (batches * parallels * 0);
                int lowerLimit = amount - (batches * parallels * 40);
                helper.assertTrue(TestUtils.isFluidWithinRange(results, lowerLimit, upperLimit),
                        "Batched Parallel LCent didn't consume correct number of fluids, consumed " +
                                (amount - results.getAmount()) + "] not [" + lowerLimit + "-" + upperLimit + "]");

                rolls[finalI - 1] = 64 - results.getAmount();

                // reset for a rerun
                for (int l = 0; l < stacks; l++) {
                    itemIn.setStackInSlot(l,
                            COBBLE.copyWithCount((batches * parallels / stacks)));
                }
                fluidIn.setFluidInTank(0, new FluidStack(LCENT_IN, 40 * batches * parallels));
            });
        }

        helper.runAfterDelay(1 + 65 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            for (int i = 0; i < REPLICAS; i++) {
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched Parallel LCent ranged fluid input test iteration " + i + " consumed [" +
                            rolls[i] + "] fluids, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                            "). If this message only appears once, this is likely a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched Parallel LCent ranged fluid input test rolled exactly even to" +
                    " Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }

    // test for multiblock machine with 16x Parallels with ranged fluid output
    @GameTest(template = "large_centrifuge_zpm_batch_parallel16",
              batch = "RangedFluidIngredients",
              timeoutTicks = 500)
    public static void multiblockLCentRangedFluidOutput16ParallelBatched(GameTestHelper helper) {
        BusHolderBatchParallel busHolder = getBussesAndFormLCENT(helper);

        final NotifiableFluidTank fluidIn = busHolder.inputHatch1.tank;
        final NotifiableFluidTank fluidOut = busHolder.outputHatch1.tank;

        int batches = 16;
        int parallels = 16;
        busHolder.controller.setBatchEnabled(true);
        busHolder.parallelHatch.setCurrentParallel(parallels);

        fluidIn.setFluidInTank(0, new FluidStack(LCENT_OUT, batches * parallels));

        // 1t to turn on, 64t per recipe run
        // check the results of all rolls together
        // repeat recipe REPLICAS times
        int[] addedRolls = new int[REPLICAS];
        for (int i = 1; i <= REPLICAS; i++) {
            final int finalI = i; // lambda preserve you
            helper.runAfterDelay(65 * finalI, () -> {
                int runs = finalI * batches * parallels;
                helper.assertTrue(fluidIn.isEmpty(),
                        "Batched Parallel LCent didn't complete correct number of recipes, completed [" +
                                (runs - fluidIn.getFluidInTank(0).getAmount()) + "] not [" + runs + "]");
                int resultCount = fluidOut.getFluidInTank(0).getAmount();
                int lowerLimit = runs * 0;
                int upperLimit = runs * 40;
                helper.assertTrue(TestUtils.isCountWithinRange(resultCount, lowerLimit, upperLimit),
                        "Batched Parallel LCent didn't produce correct number of fluids, produced [" +
                                resultCount + "] not [" + lowerLimit + "-" + upperLimit + "]");

                addedRolls[finalI - 1] = resultCount;

                // reset for a rerun
                fluidIn.setFluidInTank(0, new FluidStack(LCENT_OUT, batches * parallels));
            });
        }

        helper.runAfterDelay(1 + 65 * REPLICAS, () -> {
            // check if each roll was a multiple of run count
            boolean sus = false;
            int[] rolls = new int[REPLICAS];

            rolls[0] = addedRolls[0];
            if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[0], batches, parallels, 1)) {
                sus = true;
                GTCEu.LOGGER.warn("Batched Parallel LCent ranged fluid output test iteration " + 1 + " produced [" +
                        rolls[0] + "] fluids, a multiple of its Batch * Parallel count (" + (batches * parallels) +
                        "). If this message only appears once, this is likely a false positive.");
            }
            for (int i = 1; i < REPLICAS; i++) {
                rolls[i] = addedRolls[i] - addedRolls[i - 1];
                if (TestUtils.isStackSizeExactlyEvenMultiple(rolls[i], batches, parallels, 1)) {
                    sus = true;
                    GTCEu.LOGGER.warn("Batched Parallel LCent ranged fluid output test iteration " + (i + 1) +
                            " produced [" + rolls[i] + "] fluids, a multiple of its Batch * Parallel count (" +
                            (batches * parallels) + "). If this message only appears once, this is likely" +
                            " a false positive.");
                } else {
                    sus = false;
                    break;
                }
            }

            helper.assertFalse(sus, "Batched Parallel LCent ranged fluid output test rolled exactly even to" +
                    " Batch * Parallel count on every iteration");
            helper.succeed();
        });
    }
}
