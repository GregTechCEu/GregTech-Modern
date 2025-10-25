package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.encoding.PatternEncodingTerminalPart;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class PatternBufferTest {

    private static GTRecipeType LCR_RECIPE_TYPE;

    @BeforeBatch(batch = "PatternBuffer")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeTypeAndInsertRecipe("pattern_buffer_tests",
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES);

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_recipe_pattern_buffer"))
                .id(GTCEu.id("test_recipe_pattern_buffer"))
                .inputItems(new ItemStack(Items.RED_BED))
                .outputItems(new ItemStack(Blocks.BROWN_BED))
                .EUt(GTValues.V[GTValues.EV])
                .duration(1).buildRawRecipe());
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, MEPatternBufferPartMachine patternBuffer,
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
        MEPatternBufferPartMachine patternBuffer = (MEPatternBufferPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 1)));
        return new BusHolder(inputBus1, inputBus2, outputBus1, outputHatch1, patternBuffer, controller);
    }

    // Test for putting ingredient on the normal input bus when the pattern buffer exists on machine
    @GameTest(template = "patternbuffertest", batch = "PatternBuffer", setupTicks = 40, timeoutTicks = 200)
    public static void patternBufferNormalInputBusTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE));
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Crafting items in same bus failed, expected STONE but was " +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    // Test for checking if pattern buffers work at all
    @GameTest(template = "patternbuffertest", batch = "PatternBuffer", setupTicks = 40, timeoutTicks = 200)
    public static void patternBufferBasicRequestTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);

        IGrid grid = busHolder.patternBuffer.getGrid();

        ICraftingService craftingService = grid.getCraftingService();

        CableBusBlockEntity cbbe = (CableBusBlockEntity) helper.getBlockEntity(new BlockPos(3, 2, 1));
        PatternEncodingTerminalPart terminal = (PatternEncodingTerminalPart) cbbe.getCableBus()
                .getPart(Direction.NORTH);

        Future<ICraftingPlan> plan = craftingService.beginCraftingCalculation(
                helper.getLevel(),
                () -> IActionSource.ofMachine(terminal),
                AEItemKey.of(Items.STONE),
                1,
                CalculationStrategy.REPORT_MISSING_ITEMS);

        helper.runAfterDelay(40, () -> {
            ICraftingPlan job;
            try {
                job = plan.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                helper.fail("Job didn't get queued in 40 ticks");
                throw new RuntimeException("Oopsie, could not get job to start craft");
            }
            ICraftingSubmitResult result = craftingService.submitJob(job, null, null, true, IActionSource.empty());

            helper.assertTrue(result.successful(), "Could not queue crafting job");

            helper.succeedWhen(() -> {
                helper.assertTrue(
                        TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                                new ItemStack(Blocks.STONE)),
                        "Crafting items in same bus failed, expected STONE but was " +
                                busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
            });
        });
    }

    // Test for checking if pattern buffers work if you set distinct
    @GameTest(template = "patternbuffertest", batch = "PatternBuffer", setupTicks = 40, timeoutTicks = 200)
    public static void patternBufferDistinctDoesNothingTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.patternBuffer.setDistinct(true);

        IGrid grid = busHolder.patternBuffer.getGrid();

        ICraftingService craftingService = grid.getCraftingService();

        CableBusBlockEntity cbbe = (CableBusBlockEntity) helper.getBlockEntity(new BlockPos(3, 2, 1));
        PatternEncodingTerminalPart terminal = (PatternEncodingTerminalPart) cbbe.getCableBus()
                .getPart(Direction.NORTH);

        Future<ICraftingPlan> plan = craftingService.beginCraftingCalculation(
                helper.getLevel(),
                () -> IActionSource.ofMachine(terminal),
                AEItemKey.of(Items.STONE),
                1,
                CalculationStrategy.REPORT_MISSING_ITEMS);

        helper.runAfterDelay(40, () -> {
            ICraftingPlan job;
            try {
                job = plan.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                helper.fail("Job didn't get queued in 40 ticks");
                throw new RuntimeException("Oopsie, could not get job to start craft");
            }
            ICraftingSubmitResult result = craftingService.submitJob(job, null, null, true, IActionSource.empty());

            helper.assertTrue(result.successful(), "Could not queue crafting job");

            helper.succeedWhen(() -> {
                helper.assertTrue(
                        TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                                new ItemStack(Blocks.STONE)),
                        "Crafting items in same bus failed, expected STONE but was " +
                                busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
            });
        });
    }

    // Test for checking if pattern buffers work if you dye them
    @GameTest(template = "patternbuffertest", batch = "PatternBuffer", setupTicks = 40, timeoutTicks = 200)
    public static void patternBufferDyeingDoesNothingTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.patternBuffer.setPaintingColor(0xff);

        IGrid grid = busHolder.patternBuffer.getGrid();

        ICraftingService craftingService = grid.getCraftingService();

        CableBusBlockEntity cbbe = (CableBusBlockEntity) helper.getBlockEntity(new BlockPos(3, 2, 1));
        PatternEncodingTerminalPart terminal = (PatternEncodingTerminalPart) cbbe.getCableBus()
                .getPart(Direction.NORTH);

        Future<ICraftingPlan> plan = craftingService.beginCraftingCalculation(
                helper.getLevel(),
                () -> IActionSource.ofMachine(terminal),
                AEItemKey.of(Items.STONE),
                1,
                CalculationStrategy.REPORT_MISSING_ITEMS);

        helper.runAfterDelay(40, () -> {
            ICraftingPlan job;
            try {
                job = plan.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                helper.fail("Job didn't get queued in 40 ticks");
                throw new RuntimeException("Oopsie, could not get job to start craft");
            }
            ICraftingSubmitResult result = craftingService.submitJob(job, null, null, true, IActionSource.empty());

            helper.assertTrue(result.successful(), "Could not queue crafting job");

            helper.succeedWhen(() -> {
                helper.assertTrue(
                        TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                                new ItemStack(Blocks.STONE)),
                        "Crafting items in same bus failed, expected STONE but was " +
                                busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
            });
        });
    }
}
