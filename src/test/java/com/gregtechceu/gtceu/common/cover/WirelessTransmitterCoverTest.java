package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.modules.TextModuleBehaviour;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Supplier;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class WirelessTransmitterCoverTest {

    @GameTest(template = "central_monitor", batch = "coverTests")
    public static void wirelessTransmitterCoverTest(GameTestHelper helper) {
        CentralMonitorMachine machine = (CentralMonitorMachine) TestUtils
                .getMetaMachine(helper.getBlockEntity(new BlockPos(1, 3, 2)));
        DataAccessHatchMachine dataHatch = (DataAccessHatchMachine) TestUtils
                .getMetaMachine(helper.getBlockEntity(new BlockPos(1, 2, 2)));
        BatteryBufferMachine batteryBuffer = (BatteryBufferMachine) TestUtils
                .getMetaMachine(helper.getBlockEntity(new BlockPos(2, 2, 3)));
        WirelessTransmitterCover cover = (WirelessTransmitterCover) batteryBuffer.getCoverContainer()
                .getCoverAtSide(Direction.UP);
        MonitorGroup group = machine.getMonitorGroups().get(0);
        group.setTarget(dataHatch.getPos());
        Supplier<ItemStack> module = () -> group.getItemStackHandler().getStackInSlot(0);
        ItemStack stack = dataHatch.getDataItems().getStackInSlot(3);
        // noinspection DataFlowIssue
        cover.onDataStickUse(helper.makeMockPlayer(), stack);
        dataHatch.importItems.setStackInSlot(3, stack);
        TestUtils.assertEqual(helper, module.get(), GTItems.TEXT_MODULE.asStack());
        helper.runAtTickTime(40, () -> {
            TestUtils.assertEqual(helper, group.getTarget(helper.getLevel()),
                    helper.absolutePos(new BlockPos(2, 2, 3)));
            TestUtils.assertEqual(helper, new TextModuleBehaviour().getText(module.get()), "Energy: 5.40M/7.20M EU\n");
            helper.succeed();
        });
    }
}
