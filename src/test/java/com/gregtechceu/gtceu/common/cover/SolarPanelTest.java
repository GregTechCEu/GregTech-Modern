package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class SolarPanelTest {

    private static BatteryBufferMachine getBatteryBuffer(GameTestHelper helper) {
        return (BatteryBufferMachine) helper.getBlockEntity(new BlockPos(0, 1, 0));
    }

    @GameTest(template = "solar", batch = "coverTests")
    public static void generatesEnergyAtDayTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = getBatteryBuffer(helper);
        machine.onLoad();
        for (int y = helper.absolutePos(new BlockPos(0, 2, 0)).getY(); y < helper.getLevel().getMaxBuildHeight(); y++) {
            helper.setBlock(0, helper.relativePos(new BlockPos(0, y, 0)).getY(), 0, Blocks.AIR);
        }
        helper.runAtTickTime(80, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() > 0,
                    "Solar panel cover didn't generate energy at day time");
            helper.succeed();
        });
    }

    @GameTest(template = "solar", batch = "coverTests")
    public static void doesntGenerateEnergyAtDayWhenBlockedTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = getBatteryBuffer(helper);
        machine.onLoad();
        helper.setBlock(new BlockPos(0, 3, 0), Blocks.DIAMOND_BLOCK);
        helper.runAtTickTime(40, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() <= 1024,
                    "Solar panel cover generated energy when blocked");
            helper.succeed();
        });
    }
}
