package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
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

    @GameTest(template = "empty_5x5")
    public static void generatesEnergyAtDayTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        machine.getBatteryInventory().setStackInSlot(0, new ItemStack(GTItems.BATTERY_HV_LITHIUM, 1));
        CoverBehavior cover = GTCovers.SOLAR_PANEL[GTValues.HV].createCoverBehavior(machine.getCoverContainer(),
                Direction.UP);
        machine.getCoverContainer().setCoverAtSide(cover, Direction.UP);
        cover.onLoad();
        helper.runAtTickTime(40, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() > 0,
                    "Solar panel cover didn't generate energy at day time");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5")
    public static void generatesEnergyAtNightTest(GameTestHelper helper) {
        helper.setDayTime(18000);
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        machine.getBatteryInventory().setStackInSlot(0, new ItemStack(GTItems.BATTERY_HV_LITHIUM, 1));
        CoverBehavior cover = GTCovers.SOLAR_PANEL[GTValues.HV].createCoverBehavior(machine.getCoverContainer(),
                Direction.UP);
        machine.getCoverContainer().setCoverAtSide(cover, Direction.UP);
        cover.onLoad();
        helper.runAtTickTime(40, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() == 0,
                    "Solar panel cover generated energy at night time");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5")
    public static void generatesEnergyAtDayWhenBlockedTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        helper.setBlock(new BlockPos(0, 3, 0), Blocks.DIAMOND_BLOCK);
        machine.getBatteryInventory().setStackInSlot(0, new ItemStack(GTItems.BATTERY_HV_LITHIUM, 1));
        CoverBehavior cover = GTCovers.SOLAR_PANEL[GTValues.HV].createCoverBehavior(machine.getCoverContainer(),
                Direction.UP);
        machine.getCoverContainer().setCoverAtSide(cover, Direction.UP);
        cover.onLoad();
        helper.runAtTickTime(40, () -> {
            helper.assertTrue(machine.energyContainer.getEnergyStored() == 0,
                    "Solar panel cover generated energy when blocked");
            helper.succeed();
        });
    }
}
