package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class MultipleEnergyHatchTest {
    // This class tests the overclock logic:
    // 1x 2a ev + 1x 2a mv: run ev with 0x oc, don't run iv
    // 1x 2a ev + 1x 2a hv: run ev with 0x oc, don't run iv
    // 2x 2a ev: run ev with 1x oc, run iv with 0x oc
    // 1x 4a ev: run ev with 1x oc, don't run iv
    // 1x 16a ev: run ev with 2x oc, don't run iv
    // 1x 16a ev: run ev with 2x oc, don't run iv
    // 1x 16a ev + 1x 4a ev: run ev with 2x oc, run iv with 1x oc
    // 1x 2a iv + 1x 16a ev: run iv recipe with 0x oc, don't run LuV

    private static GTRecipeType LCR_RECIPE_TYPE;

    @BeforeBatch(batch = "MultipleEnergyHatch")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("multiple_energy_hatch_lcr_tests",
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES);

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiple_energy_hatch_ev"))
                .inputItems(new ItemStack(Items.CYAN_BED))
                .outputItems(new ItemStack(Items.CYAN_BED))
                .EUt(GTValues.V[EV])
                .duration(16)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiple_energy_hatch_iv"))
                .inputItems(new ItemStack(Items.BROWN_BED))
                .outputItems(new ItemStack(Items.BROWN_BED))
                .EUt(GTValues.V[GTValues.IV])
                .duration(16)
                .buildRawRecipe());

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiple_energy_hatch_iv"))
                .inputItems(new ItemStack(Items.GREEN_BED))
                .outputItems(new ItemStack(Items.GREEN_BED))
                .EUt(GTValues.V[GTValues.LuV])
                .duration(16)
                .buildRawRecipe());
    }

    private record BusHolder(ItemBusPartMachine inputBus, ItemBusPartMachine outputBus,
                             WorkableElectricMultiblockMachine controller,
                             EnergyHatchPartMachine energyHatch1, Optional<EnergyHatchPartMachine> energyHatch2) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableElectricMultiblockMachine controller = (WorkableElectricMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine outputBus = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 3, 0)));
        // Some instances don't have a second energy hatch
        var hatch2BE = helper.getBlockEntity(new BlockPos(1, 3, 0));
        if (hatch2BE instanceof MetaMachineBlockEntity hatch2MMBE) {
            return new BusHolder(inputBus, outputBus, controller, energyHatch,
                    Optional.of((EnergyHatchPartMachine) hatch2MMBE.getMetaMachine()));
        }

        return new BusHolder(inputBus, outputBus, controller, energyHatch, Optional.empty());
    }

    private record Hatch(int tier, int amps, long EU) {

        public Hatch(int tier, int amps) {
            this(tier, amps, V[tier]);
        }
    }

    private static void checkContainerList(GameTestHelper helper, BusHolder busHolder, List<Hatch> hatches) {
        long totalVoltage = 0;
        for (var hatch : hatches) {
            totalVoltage += hatch.EU * hatch.amps;
        }
        EnergyContainerList containerList = busHolder.controller.getEnergyContainer();

        helper.assertTrue(totalVoltage == containerList.getInputVoltage(),
                "Hatches on multiblock didn't match expected input voltage");
    }

    @GameTest(template = "energy/lcr_ev_mv", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void EvPlusMvHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(MV, 2), new Hatch(EV, 2)));

        EnergyContainerList containerList = busHolder.controller.getEnergyContainer();
        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 16 for the recipe to run
        helper.succeedOnTickWhen(17, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_ev_mv", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void EvPlusMvHatchCannotDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(MV, 2), new Hatch(EV, 2)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item crafted when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "energy/lcr_ev_hv", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void EvPlusHvHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(HV, 2), new Hatch(EV, 2)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 16 for the recipe to run
        helper.succeedOnTickWhen(17, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_ev_hv", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void EvPlusHvHatchCannotIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(HV, 2), new Hatch(EV, 2)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        // One tick to start, 16 for the recipe to run
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item crafted when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "energy/lcr_2x_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void DoubleEVHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 2), new Hatch(EV, 2)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 4 for the recipe to run
        helper.succeedOnTickWhen(5, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_2x_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void DoubleEVHatchCanDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 2), new Hatch(EV, 2)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        // One tick to start, 16 for the recipe to run
        helper.succeedOnTickWhen(17, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item didn't craft at the right tick with an 1-above-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_4a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void FourAEVHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 4)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 4 for the recipe to run
        helper.succeedOnTickWhen(5, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_4a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void FourAEVHatchCanNotDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 4)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item crafted when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "energy/lcr_16a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAEVHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 16)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 1 for the recipe to run
        helper.succeedOnTickWhen(2, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_16a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAEVHatchCanNotDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 16)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item crafted when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "energy/lcr_16a_ev_hv", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAEVPlus2AHVHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(HV, 2), new Hatch(EV, 16)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 1 for the recipe to run
        helper.succeedOnTickWhen(2, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_16a_ev_hv", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAEVPlus2AHVHatchCanNotDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(HV, 2), new Hatch(EV, 16)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item crafted when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "energy/lcr_16a_4a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAPlus4AEVHatchCanDoEVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 16), new Hatch(EV, 4)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.CYAN_BED));
        // One tick to start, 1 for the recipe to run
        helper.succeedOnTickWhen(2, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.CYAN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_16a_4a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAPlus4AEVHatchCanDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(EV, 16), new Hatch(EV, 4)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        // One tick to start, 4 for the recipe to run
        helper.succeedOnTickWhen(5, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_iv_16a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAEVPlus2AIVHatchCanDoIVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(IV, 2), new Hatch(EV, 16)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        // One tick to start, 4 for the recipe to run
        helper.succeedOnTickWhen(5, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.BROWN_BED)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "energy/lcr_iv_16a_ev", batch = "MultipleEnergyHatch", setupTicks = 10L)
    public static void SixteenAEVPlus2AIVHatchCannotDoLuVRecipeTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        checkContainerList(helper, busHolder, List.of(new Hatch(IV, 2), new Hatch(EV, 16)));

        busHolder.inputBus.getInventory().setStackInSlot(0, new ItemStack(Items.GREEN_BED));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus.getInventory().getStackInSlot(0),
                            new ItemStack(Items.GREEN_BED)),
                    "Item crafted when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }
}
