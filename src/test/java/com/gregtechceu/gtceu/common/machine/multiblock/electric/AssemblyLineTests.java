package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES;
import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class AssemblyLineTests {

    private static GTRecipeType ASSLINE_RECIPE_TYPE;

    @BeforeBatch(batch = "Assline")
    public static void prepare(ServerLevel level) {
        ASSLINE_RECIPE_TYPE = TestUtils.createRecipeType("assline_tests", ASSEMBLY_LINE_RECIPES);
        ASSLINE_RECIPE_TYPE.getLookup().addRecipe(ASSLINE_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_assline"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.ACACIA_WOOD))
                .inputFluids(new FluidStack(Fluids.WATER, 1), new FluidStack(Fluids.LAVA, 1))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                // NBT has a schematic in it with an EV energy input hatch
                .buildRawRecipe());
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2,
                             ItemBusPartMachine inputBus3, ItemBusPartMachine inputBus4,
                             FluidHatchPartMachine inputHatch1, FluidHatchPartMachine inputHatch2,
                             FluidHatchPartMachine inputHatch3, FluidHatchPartMachine inputHatch4,
                             ItemBusPartMachine outputBus1, WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 3, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(ASSLINE_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 1)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));
        ItemBusPartMachine inputBus3 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 1)));
        ItemBusPartMachine inputBus4 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(3, 1, 1)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(4, 1, 1)));
        FluidHatchPartMachine inputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        FluidHatchPartMachine inputHatch2 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 0)));
        FluidHatchPartMachine inputHatch3 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        FluidHatchPartMachine inputHatch4 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(3, 1, 0)));
        return new BusHolder(inputBus1, inputBus2, inputBus3, inputBus4,
                inputHatch1, inputHatch2, inputHatch3, inputHatch4, outputBus1, controller);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeRunsTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch1.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.succeedOnTickWhen(2, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item didn't craft at the right tick with ok recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeDoesntRunWhenItemsMovedByOneTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus3.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch1.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item crafted with inputs moved" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeDoesntRunWhenFluidsMovedByOneTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch3.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item crafted with inputs moved" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeDoesntRunWhenBothMovedByOneTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus3.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch3.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item crafted with inputs moved" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeRunsAndOnlyConsumesOneTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 2));
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD, 2));
        busHolder.inputHatch1.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 2));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 2));
        helper.runAtTickTime(1, () -> {
            // All 4 inputs had 1 consumed
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.inputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.COBBLESTONE)),
                    "Assline consumed both items when it should have consumed one");
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.inputBus2.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.ACACIA_WOOD)),
                    "Assline consumed both items when it should have consumed one");
            helper.assertTrue(
                    TestUtils.isFluidStackEqual(busHolder.inputHatch1.tank.getFluidInTank(0),
                            new FluidStack(Fluids.WATER, 1)),
                    "Assline consumed both fluids when it should have consumed one");
            helper.assertTrue(
                    TestUtils.isFluidStackEqual(busHolder.inputHatch2.tank.getFluidInTank(0),
                            new FluidStack(Fluids.LAVA, 1)),
                    "Assline consumed both fluids when it should have consumed one");
        });
        helper.runAtTickTime(2, () -> {
            // First recipe finished
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item didn't craft at the right tick with ok recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
            // All 4 inputs had the second one consumed
            helper.assertTrue(
                    busHolder.inputBus1.getInventory().getStackInSlot(0).isEmpty(),
                    "Assline consumed didn't consume both items when it should have");
            helper.assertTrue(
                    busHolder.inputBus2.getInventory().getStackInSlot(0).isEmpty(),
                    "Assline consumed didn't consume both items when it should have");
            helper.assertTrue(
                    busHolder.inputHatch1.tank.getFluidInTank(0).isEmpty(),
                    "Assline consumed didn't consume both fluids when it should have");
            helper.assertTrue(
                    busHolder.inputHatch2.tank.getFluidInTank(0).isEmpty(),
                    "Assline consumed didn't consume both fluids when it should have");
        });
        helper.runAtTickTime(3, () -> {
            // Recipe 2 has finished
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE, 2)),
                    "Item didn't craft at the right tick with ok recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeDoesntRunWhenItemsSwappedTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch1.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item crafted with inputs moved" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeDoesntRunWhenFluidsSwappedTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch1.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item crafted with inputs moved" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "ass_line_4aev_4in", batch = "Assline")
    public static void AsslineRecipeDoesntRunWhenBothSwappedTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus2.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.ACACIA_WOOD));
        busHolder.inputHatch2.tank.setFluidInTank(0, new FluidStack(Fluids.WATER, 1));
        busHolder.inputHatch1.tank.setFluidInTank(0, new FluidStack(Fluids.LAVA, 1));
        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item crafted with inputs moved" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
        TestUtils.succeedAfterTest(helper);
    }
}
