package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class DetectorCoverTest {

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testActivityDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR.asStack(), Direction.WEST);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testFluidDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        TestUtils.placeCover(helper, machine, GTItems.COVER_FLUID_DETECTOR.asStack(), Direction.WEST);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testItemDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        TestUtils.placeCover(helper, machine, GTItems.COVER_ITEM_DETECTOR.asStack(), Direction.WEST);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }
}
