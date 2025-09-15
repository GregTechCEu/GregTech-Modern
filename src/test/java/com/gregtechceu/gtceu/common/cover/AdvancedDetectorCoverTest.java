package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedFluidDetectorCover;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedItemDetectorCover;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * The "electrolyzer" template contains a creative tank with water,
 * that is set to auto-output into an electrolyzer when supplied with a redstone signal
 * The redstone lamp is connected to the covers that are placed in the tests in this class.
 * The creative tank's rate of output is equal to the electrolyzer's rate of processing
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class AdvancedDetectorCoverTest {

    @GameTest(template = "electrolyzer", batch = "coverTests", required = false)
    public static void BLOCKED_BY_LDLIB_WEIRDNESS_PROBABLY_testAdvancedActivityDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        helper.runAtTickTime(30, () -> helper.assertRedstoneSignal(
                new BlockPos(1, 2, 1),
                Direction.WEST,
                signal -> signal > 0,
                () -> "expected redstone signal"));
    }

    @GameTest(template = "electrolyzer", batch = "coverTests", required = false)
    public static void BLOCKED_BY_LDLIB_WEIRDNESS_TOO_PROBABLY_testAdvancedActivityDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        helper.runAtTickTime(35, () -> helper.pullLever(2, 2, 2));
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedFluidDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        AdvancedFluidDetectorCover cover = (AdvancedFluidDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_FLUID_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMaxValue(100000);
        cover.setMinValue(1);
        cover.setLatched(false);
        // At t=40, 36k will be inside, giving a redstone value of 5
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setLatched(true);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCoverBelowThreshold(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMinValue(1);
        cover.setMaxValue(4);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCoverAboveThreshold(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        machine.getItemHandlerCap(null, false).setStackInSlot(0, new ItemStack(Items.DIRT, 5));
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMinValue(1);
        cover.setMaxValue(4);
        cover.setLatched(true);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }
}
