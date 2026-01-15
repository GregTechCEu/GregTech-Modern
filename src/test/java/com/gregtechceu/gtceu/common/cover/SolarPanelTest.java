package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class SolarPanelTest {

    @BeforeBatch(batch = "SolarTests")
    public static void prepare(ServerLevel level) {
        level.setDayTime(6000);
    }

    private static BatteryBufferMachine makeBatteryBuffer(GameTestHelper helper, int tier) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BATTERY_BUFFER_4[tier].getBlock());
        return (BatteryBufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
    }

    private static void placeSolar(GameTestHelper helper, MetaMachine machine) {
        TestUtils.placeCover(helper, machine, GTItems.COVER_SOLAR_PANEL_HV.asStack(), Direction.UP);
    }

    @GameTest(template = "empty_5x5", batch = "SolarTests")
    public static void generatesEnergyAtDayTest(GameTestHelper helper) {
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        machine.getBatteryInventory().insertItem(0, GTItems.BATTERY_HV_LITHIUM.asStack(), false);
        placeSolar(helper, machine);
        helper.runAtTickTime(80, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() > 0,
                    "Solar panel cover didn't generate energy at day time");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5", batch = "SolarTests")
    public static void doesntGenerateEnergyAtDayWhenBlockedTest(GameTestHelper helper) {
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        helper.setBlock(new BlockPos(0, 3, 0), Blocks.DIAMOND_BLOCK);
        machine.getBatteryInventory().insertItem(0, GTItems.BATTERY_HV_LITHIUM.asStack(), false);
        placeSolar(helper, machine);
        helper.runAtTickTime(40, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() == 0,
                    "Solar panel cover generated energy when blocked");
            helper.succeed();
        });
    }
}
