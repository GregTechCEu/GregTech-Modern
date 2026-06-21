package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.HPCAMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeComputationProviderMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.RESEARCH_STATION_RECIPES;

/**
 * Tests for the Research Station / HPCA computation system.
 * The structures used here are:
 * {@code research_computer} - a Research Station fed by a Creative Computation Provider over an optical pipe.
 * {@code hpca} - a standalone HPCA (4 + 16 = 20 CWU/t) with a Computation Transmitter Hatch.
 * {@code research_computer_and_hpca} - a Research Station drawing computation from a real HPCA over optical pipes.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ResearchComputationTests {

    private static GTRecipeType RESEARCH_RECIPE_TYPE;

    // HPCA in the schematic creates 20 CWU/t
    private static final int RECIPE_CWUT = 16;

    // BlockPos's for the pipes to force a tick
    private static final BlockPos[] RESEARCH_COMPUTER_PIPES = { new BlockPos(7, 3, 1) };
    private static final BlockPos[] RESEARCH_COMPUTER_AND_HPCA_PIPES = { new BlockPos(7, 3, 1), new BlockPos(8, 3, 1) };

    @BeforeBatch(batch = "ResearchComputation")
    public static void prepare(ServerLevel level) {
        RESEARCH_RECIPE_TYPE = TestUtils.createRecipeType("research_computation_tests", RESEARCH_STATION_RECIPES);
        var handler = RESEARCH_RECIPE_TYPE.getAdditionHandler();
        handler.beginStaging();
        // Research a data stick into a data orb so completion is observable in the object holder's data slot.
        handler.addStaging(RESEARCH_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_research"))
                .inputItems(GTItems.TOOL_DATA_STICK.asStack())
                .outputItems(GTItems.TOOL_DATA_ORB.asStack())
                .EUt(GTValues.VA[GTValues.EV])
                .CWUt(RECIPE_CWUT)
                .totalCWU(48)
                .build());
        handler.completeStaging();
    }

    /**
     * Registers the optical pipe nodes into the level's pipe network.
     * <p>
     * When a structure template is placed in a GameTest world the pipe blocks are restored, but the scheduled block
     * tick that normally adds each pipe to the {@code LevelOpticalPipeNet} (queued by {@code onPlace}) never runs, so
     * the network is missing and computation cannot route through it. Calling the pipe block's tick directly performs
     * the same node addition, building the network exactly as it would form in-world.
     */
    private static void formOpticalPipeNet(GameTestHelper helper, BlockPos... pipePositions) {
        ServerLevel level = helper.getLevel();
        for (BlockPos rel : pipePositions) {
            BlockPos pos = helper.absolutePos(rel);
            BlockState state = level.getBlockState(pos);
            helper.assertTrue(state.getBlock() instanceof PipeBlock<?, ?, ?>,
                    "Expected an optical pipe at " + rel + " but found " + state.getBlock());
            ((PipeBlock<?, ?, ?>) state.getBlock()).tick(state, level, pos, level.getRandom());
        }
    }

    private static ResearchStationMachine formResearchStation(GameTestHelper helper) {
        ResearchStationMachine researchStation = (ResearchStationMachine) helper
                .getBlockEntity(new BlockPos(4, 4, 1));
        helper.assertTrue(researchStation != null, "Research Station controller not found");
        TestUtils.formMultiblock(researchStation);
        researchStation.setRecipeType(RESEARCH_RECIPE_TYPE);
        return researchStation;
    }

    private static ObjectHolderMachine getObjectHolder(GameTestHelper helper) {
        ObjectHolderMachine holder = (ObjectHolderMachine) helper.getBlockEntity(new BlockPos(1, 4, 1));
        helper.assertTrue(holder != null, "Object Holder not found");
        return holder;
    }

    private static boolean researchFinished(ObjectHolderMachine holder) {
        // On completion the Research Station replaces the data slot's item with the recipe output (a data orb).
        return TestUtils.isItemStackEqual(holder.getDataItem(false), GTItems.TOOL_DATA_ORB.asStack());
    }

    @GameTest(template = "research_computer_and_hpca",
              batch = "ResearchComputation",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void ResearchStationAndHPCAWholeSystemResearchCompletesTest(GameTestHelper helper) {
        formOpticalPipeNet(helper, RESEARCH_COMPUTER_AND_HPCA_PIPES);
        HPCAMachine hpca = (HPCAMachine) helper.getBlockEntity(new BlockPos(13, 2, 2));
        helper.assertTrue(hpca != null, "HPCA controller not found");
        TestUtils.formMultiblock(hpca);

        formResearchStation(helper);
        ObjectHolderMachine holder = getObjectHolder(helper);
        holder.setDataItem(GTItems.TOOL_DATA_STICK.asStack());

        helper.succeedWhen(() -> helper.assertTrue(researchFinished(holder),
                "Research recipe did not complete with a working HPCA providing computation"));
    }

    @GameTest(template = "research_computer_and_hpca",
              batch = "ResearchComputation",
              setupTicks = 40,
              timeoutTicks = 200)
    public static void ResearchStationAndHPCAWholeSystemFailsWithoutHpcaComputationTest(GameTestHelper helper) {
        formOpticalPipeNet(helper, RESEARCH_COMPUTER_AND_HPCA_PIPES);
        HPCAMachine hpca = (HPCAMachine) helper.getBlockEntity(new BlockPos(13, 2, 2));
        helper.assertTrue(hpca != null, "HPCA controller not found");
        TestUtils.formMultiblock(hpca);
        // Turn the HPCA off, so it can no longer provide any computation.
        hpca.setWorkingEnabled(false);

        formResearchStation(helper);
        ObjectHolderMachine holder = getObjectHolder(helper);
        holder.setDataItem(GTItems.TOOL_DATA_STICK.asStack());

        helper.onEachTick(() -> helper.assertFalse(researchFinished(holder),
                "Research recipe completed even though the HPCA was disabled"));
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "research_computer", batch = "ResearchComputation", setupTicks = 40, timeoutTicks = 200)
    public static void ResearchStationConsumesCreativeComputationTest(GameTestHelper helper) {
        formOpticalPipeNet(helper, RESEARCH_COMPUTER_PIPES);
        formResearchStation(helper);
        ObjectHolderMachine holder = getObjectHolder(helper);
        holder.setDataItem(GTItems.TOOL_DATA_STICK.asStack());

        helper.succeedWhen(() -> helper.assertTrue(researchFinished(holder),
                "Research recipe did not complete while the Creative Computation Provider was supplying computation"));
    }

    @GameTest(template = "research_computer", batch = "ResearchComputation", setupTicks = 40, timeoutTicks = 200)
    public static void ResearchStationFailsWithoutCreativeComputationTest(GameTestHelper helper) {
        formOpticalPipeNet(helper, RESEARCH_COMPUTER_PIPES);
        CreativeComputationProviderMachine creative = (CreativeComputationProviderMachine) helper
                .getBlockEntity(new BlockPos(8, 3, 1));
        helper.assertTrue(creative != null, "Creative Computation Provider not found");
        creative.setActive(false);

        formResearchStation(helper);
        ObjectHolderMachine holder = getObjectHolder(helper);
        holder.setDataItem(GTItems.TOOL_DATA_STICK.asStack());

        helper.onEachTick(() -> helper.assertFalse(researchFinished(holder),
                "Research recipe completed even though no computation was being provided"));
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "hpca", batch = "ResearchComputation", setupTicks = 40, timeoutTicks = 200)
    public static void HPCAProvidesComputationTest(GameTestHelper helper) {
        HPCAMachine hpca = (HPCAMachine) helper.getBlockEntity(new BlockPos(5, 1, 1));
        helper.assertTrue(hpca != null, "HPCA controller not found");
        TestUtils.formMultiblock(hpca);

        // The HPCA only reports computation once it has powered on (energy stored), which takes a few ticks.
        helper.succeedWhen(() -> {
            helper.assertTrue(hpca.getMaxCWUt() == 20,
                    "HPCA should provide a maximum of 20 CWU/t, got " + hpca.getMaxCWUt());
            helper.assertTrue(hpca.requestCWUt(RECIPE_CWUT, true) == RECIPE_CWUT,
                    "HPCA should be able to supply a 16 CWU/t request");
            helper.assertTrue(hpca.requestCWUt(1000, true) == 20,
                    "HPCA should cap a request at its maximum of 20 CWU/t");
        });
    }

    @GameTest(template = "hpca", batch = "ResearchComputation", setupTicks = 40, timeoutTicks = 200)
    public static void HPCAProvidesNoComputationWhenDisabledTest(GameTestHelper helper) {
        HPCAMachine hpca = (HPCAMachine) helper.getBlockEntity(new BlockPos(5, 1, 1));
        helper.assertTrue(hpca != null, "HPCA controller not found");
        TestUtils.formMultiblock(hpca);
        hpca.setWorkingEnabled(false);

        helper.onEachTick(() -> {
            helper.assertTrue(hpca.getMaxCWUt() == 0,
                    "Disabled HPCA should report 0 max CWU/t, got " + hpca.getMaxCWUt());
            helper.assertTrue(hpca.requestCWUt(RECIPE_CWUT, true) == 0,
                    "Disabled HPCA should supply 0 CWU/t");
        });
        TestUtils.succeedAfterTest(helper);
    }

    @GameTest(template = "hpca", batch = "ResearchComputation", setupTicks = 40, timeoutTicks = 3000)
    public static void HPCAOverheatsWithInsufficientCoolingTest(GameTestHelper helper) {
        // Replace a heat sink in the 3x3 component grid with a basic computation component before forming.
        // This makes 1x16 + 2x4 = 24 CWU, with 1x4 + 2x2 = 8 cooling required, but only 6 coolers.
        var component = GTResearchMachines.HPCA_COMPUTATION_COMPONENT;
        helper.setBlock(new BlockPos(2, 2, 1), component.getBlock().defaultBlockState()
                .setValue(component.getRotationState().property, Direction.SOUTH));

        HPCAMachine hpca = (HPCAMachine) helper.getBlockEntity(new BlockPos(5, 1, 1));
        helper.assertTrue(hpca != null, "HPCA controller not found");
        TestUtils.formMultiblock(hpca);

        // Only succeed once we have actually seen the full 24 CWU/t (confirming the swap took effect and the HPCA
        // powered on), and then watched overheating damage drop it below 24 - so the test can't pass vacuously.
        AtomicBoolean sawFullComputation = new AtomicBoolean(false);
        helper.onEachTick(() -> {
            // keep the HPCA under full computational load so it heats up
            hpca.requestCWUt(Integer.MAX_VALUE, false);
            int maxCWUt = hpca.getMaxCWUt();
            if (maxCWUt == 24) {
                sawFullComputation.set(true);
            }
            if (sawFullComputation.get() && maxCWUt < 24) {
                helper.succeed();
            }
        });
    }
}
