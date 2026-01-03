package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class SolarPanelTest {

    private static BatteryBufferMachine makeBatteryBuffer(GameTestHelper helper, int tier) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BATTERY_BUFFER_4[tier].getBlock());
        return (BatteryBufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
    }

    private static void placeSolar(GameTestHelper helper, MetaMachine machine) {
        TestUtils.placeCover(helper, machine, GTItems.COVER_SOLAR_PANEL_HV.asStack(), Direction.UP);
    }

    @GameTest(template = "empty_5x5", batch = "coverTests", required = false) // it doesn't fail only if running tests
                                                                              // with the command for some reason
    public static void only_works_in_game_generatesEnergyAtDayTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        machine.getBatteryInventory().insertItem(0, GTItems.BATTERY_HV_LITHIUM.asStack(), false);
        placeSolar(helper, machine);
        helper.runAtTickTime(80, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() > 0,
                    "Solar panel cover didn't generate energy at day time");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void doesntGenerateEnergyAtDayWhenBlockedTest(GameTestHelper helper) {
        helper.setDayTime(6000);
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
