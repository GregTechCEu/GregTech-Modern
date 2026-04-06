package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class DetectorCoverTest {

    @TestHolder()
    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testActivityDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        SimpleTieredMachine machine = (SimpleTieredMachine) ((IMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        machine.importFluids.setFluidInTank(0, new FluidStack(Fluids.WATER, machine.importFluids.getTankCapacity(0)));
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR.asStack(), Direction.WEST);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @TestHolder()
    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testFluidDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        SimpleTieredMachine machine = (SimpleTieredMachine) ((IMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        machine.exportFluids.setFluidInTank(0, new FluidStack(Fluids.WATER, machine.exportFluids.getTankCapacity(0)));
        TestUtils.placeCover(helper, machine, GTItems.COVER_FLUID_DETECTOR.asStack(), Direction.WEST);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @TestHolder()
    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testItemDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        SimpleTieredMachine machine = (SimpleTieredMachine) ((IMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        TestUtils.placeCover(helper, machine, GTItems.COVER_ITEM_DETECTOR.asStack(), Direction.WEST);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }
}
