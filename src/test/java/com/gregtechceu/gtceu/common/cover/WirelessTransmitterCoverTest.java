package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
@ForEachTest(groups = "coverTests")
public class WirelessTransmitterCoverTest {

    // @TestHolder()
    // @GameTest(template = "central_monitor", batch = "coverTests")
    // public static void wirelessTransmitterCoverTest(GameTestHelper helper) {
    // CentralMonitorMachine machine = (CentralMonitorMachine) TestUtils
    // .getMetaMachine(helper.getBlockEntity(new BlockPos(1, 3, 2)));
    // DataAccessHatchMachine dataHatch = (DataAccessHatchMachine) TestUtils
    // .getMetaMachine(helper.getBlockEntity(new BlockPos(1, 2, 2)));
    // BatteryBufferMachine batteryBuffer = (BatteryBufferMachine) TestUtils
    // .getMetaMachine(helper.getBlockEntity(new BlockPos(2, 2, 3)));
    // WirelessTransmitterCover cover = (WirelessTransmitterCover) batteryBuffer.getCoverContainer()
    // .getCoverAtSide(Direction.UP);
    // MonitorGroup group = machine.getMonitorGroups().get(0);
    // group.setTarget(dataHatch.getPos());
    // Supplier<ItemStack> module = () -> group.getItemStackHandler().getStackInSlot(0);
    // ItemStack stack = dataHatch.getDataItems().getStackInSlot(3);
    // // noinspection DataFlowIssue
    // cover.onDataStickUse(helper.makeMockPlayer(GameType.CREATIVE), stack);
    // dataHatch.importItems.setStackInSlot(3, stack);
    // TestUtils.assertEqual(helper, module.get(), GTItems.TEXT_MODULE.asStack());
    // helper.runAtTickTime(40, () -> {
    // TestUtils.assertEqual(helper, group.getTarget(helper.getLevel()),
    // helper.absolutePos(new BlockPos(2, 2, 3)));
    // TestUtils.assertEqual(helper, new TextModuleBehaviour().getText(module.get()), "Energy: 5.40M/7.20M EU\n");
    // helper.succeed();
    // });
    // }
}
