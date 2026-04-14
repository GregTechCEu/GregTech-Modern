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
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

import java.util.function.Supplier;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
@ForEachTest(groups = "coverTests")
public class WirelessTransmitterCoverTest {

    @TestHolder()
    @GameTest(template = "central_monitor", batch = "coverTests")
    public static void wirelessTransmitterCoverTest(GameTestHelper helper) {
        CentralMonitorMachine machine = helper.getBlockEntity(new BlockPos(1, 3, 2));
        DataAccessHatchMachine dataHatch = helper.getBlockEntity(new BlockPos(1, 2, 2));
        BatteryBufferMachine batteryBuffer = helper.getBlockEntity(new BlockPos(2, 2, 3));
        WirelessTransmitterCover cover = (WirelessTransmitterCover) batteryBuffer.getCoverContainer()
                .getCoverAtSide(Direction.UP);
        MonitorGroup group = machine.getMonitorGroups().getFirst();
        group.setTarget(dataHatch.getBlockPos());
        Supplier<ItemStack> module = () -> group.getItemStackHandler().getStackInSlot(0);
        ItemStack stack = GTItems.TOOL_DATA_STICK.asStack();
        // noinspection DataFlowIssue
        cover.onDataStickUse(helper.makeMockPlayer(GameType.CREATIVE), stack);
        dataHatch.importItems.setStackInSlot(0, stack);
        TestUtils.assertEqual(helper, module.get(), GTItems.TEXT_MODULE.asStack());
        helper.runAtTickTime(80, () -> {
            TestUtils.assertEqual(helper, group.getTarget(helper.getLevel()),
                    helper.absolutePos(new BlockPos(2, 2, 3)));
            TestUtils.assertEqual(helper, new TextModuleBehaviour().getText(module.get()), "Energy: 5.40M/7.20M EU\n");
            helper.succeed();
        });
    }
}
