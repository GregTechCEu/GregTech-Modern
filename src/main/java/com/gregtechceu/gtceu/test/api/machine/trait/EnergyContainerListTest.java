package com.gregtechceu.gtceu.test.api.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class EnergyContainerListTest {

    @NotNull
    private static IEnergyContainer createContainer(GameTestHelper helper, int amps) {
        return createContainer(helper, amps, LV);
    }

    @NotNull
    private static IEnergyContainer createContainer(GameTestHelper helper, int amps, int tier) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(0, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity mte)) {
            helper.fail("wrong block at relative pos [0,1,0]!");
            //noinspection DataFlowIssue should never get called as the method before throws.
            return null;
        }

        return NotifiableEnergyContainer.receiverContainer(mte.getMetaMachine(),
            V[tier] * 64L * amps, V[tier], amps);
    }

    @NotNull
    private static EnergyContainerList createList(GameTestHelper helper, int size, int ampsPerHatch) {
        List<IEnergyContainer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(createContainer(helper, ampsPerHatch));
        }
        return new EnergyContainerList(list);
    }

    @GameTest(template = "gtceu:energy")
    public void test2A(GameTestHelper helper) {
        // 1x 2A of LV should become 2A of 32
        check(helper, createList(helper, 1, 2), 32, 2);

        // 2x 2A of LV should become 1A of 128
        check(helper, createList(helper, 2, 2), 128, 1);

        // 3x 2A of LV should become 1A of 192
        check(helper, createList(helper, 3, 2), 192, 1);

        // 4x 2A of LV should become 2A of 128
        check(helper, createList(helper, 4, 2), 128, 2);

        // 5x 2A of LV should become 1A of 320
        check(helper, createList(helper, 5, 2), 320, 1);

        helper.succeed();
    }

    @GameTest(template = "gtceu:energy")
    public void test4A(GameTestHelper helper) {
        // 1x 4A of LV should become 1A of 128
        check(helper, createList(helper, 1, 4), 128, 1);

        // 2x 4A of LV should become 2A of 128
        check(helper, createList(helper, 2, 4), 128, 2);

        // 3x 4A of LV should become 1A of 384
        check(helper, createList(helper, 3, 4), 384, 1);

        // 4x 4A of LV should become 1A of 512
        check(helper, createList(helper, 4, 4), 512, 1);

        // 5x 4A of LV should become 1A of 640
        check(helper, createList(helper, 5, 4), 640, 1);

        helper.succeed();
    }

    @GameTest(template = "gtceu:energy")
    public void test16A(GameTestHelper helper) {
        // 1x 16A of LV should become 1A of 512
        check(helper, createList(helper, 1, 16), 512, 1);

        // 2x 16A of LV should become 2A of 512
        check(helper, createList(helper, 2, 16), 512, 2);

        // 3x 16A of LV should become 1A of 1536
        check(helper, createList(helper, 3, 16), 1536, 1);

        // 4x 16A of LV should become 1A of 2048
        check(helper, createList(helper, 4, 16), 2048, 1);

        // 5x 16A of LV should become 1A of 2560
        check(helper, createList(helper, 5, 16), 2560, 1);

        helper.succeed();
    }

    @GameTest(template = "gtceu:energy")
    public void testMixed(GameTestHelper helper) {
        List<IEnergyContainer> list = new ArrayList<>();
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 4));

        // 6A of LV should become 1A of 192
        check(helper, new EnergyContainerList(list), 192, 1);

        list = new ArrayList<>();
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 16));

        // 22A of LV should become 1A of 704
        check(helper, new EnergyContainerList(list), 704, 1);

        list = new ArrayList<>();
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 16));

        // 24A of LV should become 1A of 768
        check(helper, new EnergyContainerList(list), 768, 1);

        list = new ArrayList<>();
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 16));

        // 32A of LV should become 2A of 512
        check(helper, new EnergyContainerList(list), 512, 2);

        list = new ArrayList<>();
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 4));
        list.add(createContainer(helper, 16));

        // 32A of LV should become 2A of 512
        check(helper, new EnergyContainerList(list), 512, 2);

        list = new ArrayList<>();
        list.add(createContainer(helper, 2));
        list.add(createContainer(helper, 2, MV));

        // 2.5A of MV should become 1A of 320
        check(helper, new EnergyContainerList(list), 320, 1);

        helper.succeed();
    }

    private static void check(GameTestHelper helper, @NotNull EnergyContainerList list, long inputVoltage, long inputAmperage) {
        helper.assertTrue(list.getInputVoltage() == inputVoltage, "Bad voltage value. expected %s, got %s".formatted(inputVoltage, list.getInputVoltage()));
        helper.assertTrue(list.getInputAmperage() == inputAmperage, "Bad amperage value. expected %s, got %s".formatted(inputAmperage, list.getInputAmperage()));
    }
}
