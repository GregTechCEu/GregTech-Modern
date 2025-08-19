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
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.LARGE_CHEMICAL_RECIPES;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class IntProviderIngredientTest {

    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CR_RECIPE_TYPE;
    private static GTRecipeType ASSEMBLER_RECIPE_TYPE;

    @BeforeBatch(batch = "OverclockLogic")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_lcr_tests");
        CR_RECIPE_TYPE = TestUtils.createRecipeType("ranged_ingredient_cr_tests");
        ASSEMBLER_RECIPE_TYPE = TestUtils.createRecipeType("ranged_inputs_assembler_tests");

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic"))
                .inputItems(new ItemStack(Items.RED_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(20)
                // NBT has a schematic in it with an HV energy input hatch
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_5"))
                .inputItems(new ItemStack(Items.BROWN_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.MV])
                .duration(16)
                // NBT has a schematic in it with an HV charged singleblock CR in it
                .buildRawRecipe());
    }

    private static MetaMachine getMetaMachine(BlockEntity entity) {
        return ((MetaMachineBlockEntity) entity).getMetaMachine();
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 2, 0)));
        return new BusHolder(inputBus1, inputBus2, outputBus1, outputHatch1, controller);
    }

    // Test for running HV recipe at HV
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
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicOverTierNothingHappens(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.failIfEver(() -> {
            helper.assertFalse(
                    busHolder.outputBus1.getInventory().getStackInSlot(0).getItem().equals(Blocks.STONE.asItem()),
                    "Item crafted at one tier over when it shouldn't have");
        });
        helper.succeed();
    }


    // Test for charge usage of a singleblock HV chemical reactor running an HV recipe
    @GameTest(template = "singleblock_charged_cr", batch = "OverclockLogic")
    public static void overclockLogicHVPowerTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableEnergyContainer energyContainer = (NotifiableEnergyContainer) machine
                .getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        long originalCharge = GTValues.V[GTValues.HV] * 64L;
        helper.assertTrue(energyContainer.getEnergyStored() == originalCharge,
                "Singleblock charged CR NBT changed, machine not fully charged anymore");

        itemIn.setStackInSlot(0, new ItemStack(Items.RED_BED));
        // 1t to turn on, 16t to run the recipe
        helper.succeedOnTickWhen(17, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                            itemOut.getStackInSlot(0),
                            new ItemStack(Blocks.STONE, 1)),
                    "Singleblock CR didn't run recipe in correct time");
            long chargeUsed = originalCharge - energyContainer.getEnergyStored();
            long chargeNeeded = GTValues.V[GTValues.HV] * 16L;
            helper.assertTrue(chargeUsed == chargeNeeded,
                    "Recipe didn't consume right amount, instead of " + chargeNeeded + " it used " + chargeUsed);
        });
    }

    // Test for charge usage of a singleblock HV chemical reactor running an MV recipe
    @GameTest(template = "singleblock_charged_cr", batch = "OverclockLogic")
    public static void overclockLogicMVPowerTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableEnergyContainer energyContainer = (NotifiableEnergyContainer) machine
                .getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        long originalCharge = GTValues.V[GTValues.HV] * 64L;
        helper.assertTrue(energyContainer.getEnergyStored() == originalCharge,
                "Singleblock charged CR NBT changed, machine not fully charged anymore");

        itemIn.setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        // 1t to turn on, 8t to run the recipe (single overclock)
        helper.succeedOnTickWhen(9, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                            itemOut.getStackInSlot(0),
                            new ItemStack(Blocks.STONE, 1)),
                    "Singleblock CR didn't run recipe in correct time");
            long chargeUsed = originalCharge - energyContainer.getEnergyStored();
            // One overclock ups EU/t by STD_VOLTAGE_FACTOR, decreases time by STD_DURATION_FACTOR_INV
            long chargeNeeded = (long) ((GTValues.V[GTValues.MV] * STD_VOLTAGE_FACTOR) *
                    (16L / STD_DURATION_FACTOR_INV));
            helper.assertTrue(chargeUsed == chargeNeeded,
                    "Recipe didn't consume right amount, instead of " + chargeNeeded + " it used " + chargeUsed);
        });
    }
}
