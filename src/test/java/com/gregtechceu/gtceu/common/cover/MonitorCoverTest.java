package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class MonitorCoverTest {

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void testEnergyPlaceholders(GameTestHelper helper) {
        BatteryBufferMachine machine = (BatteryBufferMachine) TestUtils.setMachine(helper, new BlockPos(0, 1, 0),
                GTMachines.BATTERY_BUFFER_4[GTValues.HV]);
        machine.getBatteryInventory().insertItem(0, GTItems.BATTERY_HV_LITHIUM.asStack(), false);
        ComputerMonitorCover cover = (ComputerMonitorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_SCREEN.asStack(), Direction.UP);
        cover.getFormatStringLines().add("Energy: {}/{} EU");
        cover.getFormatStringArgs().addAll(List.of(
                "energy",
                "energyCapacity"));
        cover.setUpdateInterval(1);
        helper.runAtTickTime(5, () -> {
            TestUtils.assertEqual(helper, cover.getText(),
                    "Energy: 0/" + machine.energyContainer.getEnergyCapacity() + " EU");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void testCombinePlaceholder(GameTestHelper helper) {
        BatteryBufferMachine machine = (BatteryBufferMachine) TestUtils.setMachine(helper, new BlockPos(0, 1, 0),
                GTMachines.BATTERY_BUFFER_4[GTValues.HV]);
        ComputerMonitorCover cover = (ComputerMonitorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_SCREEN.asStack(), Direction.UP);
        cover.getFormatStringLines()
                .add("{if 1 {combine test 1 2 3 lol {repeat 5 \"a \"}} \"if placeholder failed somehow\"}");
        cover.setUpdateInterval(1);
        helper.runAtTickTime(5, () -> {
            TestUtils.assertEqual(helper, cover.getText(), "test 1 2 3 lol a a a a a ");
            helper.succeed();
        });
    }
}
